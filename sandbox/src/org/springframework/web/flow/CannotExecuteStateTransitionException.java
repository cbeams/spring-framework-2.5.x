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
 * Thrown if the flow system cannot execute a transition from one state to
 * another.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class CannotExecuteStateTransitionException extends FlowNavigationException {

	/**
	 * The state in which the exception occured.
	 */
	private TransitionableState sourceState;

	/**
	 * The transition that could not be executed.
	 */
	private Transition transition;

	/**
	 * Create a new state transition execution exception.
	 * @param sourceState the source state of the transition
	 * @param cause the underlying cause of this exception
	 */
	public CannotExecuteStateTransitionException(TransitionableState sourceState, Throwable cause) {
		super(sourceState.getFlow(), "Could not execute a transition from state '" + sourceState
				+ "' to another state in flow '" + sourceState.getFlow().getId() + "'", cause);
		this.sourceState = sourceState;
	}

	/**
	 * Creates a new state transition execution exception.
	 * @param sourceState the source state
	 * @param message the message of what went wrong.
	 */
	public CannotExecuteStateTransitionException(TransitionableState sourceState, String message) {
		super(sourceState.getFlow(), message);
		this.sourceState = sourceState;
	}

	/**
	 * Create a new state transition execution exception.
	 * @param transition the transition that was executing
	 * @param cause the underlying cause of this exception
	 */
	public CannotExecuteStateTransitionException(Transition transition, Throwable cause) {
		super(transition.getSourceState().getFlow(), "Could not execute transition from state '"
				+ transition.getSourceState().getId() + "' to state '" + transition.getTargetStateId() + "' in flow '"
				+ transition.getSourceState().getFlow() + "'", cause);
		this.sourceState = transition.getSourceState();
		this.transition = transition;
	}

	/**
	 * Returns the state in which the exception occured.
	 */
	public TransitionableState getSourceState() {
		return sourceState;
	}

	/**
	 * Returns the transition that could not be executed.
	 */
	public Transition getTransition() {
		return transition;
	}
}