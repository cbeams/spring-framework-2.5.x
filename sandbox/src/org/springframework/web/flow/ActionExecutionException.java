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

import org.springframework.binding.AttributeSource;
import org.springframework.core.NestedRuntimeException;

/**
 * Thrown if an unhandled, uncoverable exception is thrown when an action is
 * executed in an action state.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionExecutionException extends NestedRuntimeException {

	/**
	 * The state that was active when the exception occured.
	 */
	private State state;

	/**
	 * The action that threw an exception while executing.
	 */
	private Action action;

	/**
	 * Action usage properties.
	 */
	private AttributeSource actionProperties;

	/**
	 * Create a new action execution exception.
	 * @param state the active state
	 * @param action the action that generated an unrecoverable exception
	 * @param cause the underlying cause
	 */
	public ActionExecutionException(State state, AnnotatedAction action, Throwable cause) {
		this(state, action.getTargetAction(), action, cause);
	}

	/**
	 * Create a new action execution exception.
	 * @param state the active state
	 * @param action the action that generated an unrecoverable exception
	 * @param actionProperties action usage properties
	 * @param cause the underlying cause
	 */
	public ActionExecutionException(State state, Action action, AttributeSource actionProperties, Throwable cause) {
		this(state, action, actionProperties,
				"Exception thrown executing action '" + action + "' in state '" + state.getId() + "' of flow '"
				+ state.getFlow().getId() + "'", cause);
	}

	/**
	 * Create a new action execution exception.
	 * @param state the active state
	 * @param action the action that generated an unrecoverable exception
	 * @param actionProperties action usage properties
	 * @param message a descriptive message
	 * @param cause the underlying cause
	 */
	public ActionExecutionException(State state, Action action, AttributeSource actionProperties, String message, Throwable cause) {
		super(message, cause);
		this.state = state;
		this.action = action;
		this.actionProperties = actionProperties;
	}

	/**
	 * Returns information about the action state that invoked the action.
	 * @return the action state
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns information about the action that threw an exception when
	 * executed.
	 * @return the failing action
	 */
	public Action getAction() {
		return action;
	}
	
	/**
	 * Returns the properties (attributes) associated with the action during execution.
	 */
	public AttributeSource getActionProperties() {
		return actionProperties;
	}
}