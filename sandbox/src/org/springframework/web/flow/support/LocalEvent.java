/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.support;

import java.util.Collections;
import java.util.Map;

import org.springframework.util.Assert;

public class LocalEvent extends AbstractEvent {

	private String id;

	private String stateId;

	private Map parameters;

	public LocalEvent(String id) {
		setId(id);
	}

	public LocalEvent(String id, String stateId) {
		setId(id);
		this.stateId = stateId;
	}

	public LocalEvent(String id, Map input) {
		setId(id);
		this.parameters = input;
	}

	private void setId(String id) {
		Assert.hasText(id, "The event id is required");
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getStateId() {
		return stateId;
	}

	public Object getParameter(String parameterName) {
		if (parameters != null) {
			return parameters.get(parameterName);
		}
		else {
			return null;
		}
	}

	public Map getParameters() {
		if (parameters != null) {
			return Collections.unmodifiableMap(parameters);
		}
		else {
			return null;
		}
	}
}