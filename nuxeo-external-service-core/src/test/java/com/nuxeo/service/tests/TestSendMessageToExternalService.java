package com.nuxeo.service.tests;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogRecord;
import org.nuxeo.lib.stream.log.LogTailer;
import org.nuxeo.lib.stream.log.Name;
import org.nuxeo.runtime.api.Framework;
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
public class TestSendMessageToExternalService {

	@Inject
	protected ExternalServiceWrapper wrapper;
	
	protected LogTailer<Record> getTailer() {
		StreamService service = Framework.getService(StreamService.class);
		org.nuxeo.lib.stream.log.LogManager manager = service.getLogManager();
		LogTailer<Record> tailer = manager.createTailer(Name.of(ExternalServiceWrapperComponent.NS, "test"),
				Name.of(ExternalServiceWrapperComponent.NS, ExternalServiceWrapperComponent.RQ));
		return tailer;
	}

	@Test
	public void iCanSendAMessage() throws Exception {

		ExternalServiceWrapper extService = Framework.getService(ExternalServiceWrapper.class);

		ExternalServiceMessage msg = new ExternalServiceMessage();
		msg.command = "newDocument";
		msg.addParameter("docId", UUID.randomUUID().toString());

		String key = extService.postMessage("externalService", msg);

		LogTailer<Record> tailer = getTailer();

		LogRecord<Record> logEntry = tailer.read(Duration.ofSeconds(1));

		assertNotNull(logEntry);

		Record record = logEntry.message();
		assertTrue(record.getKey().contains(key));
		System.out.println(record.toString());

		byte[] bytes = record.getData();
		String jsonStr = new String(bytes, "UTF-8");
		assertTrue(jsonStr.contains(msg.command));
		assertTrue(jsonStr.contains(msg.getParameters().get("docId")));
		System.out.println(jsonStr);
		
		tailer.close();
	}
	
}
