/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Tiry
 */
package com.nuxeo.service.messages;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * POJO to store the messages exchanged between Nuxeo and the external service
 * 
 * @author tiry
 *
 */
public class ExternalServiceMessage {

	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Log log = LogFactory.getLog(ExternalServiceMessage.class);

	public String command;

	protected boolean success;

	public String sessionId;

	protected Map<String, String> parameters = new HashMap<>();

	public ExternalServiceMessage() {
		sessionId = UUID.randomUUID().toString();
	};

	public ExternalServiceMessage(String command) {
		super();
		this.command = command;
	};

	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public String toJson() {
		try {
			return OBJECT_MAPPER.writer().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			log.error("Unable to write JSON format for message", e);
		}
		return null;
	}

	public static ExternalServiceMessage parse(String json) {
		try {
			return OBJECT_MAPPER.readValue(json, new TypeReference<ExternalServiceMessage>() {
			});
		} catch (Exception e) {
			log.error("Unable to parse JSON for message", e);
			log.debug(json);
		}
		return null;
	}

	public static ExternalServiceMessage parse(byte[] data) {
		try {
			return parse(new String(data, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to parse JSON for message", e);
		}
		return null;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
