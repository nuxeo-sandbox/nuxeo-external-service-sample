package com.nuxeo.service;

import com.nuxeo.service.messages.ExternalServiceMessage;

public interface ExternalServiceWrapper {

	String postMessage(String serviceName, ExternalServiceMessage message);

	
	void handleResponseMessage(String serviceName, ExternalServiceMessage message);
	
}
