package org.springframework.web.flow;

/**
 * Exception thrown to veto entering of a state in a web flow.
 *  
 * @autor Keith Donald
 * @author Erwin Vervaet
 */
public abstract class EnterStateVetoException extends FlowNavigationException {

	private State state;
	
	/**
	 * Create a new enter state veto exception.
	 * @param state the state for which entering is vetoed
	 * @param message a descriptive message
	 */
	public EnterStateVetoException(State state, String message) {
		super(state.getFlow(), message);
		this.state = state;
	}

	/**
	 * Create a new enter state veto exception.
	 * @param state the state for which entering is vetoed
	 * @param message a descriptive message
	 * @param cause the underlying cause
	 */
	public EnterStateVetoException(State state, String message, Throwable cause) {
		super(state.getFlow(), message, cause);
		this.state = state;
	}
	
	/**
	 * Returns the state for which entering was vetoed.
	 */
	public State getState() {
		return state;
	}
}