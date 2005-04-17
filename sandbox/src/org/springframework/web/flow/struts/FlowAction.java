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
package org.springframework.web.flow.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowLocator;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.action.FormObjectAccessor;
import org.springframework.web.flow.config.BeanFactoryFlowServiceLocator;
import org.springframework.web.flow.execution.servlet.HttpServletFlowExecutionManager;
import org.springframework.web.flow.support.FlowExecutionListenerAdapter;
import org.springframework.web.struts.BindingActionForm;
import org.springframework.web.struts.TemplateAction;
import org.springframework.web.util.WebUtils;

/**
 * Struts Action that acts a front controller entry point into the web flow
 * system. Typically, a single FlowAction manages all flow executions by
 * parameterization with the appropriate <code>flowId</code> in views that
 * start new flow executions. Alternatively, a FlowAction may exist per
 * top-level (root) flow definition in the application.
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
 * FlowAction that fronts a single top-level flow:
 * 
 * <pre>
 *     &lt;action path=&quot;/userRegistration&quot;
 *     	type=&quot;org.springframework.web.flow.struts.FlowAction&quot;
 *     	name=&quot;bindingActionForm&quot; scope=&quot;request&quot; 
 *     	className=&quot;org.springframework.web.flow.struts.FlowActionMapping&quot;&gt;
 *     	&lt;set-property property=&quot;flowId&quot; value=&quot;user.Registration&quot; /&gt;
 *     &lt;/action&gt;
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
 * <li>Use of the <code>BindingActionForm</code> requires some minor setup in
 * <code>struts-config.xml</code>. Specifically:
 * <ol>
 * <li>A custom BindingActionForm-aware request processor is needed, to defer
 * form population:
 * 
 * <pre>
 *     &lt;controller processorClass=&quot;org.springframework.web.struts.BindingRequestProcessor&quot;/&gt; 
 * </pre>
 * 
 * <li>A <code>BindingPlugin</code> is needed, to plugin an Errors-aware
 * <code>jakarta-commons-beanutils</code> adapter:
 * 
 * <pre>
 *     &lt;plug-in className=&quot;org.springframework.web.struts.BindingPlugin&quot;/&gt;
 * </pre>
 * 
 * </ol>
 * </ul>
 * The benefits here are substantial: developers now have a powerful web flow
 * capability integrated with Struts, with a consistent-approach to POJO-based
 * binding and validation that addresses the proliferation of
 * <code>ActionForm</code> classes found in traditional Struts-based apps.
 * 
 * @see org.springframework.web.flow.struts.FlowActionMapping
 * @see org.springframework.web.flow.execution.servlet.HttpServletFlowExecutionManager
 * @see org.springframework.web.struts.BindingActionForm
 * @see org.springframework.web.struts.BindingRequestProcessor
 * @see org.springframework.web.struts.BindingPlugin
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowAction extends TemplateAction {

	private FlowLocator flowLocator;

	public void setServlet(ActionServlet actionServlet) {
		super.setServlet(actionServlet);
		this.flowLocator = new BeanFactoryFlowServiceLocator(getWebApplicationContext());
	}

	/**
	 * Returns the flow locator used to lookup flows by id. Defaults to
	 * {@link BeanFactoryFlowServiceLocator}.
	 */
	protected FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Get the flow id from given action mapping, which should be of type
	 * <code>FlowActionMapping</code>.
	 */
	protected String getFlowId(ActionMapping mapping) {
		Assert.isInstanceOf(FlowActionMapping.class, mapping);
		return ((FlowActionMapping) mapping).getFlowId();
	}

	protected ActionForward doExecuteAction(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		StrutsFlowExecutionManager flowExecutionManager = createFlowExecutionManager(mapping);
		FlowExecutionListener actionFormAdapter = createActionFormAdapter(request, form);
		ViewDescriptor viewDescriptor = flowExecutionManager.handle(mapping, form, request, response, actionFormAdapter);
		return toActionForward(viewDescriptor, mapping, request);
	}

	/**
	 * Creates the default flow execution manager. Subclasses can override this
	 * to return a specialized manager.
	 */
	protected StrutsFlowExecutionManager createFlowExecutionManager(ActionMapping mapping) {
		StrutsFlowExecutionManager manager = new StrutsFlowExecutionManager(getFlowLocator());
		String flowId = getFlowId(mapping);
		if (StringUtils.hasText(flowId)) {
			manager.setFlow(getFlowLocator().getFlow(flowId));
		}
		return manager;
	}

	/**
	 * Specialized HTTP based flow execution manager for use with Struts 1.x.
	 */
	public static class StrutsFlowExecutionManager extends HttpServletFlowExecutionManager {
		
		/**
		 * Create a new flow execution manager
		 * @param flowLocator the locator to lookup flows
		 */
		public StrutsFlowExecutionManager(FlowLocator flowLocator) {
			super(flowLocator);
		}

		/**
		 * Handle a Struts action request.
		 * @param mapping the action mapping
		 * @param form the action form
		 * @param request the current request
		 * @param response the current response
		 * @param listener a flow execution listener interested in lifecycle
		 *        events during handling of the current request
		 * @return the view descriptor
		 * @throws Exception when something goes wrong
		 */
		public ViewDescriptor handle(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response,
				FlowExecutionListener listener) throws Exception {
			return handle(new StrutsEvent(mapping, form, request, response), listener);
		}
	}

	/**
	 * Creates a flow execution listener that takes a Spring Errors instance
	 * supporting POJO-based data binding in request scope under a well-defined
	 * name and adapts it to the Struts Action form model.
	 * @param request the request
	 * @param form the action form
	 * @return the adapter
	 */
	protected FlowExecutionListener createActionFormAdapter(final HttpServletRequest request, final ActionForm form) {
		return new FlowExecutionListenerAdapter() {
			public void requestProcessed(RequestContext context) {
				if (context.isFlowExecutionActive()) {
					if (form instanceof BindingActionForm) {
						BindingActionForm bindingForm = (BindingActionForm) form;
						bindingForm.setErrors(new FormObjectAccessor(context).getFormErrors());
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
	private ActionForward toActionForward(ViewDescriptor viewDescriptor, ActionMapping mapping, HttpServletRequest request) {
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
				logger.info("No view descriptor; returning a [null] forward");
			}
			return null;
		}
	}
}