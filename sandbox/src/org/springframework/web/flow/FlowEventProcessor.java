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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles flow events signaled by the web-tier in response to web-controller
 * requests. There are two major different types of event processing operations:
 * <p>
 * <ol>
 * <li>1. A start operation, requesting the creation of a new session for the
 * Flow definition associated with this processor. A start operation transitions
 * the new flow session to its starting state.
 * <li>2. An event execution operation, signaling the occurence of a event in
 * the current state of an existing flow, submitting it for processing.
 * Processing the event occurence causes the current state of the flow to
 * transition to a new state.
 * 
 * This interface is everything web-tier controllers need to inteface with the
 * web flow system. This is their entry point.
 * 
 * @author Keith Donald
 */
public interface FlowEventProcessor {

	/**
	 * Start a new session for this flow. This will cause the flow session to
	 * enter its start state.
	 * 
	 * @param request the client http request
	 * @param response the server http response
	 * @param input optional input attributes to be passed to the new flow
	 *        session, placed in 'flow scope'
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the start event execution.
	 * @throws IllegalStateException if the event processor has not been
	 *         configured with a valid start state.
	 */
	public FlowSessionExecutionStartResult start(HttpServletRequest request, HttpServletResponse response, Map input)
			throws IllegalStateException;

	/**
	 * Start a new session for this flow, but start it in a specified state, not
	 * the default start state.
	 * 
	 * @param stateId The id of the state to start with. This must be a
	 *        TransitionableState
	 * @param request the client http request
	 * @param response the server http response
	 * @param input optional input attributes to be passed to the new flow
	 *        session, placed in 'flow scope'
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the start event execution.
	 * @throws IllegalStateException if the event processor has not been
	 *         configured with a valid start state.
	 * @throws IllegalArgumentException if the stateId is not a
	 *         TransitionableState
	 */
	public FlowSessionExecutionStartResult resume(String stateId, HttpServletRequest request,
			HttpServletResponse response, Map inputAttributes) throws IllegalStateException;

	/**
	 * Execute the event identified by <code>eventId</code> in the state
	 * identified by <code>stateId</code>
	 * 
	 * @param eventId The id of the event to execute (e.g 'submit', 'next',
	 *        'back')
	 * @param stateId The id of the state to execute this event in (e.g
	 *        'viewDetails')
	 * @param sessionExecutionStack The session execution stack, tracking any
	 *        suspended parent flows that spawned this flow (as a subflow)
	 * @param request the client http request
	 * @param response the server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 * @throws FlowNavigationException if the <code>eventId</code> is not a
	 *         valid event for the state identified by <code>stateId</code>,
	 *         or if the <code>stateId</code> does not map to a valid flow
	 *         state.
	 */
	public ViewDescriptor execute(String eventId, String stateId, FlowSessionExecutionInfo sessionExecution,
			HttpServletRequest request, HttpServletResponse response) throws FlowNavigationException;

}