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

import java.util.EventObject;
import java.util.Map;

import org.springframework.binding.AttributeAccessor;
import org.springframework.util.ToStringCreator;

/**
 * Signals the occurence of a <i>request</i> that is relevant to the web flow
 * system. Each event has a string id. An event may optionally contain
 * information about the state in which it occured. Events may have parameters.
 * Events are immutable.
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
public abstract class Event extends EventObject implements AttributeAccessor {

	/**
	 * Constructs a new event with the specified source
	 * @param source the source
	 */
	public Event(Object source) {
		super(source);
	}

	/**
	 * Returns the event identifier.
	 * @return The event id
	 */
	public abstract String getId();

	/**
	 * Returns the time at which the event occured.
	 * @return the timestamp
	 */
	public abstract long getTimestamp();

	/**
	 * Returns the state in which this event occured (optional).
	 * @return the state id, or <code>null</code> if not specified
	 */
	public abstract String getStateId();

	/**
	 * Returns an unmodifiable parameter map storing parameters associated with
	 * this event.
	 * @return the parameters of the event
	 */
	public abstract Map getParameters();

	/**
	 * Returns a parameter value given a parameter name, or <code>null</code>
	 * if no parameter was found.
	 * @param parameterName the name of the parameter
	 * @return the parameter value, or <code>null</code> if the parameter is
	 *         not present in the request
	 */
	public abstract Object getParameter(String parameterName);

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