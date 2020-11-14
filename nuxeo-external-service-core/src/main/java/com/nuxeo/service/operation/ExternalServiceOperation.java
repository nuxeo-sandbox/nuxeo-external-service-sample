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
