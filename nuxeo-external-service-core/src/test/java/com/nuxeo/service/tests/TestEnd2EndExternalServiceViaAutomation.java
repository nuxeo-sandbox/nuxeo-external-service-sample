package com.nuxeo.service.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.nuxeo.runtime.stream.RuntimeStreamFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;
import com.nuxeo.service.ExternalServiceWrapper;
import com.nuxeo.service.ExternalServiceWrapperComponent;
import com.nuxeo.service.messages.ExternalServiceMessage;
import com.nuxeo.service.operation.ExternalServiceOperation;

@RunWith(FeaturesRunner.class)
@Features({ RuntimeStreamFeature.class, AutomationFeature.class })
@Deploy("com.nuxeo.external.service.core")
public class TestEnd2EndExternalServiceViaAutomation extends BaseExternalServiceSimulator {

	@Inject
	protected ExternalServiceWrapper wrapper;

	@Inject
	protected CoreSession session;

	@Inject
	protected AutomationService automationService;

	@Test
	public void testEnd2EndUsingAutomationAPI() throws Exception {

		// call service via the Automation API

		// AutomationAPI => ExternalServiceWrapper => Message in Kafka

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("serviceName", "externalservice");
		parameters.put("command", "doSomething");

		// message properties
		String docId = UUID.randomUUID().toString();
		Properties props = new Properties();
		props.put("docId", docId);
		props.put("dc:description", "sometext");

		// Automation HTTP API requires a specific marshaling
		String flatMap = props.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().toString())
				.collect(Collectors.joining("\n"));
		parameters.put("parameters", flatMap);

		// Do the actual Automation call
		OperationContext ctx = new OperationContext(session);
		String key = (String) automationService.run(ctx, ExternalServiceOperation.ID, parameters);

		// now we need to simulate the work on the external service
		fakeExternalServiceProcessing();

		// at this point we should have received a response
		// messages => computation => ExternalServiceWrapperComponent

		Thread.sleep(1000);

		// access methods not exposed via service interface
		ExternalServiceWrapperComponent component = (ExternalServiceWrapperComponent) wrapper;

		ExternalServiceMessage message = component.getLastReceivedResponse();
		assertNotNull(message);
		assertEquals("doSomething", message.command);
		assertEquals(docId, message.getParameters().get("docId"));

	}

}
