/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.flow;

import java.io.Serializable;

/**
 * Provides management information about the current state of a ongoing flow
 * execution.
 * 
 * <p>
 * A typical use case of this interface would be a JMX MBean to monitor flow
 * execution.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionInfo extends AttributesAccessor, Serializable {

	/**
	 * Return the id of this flow execution. This is not a human
	 * readable flow definition ID, but a system generated key.
	 */
	public String getId();

	/**
	 * Return a display string suitable for logging/printing in a console
	 * containing info about this flow execution.
	 */
	public String getCaption();

	/**
	 * Is the flow execution active?
	 */
	public boolean isActive();

	/**
	 * Get the id of the active flow definition.
	 */
	public String getActiveFlowId();

	/**
	 * Return the qualified id of the executing flow, taking into account any
	 * nesting parent flows. For example,
	 * <code>registerUser.editContacts.editContact</code>.
	 */
	public String getQualifiedActiveFlowId();

	/**
	 * Get a string array stack of executing flow ids, with the active flow at
	 * the top (first element) of the stack.
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
	 * @return The id of the current state of this flow execution.
	 */
	public String getCurrentStateId();

	/**
	 * Returns the <code>eventId</code> of the last event signaled for this
	 * flow execution.
	 * @return The id of the last event
	 */
	public String getLastEventId();

	/**
	 * Returns the timestamp noting when the last event was signaled.
	 * @return The timestamp of the last event occurance
	 */
	public long getLastEventTimestamp();

	/**
	 * Does this flow id exist in this execution?
	 */
	public boolean exists(String flowId);

	/**
	 * What is the status of specified flow in this flow execution?
	 */
	public FlowSessionStatus getStatus(String flowId) throws IllegalArgumentException;

}