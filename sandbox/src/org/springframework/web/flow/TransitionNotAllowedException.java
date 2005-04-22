package org.springframework.web.flow;

/**
 * Thrown when a transition is not allowed because a precondition test failed.
 * 
 * @author Keith Donald
 */
public class TransitionNotAllowedException extends CannotExecuteStateTransitionException {

	private TransitionCriteria failedPrecondition;

	/**
	 * Creates a transition not allowed exception
	 * @param failedPrecondition the condition that failed, disallowing the transition
	 * @param transition the transition
	 * @param cause the cause
	 */
	public TransitionNotAllowedException(TransitionCriteria failedPrecondition, Transition transition) {
		super(transition, "Transition not allowed from state '"
				+ transition.getSourceState().getId() + "' to state '" + transition.getTargetStateId() + "' in flow '"
				+ transition.getSourceState().getFlow().getId() + "' -- precondition failed: " + failedPrecondition);
		this.failedPrecondition = failedPrecondition;
	}

	/**
	 * Returns the precondition test that failed, disallowing this transition.
	 * 
	 * @return The precondition that disallowed this transition.
	 */
	public TransitionCriteria getFailedPrecondition() {
		return failedPrecondition;
	}
}
