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

/**
 * A implementation of the <code>Event</code> base class for signaling events
 * from an internal, local source within a flow artifact such as an
 * <code>Action</code> or <code>State</code> definition. This is the
 * simplest <code>Event</code> implementation.
 * @author Keith Donald
 */
public class LocalEvent extends Event {

	/**
	 * The event identifier.
	 */
	private String id;

	/**
	 * The state in which this event was signaled (optional)
	 */
	private String stateId;

	/**
	 * Event parameters (optional)
	 */
	private Map parameters;

	/**
	 * Create a local event with the specified <code>id</code>.
	 * @param id the event identifier
	 */
	public LocalEvent(String id) {
		setId(id);
	}

	/**
	 * Create a local event with the specified <code>id</code> occuring in the
	 * state with the specified <code>stateId</code>.
	 * @param id the event identifier
	 * @param stateId the state in which this event occured
	 */
	public LocalEvent(String id, String stateId) {
		setId(id);
		this.stateId = stateId;
	}

	/**
	 * Create a local event with the specified <code>id</code> with the
	 * provided contextual parameters.
	 * @param id the event identifier
	 * @param parameters the event parameters
	 */
	public LocalEvent(String id, Map parameters) {
		setId(id);
		setParameters(parameters);
	}

	/**
	 * Create a local event with the specified <code>id</code> occuring in the
	 * state with the specified <code>stateId</code>.
	 * @param id the event identifier
	 * @param stateId the state in which this event occured
	 * @param parameters contextual parameters
	 */
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