/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * Thrown if a unhandled, uncoverable exception is thrown when an action is
 * executed in an action state.
 * @author Keith Donald
 */
public class ActionExecutionException extends RuntimeException {
	public ActionExecutionException(AbstractState state, Action action, Throwable cause) {
		super("Execution of action of class '" + action.getClass() + "' in state '" + state.getId() + "' of flow '"
				+ state.getFlow().getId() + "' threw a unrecoverable exception", cause);
	}
}
