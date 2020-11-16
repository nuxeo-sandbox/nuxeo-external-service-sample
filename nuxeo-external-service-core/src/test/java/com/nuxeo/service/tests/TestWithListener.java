package com.nuxeo.service.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.CoreFeature;
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
import org.nuxeo.runtime.transaction.TransactionHelper;

import com.google.inject.Inject;
import com.nuxeo.service.ExternalServiceWrapper;
import com.nuxeo.service.ExternalServiceWrapperComponent;
import com.nuxeo.service.messages.ExternalServiceMessage;

@RunWith(FeaturesRunner.class)
@Features({ RuntimeStreamFeature.class, CoreFeature.class })
@Deploy("com.nuxeo.external.service.core")
@Deploy("com.nuxeo.external.service.core.test:async-listener-contrib.xml")
public class TestWithListener {

	@Inject
	protected ExternalServiceWrapper wrapper;

	@Inject
	protected CoreSession session;

	@Inject
	protected EventService eventService;

	@Test
	public void testEnd2EndUsingListenerAndUpdate() throws Exception {

		DocumentModel doc = session.createDocumentModel("/", "sampleFile", "File");

		doc.setPropertyValue("dc:title", "Sample File");
		doc.setPropertyValue("dc:description", "Sample File from Nuxeo Repository");

		Blob blob = new StringBlob("SomeContent", "text/plain");
		blob.setFilename("sample.txt");
		blob.setMimeType("text/plain");
		doc.setPropertyValue("file:content", (Serializable) blob);

		doc = session.createDocument(doc);

		// force commit so that async listeners are triggered
		TransactionHelper.commitOrRollbackTransaction();

		eventService.waitForAsyncCompletion();

		// be nice and start a new tx
		TransactionHelper.startTransaction();

		//
		fakeExternalServiceProcessing();

		Thread.sleep(1000);

		// access methods not exposed via service interface
		ExternalServiceWrapperComponent component = (ExternalServiceWrapperComponent) wrapper;

		ExternalServiceMessage message = component.getLastReceivedResponse();
		assertNotNull(message);
		assertEquals("updateDoc", message.command);
		assertEquals(doc.getId(), message.getParameters().get("docId"));

		System.out.println(message.getParameters());

		// now re-fetch the doc from id and check that it was indeed updated
		doc = session.getDocument(new IdRef(doc.getId()));
		assertEquals("Scan", doc.getPropertyValue("dc:source"));
		assertEquals("Meeting Note", doc.getPropertyValue("dc:format"));

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

		LogRecord<Record> logEntry = tailer.read(Duration.ofSeconds(5));

		assertNotNull(logEntry);

		Record record = logEntry.message();

		ExternalServiceMessage message = messageCodec.decode(record.getData());

		ExternalServiceMessage response = new ExternalServiceMessage();

		response.command = "updateDoc";
		response.addParamater("docId", message.getParameters().get("docId"));
		response.addParamater("repository", message.getParameters().get("repository"));

		response.addParamater("success", "true");
		response.addParamater("dc:description", "newDescription");
		response.addParamater("dc:format", "Meeting Note");
		response.addParamater("dc:source", "Scan");

		String key = response.command + ":" + UUID.randomUUID().toString();

		Record responseRecord = Record.of(key, messageCodec.encode(response));

		LogAppender<Record> appender = manager.getAppender(Name.of("externalservice", "response"));

		appender.append(key, responseRecord);

	}

}
