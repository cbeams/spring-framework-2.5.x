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
import org.springframework.validation.Errors;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowEventProcessor;
import org.springframework.web.flow.FlowSessionExecutionStack;
import org.springframework.web.flow.NoSuchFlowSessionException;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.action.AbstractActionBean;
import org.springframework.web.struts.BindingActionForm;
import org.springframework.web.struts.TemplateAction;
import org.springframework.web.util.SessionKeyUtils;


/**
 * Struts Action that provides an entry point into the workflow mechanism for
 * this application.
 * @author Keith Donald
 */
public class FlowAction extends TemplateAction {

    public static final String CURRENT_STATE_ID_ATTRIBUTE_NAME = "currentStateId";

    public static final String FLOW_SESSION_ID_ATTRIBUTE_NAME = "flowId";

    public static String FLOW_SESSION_ID_PARAMETER = "_flowId";

    public static final String CURRENT_STATE_ID_PARAMETER = "_currentStateId";

    public static final String EVENT_ID_PARAMETER = "_eventId";

    public static String ACTION_FORM_ATTRIBUTE_NAME = "_bindingActionForm";

    public static String NOT_SET_EVENT_ID = "@NOT_SET@";

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
        String flowSessionId = getStringParameter(request, FLOW_SESSION_ID_PARAMETER);
        FlowSessionExecutionStack executionStack;
        ViewDescriptor viewDescriptor = null;
        if (form instanceof BindingActionForm) {
            if (logger.isDebugEnabled()) {
                logger.debug("Setting binding action form key '" + ACTION_FORM_ATTRIBUTE_NAME + "' to form " + form);
            }
            // our form is a 'special' BindingActionForm, set under a generic
            // attribute so it'll be accessible to binding flow action beans
            request.setAttribute(ACTION_FORM_ATTRIBUTE_NAME, form);
        }
        if (flowSessionId == null) {
            // No existing flow session, create a new one
            executionStack = createFlowSessionExecutionStack();
            flowSessionId = generateUniqueFlowSessionId(executionStack);
            saveFlowSession(executionStack, flowSessionId, request);
            viewDescriptor = getEventProcessor(mapping).start(executionStack, request, response, null);
        }
        else {
            // Client is participating in an existing flow session, retrieve it
            executionStack = getRequiredFlowSessionExecutionStack(flowSessionId, request);
            String currentStateIdParam = getStringParameter(request, CURRENT_STATE_ID_PARAMETER);
            if (currentStateIdParam == null) {
                if (logger.isWarnEnabled()) {
                    logger
                            .warn("Current state id was not provided in request for flow '"
                                    + flowSessionId
                                    + "' - pulling current state id from session - "
                                    + "note: if the user has been using the with browser back/forward buttons in browser, the currentState could be incorrect.");
                }
                currentStateIdParam = executionStack.getCurrentStateId();
            }
            String eventId = getStringParameter(request, EVENT_ID_PARAMETER);
            if (eventId.equals(NOT_SET_EVENT_ID)) {
                logger
                        .error("The event submitted by the browser was @NOT_SET@ - this is likely a view configuration error - "
                                + "the eventId parameter must be set to a valid event to execute within the current state '"
                                + currentStateIdParam + "' - else I don't know what to do!");
            }
            else {
                // execute the submitted event within the current state
                viewDescriptor = getEventProcessor(executionStack.getActiveFlowId()).execute(eventId,
                        currentStateIdParam, executionStack, request, response);
            }
        }

        if (executionStack.isEmpty()) {
            // event execution resulted in the entire flow ending, cleanup
            removeFlowSession(mapping, flowSessionId, request);
        }
        else {
            // We're still in the flow, inject flow model into request
            if (viewDescriptor != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[Placing information about the new current flow state in request scope]");
                    logger.debug("    - " + getFlowSessionIdAttributeName() + "=" + flowSessionId);
                    logger
                            .debug("    - " + getCurrentStateIdAttributeName() + "="
                                    + executionStack.getCurrentStateId());
                }
                request.setAttribute(getFlowSessionIdAttributeName(), flowSessionId);
                request.setAttribute(getCurrentStateIdAttributeName(), executionStack.getCurrentStateId());
                String actionFormBeanName = executionStack.getActiveFlowId() + "Form";
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting action form '" + form + "' named '" + actionFormBeanName
                            + "' in request scope.");
                }
                request.setAttribute(actionFormBeanName, form);
                if (form instanceof BindingActionForm) {
                    BindingActionForm bindingForm = (BindingActionForm)form;
                    bindingForm.setErrors((Errors)executionStack
                            .getAttribute(AbstractActionBean.LOCAL_FORM_OBJECT_ERRORS_NAME));
                    bindingForm.setHttpServletRequest(request);
                    bindingForm.setModel(executionStack);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returning view descriptor " + viewDescriptor);
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

    protected String generateUniqueFlowSessionId(FlowSessionExecutionStack stack) {
        return SessionKeyUtils.generateMD5SessionKey(String.valueOf(stack.hashCode()), true);
    }

    protected FlowSessionExecutionStack createFlowSessionExecutionStack() {
        return new FlowSessionExecutionStack();
    }

    protected void saveFlowSession(FlowSessionExecutionStack executionStack, String flowSessionId,
            HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Saving flow session '" + flowSessionId + "' in HTTP session");
        }
        request.getSession().setAttribute(flowSessionId, executionStack);
    }

    protected FlowSessionExecutionStack getRequiredFlowSessionExecutionStack(String flowSessionId,
            HttpServletRequest request) throws NoSuchFlowSessionException {
        try {
            return (FlowSessionExecutionStack)getRequiredSessionAttribute(request, flowSessionId);
        }
        catch (IllegalStateException e) {
            throw new NoSuchFlowSessionException(flowSessionId, e);
        }
    }

    private void removeFlowSession(ActionMapping mapping, String flowSessionId, HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing flow session '" + flowSessionId + "' from HTTP session");
        }
        request.getSession().removeAttribute(flowSessionId);
    }

    protected String getFlowSessionIdAttributeName() {
        return FLOW_SESSION_ID_ATTRIBUTE_NAME;
    }

    protected String getCurrentStateIdAttributeName() {
        return CURRENT_STATE_ID_ATTRIBUTE_NAME;
    }

}