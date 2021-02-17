package com.nuxeo.service.cli;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.chronicle.ChronicleLogManager;
import org.nuxeo.lib.stream.log.kafka.KafkaLogManager;
import org.nuxeo.lib.stream.log.kafka.KafkaUtils;

public abstract class BaseCLI {

	public Properties getProducerProps() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaUtils.getBootstrapServers());
		return props;
	}

	public Properties getConsumerProps() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaUtils.getBootstrapServers());
		props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30_000);
		props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 30_000);
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 120_000);
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10_000);
		props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 400);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5);
		return props;
	}

	protected LogManager createManager(String prefix, String cqPath) {
		if (cqPath!=null) {
			return new ChronicleLogManager(Path.of(cqPath));
		} else {
			return new KafkaLogManager(prefix, getProducerProps(), getConsumerProps());			
		}
	}

	protected void initSystemProperties(String config) throws Exception {

		FileInputStream propFile = new FileInputStream(config);
		Properties p = new Properties(System.getProperties());
		p.load(propFile);
		System.setProperties(p);

	}

	public void run(String[] args) {

		Options options = new Options();
		options.addOption("p", "prefix", true, "prefix used by Nuxeo in Kafka");
		options.addOption("c", "config", true, "Kafka properties file");
		options.addOption("n", "serviceName", true, "service name");
		options.addOption( "cqPath", true, "CQ path - used for unit tests");

		declareAdditionalOptions(options);

		options.addOption("h", "help", false, "Help");

		CommandLineParser parser = new DefaultParser();

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			return;
		}

		HelpFormatter formatter = new HelpFormatter();

		if (cmd.hasOption('h')) {
			formatter.printHelp("ExternalService CLI", options);
			return;
		}

		String prefix = cmd.getOptionValue("p", "nuxeo");
		String config = cmd.getOptionValue("c", "kafka.properties");
		String serviceName = cmd.getOptionValue("n", "externalservice");
		String cqPath = cmd.getOptionValue("cqPath");
		
		try {
			initSystemProperties(config);
		} catch (Exception e) {
			if (cqPath==null) {
				System.err.println("Unable to load system properties from " + config + ":" + e.getMessage());
				return;
			}
		}

		
		LogManager lm = createManager(prefix, cmd.getOptionValue("cqPath", null));

		handleCommand(cmd, options, lm, serviceName);

	}

	protected abstract void handleCommand(CommandLine cmd, Options options, LogManager lm, String serviceName);
	protected abstract void declareAdditionalOptions(Options options);

}
