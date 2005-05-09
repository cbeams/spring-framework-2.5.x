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
package org.springframework.web.flow.execution.portlet;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

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
 * Flow execution manager to manage flow executions using portlet requests and
 * the portlet session.
 * 
 * @author J.Enrique Ruiz
 * @author César Ordiñana
 */
public class PortletFlowExecutionManager extends FlowExecutionManager implements BeanFactoryAware {

	/**
	 * Creates a portlet based flow execution manager.
	 */
	public PortletFlowExecutionManager() {
		initDefaults();
	}

	/**
	 * Creates a portlet based flow execution manager.
	 * @param flow the flow to manage
	 */
	public PortletFlowExecutionManager(Flow flow) {
		initDefaults();
		setFlow(flow);
	}

	/**
	 * Creates a portlet based flow execution manager.
	 * @param flowLocator the locator to find flows to manage
	 */
	public PortletFlowExecutionManager(FlowLocator flowLocator) {
		initDefaults();
		setFlowLocator(flowLocator);
	}

	/**
	 * Set default properties for this manager.
	 */
	protected void initDefaults() {
		setFlowExecutionStorage(new PortletSessionFlowExecutionStorage());
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (getFlowLocator() == null) {
			// a convenience default for use with Spring bean factories
			setFlowLocator(new BeanFactoryFlowServiceLocator(beanFactory));
		}
	}

	/**
	 * The main entry point into managed portlet flow executions.
	 * @param request the current portlet request
	 * @param response the current portlet response
	 * @return the view descriptor of the model and view to render
	 * @throws Exception in case of errors
	 */
	public ViewDescriptor handle(PortletRequest request, PortletResponse response) throws Exception {
		return handle(createEvent(request, response));
	}

	/**
	 * The main entry point into managed portlet flow executions.
	 * @param request the current portlet request
	 * @param response the current portlet response
	 * @param flowExecutionListener a listener interested in flow
	 *        execution lifecycle events that happen <i>while handling this request</i>
	 * @return the view descriptor of the model and view to render
	 * @throws Exception in case of errors
	 */
	public ViewDescriptor handle(PortletRequest request, PortletResponse response,
			FlowExecutionListener flowExecutionListener) throws Exception {
		return handle(createEvent(request, response), flowExecutionListener);
	}

	// subclassing hooks

	/**
	 * Create a flow event wrapping given portlet request and response.
	 */
	protected Event createEvent(PortletRequest request, PortletResponse response) {
		return new PortletRequestEvent(request, response);
	}
}