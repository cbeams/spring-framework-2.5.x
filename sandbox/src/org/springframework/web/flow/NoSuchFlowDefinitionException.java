/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * @author Keith Donald
 */
public class NoSuchFlowDefinitionException extends FlowException {

	private String flowId;

	/**
	 * @param flow
	 * @param state
	 * @param cause
	 */
	public NoSuchFlowDefinitionException(String flowId, Throwable cause) {
		super(cause);
	}

	public String getMessage() {
		return "No flow definition was found with id '" + flowId + "' -- programmer error?";
	}

}