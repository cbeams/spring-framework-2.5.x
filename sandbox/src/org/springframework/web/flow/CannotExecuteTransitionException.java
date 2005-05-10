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
public class CannotExecuteTransitionException extends FlowNavigationException {

	/**
	 * The transition that could not be executed.
	 */
	private Transition transition;

	/**
	 * Create a new state transition execution exception.
	 * @param transition the transition that was executing
	 * @param cause the underlying cause of this exception
	 */
	public CannotExecuteTransitionException(Transition transition, Throwable cause) {
		super(transition.getSourceState().getFlow(), "Could not execute transition from state '"
				+ transition.getSourceState().getId() + "' in flow '"
				+ transition.getSourceState().getFlow().getId() + "'", cause);
		this.transition = transition;
	}

	/**
	 * Create a new state transition execution exception.
	 * @param transition the transition that was executing
	 * @param message a descriptive message
	 */
	public CannotExecuteTransitionException(Transition transition, String message) {
		super(transition.getSourceState().getFlow(), message);
		this.transition = transition;
	}

	/**
	 * Returns the transition that could not be executed.
	 */
	public Transition getTransition() {
		return transition;
	}
}