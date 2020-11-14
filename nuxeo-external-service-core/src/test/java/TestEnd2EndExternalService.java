import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.CoreSession;
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
import com.nuxeo.service.operation.ExternalServiceOperation;

@RunWith(FeaturesRunner.class)
@Features({RuntimeStreamFeature.class, AutomationFeature.class})
@Deploy("com.nuxeo.external.service.core")
public class TestEnd2EndExternalService {

	@Inject
	protected ExternalServiceWrapper wrapper;


    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    @Test
    public void testEnd2EndUsingAutomationAPI() throws Exception {

    	// call service via the Automation API
    	
    	OperationContext ctx = new OperationContext(session);
        
    	Map<String, Object> parameters = new HashMap<>();
    	parameters.put("serviceName", "externalservice");
    	parameters.put("command", "doSomething");
    	
    	String docId = UUID.randomUUID().toString();
    	
    	Properties props = new Properties();
    	props.put("docId", docId);
    	props.put("dc:description", "sometext");
    	
    	String flatMap = props.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().toString()).collect(Collectors.joining("\n"));
    	parameters.put("parameters", flatMap);
    	
    	String key = (String)automationService.run(ctx, ExternalServiceOperation.ID,parameters);     
    	
    	
    	// now we need to simulate the work on the external service
    	fakeExternalServiceProcessing();
    	
    	Thread.sleep(1000);
    	
    	// access methods not exposed via service interface
		ExternalServiceWrapperComponent component = (ExternalServiceWrapperComponent) wrapper;

		ExternalServiceMessage message = component.getLastReceivedResponse();
		assertNotNull(message);
		assertEquals("doSomething", message.command);
		assertEquals(docId, message.getParameters().get("docId"));

    	
    	
    }

	
    protected void fakeExternalServiceProcessing() throws Exception {
    	
		AvroCodecFactory avroFactory = new AvroCodecFactory();
		Map<String, String> options = new HashMap<>();
		options.put(AvroCodecFactory.KEY_ENCODING, "json");
		avroFactory.init(options);
		Codec<ExternalServiceMessage> messageCodec = avroFactory.newCodec(ExternalServiceMessage.class);
    	
    	StreamService service = Framework.getService(StreamService.class);
		org.nuxeo.lib.stream.log.LogManager manager = service.getLogManager();
		LogTailer<Record> tailer = manager.createTailer(Name.of(ExternalServiceWrapperComponent.NS, "test"),
				Name.of(ExternalServiceWrapperComponent.NS, ExternalServiceWrapperComponent.RQ));
	
    	
		LogRecord<Record> logEntry = tailer.read(Duration.ofSeconds(1));

		assertNotNull(logEntry);

		Record record = logEntry.message();

		
		ExternalServiceMessage message = messageCodec.decode(record.getData());

		message.addParamater("success", "true");
		message.addParamater("dc:description", "newDescription");
		message.addParamater("facet", "newfacet");
		
		
		String key = message.command + ":" + UUID.randomUUID().toString();
		
		Record responseRecord = Record.of(key, messageCodec.encode(message));

		LogAppender<Record> appender = manager.getAppender(Name.of("externalservice", "response"));

		appender.append(key, responseRecord);		
    	
    }
    
	
	
}
