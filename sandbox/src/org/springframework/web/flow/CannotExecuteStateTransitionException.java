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
 * Thrown if the flow subsystem cannot execute a state transition.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class CannotExecuteStateTransitionException extends FlowNavigationException {

	private String stateIdTransitioningFrom;

	private Transition transition;

	/**
	 * Create a new state transition exception.
	 * @param state The source state of the transition
	 * @param cause The underlying cause of this exception
	 */
	public CannotExecuteStateTransitionException(AbstractState state, Throwable cause) {
		super(state.getFlow(), cause);
		this.stateIdTransitioningFrom = state.getId();
	}
	
	/**
	 * Creates a new state transition exception.
	 * @param state The source state
	 * @param message The message of what went wrong.
	 */
	public CannotExecuteStateTransitionException(AbstractState state, String message) {
		super(state.getFlow(), message);
		this.stateIdTransitioningFrom = state.getId();
	}

	/**
	 * Create a new state transition exception.
	 * @param transition The transition that was executing
	 * @param cause The underlying cause of this exception
	 */
	public CannotExecuteStateTransitionException(Transition transition, Throwable cause) {
		super(transition.getSourceState().getFlow(), cause);
		this.transition = transition;
	}

	public String getMessage() {
		if (super.getMessage() != null) {
			return super.getMessage();
		}
		else {
			if (transition != null) {
				return "Could not execute transition from state '" + transition.getSourceState().getId()
						+ "' to state '" + transition.getTargetStateId() + "' in flow '" + getFlow().getId() + "'";
			}
			else {
				return "Could not execute a transition from state '" + stateIdTransitioningFrom
						+ "' to another state in flow '" + getFlow().getId() + "'";
			}
		}
	}
}