/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Tiry
 */

package com.nuxeo.service.cli;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogAppender;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.LogOffset;
import org.nuxeo.lib.stream.log.Name;
import org.nuxeo.lib.stream.log.kafka.KafkaLogManager;
import org.nuxeo.lib.stream.log.kafka.KafkaUtils;

public class SendResponseToNuxeo {

	public static Properties getProducerProps() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaUtils.getBootstrapServers());
		return props;
	}

	public static Properties getConsumerProps() {
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

	protected static LogManager createManager(String prefix) {
		return new KafkaLogManager(prefix, getProducerProps(), getConsumerProps());
	}

	protected static void initSystemProperties(String config) throws Exception {

		FileInputStream propFile = new FileInputStream(config);
		Properties p = new Properties(System.getProperties());
		p.load(propFile);
		System.setProperties(p);

	}

	public static void main(String[] args) {

		Options options = new Options();
		options.addOption("p", "prefix", true, "prefix used by Nuxeo in Kafka");
		options.addOption("c", "config", true, "Kafka properties file");
		options.addOption("n", "serverName", true, "service name");
		options.addOption("m", "message", true, "json message (file or inline content)");

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
			formatter.printHelp("SendResponseToNuxeo", options);
			return;
		}

		String prefix = cmd.getOptionValue("p", "nuxeo");
		String config = cmd.getOptionValue("c", "kafka.properties");
		String serviceName = cmd.getOptionValue("n", "externalservice");
		String json = cmd.getOptionValue("m");

		if (json == null) {
			System.err.println("no input message found ");
			formatter.printHelp("SendResponseToNuxeo", options);
			return;
		}
		json = json.trim();
		if (!json.startsWith("{")) {
			// load from file
			try {
				json = FileUtils.readFileToString(new File(json), "UTF-8");
			} catch (IOException e) {
				System.err.println("Unable to json from file " + json + ":" + e.getMessage());
				return;
			}
		}

		try {
			initSystemProperties(config);
		} catch (Exception e) {
			System.err.println("Unable to load system properties from " + config + ":" + e.getMessage());
			return;
		}

		LogManager lm = createManager(prefix);

		LogAppender<Externalizable> appender = lm.getAppender(Name.of(serviceName, "response"));

		String key = UUID.randomUUID().toString();

		Record record = Record.of(key, json.getBytes());

		LogOffset offset = appender.append(key, record);

		System.out.println("Message appended to offset " + offset.toString());

	}
}
