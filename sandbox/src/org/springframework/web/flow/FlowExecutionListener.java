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
 * Interface to be implemented by objects that wish to listen and respond to the
 * lifecycle of a FlowExecution.
 * <p>
 * An 'observer' that is very aspect like, allowing you to insert 'cross
 * cutting' behavior at well-defined points within a flow execution lifecycle.
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionListener {

	/**
	 * Called when a new flow execution was started.
	 * @param context Source of the event
	 */
	public void started(FlowExecutionContext context);

	/**
	 * Called when a new client HTTP request is submitted to manipulate this
	 * flow execution.
	 * @param context Source of the event
	 * @param request The request that was submitted
	 */
	public void requestSubmitted(FlowExecutionContext context, Event triggeringEvent);

	/**
	 * Called when a new client HTTP request is processed.
	 * @param context Source of the event
	 * @param request The request that was processed
	 */
	public void requestProcessed(FlowExecutionContext context, Event triggeringEvent);

	/**
	 * Called when an event is signaled in a state, but prior to a state
	 * transition.
	 * @param context Source of the event
	 * @param eventId The event that was signaled
	 */
	public void eventSignaled(FlowExecutionContext context, Event event);

	/**
	 * Called when a state transitions, after the transition occurs.
	 * @param context Source of the event
	 * @param previousState <i>From </i> state of the transition
	 * @param newState <i>To </i> state of the transition
	 */
	public void stateTransitioned(FlowExecutionContext context, State previousState, State newState);

	/**
	 * Called when a sub flow is spawned.
	 * @param context Source of the event
	 */
	public void subFlowSpawned(FlowExecutionContext context);

	/**
	 * Called when a sub flow is ended.
	 * @param context Source of the event
	 * @param endedSession Ending sub flow session
	 */
	public void subFlowEnded(FlowExecutionContext context, FlowSession endedSession);

	/**
	 * Called when the flow execution terminates.
	 * @param context Source of the event
	 * @param endedRootFlowSession Ending root flow session
	 */
	public void ended(FlowExecutionContext context, FlowSession endedRootFlowSession);
}