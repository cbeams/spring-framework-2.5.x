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
 * Provides contextual information about an actively executing flow.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowContext {

	/**
	 * Returns the time at which this flow started executing.
	 * @return the creation timestamp
	 */
	public long getCreationTimestamp();

	/**
	 * Returns the root flow definition associated with this executing flow.
	 * @return the root flow definition
	 */
	public Flow getRootFlow();

	/**
	 * Returns the definition for the flow that is currently executing.
	 * @return the flow definition for the active session.
	 */
	public Flow getActiveFlow();

	/**
	 * Returns the current state of the executing flow.
	 * @return the current state
	 */
	public State getCurrentState();

	/**
	 * Returns the time in milliseconds this flow execution has been active, or
	 * 0 if not active.
	 * @return the flow execution up time
	 */
	public long getUptime();

	/**
	 * Returns the timestamp noting when the last request to manipulate this
	 * executing flow was processed.
	 * @return the timestamp of the last client request
	 */
	public long getLastRequestTimestamp();

	/**
	 * Returns the id of the last event that occured in this executing flow.
	 * @return the last event id
	 */
	public String getLastEventId();
	
	/**
	 * Returns a display string suitable for logging/printing in a console
	 * containing info about this executing flow.
	 * @return the flow execution caption
	 */
	public String getCaption();

	/**
	 * Returns the active flow session.
	 * @return the active flow session
	 * @throws IllegalStateException when this flow is not active
	 */
	public FlowSession getActiveSession() throws IllegalStateException;

	/**
	 * Is the flow execution active?
	 * @return true if active, false if terminated or not yet started
	 */
	public boolean isActive();

}