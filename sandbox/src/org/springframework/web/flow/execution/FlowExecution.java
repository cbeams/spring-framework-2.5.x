package org.springframework.web.flow.execution;

import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowContext;
import org.springframework.web.flow.FlowLocator;
import org.springframework.web.flow.FlowNavigationException;
import org.springframework.web.flow.ViewDescriptor;

public interface FlowExecution {
	
	/**
	 * Start executing this flow, transitioning it to the start state and
	 * returning the starting model and view descriptor. Typically called by a
	 * flow controller, but also from test code.
	 * @param originatingEvent the event that occured that triggered flow
	 *        execution creation
	 * @return the starting view descriptor, which returns control to the client
	 *         and requests that a view be rendered with model data
	 * @throws IllegalStateException if this execution has already been started,
	 *         or no state is marked as the start state.
	 */
	public ViewDescriptor start(Event originatingEvent) throws IllegalStateException;

	/**
	 * Signal an occurence of the specified event in the current state of this
	 * executing flow.
	 * @param event the event that occured within the current state of this flow
	 *        execution.
	 * @return the next model and view descriptor to display for this flow
	 *         execution, this returns control to the client and requests that a
	 *         view be rendered with model data
	 * @throws FlowNavigationException if the signaled event does not map
	 *         to any state transitions in the current state
	 * @throws IllegalStateException if the flow execution is not active and
	 *         thus is no longer (or not yet) processing events
	 */
	public ViewDescriptor signalEvent(Event event) throws FlowNavigationException, IllegalStateException;
	
	/**
	 * Rehydrate this flow execution after deserialization.
	 * @param flowLocator the flow locator
	 * @param listeners the flow execution listeners.
	 */
	public void rehydrate(FlowLocator flowLocator, FlowExecutionListener[] listeners);

	/**
	 * Returns a context object providing information about this executing flow
	 * @return the execution context
	 */
	public FlowContext getContext();
	
	/**
	 * Return a list of listeners monitoring the lifecycle of this flow execution.
	 * @return the flow execution listeners
	 */
	public FlowExecutionListenerList getListeners();
}
