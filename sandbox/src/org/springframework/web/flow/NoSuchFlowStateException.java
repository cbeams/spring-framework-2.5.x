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

import org.springframework.core.Styler;

/**
 * Thrown when a state could not be found in a flow on lookup by
 * <code>stateId</code>.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchFlowStateException extends FlowNavigationException {

	private String stateId;

	/**
	 * Create a new flow state navigation exception.
	 * @param flow the containing flow
	 * @param stateId the state id that cannot be found
	 */
	public NoSuchFlowStateException(Flow flow, String stateId) {
		this(flow, stateId, null);
	}

	/**
	 * Create a new flow state navigation exception.
	 * @param flow the containing flow
	 * @param stateId the state id that cannot be found
	 * @param cause the underlying cause of this exception
	 */
	public NoSuchFlowStateException(Flow flow, String stateId, Throwable cause) {
		super(flow, "No state with state id '" + stateId + "' exists for flow '" + flow.getId()
				+ "' -- valid states are " + Styler.call(flow.getStateIds())
				+ "-- likely programmer error, check your flow configuration", cause);
		this.stateId = stateId;
	}

	/**
	 * Returns the id of the state that was not found.
	 * @return the state id
	 */
	public String getStateId() {
		return stateId;
	}
}