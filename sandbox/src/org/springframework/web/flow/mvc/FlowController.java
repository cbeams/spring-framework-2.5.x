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
import org.springframework.util.Assert;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.execution.servlet.HttpServletFlowExecutionManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Web controller for the Spring web MVC framework that handles requests using a web
 * flow. Requests are managed using an {@link HttpServletFlowExecutionManager}.
 * Consult the JavaDoc of that class for more information on how requests are
 * processed.
 * <p>
 * <b>Exposed configuration properties: </b> <br>
 * <table border="1">
 * <tr>
 * <td><b>name</b></td>
 * <td><b>description</b></td>
 * </tr>
 * <tr>
 * <td>flowExecutionManager</td>
 * <td>Configures the http servlet flow execution manager implementation to use.
 * This is a required property.</td>
 * </tr>
 * </table>
 * 
 * @see org.springframework.web.flow.execution.servlet.HttpServletFlowExecutionManager
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowController extends AbstractController implements InitializingBean {

	/**
	 * The HTTP servlet-based manager for flow executions.
	 */
	private HttpServletFlowExecutionManager flowExecutionManager;

	/**
	 * Create a new FlowController.
	 * <p>
	 * The "cacheSeconds" property is by default set to 0 (so no caching for
	 * web flow controllers).
	 */
	public FlowController() {
		initDefaults();
	}

	/**
	 * Create a new FlowController.
	 * <p>
	 * The "cacheSeconds" property is by default set to 0 (so no caching for
	 * web flow controllers).
	 */
	public FlowController(HttpServletFlowExecutionManager manager) {
		setFlowExecutionManager(manager);
		initDefaults();
	}

	/**
	 * Set default properties for this controller.
	 */
	protected void initDefaults() {
		setCacheSeconds(0);
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.flowExecutionManager, "The http servlet flow execution manager is required");
	}

	/**
	 * Returns the flow execution manager used by this controller.
	 * @return the HTTP flow execution manager
	 */
	protected HttpServletFlowExecutionManager getFlowExecutionManager() {
		return flowExecutionManager;
	}

	/**
	 * Configures the flow execution manager implementation to use.
	 * @param manager the flow execution manager.
	 */
	public void setFlowExecutionManager(HttpServletFlowExecutionManager manager) {
		this.flowExecutionManager = manager;
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// delegate to the flow execution manager to process the request
		ViewDescriptor viewDescriptor = flowExecutionManager.handle(request, response);
		// convert the view descriptor to a ModelAndView object
		return toModelAndView(viewDescriptor);
	}

	/**
	 * Create a ModelAndView object based on the information in given view
	 * descriptor. Subclasses can override this to return a specialized ModelAndView
	 * or to do custom processing on it.
	 * @param viewDescriptor the view descriptor to convert
	 * @return a new ModelAndView object
	 */
	protected ModelAndView toModelAndView(ViewDescriptor viewDescriptor) {
		return new ModelAndView(viewDescriptor.getViewName(), viewDescriptor.getModel());
	}
}