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
package org.springframework.web.flow.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowLocator;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.config.BeanFactoryFlowServiceLocator;
import org.springframework.web.flow.execution.FlowExecutionStorage;
import org.springframework.web.flow.execution.HttpServletRequestFlowExecutionManager;
import org.springframework.web.flow.execution.HttpSessionFlowExecutionStorage;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Web controller for the Spring MVC framework that handles requests using a web
 * flow. Requests are managed using an {@link HttpServletRequestFlowExecutionManager}.
 * Consult the JavaDoc of that class for more information on how requests are
 * processed.
 * <p>
 * This controller requires sessions to keep track of flow state, so it will
 * force the "requireSession" attribute defined by the AbstractController to
 * true.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name </b></td>
 * <td><b>default </b></td>
 * <td><b>description </b></td>
 * </tr>
 * <tr>
 * <td>flow</td>
 * <td><i>null</i></td>
 * <td>Set the top level fow started by this controller. This is optional.
 * </td>
 * </tr>
 * <tr>
 * <td>flowExecutionListener(s)</td>
 * <td><i>null</i></td>
 * <td>Set the flow execution listener(s) that should be notified of flow
 * execution lifecycle events.</td>
 * </tr>
 * <tr>
 * <td>flowExecutionManager</td>
 * <td>{@link org.springframework.web.flow.execution.HttpServletRequestFlowExecutionManager default}</td>
 * <td>Configures the flow execution manager implementation to use.</td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.web.flow.execution.HttpServletRequestFlowExecutionManager
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowController extends AbstractController implements InitializingBean {

	/**
	 * The flow managed by this controller, may be null if the views will
	 * parameterize this controller with the id of the flow to manage.
	 */
	private Flow flow;

	/**
	 * A helper for managed HTTP servlet request-based flow executions.
	 */
	private HttpServletRequestFlowExecutionManager flowExecutionManager;

	/**
	 * The listeners of executing flows managed by this controller.
	 */
	private FlowExecutionListener[] flowExecutionListeners;
	
	/**
	 * Create a new FlowController.
	 * <p>
	 * The "cacheSeconds" property is by default set to 0 (so no caching for
	 * web flow controllers).
	 */
	public FlowController() {
		setCacheSeconds(0);
	}

	/**
	 * Returns the top level flow started by this controller, or
	 * <code>null</code> if not set.
	 */
	protected Flow getFlow() {
		return flow;
	}
	
	/**
	 * Set the top level fow started by this controller. This is optional.
	 */
	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	/**
	 * Returns the flow execution listeners that should be notified of flow
	 * execution lifecycle events.
	 */
	protected FlowExecutionListener[] getFlowExecutionListeners() {
		return this.flowExecutionListeners;
	}
	
	/**
	 * Set the flow execution listener that should be notified of flow execution
	 * lifecycle events.
	 */
	public void setFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners = new FlowExecutionListener[] { listener };
	}

	/**
	 * Set the flow execution listeners that should be notified of flow
	 * execution lifecycle events.
	 */
	public void setFlowExecutionListeners(FlowExecutionListener[] listeners) {
		this.flowExecutionListeners = listeners;
	}

	/**
	 * Returns the flow execution manager used by this controller. Defaults
	 * to {@link HttpServletRequestFlowExecutionManager}.
	 */
	protected HttpServletRequestFlowExecutionManager getFlowExecutionManager() {
		return flowExecutionManager;
	}

	/**
	 * Configures the flow execution manager implementation to use, allowing
	 * parameterization of custom manager specializations.
	 * @param manager the flow execution manager.
	 */
	public void setFlowExecutionManager(HttpServletRequestFlowExecutionManager manager) {
		this.flowExecutionManager = manager;
	}

	public void afterPropertiesSet() throws Exception {
		// web flows need a session!
		setRequireSession(true);
		if (this.flowExecutionManager == null) {
			this.flowExecutionManager = createFlowExecutionManager();
		}
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// delegate to the flow execution manager to process the request
		ViewDescriptor viewDescriptor = flowExecutionManager.handle(request, response);
		// convert the view descriptor to a ModelAndView object
		return createModelAndViewFromViewDescriptor(viewDescriptor);
	}

	// subclassing hooks

	/**
	 * Creates the default flow execution manager. Subclasses can override this to
	 * return a specialized manager. Alternatively, they can pass in a custom
	 * flow execution manager by setting the "flowExecutionManager" property.
	 */
	protected HttpServletRequestFlowExecutionManager createFlowExecutionManager() {
		FlowExecutionStorage storage = new HttpSessionFlowExecutionStorage();
		FlowLocator flowLocator = new BeanFactoryFlowServiceLocator(getApplicationContext());
		return new HttpServletRequestFlowExecutionManager(storage, flowLocator, getFlow(), getFlowExecutionListeners());
	}
	
	/**
	 * Create a ModelAndView object based on the information in given view
	 * descriptor. Subclasses can override this to return a specialized ModelAndView
	 * or to do custom processing on it.
	 * @param viewDescriptor the view descriptor to convert
	 * @return a new ModelAndView object
	 */
	protected ModelAndView createModelAndViewFromViewDescriptor(ViewDescriptor viewDescriptor) {
		return new ModelAndView(viewDescriptor.getViewName(), viewDescriptor.getModel());
	}
}