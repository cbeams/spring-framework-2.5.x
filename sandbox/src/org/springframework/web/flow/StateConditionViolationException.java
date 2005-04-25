package org.springframework.web.flow;

public class StateConditionViolationException extends StateEventVetoedException {

	public StateConditionViolationException(Flow flow, State state, String message) {
		super(flow, state, message);
	}

	public StateConditionViolationException(Flow flow, State state, String message, Throwable cause) {
		super(flow, state, message, cause);
	}
}
