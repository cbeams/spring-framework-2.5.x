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
package org.springframework.web.flow.portlet;

import javax.portlet.*;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.execution.portlet.PortletFlowExecutionManager;
import org.springframework.web.portlet.support.AbstractController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Portlet controller for the Spring Portlet MVC framework that handles requests
 * using a web flow. Requests are managed using an
 * {@link PortletFlowExecutionManager}. Consult the JavaDoc of that class for
 * more information on how requests are processed.
 * <p>
 * Configuration note: you can configure the flow controller by passing in a
 * properly configured flow execution manager. To avoid unnecessary top-level
 * bean definitions in the application context, you can use the Spring nested
 * bean definition syntax.
 * <p>
 * Also note that the PortletFlowController requires a session and will create
 * one if it does not yet exist. It needs this session to be able to share information
 * between the action and render requests.
 * <p>
 * <b>Exposed configuration properties:</b><br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>default</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>flowExecutionManager</td>
 * <td>{@link org.springframework.web.flow.execution.portlet.PortletFlowExecutionManager default}</td>
 * <td>Configures the portlet flow execution manager implementation to use.</td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.web.flow.execution.portlet.PortletFlowExecutionManager
 * 
 * @author J.Enrique Ruiz
 * @author César Ordiñana
 */
public class PortletFlowController extends AbstractController implements BeanFactoryAware, InitializingBean {

	/**
	 * Name of the session attribute used to pass the view descriptor from the
	 * action request to the render request.
	 */
	public static final String VIEWDESCRIPTOR_ATTRIBUTE = "ActionRequest:ViewDescriptor";
	
	/**
	 * Our bean factory.
	 */
	private BeanFactory beanFactory;
	
	/**
	 * The portlet based manager for flow executions.
	 */
	private PortletFlowExecutionManager flowExecutionManager;

	/**
	 * Create a new FlowController.
	 */
	public PortletFlowController() {
		initDefaults();
	}

	/**
	 * Create a new FlowController.
	 */
	public PortletFlowController(PortletFlowExecutionManager manager) {
		setFlowExecutionManager(manager);
		initDefaults();
	}

	/**
	 * Set default properties for this controller.
	 */
	protected void initDefaults() {
		setFlowExecutionManager(new PortletFlowExecutionManager());
	}
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	protected BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.flowExecutionManager, "The portlet flow execution manager is required");
		this.flowExecutionManager.setBeanFactory(getBeanFactory());
	}

	/**
	 * Returns the flow execution manager used by this controller.
	 * @return the portlet flow execution manager
	 */
	protected PortletFlowExecutionManager getFlowExecutionManager() {
		return flowExecutionManager;
	}

	/**
	 * Configures the flow execution manager implementation to use.
	 * @param manager the flow execution manager.
	 */
	public void setFlowExecutionManager(PortletFlowExecutionManager manager) {
		this.flowExecutionManager = manager;
	}

	/**
	 * Process the Action request.
	 * @param request current portlet Action request
	 * @param response current portlet Action response
	 * @throws Exception in case of errors
	 */
	protected void handleRequestInternal(ActionRequest request, ActionResponse response) throws Exception {
		ViewDescriptor viewDescriptor = flowExecutionManager.handle(request, response);
		request.getPortletSession().setAttribute(VIEWDESCRIPTOR_ATTRIBUTE, viewDescriptor);
	}

	/**
	 * Process the request and return a ModelAndView object which the
	 * DispatcherPortlet will render. A null return is not an error: It
	 * indicates that this object completed request processing itself, thus
	 * there is no ModelAndView to render.
	 * @param request current portlet render request
	 * @param response current portlet render response
	 * @return a ModelAndView to render, or null if handled directly
	 * @throws Exception in case of errors
	 */
	protected ModelAndView handleRequestInternal(RenderRequest request, RenderResponse response) throws Exception {
		try {
			// note that we can't put the view descriptor in a request attribute because
			// request attributes are objects associated with a portlet during a single
			// portlet request: a portlet cannot assume that attributes are shared between
			// action and render requests
			ViewDescriptor viewDescriptor =
				(ViewDescriptor) request.getPortletSession().getAttribute(VIEWDESCRIPTOR_ATTRIBUTE);
			if (viewDescriptor == null) {
				viewDescriptor = flowExecutionManager.handle(request, response);
			}
	
			// convert the view descriptor to a ModelAndView object
			return toModelAndView(viewDescriptor);
		}
		finally {
			request.getPortletSession().removeAttribute(VIEWDESCRIPTOR_ATTRIBUTE);
		}
	}

	/**
	 * Create a ModelAndView object based on the information in given view
	 * descriptor. Subclasses can override this to return a specialized
	 * ModelAndView or to do custom processing on it.
	 * @param viewDescriptor the view descriptor to convert
	 * @return a new ModelAndView object
	 */
	protected ModelAndView toModelAndView(ViewDescriptor viewDescriptor) {
		return new ModelAndView(viewDescriptor.getViewName(), viewDescriptor.getModel());
	}
}