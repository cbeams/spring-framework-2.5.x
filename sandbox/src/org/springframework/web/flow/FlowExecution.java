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
package org.springframework.web.flow;

/**
 * Central interface used by clients to interact with the flow system
 * and an ongoing flow execution.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecution extends FlowExecutionMBean {

	/**
	 * Returns a mutable list of listeners attached to this flow execution.
	 * @return the flow execution listener list
	 */
	public FlowExecutionListenerList getListenerList();

	/**
	 * Start this flow execution, transitioning it to the start state and
	 * returning the starting model and view descriptor. Typically called by a
	 * flow controller, but also in test code.
	 * @param originatingEvent the event that occured that triggered flow execution
	 *        creation
	 * @return the starting view descriptor, which returns control to the client
	 *         and requests that a view be rendered with model data
	 * @throws IllegalStateException if this execution has already been started,
	 *         or no state is marked as the start state.
	 */
	public ViewDescriptor start(Event originatingEvent) throws IllegalStateException;

	/**
	 * Signal an occurence of the specified event in this flow execution.
	 * @param event the event that occured within the current state of this flow
	 *        execution.
	 * @return the next model and view descriptor to display for this flow
	 *         execution, this returns control to the client and requests that a
	 *         view be rendered with model data
	 * @throws NoSuchTransitionException if the signaled event does not map to
	 *         any state transitions in the current state
	 * @throws IllegalStateException if the flow execution is not active and
	 *         thus is no longer (or not yet) processing events
	 */
	public ViewDescriptor signalEvent(Event event) throws NoSuchTransitionException, IllegalStateException;

	/**
	 * Called by a client to restore this flow execution's state after
	 * deserialization.
	 * @param flowLocator the locator capable of supporting rehydration
	 * @param listeners the set of listeners that should be notified of
	 *        lifecycle events in this flow execution
	 */
	public void rehydrate(FlowLocator flowLocator, FlowExecutionListener[] listeners);
}