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
import org.springframework.web.flow.FlowEventProcessor;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.FlowSessionExecutionInfo;
import org.springframework.web.flow.FlowSessionExecutionStartResult;
import org.springframework.web.flow.NoSuchFlowSessionException;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.action.AbstractActionBean;
import org.springframework.web.struts.BindingActionForm;
import org.springframework.web.struts.TemplateAction;

/**
 * Struts Action that provides an entry point into the workflow mechanism for
 * this application.
 * @author Keith Donald
 */
public class FlowAction extends TemplateAction {

	private static final String ACTION_PATH_NAME_ATTRIBUTE = "actionFormBeanName";

	public static String FLOW_SESSION_ID_PARAMETER = "_flowSessionId";

	public static final String CURRENT_STATE_ID_PARAMETER = "_currentStateId";

	public static final String EVENT_ID_PARAMETER = "_eventId";

	public static final String EVENT_ID_ATTRIBUTE = "_mapped_eventId";

	public static String ACTION_FORM_ATTRIBUTE_NAME = "_bindingActionForm";

	public static String NOT_SET_EVENT_ID = "@NOT_SET@";

	// made final as these attribute keys are needed elsewhere, so they cant
	// change for now

	protected final String getFlowSessionIdAttributeName() {
		return FlowSession.FLOW_SESSION_ID_ATTRIBUTE_NAME;
	}

	protected final String getCurrentStateIdAttributeName() {
		return FlowSession.CURRENT_STATE_ID_ATTRIBUTE_NAME;
	}

	protected FlowEventProcessor getEventProcessor(ActionMapping mapping) {
		return getEventProcessor(getFlowId(mapping));
	}

	protected FlowEventProcessor getEventProcessor(String flowId) {
		Assert.hasText(flowId, "The flow id must be set to lookup the flow for this action");
		return (Flow)getBean(flowId, FlowEventProcessor.class);
	}

	protected String getFlowId(ActionMapping mapping) {
		Assert.isInstanceOf(FlowActionMapping.class, mapping);
		return ((FlowActionMapping)mapping).getFlowId();
	}

	/**
	 * The main entry point for this action. Looks for a flow session ID in the
	 * request. If none exists, it creates one. If one exists, it looks in the
	 * user's session find the current FlowSessionExecutionStack. The request
	 * should also contain the current state ID and event ID. These String
	 * values can be passed to the FlowEventProcessor to execute the action.
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
				logger.debug("Setting binding action form key '" + ACTION_FORM_ATTRIBUTE_NAME + "' to form " + form);
			}
			// our form is a 'special' BindingActionForm, set under a generic
			// attribute so it'll be accessible to binding flow action beans
			request.setAttribute(ACTION_FORM_ATTRIBUTE_NAME, form);
		}
		// end struts specific

		FlowSessionExecutionInfo sessionExecution;
		ViewDescriptor viewDescriptor = null;

		if (getStringParameter(request, FLOW_SESSION_ID_PARAMETER) == null) {
			// No existing flow session execution to lookup as no _flowSessionId
			// was provided - start a new one
			FlowSessionExecutionStartResult startResult = getEventProcessor(mapping).start(request, response, null);
			sessionExecution = startResult.getFlowSessionExecutionInfo();
			viewDescriptor = startResult.getStartingView();
			saveInHttpSession(sessionExecution, request);
		}
		else {
			// Client is participating in an existing flow session execution,
			// retrieve information about it
			sessionExecution = getRequiredFlowSessionExecution(getRequiredStringParameter(request,
					FLOW_SESSION_ID_PARAMETER), request);
			// let client tell you what state they are in (if possible)
			String currentStateIdParam = getStringParameter(request, CURRENT_STATE_ID_PARAMETER);
			if (currentStateIdParam == null) {
				if (logger.isWarnEnabled()) {
					logger
							.warn("Current state id was not provided in request for flow session '"
									+ sessionExecution.getCaption()
									+ "' - pulling current state id from session - "
									+ "note: if the user has been using the with browser back/forward buttons in browser, the currentState could be incorrect.");
				}
				currentStateIdParam = sessionExecution.getCurrentStateId();
			}
			// let client tell you what event was signaled in the current state
			String eventId = getStringParameter(request, EVENT_ID_PARAMETER);
			if (eventId == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("No '" + EVENT_ID_PARAMETER
							+ "' parameter was found; falling back to request attribute");
				}
				eventId = (String)request.getAttribute(EVENT_ID_ATTRIBUTE);
			}
			if (eventId == null) {
				throw new IllegalArgumentException(
						"The '"
								+ EVENT_ID_PARAMETER
								+ "' request parameter (or '"
								+ EVENT_ID_ATTRIBUTE
								+ "' request attribute) is required to signal an event in the current state of this executing flow '"
								+ sessionExecution.getCaption() + "' -- programmer error?");
			}
			if (eventId.equals(NOT_SET_EVENT_ID)) {
				logger.error("The event submitted by the browser was '" + NOT_SET_EVENT_ID
						+ "' - this is likely a view (jsp, etc) configuration error - " + "the '" + EVENT_ID_PARAMETER
						+ "' parameter must be set to a valid event to execute within the current state '"
						+ currentStateIdParam + "' of this flow '" + sessionExecution.getCaption()
						+ "' - else I don't know what to do!");
			}
			else {
				// execute the signaled event within the current state
				viewDescriptor = getEventProcessor(sessionExecution.getActiveFlowId()).execute(eventId,
						currentStateIdParam, sessionExecution, request, response);
			}
		}

		if (!sessionExecution.isActive()) {
			// event execution resulted in the entire flow ending, cleanup
			removeFromHttpSession(sessionExecution, request);
		}
		else {
			// We're still in the flow, inject flow model into request
			if (viewDescriptor != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("[Placing information about the new current flow state in request scope]");
					logger.debug("    - " + getFlowSessionIdAttributeName() + "=" + sessionExecution.getId());
					logger.debug("    - " + getCurrentStateIdAttributeName() + "="
							+ sessionExecution.getCurrentStateId());
				}
				request.setAttribute(getFlowSessionIdAttributeName(), sessionExecution.getId());
				request.setAttribute(getCurrentStateIdAttributeName(), sessionExecution.getCurrentStateId());
				request.setAttribute(FlowSessionExecutionInfo.FLOW_SESSION_EXECUTION_INFO_ATTRIBUTE_NAME,
						sessionExecution);

				// struts specific
				String mappingFlowId = getFlowId(mapping);
				if (StringUtils.hasText(mappingFlowId)) {
					String actionPathName = StringUtils.replace(getFlowId(mapping), ".", "/");
					String actionFormBeanName = actionPathName + "Form";
					if (logger.isDebugEnabled()) {
						logger.debug("Setting '" + ACTION_PATH_NAME_ATTRIBUTE + "' attribute to value '"
								+ actionPathName + "' in request scope.");
						logger.debug("Setting action form attribute '" + actionFormBeanName + "' to form '" + form
								+ "' in request scope.");
					}
					request.setAttribute(ACTION_PATH_NAME_ATTRIBUTE, actionPathName);
					request.setAttribute(actionFormBeanName, form);
				}
				if (form instanceof BindingActionForm) {
					BindingActionForm bindingForm = (BindingActionForm)form;
					bindingForm.setErrors((Errors)sessionExecution.getAttribute(
							AbstractActionBean.LOCAL_FORM_OBJECT_ERRORS_NAME, Errors.class));
					bindingForm.setHttpServletRequest(request);
					bindingForm.setModel(sessionExecution);
				}
				// end struts specific
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected view descriptor " + viewDescriptor);
		}
		return createForwardFromViewDescriptor(viewDescriptor, mapping, request);
	}

	/**
	 * Return a Struts ActionForward given this ViewDescriptor. We need to add
	 * all attributes from the ViewDescriptor as request attributes.
	 */
	private ActionForward createForwardFromViewDescriptor(ViewDescriptor viewDescriptor, ActionMapping mapping,
			HttpServletRequest request) {
		if (viewDescriptor != null) {
			Iterator it = viewDescriptor.getModel().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				request.setAttribute((String)entry.getKey(), entry.getValue());
			}
			ActionForward forward = mapping.findForward(viewDescriptor.getViewName());
			if (forward == null) {
				forward = new ActionForward(viewDescriptor.getViewName());
			}
			return forward;
		}
		else {
			if (logger.isInfoEnabled()) {
				logger.info("No view descriptor returned; returning a [null] forward");
			}
			return null;
		}
	}

	protected FlowSessionExecutionInfo getRequiredFlowSessionExecution(String flowSessionId, HttpServletRequest request)
			throws NoSuchFlowSessionException {
		try {
			return (FlowSessionExecutionInfo)getRequiredSessionAttribute(request, flowSessionId);
		}
		catch (IllegalStateException e) {
			throw new NoSuchFlowSessionException(flowSessionId, e);
		}
	}

	protected void saveInHttpSession(FlowSessionExecutionInfo sessionInfo, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Saving flow session '" + sessionInfo.getId() + "' in HTTP session");
		}
		request.getSession().setAttribute(sessionInfo.getId(), sessionInfo);
	}

	private void removeFromHttpSession(FlowSessionExecutionInfo sessionInfo, HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow session '" + sessionInfo.getId() + "' from HTTP session");
		}
		request.getSession().removeAttribute(sessionInfo.getId());
	}
}