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
package org.springframework.web.flow.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowExecutionListener;

/**
 * Abstract base implementation of a flow builder defining common functionality
 * needed by most concrete flow builder implementations.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class BaseFlowBuilder implements FlowBuilder, BeanFactoryAware {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The service locator that locates flow related artifacts by identifier or
	 * implementation class, as needed by this builder.
	 */
	private FlowServiceLocator flowServiceLocator = new BeanFactoryFlowServiceLocator();

	/**
	 * The collection of default flow execution listeners to attach the flow
	 * produced by this builder.
	 */
	private Collection flowExecutionListeners = new ArrayList(3);

	/**
	 * An abstract factory for flow creation.
	 */
	private FlowCreator flowCreator = new DefaultFlowCreator();

	/**
	 * Factory that creates transition criteria.
	 */
	private TransitionCriteriaCreator transitionCriteriaCreator = new SimpleTransitionCriteriaCreator();

	/**
	 * The <code>Flow</code> produced by this builder.
	 */
	private Flow flow;

	/**
	 * Default constructor for subclassing.
	 */
	protected BaseFlowBuilder() {
	}

	/**
	 * Create a base flow builder which will pull services (other flow defs,
	 * actions, model mappers, etc.) from the service locator.
	 * @param flowServiceLocator the locator
	 */
	protected BaseFlowBuilder(FlowServiceLocator flowServiceLocator) {
		setFlowServiceLocator(flowServiceLocator);
	}

	/**
	 * Create a base flow builder which will pull services from the service
	 * locator, and delegate construction of the Flow instance to the specified
	 * <code>FlowCreator</code>
	 * @param flowServiceLocator the locator
	 * @param flowCreator the flow creator
	 */
	protected BaseFlowBuilder(FlowServiceLocator flowServiceLocator, FlowCreator flowCreator) {
		setFlowServiceLocator(flowServiceLocator);
		setFlowCreator(flowCreator);
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (flowServiceLocator instanceof BeanFactoryFlowServiceLocator) {
			((BeanFactoryFlowServiceLocator)flowServiceLocator).setBeanFactory(beanFactory);
		}
	}

	/**
	 * Returns the flow creation strategy to use.
	 */
	protected FlowCreator getFlowCreator() {
		return flowCreator;
	}

	/**
	 * Set the flow creation strategy to use.
	 */
	public void setFlowCreator(FlowCreator flowCreator) {
		this.flowCreator = flowCreator;
	}

	/**
	 * Returns the factory used to create transition criteria.
	 */
	public TransitionCriteriaCreator getTransitionCriteriaCreator() {
		return transitionCriteriaCreator;
	}

	/**
	 * Set the factory used to create transition criteria.
	 */
	public void setTransitionCriteriaCreator(TransitionCriteriaCreator transitionCriteriaCreator) {
		this.transitionCriteriaCreator = transitionCriteriaCreator;
	}

	/**
	 * Returns the flow service location strategy in use.
	 */
	protected FlowServiceLocator getFlowServiceLocator() {
		return flowServiceLocator;
	}

	/**
	 * Set the flow service location strategy to use.
	 */
	public void setFlowServiceLocator(FlowServiceLocator flowServiceLocator) {
		this.flowServiceLocator = flowServiceLocator;
	}

	/**
	 * Set the default listener that will be associated with each execution of
	 * the flow created by this builder.
	 */
	public void setFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners.clear();
		this.flowExecutionListeners.add(listener);
	}

	/**
	 * Set the default listeners that will be associated with each execution of
	 * the flow created by this builder.
	 */
	public void setFlowExecutionListeners(FlowExecutionListener[] listeners) {
		this.flowExecutionListeners.clear();
		this.flowExecutionListeners.addAll(Arrays.asList(listeners));
	}

	public void buildExecutionListeners() throws FlowBuilderException {
		if (!this.flowExecutionListeners.isEmpty()) {
			getFlow().getFlowExecutionListenerList().add(
					(FlowExecutionListener[])flowExecutionListeners.toArray(new FlowExecutionListener[0]));
		}
	}

	/**
	 * Get the flow (result) built by this builder.
	 */
	protected Flow getFlow() {
		return flow;
	}

	/**
	 * Set the flow being built by this builder.
	 */
	protected void setFlow(Flow flow) {
		this.flow = flow;
	}

	public Flow getResult() {
		return getFlow();
	}

	/**
	 * Create the instance of the Flow built by this builder. Subclasses may
	 * override to return a custom Flow implementation, or simply pass in a
	 * custom FlowCreator implementation.
	 * 
	 * @param id the flow identifier
	 * @param properties optional, additional flow properties
	 * @return the flow built by this builder
	 */
	protected Flow createFlow(String id, Map properties) {
		return this.flowCreator.createFlow(id, properties);
	}

	/**
	 * The default FlowCreator implementation. This just instantiates the
	 * <code>Flow</code> class. If you need a custom <code>Flow</code>
	 * implementation, configure the BaseFlowBuilder with a custom
	 * <code>FlowCreator</code> factory.
	 * 
	 * @see org.springframework.web.flow.Flow
	 */
	public static class DefaultFlowCreator implements FlowCreator {
		public Flow createFlow(String flowId, Map properties) {
			return new Flow(flowId, properties);
		}
	}
}