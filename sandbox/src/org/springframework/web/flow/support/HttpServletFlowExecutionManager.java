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
package org.springframework.web.flow.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowConstants;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowLocator;
import org.springframework.web.flow.NoSuchFlowExecutionException;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.util.WebUtils;

/**
 * Helper to manage flow execution and process requests coming into a flow
 * execution. This class provides numerous methods that can be extended in
 * subclasses to fine-tune the execution algorithm.
 * <p>
 * The
 * {@link #handleRequest(HttpServletRequest, HttpServletResponse, FlowExecutionListener) handleRequest}
 * method implements the following algorithm:
 * <ol>
 * <li>Look for a flow execution id in the request (in a parameter named
 * "_flowExecutionId").</li>
 * <li>If a flow execution id is not found, a new flow execution will be
 * created. The top-level flow for which the execution is created is determined
 * by first looking for a flow id specified in the request using the "_flowId"
 * request parameter. If this parameter is present, the specified flow will be
 * used, after lookup using a flow locator. If no "_flowId" parameter is
 * present, the default top-level flow configured for this manager is used.</li>
 * <li>If a flow execution id is found, the corresponding flow execution is
 * obtained from the HTTP session.</li>
 * <li>If a new flow execution was created in the previous steps, it will be
 * started.</li>
 * <li>If an existing flow execution is continued, current state id
 * ("_currentStateId") and event id ("_eventId") parameter values will be
 * obtained from the request and will be signaled in the flow execution.</li>
 * </ol>
 * <p>
 * Note that this class does not implement a <i>flow execution manager interface</i>
 * or something like that. The flow execution manager is very protocol specific
 * and should only be used by controllers that need to manage flow executions
 * for incoming HttpServletRequests.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class HttpServletFlowExecutionManager {

	protected final Log logger = LogFactory.getLog(HttpServletFlowExecutionManager.class);

	private Flow flow;

	private FlowLocator flowLocator;

	private FlowExecutionListener[] flowExecutionListeners;

	/**
	 * Create a new flow execution manager. Since no default flow is specified,
	 * the id of the flow for which executions will be managed is expected in
	 * the request parameter "_flowId".
	 * @param flowLocator the flow locator to use for flow lookup
	 */
	public HttpServletFlowExecutionManager(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Create a new flow execution manager.
	 * @param flowLocator the flow locator to use for flow lookup of possible
	 *        other flows specified using the "_flowId" request parameter
	 * @param flow the default flow for which executions will be managed
	 */
	public HttpServletFlowExecutionManager(FlowLocator flowLocator, Flow flow) {
        this.flowLocator = flowLocator;
		this.flow = flow;
	}

	/**
	 * Create a new flow execution manager.
	 * @param flowLocator the flow locator to use for flow lookup of possible
	 *        other flows specified using the "_flowId" request parameter
	 * @param flow the default flow for which executions will be managed
	 * @param flowExecutionListeners the listeners to attach to executing flows
	 */
	public HttpServletFlowExecutionManager(FlowLocator flowLocator, Flow flow,
			FlowExecutionListener[] flowExecutionListeners) {
		this.flowLocator = flowLocator;
		this.flow = flow;
		this.flowExecutionListeners = flowExecutionListeners;
	}

	/**
	 * Returns the flow whose executions are managed by this manager.
	 * Could be <code>null</code> if there is no preconfigured flow and
	 * the id of the flow for which executions will be managed is sent
	 * in a request parameter "_flowId".
	 */
	protected Flow getFlow() {
		return flow;
	}
	
	/**
	 * Set the flow whose executions will be managed if there is no alternate
	 * flow id specified in a "_flowId" request parameter.
	 */
	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	/**
	 * Returns the array of flow execution listeners.
	 * @return the flow execution listeners
	 */
	protected FlowExecutionListener[] getFlowExecutionListeners() {
		return this.flowExecutionListeners;
	}
	
	/**
	 * Set the flow execution listener that will be notified of managed
	 * flow executions.
	 */
	public void setFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners = new FlowExecutionListener[] { listener };
	}
	
	/**
	 * Sets the flow execution listeners that will be notified of managed
	 * flow executions.
	 */
	public void setFlowExecutionListeners(FlowExecutionListener[] flowExecutionListeners) {
		this.flowExecutionListeners = flowExecutionListeners;
	}

	/**
	 * Returns the flow locator to use for lookup of flows specified using the
	 * "_flowId" request parameter.
	 */
	protected FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Set the flow locator to use for lookup of flows specified using the
	 * "_flowId" request parameter.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	// internal worker methods

	/**
	 * The main entry point into managed HTTP-based flow executions.
	 * @param request the current HTTP request
	 * @param response the current HTTP response
	 * @return the view descriptor of the model and view to render
	 * @throws Exception in case of errors
	 */
	public ViewDescriptor handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handleRequest(request, response, null);
	}

	/**
	 * The main entry point into managed HTTP-based flow executions.
	 * @param request the current HTTP request
	 * @param response the current HTTP response
	 * @param executionListener a listener interested in flow execution
	 *        lifecycle events that happen <i>while handling this request</i>
	 * @return the view descriptor of the model and view to render
	 * @throws Exception in case of errors
	 */
	public ViewDescriptor handleRequest(HttpServletRequest request, HttpServletResponse response,
			FlowExecutionListener executionListener) throws Exception {
		FlowExecution flowExecution;
		ViewDescriptor viewDescriptor;
		if (isNewFlowExecutionRequest(request)) {
			// start a new flow execution
			flowExecution = createFlowExecution(getFlow(request));
			if (executionListener != null) {
				flowExecution.getListenerList().add(executionListener);
			}
			viewDescriptor = flowExecution.start(createEvent(request, response));
			saveInHttpSession(flowExecution, request);
		}
		else {
			// client is participating in an existing flow execution,
			// retrieve information about it
			flowExecution = getRequiredFlowExecution(request);
			// rehydrate the execution if neccessary (if it had been serialized
			// out)
			flowExecution.rehydrate(getFlowLocator(), flowExecutionListeners);
			if (executionListener != null) {
				flowExecution.getListenerList().add(executionListener);
			}
			// signal the event within the current state
			Event event = createEvent(request, response);
			Assert.hasText(event.getId(), "The '" + getEventIdParameterName() + "' request parameter (or '"
					+ getEventIdRequestAttributeName() + "' request attribute) is not present in the request -- "
					+ "make sure the submitting view or other client provides it as input");
			// see if the eventId was set to a static marker placeholder because
			// of a view configuration error
			if (event.getId().equals(getNotSetEventIdParameterMarker())) {
				throw new IllegalArgumentException("The eventId in the request was the 'not set' marker '"
						+ getNotSetEventIdParameterMarker()
						+ "' -- this is likely a view (jsp, etc) configuration error -- the '"
						+ getEventIdParameterName() + "' parameter must be set to a valid event");
			}
			viewDescriptor = flowExecution.signalEvent(event);
		}
		if (!flowExecution.isActive()) {
			// event execution resulted in the entire flow ending, cleanup
			removeFromHttpSession(flowExecution, request);
		}
		if (executionListener != null) {
			flowExecution.getListenerList().remove(executionListener);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected view descriptor " + viewDescriptor);
		}
		return viewDescriptor;
	}

	// subclassing hooks

	/**
	 * Obtain a flow to use from given request. If there is a flow id parameter
	 * specified in the request, the flow with that id will be returend after
	 * lookup using the flow locator. If no flow id parameter is present in the
	 * request, the default top-level flow will be returned.
	 */
	protected Flow getFlow(HttpServletRequest request) {
		String flowId = request.getParameter(getFlowIdParameterName());
		if (!StringUtils.hasText(flowId)) {
			Assert.notNull(this.flow, "This flow execution manager is not configured with a default top-level flow");
			return this.flow;
		}
		else {
			Assert.notNull(this.flowLocator,
					"The flow locator is required to lookup flows to execute by a flow id request parameter");
			return this.flowLocator.getFlow(flowId);
		}
	}

	/**
	 * Create a flow event wrapping given request and response.
	 */
	protected Event createEvent(HttpServletRequest request, HttpServletResponse response) {
		return new HttpServletRequestEvent(request, response, getEventIdParameterName(),
				getEventIdRequestAttributeName(), getCurrentStateIdParameterName(), getParameterNameValueDelimiter());
	}

	/**
	 * Create a new flow execution for given flow.
	 * @param flow the flow
	 * @return the created flow execution
	 */
	protected FlowExecution createFlowExecution(Flow flow) {
		FlowExecution flowExecution = flow.createExecution();
		flowExecution.getListenerList().add(flowExecutionListeners);
		return flowExecution;
	}

	/**
	 * Check if given request is a request for a new flow execution, or a
	 * continuation of an existing one.
	 * @param request the HTTP request to check
	 * @return true or false
	 */
	protected boolean isNewFlowExecutionRequest(HttpServletRequest request) {
		return request.getParameter(getFlowExecutionIdParameterName()) == null;
	}

	/**
	 * Returns the name of the flow id parameter in the request ("_flowId").
	 */
	protected String getFlowIdParameterName() {
		return FlowConstants.FLOW_ID_PARAMETER;
	}

	/**
	 * Returns the name of the flow execution id parameter in the request
	 * ("_flowExecutionId").
	 */
	protected String getFlowExecutionIdParameterName() {
		return FlowConstants.FLOW_EXECUTION_ID_PARAMETER;
	}

	/**
	 * Returns the name of the event id parameter in the request ("_eventId").
	 */
	protected String getEventIdParameterName() {
		return FlowConstants.EVENT_ID_PARAMETER;
	}

	/**
	 * Returns the name of the event id attribute in the request
	 * ("_mapped_eventId").
	 * <p>
	 * This is useful when working with image buttons and javscript
	 * restrictions. For example, an intercepting servlet filter can process a
	 * image button with a name in the format "_pname__eventId_pvalue_submit"
	 * and set the proper "mapped' eventId attribute in the request.
	 */
	protected String getEventIdRequestAttributeName() {
		return FlowConstants.EVENT_ID_REQUEST_ATTRIBUTE;
	}

	/**
	 * Returns the marker value indicating that the event id parameter was not
	 * set properly in the request because of view configuration error ({@link FlowConstants#NOT_SET_EVENT_ID}).
	 * <p>
	 * This is useful when a view relies on an dynamic means to set the eventId
	 * request parameter, for example, using javascript. This approach assumes
	 * the "not set" marker value will be a static default (a kind of fallback,
	 * submitted if the eventId does not get set to the proper dynamic value
	 * onClick, for example, if javascript was disabled).
	 */
	protected String getNotSetEventIdParameterMarker() {
		return FlowConstants.NOT_SET_EVENT_ID;
	}

	/**
	 * Returns the name of the current state id parameter in the request
	 * ("_currentStateId").
	 */
	protected String getCurrentStateIdParameterName() {
		return FlowConstants.CURRENT_STATE_ID_PARAMETER;
	}

	/**
	 * Returns the default delimiter used to separate a request parameter name
	 * and value when both are embedded in the name of the request parameter
	 * (e.g. when using an HTML submit button).
	 */
	protected String getParameterNameValueDelimiter() {
		return "_";
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
	 * @throws ServletRequestBindingException If there is no flow execution id
	 *         bound in given request.
	 */
	protected FlowExecution getRequiredFlowExecution(HttpServletRequest request) throws NoSuchFlowExecutionException,
			ServletRequestBindingException {
		String flowExecutionId = RequestUtils.getRequiredStringParameter(request, getFlowExecutionIdParameterName());
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
	protected void removeFromHttpSession(FlowExecution flowExecution, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow execution '" + flowExecution.getId() + "' from HTTP session");
		}
		request.getSession(false).removeAttribute(flowExecution.getId());
	}
}