/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * Subinterface of <code>FlowSessionExecutionInfo</code> that exposes mutable
 * operations. Designed for use by the <code>FlowSessionExecutionListener</code>,
 * which is a bit more privleged than pure for-management clients. This
 * interface may also be used in situations where other privileged objects need
 * access to flow definition configuration details during session execution.
 * 
 * Note: though these definitions are exposed to clients via this interface,
 * they should *not* be modified post application startup. Mutable operations on
 * mutable configuration operations should be treated as frozen after a flow's
 * session execution commences.
 * 
 * @author Keith Donald
 */
public interface FlowSessionExecution extends FlowSessionExecutionInfo, MutableAttributesAccessor {

	/**
	 * Returns this session execution's active flow definition.
	 * @return The active flow definition
	 */
	public Flow getActiveFlow();

	/**
	 * Returns this session execution's root flow definition.
	 * @return The root flow definition.
	 */
	public Flow getRootFlow();

	/**
	 * Returns this session execution's current state definition.
	 * @return the current state definition.
	 */
	public AbstractState getCurrentState();
}