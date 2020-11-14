import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogAppender;
import org.nuxeo.lib.stream.log.LogRecord;
import org.nuxeo.lib.stream.log.LogTailer;
import org.nuxeo.lib.stream.log.Name;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.codec.AvroCodecFactory;
import org.nuxeo.runtime.stream.RuntimeStreamFeature;
import org.nuxeo.runtime.stream.StreamService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;
import com.nuxeo.service.ExternalServiceWrapper;
import com.nuxeo.service.ExternalServiceWrapperComponent;
import com.nuxeo.service.messages.ExternalServiceMessage;

@RunWith(FeaturesRunner.class)
@Features(RuntimeStreamFeature.class)
@Deploy("com.nuxeo.external.service.core")
public class TestReceiveMessageFromExternalService {

	@Inject
	protected ExternalServiceWrapper wrapper;
	
	protected LogTailer<Record> getTailer() {
		StreamService service = Framework.getService(StreamService.class);
		org.nuxeo.lib.stream.log.LogManager manager = service.getLogManager();
		LogTailer<Record> tailer = manager.createTailer(Name.of(ExternalServiceWrapperComponent.NS, "test"),
				Name.of(ExternalServiceWrapperComponent.NS, ExternalServiceWrapperComponent.RQ));
		return tailer;
	}

	
	protected String produceServiceResponse() {
		
		AvroCodecFactory avroFactory = new AvroCodecFactory();
		Map<String, String> options = new HashMap<>();
		options.put(AvroCodecFactory.KEY_ENCODING, "json");
		avroFactory.init(options);

		Codec<ExternalServiceMessage> messageCodec = avroFactory.newCodec(ExternalServiceMessage.class);
	
		ExternalServiceMessage message = new ExternalServiceMessage();
		message.command="someCommand";
		
		String docId = UUID.randomUUID().toString();
		message.addParamater("docId", docId);
		message.addParamater("success", "true");
		message.addParamater("dc:description", "newDescription");
		message.addParamater("facet", "newfacet");
		
		
		String key = message.command + ":" + UUID.randomUUID().toString();
		Record record = Record.of(key, messageCodec.encode(message));

		StreamService service = Framework.getService(StreamService.class);

		org.nuxeo.lib.stream.log.LogManager manager = service.getLogManager();

		LogAppender<Record> appender = manager.getAppender(Name.of("externalservice", "response"));

		appender.append(key, record);
		
		return docId;
		
	}
	
	@Test
	public void iCanReceiveAMessage() throws Exception {

		String docId = produceServiceResponse();
						
	    Thread.sleep(2000);
	    
		// access methods not exposed via service interface
		ExternalServiceWrapperComponent component = (ExternalServiceWrapperComponent) wrapper;

		ExternalServiceMessage message = component.getLastReceivedResponse();
		assertNotNull(message);
		assertEquals("someCommand", message.command);
		assertEquals(docId, message.getParameters().get("docId"));
	}
	

	
	
}
