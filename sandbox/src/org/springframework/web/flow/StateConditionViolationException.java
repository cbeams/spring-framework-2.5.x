package org.springframework.web.flow;

public class StateConditionViolationException extends StateEventVetoedException {

	public StateConditionViolationException(State state, String message) {
		super(state, message);
	}

	public StateConditionViolationException(State state, String message, Throwable cause) {
		super(state, message, cause);
	}
}
