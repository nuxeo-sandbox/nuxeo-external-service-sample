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
package com.nuxeo.service.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;

import com.nuxeo.service.ExternalServiceWrapper;
import com.nuxeo.service.messages.ExternalServiceMessage;

/**
 * Automation Operation exposing the {@link ExternalServiceWrapper} service via http
 * 
 * @author tiry
 *
 */
@Operation(id = ExternalServiceOperation.ID, category = Constants.CAT_SERVICES, label = "Bulk export a dataset", description = "Run a bulk export on a set of documents expressed by a NXQL query.")
public class ExternalServiceOperation {

	private static final Logger log = LogManager.getLogger(ExternalServiceOperation.class);

	public static final String ID = "Service.External";

	@Context
	protected ExternalServiceWrapper service;

	@Context
	protected CoreSession session;

	@Param(name = "serviceName")
	protected String serviceName;

	@Param(name = "command")
	protected String command;

	@Param(name = "parameters", required = false)
	protected Properties parameters;

	@OperationMethod
	public String run() {
		ExternalServiceMessage message = new ExternalServiceMessage();
		message.command=command;
		message.getParameters().putAll(parameters);
		return service.postMessage(serviceName, message);
	}

}
