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

import java.util.Map;

import org.springframework.binding.AttributeAccessor;
import org.springframework.util.ToStringCreator;

/**
 * Signals the occurence of something that happens relavent to the web.flow
 * system. Each event has a string id. An event may contain information about
 * the state in which it occured. Events may have parameters. Events are
 * immutable.
 * <p>
 * For example, a "submit" event might signal that a Submit button was pressed
 * in a web browser. A "success" event might signal an action executed
 * successfully. A "finish" event might signal a sub flow ended normally.
 * <p>
 * Why is this class abstract and not an interface? A specific design choice. An
 * event does not define a generic contract or role, it is expected that
 * specializations of this base class be "Events" and not part of some other
 * inheritence hierarchy.
 * @author Keith Donald
 */
public abstract class Event implements AttributeAccessor {

	/**
	 * Returns the event identifier.
	 * @return The event id
	 */
	public abstract String getId();

	/**
	 * The time in which the event occured
	 * @return the timestamp
	 */
	public abstract long getTimestamp();

	/**
	 * Returns the state in which this event occured (optional).
	 * @return The state id
	 */
	public abstract String getStateId();

	/**
	 * Returns a unmodifiable parameter map storing parameters associated with
	 * this event.
	 * @return The events parameters
	 */
	public abstract Map getParameters();

	/**
	 * Returns a parameter value given a parameter name, or <code>null</code>
	 * if no parameter was found.
	 * @param parameterName
	 * @return
	 */
	public abstract Object getParameter(String parameterName);

	public boolean containsAttribute(String attributeName) {
		return getParameters().containsKey(attributeName);
	}

	public Object getAttribute(String attributeName) {
		return getParameter(attributeName);
	}

	public String toString() {
		return new ToStringCreator(this).append("id", getId()).append("stateId", getStateId()).append("parameters",
				getParameters()).toString();
	}
}