/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.support;

import java.util.Collections;
import java.util.Map;

public class LocalEvent extends AbstractEvent {

	private String id;

	private String stateId;

	private Map parameters;

	public LocalEvent(String id) {
		this.id = id;
	}

	public LocalEvent(String id, String stateId) {
		this.id = id;
		this.stateId = stateId;
	}

	public LocalEvent(String id, Map input) {
		this.id = id;
		this.parameters = input;
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