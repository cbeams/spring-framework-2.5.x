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
package org.springframework.web.flow.execution;

import java.util.Iterator;

import org.springframework.web.flow.Event;

/**
 * Abstract superclass for events encapsulating a request coming into a
 * flow execution from an external source (client). This kind of event is typically
 * the source event of a <code>RequestContext</code> in a flow execution.
 * 
 * @see org.springframework.web.flow.RequestContext
 * @see org.springframework.web.flow.execution.FlowExecution
 * 
 * @author Erwin Vervaet
 */
public abstract class ExternalEvent extends Event {

	/**
	 * Clients can send the event to be signaled in an event
	 * parameter with this name ("_eventId").
	 */
	public static final String EVENT_ID_PARAMETER = "_eventId";

	/**
	 * Clients can send the current state in an event parameter
	 * with this name ("_currentStateId").
	 */
	public static final String CURRENT_STATE_ID_PARAMETER = "_currentStateId";

	/**
	 * The default delimiter used when a parameter value is sent as
	 * part of the name of an event parameter (e.g. "_eventId_value=bar").
	 */
	public static final String PARAMETER_VALUE_DELIMITER = "_";

	/**
	 * Creates an external event with the specified external source,
	 * for example a HTTP request.
	 * @param source the event source
	 */
	public ExternalEvent(Object source) {
		super(source);
	}

	// support methods

	/**
	 * Obtain a named parameter from the event parameters. This method will
	 * try to obtain a parameter value using the following algorithm:
	 * <ol>
	 * <li>Try to get the parameter value using just the given
	 * <i>logical</i> name. This handles parameters of the form
	 * <tt>logicalName = value</tt>. For normal parameters, e.g.
	 * submitted using a hidden HTML form field, this will return the requested
	 * value.</li>
	 * <li>Try to obtain the parameter value from the parameter name, where the
	 * parameter name in the event is of the form
	 * <tt>logicalName_value = xyz</tt> with "_" being the specified
	 * delimiter. This deals with parameter values submitted using an HTML form
	 * submit button.</li>
	 * <li>If the value obtained in the previous step has a ".x" or ".y"
	 * suffix, remove that. This handles cases where the value was submitted
	 * using an HTML form image button. In this case the parameter in the
	 * event would actually be of the form <tt>logicalName_value.x = 123</tt>.
	 * </li>
	 * </ol>
	 * @param logicalName the <i>logical</i> name of the request parameter
	 * @param delimiter the delimiter to use
	 * @return the value of the parameter, or <code>null</code> if the
	 *         parameter does not exist in given request
	 */
	protected Object searchForParameter(String logicalName, String delimiter) {
		// first try to get it as a normal name=value parameter
		Object value = getParameter(logicalName);
		if (value != null) {
			return value;
		}
		// if no value yet, try to get it as a name_value=xyz parameter
		String prefix = logicalName + delimiter;
		Iterator paramNames = getParameters().keySet().iterator();
		while (paramNames.hasNext()) {
			String paramName = (String)paramNames.next();
			if (paramName.startsWith(prefix)) {
				String strValue = paramName.substring(prefix.length());
				// support images buttons, which would submit parameters as
				// name_value.x=123
				if (strValue.endsWith(".x") || strValue.endsWith(".y")) {
					strValue = strValue.substring(0, strValue.length() - 2);
				}
				return strValue;
			}
		}
		// we couldn't find the parameter value
		return null;
	}
}