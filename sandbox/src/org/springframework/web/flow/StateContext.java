package org.springframework.web.flow;

import java.util.Map;

/**
 * Mutable control interface for states to use to manipulate the state of an
 * ongoing flow execution request. Used internally by the various state types
 * when they are entered.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface StateContext extends RequestContext {
	
	/**
	 * Update the last event that occured in the executing flow.
	 * @param lastEvent the last event that occured
	 */
	public void setLastEvent(Event lastEvent);

	/**
	 * Update the last transition that executed in the executing flow.
	 * @param lastTransition the last transition that executed
	 */
	public void setLastTransition(Transition lastTransition);
	
	/**
	 * Set the current state of the flow execution linked to this request.
	 * @param state the current state
	 */
	public void setCurrentState(State state);
	
	/**
	 * Spawn a new flow session and activate it in the currently executing
	 * flow.  Also transitions the spawned flow to its start state.
	 * @param startState the state the new flow should start in
	 * @param input initial contents of the newly created flow session
	 * @return the newly created and activated flow session
	 * @throws IllegalStateException when the flow execution is not active
	 */
	public ViewDescriptor spawn(State startState, Map input) throws IllegalStateException;

	/**
	 * End the active flow session.
	 * @return the ended session
	 * @throws IllegalStateException when the flow execution is not active
	 */
	public FlowSession endActiveSession() throws IllegalStateException;
}
