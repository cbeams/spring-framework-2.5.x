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
import org.springframework.web.flow.action.AbstractAction;
import org.springframework.web.flow.config.BeanFactoryFlowServiceLocator;
import org.springframework.web.flow.mvc.HttpFlowExecutionManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.struts.BindingActionForm;
import org.springframework.web.struts.TemplateAction;

/**
 * Struts Action that acts a front controller entry point into the web flow
 * system. Typically, a FlowAction exists per top-level (root) flow definition
 * in the application. Alternatively, a single FlowController may manage all
 * flow executions by parameterization with the appropriate <code>flowId</code>
 * in views that start new flow executions.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAction extends TemplateAction {

	public static String ACTION_FORM_ATTRIBUTE = "_bindingActionForm";

	public static final String ACTION_PATH_ATTRIBUTE = "actionPath";

	private HttpFlowExecutionManager executionManager;

	protected String getActionPathAttributeName() {
		return ACTION_PATH_ATTRIBUTE;
	}

	protected String getActionFormAttributeName() {
		return ACTION_FORM_ATTRIBUTE;
	}

	protected Flow getFlow(ActionMapping mapping) {
		return (Flow)getWebApplicationContext().getBean(getFlowId(mapping));
	}

	private String getFlowId(ActionMapping mapping) {
		Assert.isInstanceOf(FlowActionMapping.class, mapping);
		return ((FlowActionMapping)mapping).getFlowId();
	}

	/**
	 * The main entry point for this action. Looks for a flow execution ID in
	 * the request. If none exists, it creates one. If one exists, it looks in
	 * the user's session to find the current FlowExecution. The request should
	 * also contain the current state ID and event ID. These String values can
	 * be passed to the FlowExecution to execute the action. Execution will
	 * typically result in a state transition.
	 * @see org.springframework.web.struts.TemplateAction#doExecuteAction(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected ActionForward doExecuteAction(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		synchronized (this) {
			if (this.executionManager == null) {
				this.executionManager = new HttpFlowExecutionManager(getFlow(mapping),
						new BeanFactoryFlowServiceLocator(getWebApplicationContext()));
			}
		}
		if (form instanceof BindingActionForm) {
			if (logger.isDebugEnabled()) {
				logger.debug("Setting binding action form key '" + getActionFormAttributeName() + "' to form " + form);
			}
			// our form is a 'special' BindingActionForm, set under a generic
			// attribute so it'll be accessible to binding flow action beans
			request.setAttribute(getActionFormAttributeName(), form);
		}
		ModelAndView modelAndView = this.executionManager.handleRequest(request, response,
				getFlowExecutionInput(request));
		// this is not very clean
		FlowExecution flowExecution = (FlowExecution)modelAndView.getModel().get(FlowExecution.ATTRIBUTE_NAME);
		if (flowExecution.isActive()) {
			// struts specific
			String mappingFlowId = getFlowId(mapping);
			if (StringUtils.hasText(mappingFlowId)) {
				String actionPathName = StringUtils.replace(mappingFlowId, ".", "/");
				String actionFormBeanName = actionPathName + "Form";
				if (logger.isDebugEnabled()) {
					logger.debug("Setting '" + getActionPathAttributeName() + "' attribute to value '" + actionPathName
							+ "' in request scope.");
					logger.debug("Setting action form attribute '" + actionFormBeanName + "' to form '" + form
							+ "' in request scope.");
				}
				request.setAttribute(getActionPathAttributeName(), actionPathName);
				request.setAttribute(actionFormBeanName, form);
			}
			if (form instanceof BindingActionForm) {
				BindingActionForm bindingForm = (BindingActionForm)form;
				bindingForm.setErrors((Errors)flowExecution.getAttribute(AbstractAction.LOCAL_FORM_OBJECT_ERRORS_NAME,
						Errors.class));
				bindingForm.setRequest(request);
			}
		}
		return createForwardFromModelAndView(modelAndView, mapping, request);
	}

	protected Map getFlowExecutionInput(HttpServletRequest request) {
		return null;
	}

	/**
	 * Return a Struts ActionForward given a ModelAndView. We need to add all
	 * attributes from the ModelAndView as request attributes.
	 */
	private ActionForward createForwardFromModelAndView(ModelAndView modelAndView, ActionMapping mapping,
			HttpServletRequest request) {
		if (modelAndView != null) {
			Iterator it = modelAndView.getModel().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				request.setAttribute((String)entry.getKey(), entry.getValue());
			}
			ActionForward forward = mapping.findForward(modelAndView.getViewName());
			if (forward == null) {
				forward = new ActionForward(modelAndView.getViewName());
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
}