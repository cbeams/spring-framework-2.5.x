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
 * Interface to be implemented by objects that can listen to the operation of a
 * Flow object.
 * @author Keith Donald
 */
public interface FlowExecutionListener {

	/**
	 * Called when a new flow session execution was started.
	 * @param flowExecution
	 */
	public void started(FlowExecution flowExecution);

	/**
	 * Called when a new client HTTP request is submitted to manipulate this
	 * session execution.
	 * @param flowExecution
	 * @param request
	 */
	public void requestSubmitted(FlowExecution flowExecution, HttpServletRequest request);

	/**
	 * Called when a new client HTTP request is processed.
	 * @param flowExecution
	 * @param request
	 */
	public void requestProcessed(FlowExecution flowExecution, HttpServletRequest request);

	/**
	 * Called when an event is signaled in a state, prior to a state transition.
	 * @param flowExecution
	 * @param eventId
	 */
	public void eventSignaled(FlowExecution flowExecution, String eventId);

	/**
	 * Called when a state transitions, after the transition occurs.
	 * @param flowExecution
	 * @param previousState
	 * @param newState
	 */
	public void stateTransitioned(FlowExecution flowExecution, AbstractState previousState,
			AbstractState newState);

	/**
	 * Called when a sub flow is spawned.
	 * @param flowExecution
	 */
	public void subFlowSpawned(FlowExecution flowExecution);

	/**
	 * Called when a sub flow is ended.
	 * @param flowExecution
	 * @param endedSession
	 */
	public void subFlowEnded(FlowExecution flowExecution, FlowSession endedSession);

	public void ended(FlowExecution flowExecution, FlowSession endedRootFlowSession);

}