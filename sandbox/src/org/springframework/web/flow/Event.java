/*
 * Copyright 2002-2005 the original author or authors.
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
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.AttributeSource;
import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Signals the occurence of something the webflow system should respond to.
 * Each event has a string id that provides a key for what happen: e.g "coinInserted", or "pinDropped".
 * An event may optionally contain information about the state in which it occured, e.g "displayingVendingMachine",
 * or "waitingForUser".  Events may have parameters that provide arbitrary payload data.
 * <p>
 * For example, a "submit" event might signal that a Submit button was pressed
 * in a web browser. A "success" event might signal an action executed
 * successfully. A "finish" event might signal a sub flow ended normally.
 * <p>
 * Why is this class abstract and not an interface? A specific design choice. An
 * event does not define a generic contract or role, it is expected that
 * specializations of this base class be "Events" and not part of some other
 * inheritence hierarchy.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class Event extends EventObject implements AttributeSource {

	/**
	 * The event identifier.
	 */
	private String id;

	/**
	 * The time the event occured.
	 */
	private long timestamp = System.currentTimeMillis();

	/**
	 * The state the event occured in (optional).
	 */
	private String stateId;

	/**
	 * Event parameters (optional event payload).
	 */
	private Map parameters;
	
	/**
	 * Constructs a new event with the specified source.
	 * @param source the source of the event
	 */
	public Event(Object source) {
		super(source);
	}

	/**
	 * Create a new event with the specified <code>id</code>.
	 * @param source the source of the event
	 * @param id the event identifier
	 */
	public Event(Object source, String id) {
		super(source);
		setRequiredId(id);
	}

	/**
	 * Create a new event with the specified <code>id</code> and the
	 * provided contextual parameters.
	 * @param source the source of the event
	 * @param id the event identifier
	 * @param parameters the event parameters
	 */
	public Event(Object source, String id, Map parameters) {
		super(source);
		setRequiredId(id);
		setParameters(parameters);
	}

	/**
	 * Create a new event with the specified <code>id</code> occuring in the
	 * state with the specified <code>stateId</code> and the provided
	 * contextual parameters.
	 * @param source the source of the event
	 * @param id the event identifier
	 * @param stateId the state in which this event occured
	 * @param parameters contextual parameters
	 */
	public Event(Object source, String id, String stateId, Map parameters) {
		super(source);
		setRequiredId(id);
		setStateId(stateId);
		setParameters(parameters);
	}

	/**
	 * Returns the event identifier, or <code>null</code> if not
	 * available (e.g. for an event starting a flow).
	 * @return the event id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Set the event identifier and make sure it is not null.
	 * @param id the event identifier
	 */
	protected void setRequiredId(String id) {
		Assert.hasText(id, "The event id is required for this use -- please set to a non-blank string identifier");
		this.id = id;
	}

	/**
	 * Set the event identifier.
	 */
	protected void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Returns the time at which the event occured.
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Returns the state in which this event occured (optional).
	 * @return the state id, or <code>null</code> if not specified
	 */
	public String getStateId() {
		return this.stateId;
	}

	/**
	 * Set the state identifier.
	 */
	protected void setStateId(String stateId) {
		this.stateId = stateId;
	}

	/**
	 * Returns a parameter value given a parameter name, or <code>null</code>
	 * if the parameter was not found.
	 * @param parameterName the name of the parameter
	 * @return the parameter value, or <code>null</code> if the parameter is
	 *         not present in the event
	 */
	public Object getParameter(String parameterName) {
		if (parameters != null) {
			return parameters.get(parameterName);
		}
		else {
			return null;
		}
	}

	/**
	 * Returns an unmodifiable parameter map storing parameters associated with
	 * this event.
	 * @return the parameters of the event
	 */
	public Map getParameters() {
		if (parameters != null) {
			return Collections.unmodifiableMap(parameters);
		}
		else {
			return null;
		}
	}

	/**
	 * Set the contextual parameters.
	 */
	protected void setParameters(Map parameters) {
		if (parameters != null) {
			this.parameters = new HashMap(parameters);
		}
	}

	/**
	 * Add given parameters to the set of parameters of this event.
	 */
	protected void addParameters(Map parameters) {
		this.parameters.putAll(parameters);
	}

	// implementing AttributeAccessor

	public boolean containsAttribute(String attributeName) {
		return getParameters().containsKey(attributeName);
	}

	public Object getAttribute(String attributeName) {
		return getParameter(attributeName);
	}

	public String toString() {
		return new ToStringCreator(this).append("source", getSource()).append("id", getId()).append("stateId",
				getStateId()).append("parameters", getParameters()).toString();
	}
}