package org.springframework.web.flow;

public abstract class StateEventVetoedException extends FlowNavigationException {

	private State state;
	
	public StateEventVetoedException(State state, String message) {
		super(state.getFlow(), message);
		this.state = state;
	}

	public StateEventVetoedException(State state, String message, Throwable cause) {
		super(state.getFlow(), message, cause);
		this.state = state;
	}
	
	public State getState() {
		return state;
	}
}