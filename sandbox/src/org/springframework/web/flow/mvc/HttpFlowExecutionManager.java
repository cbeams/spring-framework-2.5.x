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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowConstants;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowExecutionStack;
import org.springframework.web.flow.NoSuchFlowExecutionException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

/**
 * Helper to manage flow execution and process requests coming into a flow
 * execution.
 * 
 * @author Erwin Vervaet
 */
public class HttpFlowExecutionManager {

	private Log logger;

	private Flow flow;

	private BeanFactory beanFactory;

	private Collection flowExecutionListeners;

	public HttpFlowExecutionManager(Log logger, Flow flow, BeanFactory beanFactory, Collection flowExecutionListeners) {
		Assert.notNull(logger, "The logger is required");
		Assert.notNull(beanFactory, "The bean factory is required");
		this.logger = logger;
		this.flow = flow;
		this.beanFactory = beanFactory;
		this.flowExecutionListeners = flowExecutionListeners;
	}

	protected String getCurrentStateIdAttributeName() {
		return FlowConstants.CURRENT_STATE_ID_ATTRIBUTE;
	}

	protected String getCurrentStateIdParameterName() {
		return FlowConstants.CURRENT_STATE_ID_PARAMETER;
	}

	protected String getFlowExecutionIdAttributeName() {
		return FlowConstants.FLOW_EXECUTION_ID_ATTRIBUTE;
	}

	protected String getFlowExecutionIdParameterName() {
		return FlowConstants.FLOW_EXECUTION_ID_PARAMETER;
	}

	protected String getFlowExecutionAttributeName() {
		return FlowExecution.ATTRIBUTE_NAME;
	}

	private String getEventIdRequestAttributeName() {
		return FlowConstants.EVENT_ID_REQUEST_ATTRIBUTE;
	}

	protected String getEventIdParameterName() {
		return FlowConstants.EVENT_ID_PARAMETER;
	}

	protected String getNotSetEventIdParameterMarker() {
		return FlowConstants.NOT_SET_EVENT_ID;
	}

	protected String getFlowIdParameterName() {
		return FlowConstants.FLOW_ID_PARAMETER;
	}

	public ModelAndView process(HttpServletRequest request, HttpServletResponse response, Map inputData)
			throws Exception {
		FlowExecution flowExecution;
		ModelAndView modelAndView;
		if (isNewFlowExecutionRequest(request)) {
			// start a new flow execution
			if (flow == null) {
				// try to extract flow definition to use from request
				flow = createFlow(request);
			}
			flowExecution = createFlowExecution(flow);
			modelAndView = flowExecution.start(inputData, request, response);
			saveInHttpSession(flowExecution, request);
		}
		else {
			// Client is participating in an existing flow execution,
			// retrieve information about it
			flowExecution = getRequiredFlowExecution(request);

			// let client tell you what state they are in (if possible)
			String stateId = RequestUtils.getStringParameter(request, getCurrentStateIdParameterName(), null);

			// let client tell you what event was signaled in the current state
			String eventId = RequestUtils.getStringParameter(request, getEventIdParameterName(), null);

			if (eventId == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("No '" + getEventIdParameterName()
							+ "' parameter was found; falling back to request attribute");
				}
				eventId = (String)request.getAttribute(getEventIdRequestAttributeName());
			}
			if (eventId == null) {
				throw new IllegalArgumentException(
						"The '"
								+ getEventIdParameterName()
								+ "' request parameter (or '"
								+ getEventIdRequestAttributeName()
								+ "' request attribute) is required to signal an event in the current state of this executing flow '"
								+ flowExecution.getCaption() + "' -- programmer error?");
			}
			if (eventId.equals(getNotSetEventIdParameterMarker())) {
				throw new IllegalArgumentException("The eventId submitted by the browser was the 'not set' marker '"
						+ getNotSetEventIdParameterMarker()
						+ "' - this is likely a view (jsp, etc) configuration error - " + "the '"
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
		else {
			// We're still in the flow, inject flow model into request
			if (modelAndView != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("[Placing information about the new current flow state in request scope]");
					logger.debug("    - " + getFlowExecutionIdAttributeName() + "=" + flowExecution.getId());
					logger.debug("    - " + getCurrentStateIdAttributeName() + "=" + flowExecution.getCurrentStateId());
				}
				request.setAttribute(getFlowExecutionIdAttributeName(), flowExecution.getId());
				request.setAttribute(getCurrentStateIdAttributeName(), flowExecution.getCurrentStateId());
				request.setAttribute(getFlowExecutionAttributeName(), flowExecution);
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected model and view " + modelAndView);
		}
		return modelAndView;
	}

	/**
	 * Obtain a flow to use from given request.
	 * @throws ServletRequestBindingException When no flow id was bound to the
	 *         request
	 */
	public Flow createFlow(HttpServletRequest request) throws ServletRequestBindingException {
		return (Flow)beanFactory.getBean(RequestUtils.getRequiredStringParameter(request, getFlowIdParameterName()));
	}

	/**
	 * Create a new flow execution for given flow.
	 * @param flow The flow
	 * @return The created flow execution
	 */
	public FlowExecution createFlowExecution(Flow flow) {
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
	public boolean isNewFlowExecutionRequest(HttpServletRequest request) {
		return RequestUtils.getStringParameter(request, getFlowExecutionIdParameterName(), null) == null;
	}

	/**
	 * Save the flow execution in the HTTP session associated with given
	 * request.
	 */
	public void saveInHttpSession(FlowExecution flowExecution, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Saving flow execution '" + flowExecution.getId() + "' in HTTP session");
		}
		request.getSession(false).setAttribute(flowExecution.getId(), flowExecution);
	}

	/**
	 * Get the flow execution with given unique id from the HTTP session
	 * associated with given request.
	 * @throws NoSuchFlowExecutionException If there is no flow execution with
	 *         specified id in the HTTP session associated with given request.
	 */
	public FlowExecution getRequiredFlowExecution(HttpServletRequest request) throws NoSuchFlowExecutionException {
		String flowExecutionId;
		try {
			flowExecutionId = RequestUtils.getRequiredStringParameter(request, getFlowExecutionIdParameterName());
		}
		catch (ServletRequestBindingException e) {
			//this should not happen
			throw new NoSuchFlowExecutionException(null, e);
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
}