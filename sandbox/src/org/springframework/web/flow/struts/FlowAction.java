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
package org.springframework.web.flow.struts;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.FlowExecutionInfo;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowExecutionStack;
import org.springframework.web.flow.NoSuchFlowExecutionException;
import org.springframework.web.flow.action.AbstractAction;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.struts.BindingActionForm;
import org.springframework.web.struts.TemplateAction;

/**
 * Struts Action that provides an entry point into the workflow mechanism for
 * this application.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAction extends TemplateAction {

	public static final String CURRENT_STATE_ID_ATTRIBUTE = "currentStateId";

	public static final String FLOW_EXECUTION_ID_ATTRIBUTE = "flowExecutionId";

	public static final String EVENT_ID_ATTRIBUTE = "_mapped_eventId";

	public static final String FLOW_EXECUTION_ID_PARAMETER = "_flowExecutionId";

	public static final String CURRENT_STATE_ID_PARAMETER = "_currentStateId";

	public static final String EVENT_ID_PARAMETER = "_eventId";

	public static String NOT_SET_EVENT_ID = "@NOT_SET@";

	public static String ACTION_FORM_ATTRIBUTE = "_bindingActionForm";

	public static final String ACTION_PATH_ATTRIBUTE = "actionPath";
	
	private FlowExecutionListener[] flowExecutionListeners;

	protected String getFlowExecutionIdParameterName() {
		return FLOW_EXECUTION_ID_PARAMETER;
	}

	protected String getCurrentStateIdParameterName() {
		return CURRENT_STATE_ID_PARAMETER;
	}

	protected String getEventIdParameterName() {
		return EVENT_ID_PARAMETER;
	}

	protected String getNotSetEventIdParameterMarker() {
		return NOT_SET_EVENT_ID;
	}

	protected String getFlowExecutionIdAttributeName() {
		return FLOW_EXECUTION_ID_ATTRIBUTE;
	}

	protected String getCurrentStateIdAttributeName() {
		return CURRENT_STATE_ID_ATTRIBUTE;
	}

	private String getEventIdAttributeName() {
		return EVENT_ID_ATTRIBUTE;
	}

	protected String getFlowExecutionInfoAttributeName() {
		return FlowExecutionInfo.ATTRIBUTE_NAME;
	}

	protected String getActionPathAttributeName() {
		return ACTION_PATH_ATTRIBUTE;
	}

	protected String getActionFormAttributeName() {
		return ACTION_FORM_ATTRIBUTE;
	}

	protected Flow getFlow(ActionMapping mapping) {
		return (Flow)getBean(getFlowId(mapping), Flow.class);
	}

	protected String getFlowId(ActionMapping mapping) {
		Assert.isInstanceOf(FlowActionMapping.class, mapping);
		return ((FlowActionMapping)mapping).getFlowId();
	}

	/**
	 * The main entry point for this action. Looks for a flow execution ID in the
	 * request. If none exists, it creates one. If one exists, it looks in the
	 * user's session find the current FlowExecution. The request
	 * should also contain the current state ID and event ID. These String
	 * values can be passed to the FlowExecution to execute the action.
	 * Execution will typically result in a state transition.
	 * @see org.springframework.web.struts.TemplateAction#doExecuteAction(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected ActionForward doExecuteAction(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// struts specific
		if (form instanceof BindingActionForm) {
			if (logger.isDebugEnabled()) {
				logger.debug("Setting binding action form key '" + getActionFormAttributeName() + "' to form " + form);
			}
			// our form is a 'special' BindingActionForm, set under a generic
			// attribute so it'll be accessible to binding flow action beans
			request.setAttribute(getActionFormAttributeName(), form);
		}
		// end struts specific

		FlowExecution flowExecution;
		ModelAndView mv;

		if (getStringParameter(request, getFlowExecutionIdParameterName()) == null) {
			// No existing flow execution to lookup as no _flowExecutionId
			// was provided - start a new one
			Flow flow = getFlow(mapping);
			flowExecution = createFlowExecution(flow);
			mv = flowExecution.start(null, request, response);
			saveInHttpSession(flowExecution, request);
		}
		else {
			// Client is participating in an existing flow execution,
			// retrieve information about it
			flowExecution = getRequiredFlowExecution(getRequiredStringParameter(request,
					getFlowExecutionIdParameterName()), request);

			// let client tell you what state they are in (if possible)
			String stateId = getStringParameter(request, getCurrentStateIdParameterName());

			// let client tell you what event was signaled in the current state
			String eventId = getStringParameter(request, getEventIdParameterName());

			if (eventId == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("No '" + getEventIdParameterName()
							+ "' parameter was found; falling back to request attribute");
				}
				eventId = (String)request.getAttribute(getEventIdAttributeName());
			}
			if (eventId == null) {
				throw new IllegalArgumentException(
						"The '"
								+ getEventIdParameterName()
								+ "' request parameter (or '"
								+ getEventIdAttributeName()
								+ "' request attribute) is required to signal an event in the current state of this executing flow '"
								+ flowExecution.getCaption() + "' -- programmer error?");
			}
			if (eventId.equals(getNotSetEventIdParameterMarker())) {
				throw new IllegalArgumentException("The eventId submitted by the browser was the 'not set' marker '" + getNotSetEventIdParameterMarker()
						+ "' - this is likely a view (jsp, etc) configuration error - " + "the '"
						+ getEventIdParameterName()
						+ "' parameter must be set to a valid event to execute within the current state '" + stateId
						+ "' of this flow '" + flowExecution.getCaption() + "' - else I don't know what to do!");
			}
			else {
				// execute the signaled event within the current state
				mv = flowExecution.signalEvent(eventId, stateId, request, response);
			}
		}

		if (!flowExecution.isActive()) {
			// event execution resulted in the entire flow ending, cleanup
			removeFromHttpSession(flowExecution, request);
		}
		else {
			// We're still in the flow, inject flow model into request
			if (mv != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("[Placing information about the new current flow state in request scope]");
					logger.debug("    - " + getFlowExecutionIdAttributeName() + "=" + flowExecution.getId());
					logger.debug("    - " + getCurrentStateIdAttributeName() + "=" + flowExecution.getCurrentStateId());
				}
				request.setAttribute(getFlowExecutionIdAttributeName(), flowExecution.getId());
				request.setAttribute(getCurrentStateIdAttributeName(), flowExecution.getCurrentStateId());
				request.setAttribute(getFlowExecutionInfoAttributeName(), flowExecution);

				// struts specific
				String mappingFlowId = getFlowId(mapping);
				if (StringUtils.hasText(mappingFlowId)) {
					String actionPathName = StringUtils.replace(getFlowId(mapping), ".", "/");
					String actionFormBeanName = actionPathName + "Form";
					if (logger.isDebugEnabled()) {
						logger.debug("Setting '" + getActionPathAttributeName() + "' attribute to value '"
								+ actionPathName + "' in request scope.");
						logger.debug("Setting action form attribute '" + actionFormBeanName + "' to form '" + form
								+ "' in request scope.");
					}
					request.setAttribute(getActionPathAttributeName(), actionPathName);
					request.setAttribute(actionFormBeanName, form);
				}
				if (form instanceof BindingActionForm) {
					BindingActionForm bindingForm = (BindingActionForm)form;
					bindingForm.setErrors((Errors)flowExecution.getAttribute(
							AbstractAction.LOCAL_FORM_OBJECT_ERRORS_NAME, Errors.class));
					bindingForm.setHttpServletRequest(request);
					bindingForm.setModel(flowExecution);
				}
				// end struts specific
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected model and view " + mv);
		}
		return createForwardFromModelAndView(mv, mapping, request);
	}
	
	public void setFlowExecutionListeners(FlowExecutionListener[] flowExecutionListeners) {
		this.flowExecutionListeners = flowExecutionListeners;
	}
	
	protected FlowExecution createFlowExecution(Flow flow) {
		FlowExecution flowExecution = new FlowExecutionStack(flow);
		flowExecution.addFlowExecutionListeners(flowExecutionListeners);
		return flowExecution;
	}

	/**
	 * Return a Struts ActionForward given a ModelAndView. We need to add
	 * all attributes from the ModelAndView as request attributes.
	 */
	private ActionForward createForwardFromModelAndView(ModelAndView mv, ActionMapping mapping,
			HttpServletRequest request) {
		if (mv != null) {
			Iterator it = mv.getModel().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				request.setAttribute((String)entry.getKey(), entry.getValue());
			}
			ActionForward forward = mapping.findForward(mv.getViewName());
			if (forward == null) {
				forward = new ActionForward(mv.getViewName());
			}
			return forward;
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("No model and view; returning a [null] forward");
			}
			return null;
		}
	}

	protected FlowExecution getRequiredFlowExecution(String flowExecutionId, HttpServletRequest request)
			throws NoSuchFlowExecutionException {
		try {
			return (FlowExecution)getRequiredSessionAttribute(request, flowExecutionId);
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowExecutionException(flowExecutionId, e);
		}
	}

	protected void saveInHttpSession(FlowExecution flowExecution, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Saving flow execution '" + flowExecution.getId() + "' in HTTP session");
		}
		request.getSession().setAttribute(flowExecution.getId(), flowExecution);
	}

	private void removeFromHttpSession(FlowExecution flowExecution, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow execution '" + flowExecution.getId() + "' from HTTP session");
		}
		request.getSession().removeAttribute(flowExecution.getId());
	}
}