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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;

/**
 * A base super class for a state definition, associatable with any number of
 * Flow definitions. Types of states include action states, view states, subflow
 * states, and end states.
 * 
 * @author Keith Donald
 */
public abstract class AbstractState implements Serializable {

	public static final String CURRENT_STATE_ID_ATTRIBUTE = "_currentStateId";

	protected final Log logger = LogFactory.getLog(getClass());

	private String id;

	private Flow flow;

	public AbstractState(Flow flow, String id) {
		Assert.hasText(id, "The state must have a valid identifier");
		setFlow(flow);
		this.id = id;
	}

	protected void setFlow(Flow flow) {
		this.flow = flow;
	}

	public Flow getFlow() {
		return flow;
	}

	public boolean isTransitionable() {
		return false;
	}

	public boolean isViewState() {
		return false;
	}

	public boolean isActionState() {
		return false;
	}

	public boolean isSubFlowState() {
		return false;
	}

	public boolean isEndState() {
		return false;
	}

	public String getId() {
		return id;
	}

	protected FlowServiceLocator getFlowServiceLocator() {
		return getFlow().getServiceLocator();
	}

	public boolean equals(Object o) {
		if (!(o instanceof AbstractState)) {
			return false;
		}
		AbstractState s = (AbstractState)o;
		return id.equals(s.id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * Requesting entering of this state for the active (currently executing)
	 * flow session.
	 * @param flow The flow definition associated with the executing flow
	 *        session
	 * @param sessionExecutionStack The session execution stack, tracking the
	 *        current active flow session
	 * @param request The client http request
	 * @param response The server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 */
	public final ViewDescriptor enter(FlowSessionExecutionStack sessionExecution, HttpServletRequest request,
			HttpServletResponse response) {
		if (logger.isDebugEnabled()) {
			logger.debug("Entering state '" + this + "' in flow '" + sessionExecution.getActiveFlowId() + "'");
		}
		sessionExecution.setCurrentState(this);
		return doEnterState(sessionExecution, request, response);
	}

	/**
	 * Hook method to do any processing as a result of entering this state.
	 * @param flow The flow definition associated with the executing flow
	 *        session
	 * @param sessionExecutionStack The session execution stack, tracking the
	 *        current active flow session
	 * @param request The client http request
	 * @param response The server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 */
	protected abstract ViewDescriptor doEnterState(FlowSessionExecutionStack sessionExecutionStack,
			HttpServletRequest request, HttpServletResponse response);

	public String toString() {
		ToStringCreator creator = new ToStringCreator(this).append(id, getId());
		createToString(creator);
		return creator.toString();
	}

	protected void createToString(ToStringCreator creator) {

	}
}