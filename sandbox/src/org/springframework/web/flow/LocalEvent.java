/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.flow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

public class LocalEvent extends Event {
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

	public LocalEvent(String id, Map parameters) {
		setId(id);
		setParameters(parameters);
	}

	public LocalEvent(String id, String stateId, Map parameters) {
		setId(id);
		this.stateId = stateId;
		setParameters(parameters);
	}

	private void setId(String id) {
		Assert.hasText(id, "The event id is required");
		this.id = id;
	}

	private void setParameters(Map parameters) {
		if (parameters != null) {
			this.parameters = new HashMap(parameters);
		}
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