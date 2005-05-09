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
package org.springframework.web.flow.execution.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowLocator;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.config.BeanFactoryFlowServiceLocator;
import org.springframework.web.flow.execution.FlowExecutionListener;
import org.springframework.web.flow.execution.FlowExecutionManager;

/**
 * Flow execution manager to manage flow executions using HTTP servlet
 * requests and the HTTP session.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class HttpServletFlowExecutionManager extends FlowExecutionManager implements BeanFactoryAware {

	/**
	 * Creates an HTTP-servlet based flow execution manager.
	 */
	public HttpServletFlowExecutionManager() {
		initDefaults();
	}

	/**
	 * Creates an HTTP-servlet based flow execution manager.
	 * @param flow the flow to manage
	 */
	public HttpServletFlowExecutionManager(Flow flow) {
		initDefaults();
		setFlow(flow);
	}

	/**
	 * Creates an HTTP-servlet based flow execution manager.
	 * @param flowLocator the locator to find flows to manage
	 */
	public HttpServletFlowExecutionManager(FlowLocator flowLocator) {
		initDefaults();
		setFlowLocator(flowLocator);
	}

	/**
	 * Set default properties for this manager.
	 */
	protected void initDefaults() {
		setFlowExecutionStorage(new HttpSessionFlowExecutionStorage());
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (getFlowLocator() == null) {
			// a convenience default for use with Spring bean factories
			setFlowLocator(new BeanFactoryFlowServiceLocator(beanFactory));
		}
	}

	/**
	 * The main entry point into managed HTTP-based flow executions.
	 * @param request the current HTTP request
	 * @param response the current HTTP response
	 * @return the view descriptor of the model and view to render
	 * @throws Exception in case of errors
	 */
	public ViewDescriptor handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return handle(createEvent(request, response));
	}

	/**
	 * The main entry point into managed HTTP-based flow executions.
	 * @param request the current HTTP request
	 * @param response the current HTTP response
	 * @param flowExecutionListener a listener interested in flow execution
	 *        lifecycle events that happen <i>while handling this request</i>
	 * @return the view descriptor of the model and view to render
	 * @throws Exception in case of errors
	 */
	public ViewDescriptor handle(HttpServletRequest request, HttpServletResponse response,
			FlowExecutionListener flowExecutionListener) throws Exception {
		return handle(createEvent(request, response), flowExecutionListener);
	}

	// subclassing hooks

	/**
	 * Create a flow event wrapping given request and response. Subclasses
	 * can override this, e.g. when the want to use special names for the
	 * request parameters.
	 */
	protected Event createEvent(HttpServletRequest request, HttpServletResponse response) {
		return new HttpServletRequestEvent(request, response);
	}

}