package com.nuxeo.service.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.stream.RuntimeStreamFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;
import com.nuxeo.service.ExternalServiceWrapper;
import com.nuxeo.service.ExternalServiceWrapperComponent;
import com.nuxeo.service.messages.ExternalServiceMessage;

@RunWith(FeaturesRunner.class)
@Features({ RuntimeStreamFeature.class, CoreFeature.class })
@Deploy("com.nuxeo.external.service.core")
public class TestEnd2EndExternalService extends BaseExternalServiceSimulator {

	@Inject
	protected ExternalServiceWrapper wrapper;

	@Inject
	protected CoreSession session;

	@Test
	public void testEnd2EndUsingJavaAPI() throws Exception {

		ExternalServiceMessage message = new ExternalServiceMessage("doSomething");

		String docId = UUID.randomUUID().toString();

		message.addParameter("docId", docId);
		message.addParameter("dc:description", "sometext");

		wrapper.postMessage("externalservice", message);

		// now we need to simulate the work on the external service
		fakeExternalServiceProcessing();

		// at this point we should have received a response
		// messages => computation => ExternalServiceWrapperComponent

		Thread.sleep(1000);

		// access methods not exposed via service interface
		ExternalServiceWrapperComponent component = (ExternalServiceWrapperComponent) wrapper;

		ExternalServiceMessage reponse = component.getLastReceivedResponse();
		assertNotNull(reponse);
		assertEquals("doSomething", reponse.command);
		assertEquals(docId, reponse.getParameters().get("docId"));

	}

}
