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
package org.springframework.web.flow.execution;

import org.springframework.web.flow.FlowNavigationException;
import org.springframework.web.flow.State;

/**
 * Exception thrown to veto entering of a state in a web flow.
 *  
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class EnterStateVetoException extends FlowNavigationException {

	private State state;
	
	/**
	 * Create a new enter state veto exception.
	 * @param state the state for which entering is vetoed
	 * @param message a descriptive message
	 */
	public EnterStateVetoException(State state, String message) {
		super(state.getFlow(), message);
		this.state = state;
	}

	/**
	 * Create a new enter state veto exception.
	 * @param state the state for which entering is vetoed
	 * @param message a descriptive message
	 * @param cause the underlying cause
	 */
	public EnterStateVetoException(State state, String message, Throwable cause) {
		super(state.getFlow(), message, cause);
		this.state = state;
	}
	
	/**
	 * Returns the state for which entering was vetoed.
	 */
	public State getState() {
		return state;
	}
}