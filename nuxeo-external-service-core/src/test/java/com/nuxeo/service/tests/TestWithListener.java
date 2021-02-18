package com.nuxeo.service.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.stream.RuntimeStreamFeature;
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
public class TestWithListener extends BaseExternalServiceSimulator {

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

	protected ExternalServiceMessage computeReply(ExternalServiceMessage request) {

		
		// check that we have info about the source blob
		String blobKey = request.getParameters().get("blob_key");
		assertNotNull(blobKey);
		
		ExternalServiceMessage response = new ExternalServiceMessage();

		response.command = "updateDoc";
		response.addParameter("docId", request.getParameters().get("docId"));
		response.addParameter("repository", request.getParameters().get("repository"));
		response.addParameter("dc:description", "newDescription");
		response.addParameter("dc:format", "Meeting Note");
		response.addParameter("dc:source", "Scan");
		
		response.addParameter("ocr_fulltext", "foobar");
		
		response.setSuccess(true);

		response.sessionId=request.sessionId;
		
		return response;
	}

}
