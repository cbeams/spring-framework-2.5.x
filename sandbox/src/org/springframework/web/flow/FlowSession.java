package org.springframework.web.flow;

import org.springframework.binding.AttributeSource;

public interface FlowSession {

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
	 * @return
	 */
	public FlowSession getParent();
	
	/**
	 * @return
	 */
	public boolean hasParent();
	
	/**
	 * Return the session attributes -- "flow scope".
	 * @return the attributes
	 */
	public AttributeSource getAttributes();
}