/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * Thrown if the flow subsystem cannot execute a state transition.
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
	public CannotExecuteStateTransitionException(AbstractState state, Throwable cause) {
		super(state.getFlow(), cause);
		this.stateIdTransitioningFrom = state.getId();
	}

	/**
	 * @param transition
	 * @param flow
	 * @param currentStateId
	 * @param cause
	 */
	public CannotExecuteStateTransitionException(Transition transition, Throwable cause) {
		super(transition.getSourceState().getFlow(), cause);
		this.transition = transition;
	}

	public String getMessage() {
		if (transition != null) {
			return "Could not execute transition from state '" + transition.getSourceState().getId() + "' to state '"
					+ transition.getTargetStateId() + "' in flow '" + getFlow().getId() + "'";
		}
		else {
			return "Could not execute a transition from state '" + stateIdTransitioningFrom
					+ "' to another state in flow '" + getFlow().getId() + "'";
		}
	}

}