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
public interface FlowSessionExecutionListener {

	/**
	 * Called when a new flow session execution was started.
	 * @param sessionExecution
	 */
	public void started(FlowSessionExecution sessionExecution);

	/**
	 * Called when a new client HTTP request is submitted to manipulate this
	 * session execution.
	 * @param sessionExecution
	 * @param request
	 */
	public void requestSubmitted(FlowSessionExecution sessionExecution, HttpServletRequest request);

	/**
	 * Called when a new client HTTP request is processed.
	 * @param sessionExecution
	 * @param request
	 */
	public void requestProcessed(FlowSessionExecution sessionExecution, HttpServletRequest request);

	/**
	 * Called when an event is signaled in a state, prior to a state transition.
	 * @param sessionExecution
	 * @param eventId
	 */
	public void eventSignaled(FlowSessionExecution sessionExecution, String eventId);

	/**
	 * Called when a state transitions, after the transition occurs.
	 * @param sessionExecution
	 * @param previousState
	 * @param newState
	 */
	public void stateTransitioned(FlowSessionExecution sessionExecution, AbstractState previousState,
			AbstractState newState);

	/**
	 * Called when a sub flow is spawned.
	 * @param sessionExecution
	 */
	public void subFlowSpawned(FlowSessionExecution sessionExecution);

	/**
	 * Called when a sub flow is ended.
	 * @param sessionExecution
	 * @param endedSession
	 */
	public void subFlowEnded(FlowSessionExecution sessionExecution, FlowSession endedSession);

	public void ended(FlowSessionExecution sessionExecution, FlowSession endedRootFlowSession);

}