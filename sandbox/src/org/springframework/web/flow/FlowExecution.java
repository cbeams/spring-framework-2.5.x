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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * Subinterface of <code>FlowExecutionInfo</code> that exposes additional
 * information and operations on a flow execution.
 * <p>
 * While the FlowExecutionInfo interface is fit for external management clients
 * (for example, JMX-based), this interface is designed for use by internal
 * clients; for example, the front <code>FlowController</code> and
 * <code>FlowExecutionListener</code>, which are more privileged and more
 * flow-system-aware than pure for-management clients.
 * <p>
 * This interface may also be used in situations where other privileged objects
 * need access to flow definition configuration details during flow execution.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecution extends FlowExecutionInfo, MutableAttributesAccessor {

	/**
	 * A flow execution is available as an attribute from the
	 * <code>getAttribute()</code> method using this priviledged name.
	 */
	public static String ATTRIBUTE_NAME = "flowExecution";

	/**
	 * Returns a mutable list of listeners attached to this flow execution.
	 * @return The flow execution listener list
	 */
	public FlowExecutionListenerList getListenerList();

	/**
	 * Returns this flow execution's active flow definition.
	 * @return The active flow definition
	 */
	public Flow getActiveFlow();

	/**
	 * Returns this flow execution's root flow definition.
	 * @return The root flow definition.
	 */
	public Flow getRootFlow();

	/**
	 * Returns this flow execution's current state definition.
	 * @return the current state definition.
	 */
	public AbstractState getCurrentState();

	/**
	 * Start this flow execution, transitioning it to the start state and
	 * returning the starting model and view descriptor. Typically called by the
	 * FlowController, but also in test code.
	 * @param input Model input attributes to the flow execution
	 * @param request The current http request
	 * @param response The current http response
	 * @return The starting model and view.
	 */
	public ModelAndView start(Map input, HttpServletRequest request, HttpServletResponse response);

	/**
	 * Signal an occurence of the specified event in the (optionally) provided
	 * state of this flow execution.
	 * @param eventId The event that occured
	 * @param stateId The state the event occured in (can be null)
	 * @param request The current http request
	 * @param response The current http response
	 * @return The next model and view descriptor to display for this flow
	 *         execution.
	 */
	public ModelAndView signalEvent(String eventId, String stateId, HttpServletRequest request,
			HttpServletResponse response);

}