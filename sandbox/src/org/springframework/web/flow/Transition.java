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
 * @author Keith Donald
 */
public class Transition implements Serializable {
	private static final Log logger = LogFactory.getLog(Transition.class);

	public static final String EVENT_ID_ATTRIBUTE = "_eventId";

	private String id;

	private String toState;

	public Transition(String id, String toState) {
		Assert.notNull(id, "The id property is required");
		Assert.notNull(toState, "The toState property is required");
		this.id = id;
		this.toState = toState;
	}

	public String getId() {
		return id;
	}

	public String getToState() {
		return toState;
	}

	public ViewDescriptor execute(Flow flow, TransitionableState fromState, FlowSessionExecutionStack sessionExecution,
			HttpServletRequest request, HttpServletResponse response) {
		assertActiveFlow(sessionExecution, flow);
		updateCurrentStateIfNeccessary(sessionExecution, fromState);
		String qualifiedActiveFlowId = null;
		if (logger.isDebugEnabled()) {
			qualifiedActiveFlowId = sessionExecution.getQualifiedActiveFlowId();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Event '" + getId() + "' within state '" + fromState.getId() + "' for flow '"
					+ qualifiedActiveFlowId + "' signaled; processing...");
		}
		sessionExecution.setLastEventId(getId());

		if (flow.isLifecycleListenerSet()) {
			flow.getFlowLifecycleListener().flowEventSignaled(flow, getId(), fromState, sessionExecution, request);
		}

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Executing transition from state '" + fromState.getId() + "' to state '" + getToState()
						+ "' in flow '" + qualifiedActiveFlowId + "'");
			}

			ViewDescriptor descriptor = flow.getRequiredState(getToState()).enter(flow, sessionExecution, request,
					response);

			if (flow.isLifecycleListenerSet()) {
				flow.getFlowLifecycleListener().flowEventProcessed(flow, getId(), fromState, sessionExecution, request);
			}

			if (logger.isDebugEnabled()) {
				if (sessionExecution.isActive()) {
					logger.debug("Event '" + getId() + "' within last state '" + fromState.getId()
							+ "' for flow '" + qualifiedActiveFlowId
							+ "' was processed; as a result, the new state is '" + sessionExecution.getCurrentStateId()
							+ "' in flow '" + sessionExecution.getQualifiedActiveFlowId() + "'");
				}
				else {
					logger.debug("Event '" + getId() + "' within last state '" + fromState.getId()
							+ "' for flow '" + qualifiedActiveFlowId
							+ "' was processed; as a result, flow session execution has ended");
				}
			}
			return descriptor;
		}
		catch (NoSuchFlowStateException e) {
			throw new CannotExecuteStateTransitionException(this, flow, fromState.getId(), e);
		}
	}

	protected void assertActiveFlow(FlowSessionExecution sessionExecution, Flow flow) {
		if (!flow.getId().equals(sessionExecution.getActiveFlowId())) {
			throw new IllegalStateException("Assertion failed - the flow ID '" + flow.getId()
					+ "' must equal the active flow ID '" + sessionExecution.getActiveFlowId()
					+ "' for this flow session execution");
		}
	}

	protected void updateCurrentStateIfNeccessary(FlowSessionExecutionStack sessionExecution,
			TransitionableState fromState) {
		if (!fromState.getId().equals(sessionExecution.getCurrentStateId())) {
			if (logger.isInfoEnabled()) {
				logger.info("Event '" + getId() + "' in state '" + fromState.getId()
						+ "' was signaled by client; however the current flow session execution state is '"
						+ sessionExecution.getCurrentStateId() + "'; updating current state to '" + fromState.getId()
						+ "'");
			}
			sessionExecution.setCurrentStateId(fromState.getId());
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("id", id).append("toState", toState).toString();
	}
}