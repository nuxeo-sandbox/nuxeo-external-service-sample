package com.nuxeo.service;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.runtime.model.Descriptor;

@XObject("externalService")
public class ExternalServiceConfigDescriptor implements Descriptor {

	@XNode("@id")
	private String id;

	@XNode("@name")
	private String name;

	@XNode("description")
	private String description;

	@XNode("label")
	private String label;

	@XNode("namespace")
	private String namespace;

	@Override
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getLabel() {
		return label;
	}

	public String getNamespace() {
		return namespace;
	}

	
	

}
