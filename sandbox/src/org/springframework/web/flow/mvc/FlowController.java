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
package org.springframework.web.flow.mvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowLocator;
import org.springframework.web.flow.config.BeanFactoryFlowServiceLocator;
import org.springframework.web.flow.support.HttpFlowExecutionManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Web controller for the Spring MVC framework that handles requests using a web
 * flow.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowController extends AbstractController implements InitializingBean {

	private Flow flow;

	private Collection flowExecutionListeners;

	private HttpFlowExecutionManager manager;

	/**
	 * Set the top level fow started by this controller. This is optional. When
	 * not specified, the controller will try to obtain a flow id from the
	 * incoming request.
	 */
	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	/**
	 * Set the flow execution listener that should be notified of flow execution
	 * lifecycle events.
	 */
	public void setFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners = new ArrayList(1);
		this.flowExecutionListeners.add(listener);
	}

	/**
	 * Set the flow execution listeners that should be notified of flow
	 * execution lifecycle events.
	 */
	public void setFlowExecutionListeners(FlowExecutionListener[] listeners) {
		this.flowExecutionListeners = Arrays.asList(listeners);
	}

	public void afterPropertiesSet() throws Exception {
		//web flows need a session!
		setRequireSession(true);
		
		FlowLocator flowLocator = new BeanFactoryFlowServiceLocator(getApplicationContext());
		this.manager = new HttpFlowExecutionManager(this.flow, flowLocator, flowExecutionListeners);
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//delegate to the flow execution manager to process the request
		return manager.handleRequest(request, response);
	}
}