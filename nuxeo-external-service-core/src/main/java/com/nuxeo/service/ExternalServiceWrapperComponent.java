package com.nuxeo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogAppender;
import org.nuxeo.lib.stream.log.Name;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.codec.AvroCodecFactory;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.runtime.model.Extension;
import org.nuxeo.runtime.stream.StreamService;

import com.nuxeo.service.messages.ExternalServiceMessage;

public class ExternalServiceWrapperComponent extends DefaultComponent implements ExternalServiceWrapper {

	private static final Logger log = LogManager.getLogger(ExternalServiceWrapperComponent.class);

	protected AvroCodecFactory avroFactory;

	protected Codec<ExternalServiceMessage> messageCodec;

	public static final String NS = "externalservice";
	public static final String RQ = "request";
	public static final String RS = "response";

	/**
	 * Component activated notification. Called when the component is activated. All
	 * component dependencies are resolved at that moment. Use this method to
	 * initialize the component.
	 *
	 * @param context the component context.
	 */
	@Override
	public void activate(ComponentContext context) {
		super.activate(context);
	}

	/**
	 * Component deactivated notification. Called before a component is
	 * unregistered. Use this method to do cleanup if any and free any resources
	 * held by the component.
	 *
	 * @param context the component context.
	 */
	@Override
	public void deactivate(ComponentContext context) {
		super.deactivate(context);
	}

	/**
	 * Registers the given extension.
	 *
	 * @param extension the extension to register
	 */
	@Override
	public void registerExtension(Extension extension) {
		super.registerExtension(extension);
	}

	/**
	 * Unregisters the given extension.
	 *
	 * @param extension the extension to unregister
	 */
	@Override
	public void unregisterExtension(Extension extension) {
		super.unregisterExtension(extension);
	}

	/**
	 * Start the component. This method is called after all the components were
	 * resolved and activated
	 *
	 * @param context the component context. Use it to get the current bundle
	 *                context
	 */
	@Override
	public void start(ComponentContext context) {

		super.start(context);

		avroFactory = new AvroCodecFactory();
		Map<String, String> options = new HashMap<>();
		options.put(AvroCodecFactory.KEY_ENCODING, "json");
		avroFactory.init(options);

		messageCodec = avroFactory.newCodec(ExternalServiceMessage.class);
	}

	/**
	 * Stop the component.
	 *
	 * @param context the component context. Use it to get the current bundle
	 *                context
	 * @throws InterruptedException
	 */
	@Override
	public void stop(ComponentContext context) throws InterruptedException {
		// do nothing by default. You can remove this method if not used.
	}

	// -----------------------------

	public static final String XP_CONFIG = "config";

	public List<ExternalServiceConfigDescriptor> getConfigs() {
		return this.getDescriptors(XP_CONFIG);
	}

	public ExternalServiceConfigDescriptor getConfig(String serviceName) {

		for (ExternalServiceConfigDescriptor config : getConfigs()) {
			if (serviceName.equalsIgnoreCase(config.getName())) {
				return config;
			}
		}
		return null;

	}
	
	@Override
	public String postMessage(String serviceName, ExternalServiceMessage message) {

		String key = message.command + ":" + UUID.randomUUID().toString();
		Record record = Record.of(key, messageCodec.encode(message));

		StreamService service = Framework.getService(StreamService.class);

		org.nuxeo.lib.stream.log.LogManager manager = service.getLogManager();

		LogAppender<Record> appender = manager.getAppender(Name.of(getConfig(serviceName).getNamespace(), RQ));

		appender.append(key, record);

		return key;
	}
	
	protected ExternalServiceMessage lastReceivedResponse;
	
	public ExternalServiceMessage getLastReceivedResponse() {
		return lastReceivedResponse;
	}
	
	@Override
	public void handleResponseMessage(String serviceName, ExternalServiceMessage message) {	
		
		// mainly for unit testing purpose
		lastReceivedResponse=message;
		
		// do something
	}
	

}
