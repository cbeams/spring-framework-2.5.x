package org.springframework.web.flow;

/**
 * A single client session instance for a <code>Flow</code> participating in a
 * <code>FlowExecution</code>. Also acts as a "flow-scope" data model.
 * <p>
 * The stack of executing flow sessions represents the complete state of an
 * ongoing flow execution.
 * <p>
 * A flow session will go through several statuses during its lifecycle.
 * Initially it will be {@link FlowSessionStatus#CREATED}. Once the flow
 * session is activated in a flow execution, it becomes
 * {@link FlowSessionStatus#ACTIVE}. If the flow session would spawn a subflow
 * session, it will become {@link FlowSessionStatus#SUSPENDED} until the subflow
 * returns (ends). When the flow session is ended by the flow execution,
 * its status becomes {@link FlowSessionStatus#ENDED}, ending its lifecycle.
 * <p>
 * Note that a flow <i>session</i> is in no way linked to an HTTP session! It
 * just uses the familiar "request/session" naming convention.
 * 
 * @see org.springframework.web.flow.FlowExecution
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowSession {
	
	/**
	 * Returns information about the ongoing flow execution that
	 * contains this flow session.
	 */
	public FlowExecutionInfo getFlowExecutionInfo();
	
	/**
	 * Returns the flow associated with this flow session.
	 */
	public Flow getFlow();

	/**
	 * Returns the state that is currently active in this flow session.
	 */
	public State getState();
	
	/**
	 * Returns the current status of this flow session.
	 */
	public FlowSessionStatus getStatus();

	/**
	 * Returns the parent flow session in the current flow execution,
	 * or <code>null</code> if there is not parent flow session.
	 */
	public FlowSession getParent();
	
	/**
	 * Returns whether this flow session is the root flow session in 
	 * the ongoing flow execution. The root flow session does not have
	 * a parent flow session. 
	 */
	public boolean isRoot();
	
	/**
	 * Return the session attributes -- "flow scope".
	 * @return the flow scope attributes
	 */
	public Scope getFlowScope();
}