/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * @author Keith Donald
 */
public class CannotExecuteStateTransitionException extends FlowNavigationException {

	private Transition transition;

	private String stateIdTransitioningFrom;

	/**
	 * @param flow
	 * @param currentStateId
	 * @param cause
	 */
	public CannotExecuteStateTransitionException(Flow flow, String currentStateId, Throwable cause) {
		super(flow, cause);
		this.stateIdTransitioningFrom = currentStateId;
	}

	/**
	 * @param transition
	 * @param flow
	 * @param currentStateId
	 * @param cause
	 */
	public CannotExecuteStateTransitionException(Transition transition, Flow flow, String currentStateId,
			Throwable cause) {
		super(flow, cause);
		this.transition = transition;
		this.stateIdTransitioningFrom = currentStateId;
	}

	public String getMessage() {
		if (transition != null) {
			return "Could not execute transition from state '" + stateIdTransitioningFrom + "' to state '"
					+ transition.getToState() + "' on event '" + transition.getId() + "' in flow '" + getFlow().getId()
					+ "' -- programmer error?";
		}
		else {
			return "Could not execute a transition from state '" + stateIdTransitioningFrom
					+ "' to another state in flow '" + getFlow().getId() + "'; no event was signaled! -- programmer error?";
		}
	}

}