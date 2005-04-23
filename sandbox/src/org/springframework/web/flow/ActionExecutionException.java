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
	 * The action that threw an exception while executing.
	 */
	private State state;

	/**
	 * The action that threw an exception while executing.
	 */
	private Action action;

	/**
	 * Action execution attributes.
	 */
	private AttributeSource actionAttributes;

	/**
	 * @param state
	 * @param attributes
	 * @param cause
	 */
	public ActionExecutionException(State state, AnnotatedAction attributes, Throwable cause) {
		this(state, attributes.getTargetAction(), attributes, cause);
	}

	/**
	 * Create a new action execution exception.
	 */
	public ActionExecutionException(State state, Action action, AttributeSource actionAttributes, Throwable cause) {
		super("Exception thrown executing action '" + action + "' in state '" + state.getId() + "' of flow '"
				+ state.getFlow().getId() + "'", cause);
		this.state = state;
		this.action = action;
		this.actionAttributes = actionAttributes;
	}

	/**
	 * Create a new action execution exception.
	 */
	public ActionExecutionException(State state, Action action, AttributeSource actionAttributes, String message, Throwable cause) {
		super(message, cause);
		this.state = state;
		this.action = action;
		this.actionAttributes = actionAttributes;
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
	 * @return
	 */
	public AttributeSource getActionAttributes() {
		return actionAttributes;
	}
}