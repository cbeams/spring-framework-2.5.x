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

import javax.servlet.http.HttpServletRequest;

/**
 * Interface to be implemented by objects that wish to listen and respond to the
 * lifecycle of a FlowExecution.
 * <p>
 * An 'observer' that is very "aspect" like, allowing you to insert 'cross
 * cutting' behaivior at well-defined points within a flow execution lifecycle.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionListener {

	/**
	 * Called when a new flow execution was started.
	 * @param flowExecution Source of the event
	 */
	public void started(FlowExecution flowExecution);

	/**
	 * Called when a new client HTTP request is submitted to manipulate this
	 * flow execution.
	 * @param flowExecution Source of the event
	 * @param request The request that was submitted
	 */
	public void requestSubmitted(FlowExecution flowExecution, HttpServletRequest request);

	/**
	 * Called when a new client HTTP request is processed.
	 * @param flowExecution Source of the event
	 * @param request The request that was processed
	 */
	public void requestProcessed(FlowExecution flowExecution, HttpServletRequest request);

	/**
	 * Called when an event is signaled in a state, but prior to a state transition.
	 * @param flowExecution Source of the event
	 * @param eventId The event that was signaled
	 */
	public void eventSignaled(FlowExecution flowExecution, String eventId);

	/**
	 * Called when a state transitions, after the transition occurs.
	 * @param flowExecution Source of the event
	 * @param previousState <i>From </i> state of the transition
	 * @param newState <i>To </i> state of the transition
	 */
	public void stateTransitioned(FlowExecution flowExecution, AbstractState previousState, AbstractState newState);

	/**
	 * Called when a sub flow is spawned.
	 * @param flowExecution Source of the event
	 */
	public void subFlowSpawned(FlowExecution flowExecution);

	/**
	 * Called when a sub flow is ended.
	 * @param flowExecution Source of the event
	 * @param endedSession Ending sub flow session
	 */
	public void subFlowEnded(FlowExecution flowExecution, FlowSession endedSession);

	/**
	 * Called when the flow execution terminates.
	 * @param flowExecution Source of the event
	 * @param endedRootFlowSession Ending root flow session
	 */
	public void ended(FlowExecution flowExecution, FlowSession endedRootFlowSession);

}