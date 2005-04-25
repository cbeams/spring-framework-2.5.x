package org.springframework.web.flow;

public abstract class StateEventVetoedException extends FlowNavigationException {

	private State state;
	
	public StateEventVetoedException(Flow flow, State state, String message) {
		super(flow, message);
		this.state = state;
	}

	public StateEventVetoedException(Flow flow, State state, String message, Throwable cause) {
		super(flow, message, cause);
		this.state = state;
	}
	
	public State getState() {
		return state;
	}
}