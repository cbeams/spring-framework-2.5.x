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

import java.util.Map;

/**
 * Mutable control interface for states to use to manipulate the state of an
 * ongoing flow execution request. Used internally by the various state types when
 * they are entered.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface StateContext extends RequestContext {

	/**
	 * Update the current state of the ongoing flow execution.
	 * @param state the new current state
	 */
	public void setCurrentState(State state) throws IllegalStateException;

	/**
	 * Update the last event that occured in the ongoing flow execution.
	 * @param lastEvent the last event that occured
	 */
	public void setLastEvent(Event lastEvent);

	/**
	 * Get the flow session currently active in the flow execution.
	 * @throws IllegalStateException when the flow execution is not active
	 */
	public FlowSession getActiveFlowSession() throws IllegalStateException;

	/**
	 * Get the flow session of the parent flow of the flow currently
	 * active in the flow execution.
	 * @throws IllegalStateException when the flow execution is not active
	 */
	public FlowSession getParentFlowSession() throws IllegalStateException;

	/**
	 * End the active flow session.
	 * @return the ended session
	 */
	public FlowSession endActiveFlowSession() throws IllegalStateException;

	/**
	 * Spawn the provided flow definition as a subflow, activating
	 * it and parameterizing it with the provided sub flow input.
	 * @param subFlow the subflow
	 * @param subFlowInput the subflow input attributes
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the spawned subflow
	 */
	public ViewDescriptor spawn(Flow subFlow, Map subFlowInput);
}
