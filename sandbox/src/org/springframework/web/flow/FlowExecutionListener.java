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
 * Interface to be implemented by objects that wish to listen and respond to the
 * lifecycle of a FlowExecution.
 * <p>
 * An 'observer' that is very aspect like, allowing you to insert 'cross
 * cutting' behavior at well-defined points within a flow execution lifecycle.
 * 
 * @see org.springframework.web.flow.FlowExecution
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionListener {

	/**
	 * Called when a new flow execution was started.
	 * @param context source of the event
	 */
	public void started(RequestContext context);

	/**
	 * Called when a new client request is submitted to manipulate this
	 * flow execution.
	 * @param context the source of the event, with an 'orignatingEvent' property for access the request event
	 */
	public void requestSubmitted(RequestContext context);

	/**
	 * Called when a client request has completed processing.
	 * @param context the source of the event, with an 'orignatingEvent' property for access the request event
	 */
	public void requestProcessed(RequestContext context);

	/**
	 * Called when an event is signaled in a state, but prior to a state
	 * transition.
	 * @param context the source of the event, with a 'lastEvent' property for accessing the signaled event
	 */
	public void eventSignaled(RequestContext context);

	/**
	 * Called when a state transitions, after the transition occurs.
	 * @param context the source of the event
	 * @param previousState <i>from</i> state of the transition
	 * @param newState <i>to</i> state of the transition
	 */
	public void stateTransitioned(RequestContext context, State previousState, State newState);

	/**
	 * Called when a sub flow is spawned.
	 * @param context the source of the event
	 */
	public void subFlowSpawned(RequestContext context);

	/**
	 * Called when a sub flow has ended.
	 * @param context the source of the event
	 * @param endedSession ending sub flow session
	 */
	public void subFlowEnded(RequestContext context, FlowSession endedSession);

	/**
	 * Called when the flow execution terminates.
	 * @param context the source of the event
	 * @param endedRootFlowSession ending root flow session
	 */
	public void ended(RequestContext context, FlowSession endedRootFlowSession);
}