/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Provides management information about the current state of a ongoing flow
 * execution.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionInfo extends AttributesAccessor, Serializable {

	public static String ATTRIBUTE_NAME = "flowExecutionInfo";

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
	 * Return the id of the root level flow definition. May be the same as the
	 * active flow id if the active flow is the root flow.
	 * @return The root flow id
	 */
	public String getRootFlowId();

	/**
	 * Are we currently in the root flow? There can be any depth of nested
	 * subflows below this, but sometimes the first subflow below the root may
	 * require special treatment.
	 * @return whether we're in the root flow
	 */
	public boolean isRootFlowActive();

	/**
	 * @return The id of the current state of this flow session execution.
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

	/**
	 * Add a flow execution listener; the added listener will receive callbacks
	 * on events occuring in this flow execution.
	 * @param listener The execution listener to add.
	 */
	public void addFlowExecutionListener(FlowExecutionListener listener);
	
	/**
	 * Register given collection of flow execution listeners with this flow execution.
	 * The added listeners will receive callbacks on events occuring in this flow
	 * execution.
	 * @param listeners The collection of listeners to add.
	 */
	public void addAllFlowExecutionListeners(FlowExecutionListener[] listeners);

	/**
	 * Remove an existing flow execution listener; the removed listener will no
	 * longer receive callbacks and if left unreferenced will be eligible for
	 * garbage collection.
	 * @param listener The execution listener to remove.
	 */
	public void removeFlowExecutionListener(FlowExecutionListener listener);

	/**
	 * Returns an iterator looping over the list of listeners registered with
	 * this flow execution.
	 */
	public Iterator getFlowExecutionListenersIterator();
}