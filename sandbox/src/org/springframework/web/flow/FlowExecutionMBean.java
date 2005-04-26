/*
 * Copyright 2002-2005 the original author or authors.
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

/**
 * Provides management information about an ongoing flow execution.
 * <p>
 * A typical use case of this interface would be a JMX MBean to monitor
 * flow execution.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowExecutionMBean {

	/**
	 * Returns the timestamp on which this execution was created.
	 * @return the creation timestamp
	 */
	public long getCreationTimestamp();

	/**
	 * Returns the time in milliseconds this flow execution has been active, or
	 * 0 if not active.
	 * @return the flow execution up time
	 */
	public long getUptime();

	/**
	 * Return a display string suitable for logging/printing in a console
	 * containing info about this flow execution.
	 * @return the flow execution caption
	 */
	public String getCaption();

	/**
	 * Is the flow execution active?
	 * @return true if active, false if terminated or not yet started
	 */
	public boolean isActive();

	/**
	 * Get the id of the active flow definition.
	 * @return the active flow id, e.g. "editContact"
	 * @throws IllegalStateException this execution is not active
	 */
	public String getActiveFlowId() throws IllegalStateException;

	/**
	 * Return the qualified id of the executing flow, taking into account any
	 * nesting parent flows. For example, <code>registerUser.editContacts.editContact</code>.
	 * @return the qualified active flow id
	 * @throws IllegalStateException this execution is not active
	 */
	public String getQualifiedActiveFlowId() throws IllegalStateException;

	/**
	 * Get a string array stack of executing flow ids, with the active flow at
	 * the top (first element) of the stack.
	 * @throws IllegalStateException this execution is not active
	 */
	public String[] getFlowIdStack() throws IllegalStateException;

	/**
	 * Return the id of the root level flow definition. May be the same as the
	 * active flow id if the active flow is the root flow.
	 * @return the root flow id
	 */
	public String getRootFlowId();

	/**
	 * Are we currently in the root flow?
	 * @return whether we're in the root flow
	 */
	public boolean isRootFlowActive();

	/**
	 * Returns the id of the current state of this flow execution.
	 * @throws IllegalStateException this execution is not active
	 */
	public String getCurrentStateId() throws IllegalStateException;

	/**
	 * Returns the id of the last event signaled within this flow execution.
	 * @return the last event ud, or <code>null</code> if no valid event has
	 *         been signaled so far
	 */
	public String getLastEventId();

	/**
	 * Returns the timestamp noting when the last request to manipulate this
	 * executing flow was processed.
	 * @return the timestamp of the last client request
	 */
	public long getLastRequestTimestamp();
}