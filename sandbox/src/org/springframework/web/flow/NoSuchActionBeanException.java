/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * @author Keith Donald
 */
public class NoSuchActionBeanException extends FlowNavigationException {
	private ActionState state;

	/**
	 * @param flow
	 * @param state
	 * @param cause
	 */
	public NoSuchActionBeanException(Flow flow, ActionState state, Throwable cause) {
		super(flow, cause);
		this.state = state;
	}

	public String getMessage() {
		return "No action bean could be retrieved with id '" + state.getActionBeanName() + "' for action state '" + state.getId()
				+ "' of flow '" + getFlow().getId() + "' -- programmer error?";
	}

}