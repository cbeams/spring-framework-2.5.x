/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.io.Serializable;

/**
 * @author Keith Donald
 */
public interface FlowSessionExecutionInfo extends AttributesAccessor, Serializable {

	public static String FLOW_SESSION_EXECUTION_INFO_ATTRIBUTE_NAME = "flowSessionExecutionInfo";

	/**
	 * Return the id of this flow session execution. This is not a human
	 * readable flow definition ID, but a system generated session key.
	 * @return
	 */
	public String getId();

	/**
	 * Return a display string suitable for logging/printing in a console
	 * containing info about this session execution.
	 * @return
	 */
	public String getCaption();

	/**
	 * Is the flow session execution active?
	 * @return
	 */
	public boolean isActive();

	/**
	 * Are we currently in the root flow? There can be any depth of nested
	 * subflows below this, but sometimes the first subflow below the root may
	 * require special treatment.
	 * @return whether we're in the root flow
	 */
	public boolean isRootFlowActive();

	/**
	 * Get the id of the active flow definition.
	 * @return
	 */
	public String getActiveFlowId();

	/**
	 * Return the qualified id of the executing flow, taking into account any
	 * nesting parent flows. For example,
	 * <code>RegisterUser.EditContacts.EditContact</code>.
	 * @return
	 */
	public String getQualifiedActiveFlowId();

	/**
	 * Get a string array stack of executing flow ids, with the active flow at
	 * the top (first element) of the stack.
	 * @return
	 */
	public String[] getFlowIdStack();

	/**
	 * @return
	 */
	public String getCurrentStateId();

	/**
	 * Returns the <code>eventId</code> of the current event signaled for this
	 * flow session execution.
	 * @return The id of the current event
	 */
	public String getLastEventId();

	/**
	 * Returns the timestamp noting when the last event was signaled.
	 * @return The timestamp of the last event occurance
	 */
	public long getLastEventTimestamp();

	/**
	 * Does this flow id exist in this session? (That is it is either active or
	 * suspended)
	 * @return
	 */
	public boolean exists(String flowId);

	/**
	 * What is the status of this flow id in this session?
	 * @param flowId
	 * @return The status
	 */
	public FlowSessionStatus getStatus(String flowId) throws IllegalArgumentException;
	
}