package com.nuxeo.service.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.reflect.Field;

import org.apache.commons.io.FileUtils;
import org.nuxeo.lib.stream.log.UnifiedLogManager;
import org.nuxeo.lib.stream.log.chronicle.ChronicleLogManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.stream.StreamService;

import com.nuxeo.service.cli.GetRequestFromNuxeo;
import com.nuxeo.service.cli.SendResponseToNuxeo;
import com.nuxeo.service.messages.ExternalServiceMessage;

public class BaseExternalServiceSimulator {

	protected String cqPath = null;

	// in the context of unit tests
	// we use CQ as a replacement of Kafka
	// we just need to extract the path when CQ was initialized
	// so that we can have the CLI use the same path
	// (when Using Kafka this is not needed)
	protected String getCQPath() throws Exception {
		if (cqPath == null) {
			StreamService service = Framework.getService(StreamService.class);
			UnifiedLogManager ulm = (UnifiedLogManager) service.getLogManager();
			Field cq = ulm.getClass().getDeclaredField("cqManager");
			cq.setAccessible(true);
			ChronicleLogManager cqlm = (ChronicleLogManager) cq.get(ulm);
			cqPath = cqlm.getBasePath();
		}
		return cqPath;
	}

	protected void fakeExternalServiceProcessing() throws Exception {

		// use CLI to read from topic
		String jsonCmd = fakeServiceReceive();
		assertNotNull(jsonCmd);
		
		ExternalServiceMessage request = ExternalServiceMessage.parse(jsonCmd);
		assertNotNull(request);
		
		ExternalServiceMessage reply = computeReply(request);		
		assertNotNull(reply);

		assertEquals(request.sessionId, reply.sessionId);
		
		// call the CLI to reply to Nuxeo
		fakeServiceResponse(reply);
	}

	protected ExternalServiceMessage computeReply(ExternalServiceMessage request) {
		ExternalServiceMessage reply = ExternalServiceMessage.parse(request.toJson());		
		reply.setSuccess(true);
		reply.addParameter("data", "DataFromExternalService");
		reply.sessionId=request.sessionId;
		return reply; 
	}
	
	protected void fakeServiceResponse(ExternalServiceMessage msg) throws Exception {
		String jsonResponse = msg.toJson();
		String[] args = new String[] { "-cqPath", getCQPath(), "-n", "externalservice", "-m", jsonResponse };
		SendResponseToNuxeo.main(args);
	}
	
	protected String fakeServiceReceive() throws Exception {
		// call CLI to read the message
		File out = File.createTempFile("message", ".json");
		String[] args = new String[] { "-cqPath", getCQPath(), "-o", out.getAbsolutePath() };
		GetRequestFromNuxeo.main(args);
		// CLI should have written the result in the tmp file
		return FileUtils.readFileToString(out, "UTF-8");
	}
	
}
