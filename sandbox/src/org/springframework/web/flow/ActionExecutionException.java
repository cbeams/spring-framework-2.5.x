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
	 * The action executor that threw an exception while executing.
	 */
	private ActionState.ActionExecutor actionExecutor;

	/**
	 * Create a new action execution exception.
	 * @param message the message as to why execution failed
	 * @param cause the underlying cause of the exception, thrown by the action
	 */
	public ActionExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new action execution exception.
	 * @param actionExecutor the action executor that generated the exception
	 * @param cause the underlying cause of the exception, thrown by the action
	 */
	public ActionExecutionException(ActionState.ActionExecutor actionExecutor, Throwable cause) {
		super("Executing action '" + actionExecutor + "' in state '" + actionExecutor.getAction().getState().getId()
				+ "' of flow '" + actionExecutor.getAction().getState().getFlow().getId()
				+ "' threw an unrecoverable exception", cause);
		this.actionExecutor = actionExecutor;
	}

	/**
	 * Returns information about the action that threw an exception when
	 * executed.
	 * @returns the action
	 */
	public ActionStateAction getAction() {
		return actionExecutor.getAction();
	}
}