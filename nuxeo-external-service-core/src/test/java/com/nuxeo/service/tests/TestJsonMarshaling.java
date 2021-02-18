package com.nuxeo.service.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.nuxeo.service.messages.ExternalServiceMessage;

public class TestJsonMarshaling {

	@Test
	public void IcanMarshallAndUnmarshalMessage() {
		ExternalServiceMessage esm = new ExternalServiceMessage();		
		esm.command="Command1";
		esm.addParameter("p1", "v1");
		esm.addParameter("p2", "v2");
		esm.addParameter("nestedJson", "{ \"filenames\": [ \"gs://somebucket/someprefix/somefile.json\"] }");
		
		String json = esm.toJson();
		
		ExternalServiceMessage esm2 = ExternalServiceMessage.parse(json);
		assertEquals(esm.command, esm2.command);
		for (String k : esm.getParameters().keySet()) {			
			assertEquals(esm.getParameters().get(k), esm2.getParameters().get(k));				
		}		
	}
	
	
	
	@Test
	public void IcanUnmarshalMessage() {
		
		String json = "{\"command\":\"doSomething\",\"success\":false,\"parameters\":{\"dc:description\":\"sometext\",\"docId\":\"4afd78ca-fc47-4cbe-9f92-5ec809b0f4cb\"}}";
		
		ExternalServiceMessage esm = ExternalServiceMessage.parse(json);
		assertNotNull(esm);
		
		assertFalse(esm.isSuccess());
	}
	
}
