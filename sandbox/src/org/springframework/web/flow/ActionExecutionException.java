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
 * Thrown if an unhandled, uncoverable exception is thrown when an action is
 * executed in an action state.
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionExecutionException extends RuntimeException {

	/**
	 * Create a new action execution exception.
	 * @param state The action state that was executing the action
	 * @param action The action that generated an exception
	 * @param cause The underlying cause of this exception
	 */
	public ActionExecutionException(ActionState state, ActionState.NamedAction action, Throwable cause) {
		super("Executing action '" + action + "' in state '" + state.getId() + "' of flow '" + state.getFlow().getId()
				+ "' threw an unrecoverable exception", cause);
	}
}