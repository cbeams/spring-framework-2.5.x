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
package org.springframework.web.flow;

/**
 * Central client facade interface of the Flow system.
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecution extends FlowExecutionMBean {

	/**
	 * Returns a mutable list of listeners attached to this flow execution.
	 * @return The flow execution listener list
	 */
	public FlowExecutionListenerList getListenerList();

	/**
	 * Start this flow execution, transitioning it to the start state and
	 * returning the starting model and view descriptor. Typically called by the
	 * FlowController, but also in test code.
	 * @param event The event that occured
	 * @return The starting model and view.
	 * @throws IllegalStateException if this execution has already been started,
	 *         or no state is marked as the start state.
	 */
	public ViewDescriptor start(Event startingEvent) throws IllegalStateException;

	/**
	 * Signal an occurence of the specified event in the (optionally) provided
	 * state of this flow execution.
	 * @param event The event that occured
	 * @return The next model and view descriptor to display for this flow
	 *         execution.
	 * @throws EventNotSupportedException if the signaled event does not map to
	 *         any state transitions in the state.
	 * @throws IllegalStateException if the flow execution is not active and
	 *         thus no longer processing events.
	 */
	public ViewDescriptor signalEvent(Event event) throws EventNotSupportedException, IllegalStateException;

	/**
	 * Called by a controller to restore this execution's state after
	 * deserialization if neccessary.
	 * @param flowLocator the locator
	 * @param listeners the set of listeners that should be notified of
	 *        lifecycle events in this flow execution
	 */
	public void rehydrate(FlowLocator flowLocator, FlowExecutionListener[] listeners);

}