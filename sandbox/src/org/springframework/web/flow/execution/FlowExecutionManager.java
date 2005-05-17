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
package org.springframework.web.flow.execution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.config.BeanFactoryFlowServiceLocator;
import org.springframework.web.flow.execution.impl.FlowExecutionImpl;

/**
 * A manager for the executing flows of the application. This object is responsible for
 * creating new flow executions as requested by the client, as well as signaling events
 * for processing by existing, paused executions (that are waiting to be resumed in response
 * to a user event).
 * <p>
 * The {@link #onEvent(Event)} method implements the following algorithm:
 * <ol>
 * <li>Look for a flow execution id in the event (in a parameter named
 * "_flowExecutionId").</li>
 * <li>If no flow execution id is found, a new flow execution is created.
 * The top-level flow for which the execution is created is determined
 * by first looking for a flow id specified in the event using the "_flowId"
 * parameter. If this parameter is present the specified flow will be
 * used, after lookup using a flow locator. If no "_flowId" parameter is
 * present, the default top-level flow configured for this manager is used.</li>
 * <li>If a flow execution id is found, the previously saved flow execution
 * with that id is loaded from the storage.</li>
 * <li>If a new flow execution was created in the previous steps, it is
 * started.</li>
 * <li>If an existing flow execution is loaded from storage, the current state id
 * ("_currentStateId") and event id ("_eventId") parameter values are
 * extracted from the event. The event is then signaled in that state, and 
 * the executing flow is resumed in that state.</li>
 * <li>If the flow execution is still active after event processing, it
 * is saved in storage. This process generates a unique flow execution
 * id that will be exposed to the caller for reference on subsequent events.</li>
 * </ol>
 * 
 * @see org.springframework.web.flow.execution.FlowExecution
 * @see org.springframework.web.flow.execution.FlowExecutionStorage
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class FlowExecutionManager implements BeanFactoryAware {

	/**
	 * Clients can send the id (name) of the flow to be started
	 * using an event parameter with this name ("_flowId").
	 */
	public static final String FLOW_ID_PARAMETER = "_flowId";

	/**
	 * Clients can send the flow execution id using an event
	 * parameter with this name ("_flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_PARAMETER = "_flowExecutionId";

	/**
	 * The id of the flow execution will be exposed to the view in a model
	 * attribute with this name ("flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_ATTRIBUTE = "flowExecutionId";

	/**
	 * The flow execution itself will be exposed to the view in a model
	 * attribute with this name ("flowExecution").
	 */
	public static final String FLOW_EXECUTION_ATTRIBUTE = "flowExecution";

	/**
	 * The current state of the flow execution will be exposed to the view in a
	 * model attribute with this name ("currentStateId").
	 */
	public static final String CURRENT_STATE_ID_ATTRIBUTE = "currentStateId";

	/**
	 * Event id value indicating that the event has not been set ("@NOT_SET@").
	 */
	public static final String NOT_SET_EVENT_ID = "@NOT_SET@";
	

	protected final Log logger = LogFactory.getLog(FlowExecutionManager.class);
	
	private BeanFactory beanFactory;

	private Flow flow;

	private FlowLocator flowLocator = new BeanFactoryFlowServiceLocator();

	private FlowExecutionListener[] listeners;

	private FlowExecutionStorage storage;
	
	private TransactionSynchronizer transactionSynchronizer = new TokenTransactionSynchronizer();

	/**
	 * Create a new flow execution manager. Before use, the manager should
	 * be appropriately configured using setter methods. At least the flow
	 * execution storage strategy should be set!
	 * 
	 * @see #setFlow(Flow)
	 * @see #setFlowLocator(FlowLocator)
	 * @see #setListener(FlowExecutionListener)
	 * @see #setListeners(FlowExecutionListener[])
	 * @see #setStorage(FlowExecutionStorage) 
	 */
	public FlowExecutionManager() {
	}
	
	/**
	 * Returns this flow execution manager's bean factory.
	 */
	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (getFlowLocator() instanceof BeanFactoryAware) {
			// make the BeanFactoryFlowServiceLocator work
			((BeanFactoryAware)getFlowLocator()).setBeanFactory(beanFactory);
		}
	}

	/**
	 * Returns the flow whose executions are managed by this manager.
	 * Could be <code>null</code> if there is no preconfigured flow and
	 * the id of the flow for which executions will be managed is sent
	 * in an event parameter "_flowId".
	 */
	protected Flow getFlow() {
		return flow;
	}

	/**
	 * Set the flow whose executions will be managed if there is no alternate
	 * flow id specified in a "_flowId" event parameter.
	 */
	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	/**
	 * Returns the flow locator to use for lookup of flows specified using the
	 * "_flowId" event parameter.
	 */
	protected FlowLocator getFlowLocator() {
		return flowLocator;
	}

	/**
	 * Set the flow locator to use for lookup of flows specified using the
	 * "_flowId" event parameter.
	 */
	public void setFlowLocator(FlowLocator flowLocator) {
		this.flowLocator = flowLocator;
	}

	/**
	 * Returns the array of flow execution listeners.
	 * @return the flow execution listeners
	 */
	protected FlowExecutionListener[] getListeners() {
		return this.listeners;
	}

	/**
	 * Set the flow execution listener that will be notified of managed
	 * flow executions.
	 */
	public void setListener(FlowExecutionListener listener) {
		this.listeners = new FlowExecutionListener[] { listener };
	}

	/**
	 * Sets the flow execution listeners that will be notified of managed
	 * flow executions.
	 */
	public void setListeners(FlowExecutionListener[] listeners) {
		this.listeners = listeners;
	}
	
	/**
	 * Returns the storage strategy used by the flow execution manager.
	 */
	public FlowExecutionStorage getStorage() {
		return storage;
	}

	/**
	 * Set the storage strategy used by the flow execution manager.
	 */
	public void setStorage(FlowExecutionStorage storage) {
		Assert.notNull(storage, "The flow execution storage strategy is required");
		this.storage = storage;
	}
	
	/**
	 * Return the application transaction synchronization strategy to use.
	 * This defaults to a <i>synchronizer token</i> based transaction management
	 * system.
	 */
	public TransactionSynchronizer getTransactionSynchronizer() {
		return transactionSynchronizer;
	}
	
	/**
	 * Set the application transaction synchronization strategy to use.
	 */
	public void setTransactionSynchronizer(TransactionSynchronizer transactionSynchronizer) {
		this.transactionSynchronizer = transactionSynchronizer;
	}
	
	// event management

	/**
	 * Signal the occurence of the specified event - this is the entry point into the 
	 * webflow system for managing all executing flows.
	 * @param event the event that occured
	 * @return the view descriptor of the model and view to render
	 * @throws Exception in case of errors
	 */
	public ViewDescriptor onEvent(Event event) throws Exception {
		return onEvent(event, null);
	}

	/**
	 * Signal the occurence of the specified event - this is the entry point into the 
	 * webflow system for managing all executing flows.
	 * @param event the event that occured
	 * @param listener a listener interested in flow execution
	 *        lifecycle events that happen <i>while handling this event</i>
	 * @return the view descriptor of the model and view to render
	 * @throws Exception in case of errors
	 */
	public ViewDescriptor onEvent(Event event, FlowExecutionListener listener) throws Exception {
		FlowExecution flowExecution;
		ViewDescriptor viewDescriptor;
		String id = getFlowExecutionId(event);
		if (id == null) {
			// start a new flow execution
			flowExecution = createFlowExecution(getFlow(event));
			if (listener != null) {
				flowExecution.getListeners().add(listener);
			}
			viewDescriptor = flowExecution.start(event);
		}
		else {
			// client is participating in an existing flow execution,
			// retrieve information about it
			flowExecution = getStorage().load(id, event);
			// rehydrate the execution if neccessary (if it had been serialized out)
			flowExecution.rehydrate(getFlowLocator(), getListeners(), getTransactionSynchronizer());
			if (listener != null) {
				flowExecution.getListeners().add(listener);
			}
			// signal the event within the current state
			Assert.hasText(event.getId(), "No event id could be obtained -- "
					+ "make sure the submitting view or other client provides it as input");
			// see if the eventId was set to a static marker placeholder because
			// of a client configuration error
			if (event.getId().equals(getNotSetEventIdParameterMarker())) {
				throw new IllegalArgumentException("The received event id was the 'not set' marker '"
						+ getNotSetEventIdParameterMarker()
						+ "' -- this is likely a view (jsp, etc) configuration error --"
						+ "the event id parameter must be set to a valid event");
			}
			viewDescriptor = flowExecution.signalEvent(event);
		}
		if (flowExecution.isActive()) {
			// save the flow execution for future use
			id = getStorage().save(id, flowExecution, event);
		}
		else {
			// event execution resulted in the entire flow execution ending, cleanup
			if (id != null) {
				getStorage().remove(id, event);
			}
		}
		if (listener != null) {
			flowExecution.getListeners().remove(listener);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning selected view descriptor " + viewDescriptor);
		}
		return prepareViewDescriptor(viewDescriptor, id, flowExecution);
	}

	// subclassing hooks

	/**
	 * Create a new flow execution for given flow. Subclasses could redefine this
	 * if they which to use a specialized FlowExecution implementation class.
	 * @param flow the flow
	 * @return the created flow execution
	 */
	protected FlowExecution createFlowExecution(Flow flow) {
		return new FlowExecutionImpl(getFlow(), getListeners(), getTransactionSynchronizer());
	}

	/**
	 * Obtain a flow to use from given event. If there is a "_flowId" parameter
	 * specified in the event, the flow with that id will be returend after
	 * lookup using the flow locator. If no "_flowId" parameter is present in the
	 * event, the default top-level flow will be returned.
	 */
	protected Flow getFlow(Event event) {
		String flowId = (String)event.getParameter(getFlowIdParameterName());
		if (!StringUtils.hasText(flowId)) {
			Assert.notNull(getFlow(),
					"This flow execution manager is not configured with a default top-level flow; thus, "
							+ "the flow to execute must be provided by client views via the '"
							+ getFlowIdParameterName() + "' parameter, yet no parameter was provided in this event");
			return getFlow();
		}
		else {
			Assert.notNull(getFlowLocator(), "The flow locator is required to lookup the requested flow with id '"
					+ flowId + "'; however, the flowLocator property is null");
			return getFlowLocator().getFlow(flowId);
		}
	}

	/**
	 * Returns the name of the flow id parameter in the event ("_flowId").
	 */
	protected String getFlowIdParameterName() {
		return FLOW_ID_PARAMETER;
	}

	/**
	 * Obtain a unique flow execution id from given event.
	 * @param event the event
	 * @return the obtained id or <code>null</code> if not found
	 */
	protected String getFlowExecutionId(Event event) {
		return (String)event.getParameter(getFlowExecutionIdParameterName());
	}

	/**
	 * Returns the name of the flow execution id parameter in the event
	 * ("_flowExecutionId").
	 */
	protected String getFlowExecutionIdParameterName() {
		return FLOW_EXECUTION_ID_PARAMETER;
	}

	/**
	 * Returns the marker value indicating that the event id parameter was not
	 * set properly in the event because of a view configuration error ("@NOT_SET@").
	 * <p>
	 * This is useful when a view relies on an dynamic means to set the eventId
	 * event parameter, for example, using javascript. This approach assumes
	 * the "not set" marker value will be a static default (a kind of fallback,
	 * submitted if the eventId does not get set to the proper dynamic value
	 * onClick, for example, if javascript was disabled).
	 */
	protected String getNotSetEventIdParameterMarker() {
		return NOT_SET_EVENT_ID;
	}

	/**
	 * Do any processing necessary before given view descriptor can be returned
	 * to the client of the flow execution manager. This implementation adds
	 * a number of <i>infrastructure attributes</i> to the model that will be
	 * exposed to the view.
	 * @param viewDescriptor the view descriptor to be processed
	 * @param flowExecutionId the unique id of the flow execution
	 * @param flowExecution the flow execution
	 * @return the processed view descriptor
	 */
	protected ViewDescriptor prepareViewDescriptor(ViewDescriptor viewDescriptor, String flowExecutionId,
			FlowExecution flowExecution) {
		if (flowExecution.isActive()) {
			// make the unique flow execution id available in the model
			viewDescriptor.addObject(FLOW_EXECUTION_ID_ATTRIBUTE, flowExecutionId);
			// make the flow execution itself available in the model
			viewDescriptor.addObject(FLOW_EXECUTION_ATTRIBUTE, flowExecution);
			// add some convenience values for views that aren't easily JavaBean aware
			viewDescriptor.addObject(CURRENT_STATE_ID_ATTRIBUTE, flowExecution.getActiveSession().getCurrentState().getId());
		}
		return viewDescriptor;
	}
}