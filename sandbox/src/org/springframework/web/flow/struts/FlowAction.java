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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowExecutionContext;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowLocator;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.action.AbstractAction;
import org.springframework.web.flow.config.BeanFactoryFlowServiceLocator;
import org.springframework.web.flow.support.FlowExecutionListenerAdapter;
import org.springframework.web.flow.support.HttpServletFlowExecutionManager;
import org.springframework.web.struts.BindingActionForm;
import org.springframework.web.struts.TemplateAction;
import org.springframework.web.util.WebUtils;

/**
 * Struts Action that acts a front controller entry point into the web flow
 * system. Typically, a FlowAction exists per top-level (root) flow definition
 * in the application. Alternatively, a single FlowAction may manage all flow
 * executions by parameterization with the appropriate <code>flowId</code> in
 * views that start new flow executions.
 * <p>
 * Requests are managed by and delegated to a
 * {@link HttpServletFlowExecutionManager}, allowing reuse of common front flow
 * controller logic in other environments. Consult the JavaDoc of that class for
 * more information on how requests are processed.
 * <p>
 * This class also is aware of the <code>BindingActionForm</code> adapter,
 * which adapts Spring's data binding infrastructure (based on POJO binding, a
 * standard Errors interface, and property editor type conversion) to the Struts
 * action form model. This gives backend web-tier developers full support for
 * POJO-based binding with minimal hassel, while still providing consistency to
 * view developers who already have a lot of experience with Struts for markup
 * and request dispatching.
 * <p>
 * Below is an example <code>struts-config.xml</code> configuration for a
 * Flow-action that fronts a single top-level flow:
 * 
 * <pre>
 *              &lt;action path=&quot;/userRegistration&quot;
 *                  type=&quot;org.springframework.web.flow.struts.FlowAction&quot;
 *                  name=&quot;bindingActionForm&quot; scope=&quot;request&quot; 
 *                  className=&quot;org.springframework.web.flow.struts.FlowActionMapping&quot;&gt;
 *                      &lt;set-property property=&quot;flowId&quot; value=&quot;user.Registration&quot; /&gt;
 *              &lt;/action&gt;
 * </pre>
 * 
 * This example associates the logical request URL
 * <code>/userRegistration.do</code> with the <code>Flow</code> indentified
 * by the id <code>user.Registration</code>. Alternatively, the
 * <code>flowId</code> could have been left blank and provided in dynamic
 * fashion by the views (allowing a single <code>FlowAction</code> to manage
 * any number of flow executions). A binding action form instance is set in
 * request scope, acting as an adapter enabling POJO-based binding and
 * validation with Spring.
 * <p>
 * Other notes regarding Struts web-flow integration:
 * <ul>
 * <li>Logical view names returned when <code>ViewStates</code> and
 * <code>EndStates</code> are entered are mapped to physical view templates
 * using standard Struts action forwards (typically global forwards).
 * <li>Use of the BindingActionForm requires some minor setup in
 * <code>struts-config.xml</code>. Specifically:
 * <ol>
 * <li>A custom BindingActionForm-aware request processor is needed, to defer
 * form population:<br>
 * 
 * <tt>
 * &lt;controller processorClass=&quot;org.springframework.web.struts.BindingRequestProcessor&quot;/&gt; 
 * </tt>
 * 
 * <li>A <code>BindingPlugin</code> is needed, to plugin a Errors-aware
 * <code>jakarta-commons-beanutils</code> adapter:<br>
 * 
 * <tt>
 * &lt;plug-in className=&quot;org.springframework.web.struts.BindingPlugin&quot;/&gt;
 * </tt>
 * 
 * </ol>
 * </ul>
 * The benefits here are substantial--developers now have a powerful webflow
 * capability integrated with Struts, with a consistent-approach to POJO-based
 * binding and validation that addresses the proliferation of
 * <code>ActionForm</code> classes found in traditional Struts-based apps.
 * @author Keith Donald
 * @author Erwin Vervaet
 * @see org.springframework.web.flow.support.HttpServletFlowExecutionManager
 * @see org.springframework.web.struts.BindingActionForm
 */
public class FlowAction extends TemplateAction {

	protected ActionForward doExecuteAction(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		FlowLocator locator = new BeanFactoryFlowServiceLocator(getWebApplicationContext());
		HttpServletFlowExecutionManager flowExecutionManager = new HttpServletFlowExecutionManager(getFlowId(mapping),
				locator);
		FlowExecutionListener actionFormAdapter = createActionFormAdapter(request, form);
		ViewDescriptor viewDescriptor = flowExecutionManager.handleRequest(request, response, actionFormAdapter);
		return createForwardFromViewDescriptor(viewDescriptor, mapping, request);
	}

	/**
	 * Get the flow id from given action mapping, which should be of type
	 * <code>FlowActionMapping</code>.
	 */
	private String getFlowId(ActionMapping mapping) {
		Assert.isInstanceOf(FlowActionMapping.class, mapping);
		return ((FlowActionMapping)mapping).getFlowId();
	}

	protected FlowExecutionListener createActionFormAdapter(final HttpServletRequest request, final ActionForm form) {
		return new FlowExecutionListenerAdapter() {
			public void requestProcessed(FlowExecutionContext context, Event triggeringEvent) {
				if (context.isFlowExecutionActive()) {
					if (form instanceof BindingActionForm) {
						BindingActionForm bindingForm = (BindingActionForm)form;
						bindingForm.setErrors((Errors)context.requestScope().getAttribute(
								AbstractAction.FORM_OBJECT_ERRORS_ATTRIBUTE, Errors.class));
						bindingForm.setRequest(request);
					}
				}
			}
		};
	}

	/**
	 * Return a Struts ActionForward given a ViewDescriptor. We need to add all
	 * attributes from the ViewDescriptor as request attributes.
	 */
	private ActionForward createForwardFromViewDescriptor(ViewDescriptor viewDescriptor, ActionMapping mapping,
			HttpServletRequest request) {
		if (viewDescriptor != null) {
			WebUtils.exposeRequestAttributes(request, viewDescriptor.getModel());
			ActionForward forward = mapping.findForward(viewDescriptor.getViewName());
			if (forward == null) {
				forward = new ActionForward(viewDescriptor.getViewName());
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