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
import org.springframework.web.flow.support.HttpServletFlowExecutionManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Web controller for the Spring MVC framework that handles requests using a web
 * flow. Requests are managed using an {@link HttpServletFlowExecutionManager}.
 * Consult the JavaDoc of that class for more information on how requests are
 * processed.
 * <p>
 * This controller requires sessions to keep track of flow state. So it will
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
 * <td><i>null </i></td>
 * <td>Set the top level fow started by this controller. This is optional.
 * </td>
 * </tr>
 * <tr>
 * <td>flowExecutionListener(s)</td>
 * <td><i>null </i></td>
 * <td>Set the flow execution listener(s) that should be notified of flow
 * execution lifecycle events.</td>
 * </tr>
 * </table>
 * @author Erwin Vervaet
 * @author Keith Donald
 * @see org.springframework.web.flow.support.HttpServletFlowExecutionManager
 */
public class FlowController extends AbstractController implements InitializingBean {

	/**
	 * The flow managed by this controller, may be null if the views will
	 * parameterize this controller with the id of the flow to manage.
	 */
	private Flow flow;

	/**
	 * The list of flow execution listeners to attach to flow executions managed
	 * by this controller.
	 */
	private FlowExecutionListener[] flowExecutionListeners;

	/**
	 * A helper for managed http servlet request-based flow executions in the
	 * http session.
	 */
	private HttpServletFlowExecutionManager manager;

	/**
	 * Set the top level fow started by this controller. This is optional.
	 */
	public void setFlow(Flow flow) {
		this.flow = flow;
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

	public void afterPropertiesSet() throws Exception {
		// web flows need a session!
		setRequireSession(true);
		// setup our flow execution manager
		this.manager = createHttpFlowExecutionManager();
	}

	/**
	 * Create a new HTTP flow execution manager. Subclasses can override this to
	 * return a specialed manager.
	 */
	protected HttpServletFlowExecutionManager createHttpFlowExecutionManager() {
		FlowLocator flowLocator = new BeanFactoryFlowServiceLocator(getApplicationContext());
		return new HttpServletFlowExecutionManager(this.flow, flowLocator, flowExecutionListeners);
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// delegate to the flow execution manager to process the request
		ViewDescriptor viewDescriptor = manager.handleRequest(request, response);
		ModelAndView mv = new ModelAndView(viewDescriptor.getViewName(), viewDescriptor.getModel());
		return mv;
	}
}