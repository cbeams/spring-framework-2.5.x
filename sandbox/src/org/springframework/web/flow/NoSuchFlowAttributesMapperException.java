/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * @author Keith Donald
 */
public class NoSuchFlowAttributesMapperException extends FlowNavigationException {
	private SubFlowState state;

	/**
	 * @param flow
	 * @param state
	 * @param cause
	 */
	public NoSuchFlowAttributesMapperException(Flow flow, SubFlowState state, Throwable cause) {
		super(flow, cause);
		this.state = state;
	}

	public String getMessage() {
		return "No attribute mapper was found with id '" + state.getAttributesMapperId() + "' for sub flow state '" + state.getId()
				+ "' of flow '" + getFlow().getId() + "' -- programmer error?";
	}

}