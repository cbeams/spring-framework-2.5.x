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

/**
 * Mutable control interface for states to use to manipulate the state of an
 * ongoing flow execution. Invoked internally by the various state types when
 * they are entered.
 * @author Keith Donald
 */
public interface StateContext extends FlowExecutionContext {

	/**
	 * Update the current state of the ongoing flow execution managed in this
	 * context.
	 * @param state The new current state
	 */
	public void setCurrentState(State state) throws IllegalStateException;

	/**
	 * Update the last event that occured in the ongoing flow execution managed
	 * in this context
	 * @param lastEvent The last event that occured
	 */
	public void setLastEvent(Event lastEvent);

	/**
	 * Return the active flow session, the local client instance of a single
	 * flow, the one at the top of the execution stack.
	 * @return The active flow session
	 */
	public FlowSession getActiveFlowSession() throws IllegalStateException;

	/**
	 * End the active flow session.
	 * @return The ended session.
	 */
	public FlowSession endActiveFlowSession() throws IllegalStateException;

	/**
	 * Spawn a session for the provided flow definition as a subflow, activating
	 * it and parameterizing it with the provided sub flow input.
	 * @param subFlow The subflow
	 * @param subFlowInput the subflow input attributes
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 */
	public ViewDescriptor spawn(Flow subFlow, Map subFlowInput);
}
