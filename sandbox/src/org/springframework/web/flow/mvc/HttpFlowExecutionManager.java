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
package org.springframework.web.flow.mvc;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowConstants;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowExecutionStack;
import org.springframework.web.flow.NoSuchFlowExecutionException;
import org.springframework.web.flow.config.FlowServiceLocator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

/**
 * Helper to manage flow execution and process requests coming into a flow
 * execution.
 * 
 * @author Erwin Vervaet
 */
public class HttpFlowExecutionManager {

	private static final Log logger = LogFactory.getLog(HttpFlowExecutionManager.class);

	private Flow flow;

	private FlowServiceLocator flowServiceLocator;

	private Collection flowExecutionListeners;

	public HttpFlowExecutionManager(Flow flow) {
		this.flow = flow;
	}

	public HttpFlowExecutionManager(Flow flow, Collection flowExecutionListeners) {
		this.flow = flow;
		this.flowExecutionListeners = flowExecutionListeners;
	}

	public HttpFlowExecutionManager(FlowServiceLocator flowServiceLocator) {
		this.flowServiceLocator = flowServiceLocator;
	}

	public HttpFlowExecutionManager(FlowServiceLocator flowServiceLocator, Collection flowExecutionListeners) {
		this.flowServiceLocator = flowServiceLocator;
		this.flowExecutionListeners = flowExecutionListeners;
	}

	public HttpFlowExecutionManager(Flow flow, FlowServiceLocator flowServiceLocator) {
		this.flow = flow;
		this.flowServiceLocator = flowServiceLocator;
	}

	public HttpFlowExecutionManager(Flow flow, FlowServiceLocator flowServiceLocator, Collection flowExecutionListeners) {
		this.flow = flow;
		this.flowServiceLocator = flowServiceLocator;
		this.flowExecutionListeners = flowExecutionListeners;
	}

	protected String getFlowIdParameterName() {
		return FlowConstants.FLOW_ID_PARAMETER;
	}

	protected String getFlowExecutionIdParameterName() {
		return FlowConstants.FLOW_EXECUTION_ID_PARAMETER;
	}

	protected String getCurrentStateIdParameterName() {
		return FlowConstants.CURRENT_STATE_ID_PARAMETER;
	}

	protected String getEventIdParameterName() {
		return FlowConstants.EVENT_ID_PARAMETER;
	}

	protected String getEventIdRequestAttributeName() {
		return FlowConstants.EVENT_ID_REQUEST_ATTRIBUTE;
	}

	protected String getNotSetEventIdParameterMarker() {
		return FlowConstants.NOT_SET_EVENT_ID;
	}

	protected String getParameterValueDelimiter() {
		return "_";
	}

	/**
	 * The main entry point into managed HTTP-based flow executions. Looks for a
	 * flow execution id in the request. If none exists, it creates one. If one
	 * exists, it looks in the user's session to find the current FlowExecution.
	 * The request should also contain the current state id and event id. These
	 * String values will be passed to the FlowExecution to execute the action.
	 * Execution will typically result in a state transition.
	 * @param request the current HTTP request
	 * @param response the current HTTP response
	 * @param inputAttributes input data to be passed to the FlowExecution when
	 *        creating a new FlowExecution
	 * @return the model and view to render
	 * @throws Exception in case of errors
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response, Map inputAttributes)
			throws Exception {
		FlowExecution flowExecution;
		ModelAndView modelAndView;
		if (isNewFlowExecutionRequest(request)) {
			// start a new flow execution
			if (this.flow == null) {
				// try to extract flow definition to use from request
				this.flow = getFlow(request);
			}
			flowExecution = createFlowExecution(flow);
			modelAndView = flowExecution.start(inputAttributes, request, response);
			saveInHttpSession(flowExecution, request);
		}
		else {
			// client is participating in an existing flow execution,
			// retrieve information about it
			flowExecution = getRequiredFlowExecution(request);

			// let client tell you what state they are in (if possible)
			String stateId = request.getParameter(getCurrentStateIdParameterName());

			// let client tell you what event was signaled in the current state
			String eventId = request.getParameter(getEventIdParameterName());

			if (!StringUtils.hasText(eventId)) {
				if (logger.isDebugEnabled()) {
					logger.debug("No '" + getEventIdParameterName() + "' parameter was found; falling back to '"
							+ getEventIdRequestAttributeName() + "' request attribute");
				}
				eventId = (String)request.getAttribute(getEventIdRequestAttributeName());
			}
			if (!StringUtils.hasText(eventId)) {
				eventId = getRequestParameter(request, getEventIdParameterName());
				if (!StringUtils.hasText(eventId)) {
					throw new IllegalArgumentException(
							"The '"
									+ getEventIdParameterName()
									+ "' request parameter (or '"
									+ getEventIdRequestAttributeName()
									+ "' request attribute) is required to signal an event in the current state of this executing flow '"
									+ flowExecution.getCaption() + "' -- programmer error?");
				}
			}
			if (eventId.equals(getNotSetEventIdParameterMarker())) {
				throw new IllegalArgumentException("The eventId submitted by the browser was the 'not set' marker '"
						+ getNotSetEventIdParameterMarker()
						+ "' - this is likely a view (jsp, etc) configuration error - the '"
						+ getEventIdParameterName()
						+ "' parameter must be set to a valid event to execute within the current state '" + stateId
						+ "' of this flow '" + flowExecution.getCaption() + "' - else I don't know what to do!");
			}
			// execute the signaled event within the current state
			modelAndView = flowExecution.signalEvent(eventId, stateId, request, response);
		}
		if (!flowExecution.isActive()) {
			// event execution resulted in the entire flow ending, cleanup
			removeFromHttpSession(flowExecution, request);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected model and view " + modelAndView);
		}
		return modelAndView;
	}

	/**
	 * Obtain a flow to use from given request.
	 */
	protected Flow getFlow(HttpServletRequest request) {
		Assert.notNull("The flow service locator is required to lookup flows to execute by id");
		return flowServiceLocator.getFlow(getRequestParameter(request, getFlowIdParameterName(),
				getParameterValueDelimiter()));
	}

	/**
	 * Create a new flow execution for given flow.
	 * @param flow The flow
	 * @return The created flow execution
	 */
	protected FlowExecution createFlowExecution(Flow flow) {
		FlowExecution flowExecution = new FlowExecutionStack(flow);
		if (flowExecutionListeners != null && !flowExecutionListeners.isEmpty()) {
			flowExecution.getListenerList().add(
					(FlowExecutionListener[])flowExecutionListeners.toArray(new FlowExecutionListener[0]));
		}
		return flowExecution;
	}

	/**
	 * Check if given request is a request for a new flow execution, or a
	 * continuation of an existing one.
	 * @param request the HTTP request to check
	 * @return true or false
	 */
	protected boolean isNewFlowExecutionRequest(HttpServletRequest request) {
		return getRequestParameter(request, getFlowExecutionIdParameterName(), getParameterValueDelimiter()) == null;
	}

	/**
	 * Save the flow execution in the HTTP session associated with given
	 * request.
	 */
	protected void saveInHttpSession(FlowExecution flowExecution, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Saving flow execution '" + flowExecution.getId() + "' in HTTP session");
		}
		request.getSession(false).setAttribute(flowExecution.getId(), flowExecution);
	}

	/**
	 * Get an existing flow execution from the HTTP session associated with
	 * given request.
	 * @throws NoSuchFlowExecutionException If there is no flow execution in the
	 *         HTTP session associated with given request.
	 */
	protected FlowExecution getRequiredFlowExecution(HttpServletRequest request) throws NoSuchFlowExecutionException {
		String flowExecutionId = getRequestParameter(request, getFlowExecutionIdParameterName());
		if (!StringUtils.hasText(flowExecutionId)) {
			throw new IllegalStateException("The '" + getFlowExecutionIdParameterName()
					+ "' parameter is not present in the request; not enough information to lookup flow execution");
		}
		try {
			return (FlowExecution)WebUtils.getRequiredSessionAttribute(request, flowExecutionId);
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowExecutionException(flowExecutionId, e);
		}
	}

	/**
	 * Remove given flow execution from the HTTP session associated with given
	 * request.
	 */
	public void removeFromHttpSession(FlowExecution flowExecution, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow execution '" + flowExecution.getId() + "' from HTTP session");
		}
		request.getSession(false).removeAttribute(flowExecution.getId());
	}

	protected String getRequestParameter(HttpServletRequest request, String logicalName) {
		return getRequestParameter(request, logicalName, null);
	}

	/**
	 * Obtain a named parameter from an HTTP servlet request. This method will
	 * try to obtain a parameter value using the following algorithm:
	 * <ol>
	 * <li>Try to get the parameter value from the request using just the given
	 * <i>logical </i> name. This handles request parameters of the form
	 * <tt>logicalName = value</tt>. For normal request parameters, e.g.
	 * submitted using a hidden HTML form field, this will return the requested
	 * value.</li>
	 * <li>Try to obtain the parameter value from the parameter name, where the
	 * parameter name in the request is of the form
	 * <tt>logicalName_value = xyz</tt> with "_" the being the specified
	 * delimiter. This deals with parameter values submitted using an HTML form
	 * submit button.</li>
	 * <li>If the value obtained in the previous step has a ".x" or ".y"
	 * suffix, remove that. This handles cases where the value was submitted
	 * using an HTML form image button. In this case the parameter in the
	 * request would actually be of the form <tt>logicalName_value.x = 123</tt>.
	 * </li>
	 * </ol>
	 * @param request the current HTTP request
	 * @param logicalName the <i>logical </i> name of the request parameter
	 * @param delimiter the delimiter to use
	 * @return the value of the parameter, or <code>null</code> if the
	 *         parameter does not exist in given request
	 */
	protected String getRequestParameter(HttpServletRequest request, String logicalName, String delimiter) {
		//first try to get it as a normal name=value parameter
		String value = request.getParameter(logicalName);
		if (value != null) {
			return value;
		}
		if (!StringUtils.hasText(delimiter)) {
			delimiter = getParameterValueDelimiter();
		}
		//if no value yet, try to get it as a name_value=xyz parameter
		String prefix = logicalName + delimiter;
		Enumeration paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = (String)paramNames.nextElement();
			if (paramName.startsWith(prefix)) {
				value = paramName.substring(prefix.length());
				//support images buttons, which would submit parameters as
				//name_value.x=123
				if (value.endsWith(".x") || value.endsWith(".y")) {
					value = value.substring(0, value.length() - 2);
				}
				return value;
			}
		}
		//we couldn't find the parameter value
		return null;
	}
}