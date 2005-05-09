package org.springframework.web.flow.support;

import org.springframework.web.flow.State;
import org.springframework.web.flow.execution.EnterStateVetoException;

public class StateConditionViolationException extends EnterStateVetoException {

	public StateConditionViolationException(State state, String message) {
		super(state, message);
	}

	public StateConditionViolationException(State state, String message, Throwable cause) {
		super(state, message, cause);
	}
}
