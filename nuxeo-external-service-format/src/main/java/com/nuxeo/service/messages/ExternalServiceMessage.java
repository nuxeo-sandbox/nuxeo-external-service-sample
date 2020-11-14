package com.nuxeo.service.messages;

import java.util.HashMap;
import java.util.Map;

public class ExternalServiceMessage {

	public String command;

	protected Map<String, String> parameters = new HashMap<>();

	public void addParamater(String name, String value) {
		parameters.put(name, value);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

}
