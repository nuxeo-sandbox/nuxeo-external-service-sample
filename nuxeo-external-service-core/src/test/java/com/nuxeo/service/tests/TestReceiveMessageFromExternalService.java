package com.nuxeo.service.tests;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.runtime.stream.RuntimeStreamFeature;
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
public class TestReceiveMessageFromExternalService extends BaseExternalServiceSimulator {

	@Inject
	protected ExternalServiceWrapper wrapper;

	protected String produceServiceResponse() throws Exception {
		ExternalServiceMessage message = new ExternalServiceMessage("someCommand");
		String docId = UUID.randomUUID().toString();
		message.addParameter("docId", docId);
		message.addParameter("success", "true");
		message.addParameter("dc:description", "newDescription");
		message.addParameter("facet", "newfacet");
		fakeServiceResponse(message);
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
