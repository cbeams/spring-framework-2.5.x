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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;

/**
 * Provides end state functionality for a web flow.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 */
public class EndState extends AbstractState {

	private String viewName;

	public EndState(String id) {
		super(id);
	}

	public EndState(String id, String viewName) {
		super(id);
		setViewName(viewName);
	}

	public EndState(Flow flow, String id) {
		super(flow, id);
	}

	public EndState(Flow flow, String id, String viewName) {
		super(flow, id);
		setViewName(viewName);
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	protected String getViewName() {
		return viewName;
	}

	public boolean isEndState() {
		return true;
	}

	protected ViewDescriptor doEnterState(FlowExecutionStack sessionExecution, HttpServletRequest request,
			HttpServletResponse response) {
		ViewDescriptor descriptor;
		if (getViewName() != null) {
			descriptor = new ViewDescriptor(getViewName(), sessionExecution.getAttributes());
		}
		else {
			descriptor = ViewDescriptor.NULL_OBJECT;
		}
		FlowSession endingFlowSession = sessionExecution.endActiveSession();
		Assert.isTrue(endingFlowSession.getCurrentState().equals(this),
				"The ending flow session's current state should always equal this end state, but it doesn't "
						+ "-- this should not happen");
		if (logger.isDebugEnabled()) {
			logger.debug("Session for flow '" + getFlow().getId() + "' ended, details=" + endingFlowSession);
		}
		if (sessionExecution.isActive()) {
			// session execution is still active, resume in parent
			if (logger.isDebugEnabled()) {
				logger.debug("Resuming parent flow '" + sessionExecution.getQualifiedActiveFlowId() + "' in state '"
						+ sessionExecution.getCurrentStateId() + "'");
			}
			Flow resumingParentFlow = sessionExecution.getActiveFlow();
			Assert.isInstanceOf(SubFlowState.class, sessionExecution.getCurrentState());
			SubFlowState resumingState = (SubFlowState)sessionExecution.getCurrentState();
			if (resumingState.getAttributesMapper() != null) {
				if (logger.isDebugEnabled()) {
					logger
							.debug("Messaging the configured attributes mapper to map subflow attributes back up to the resuming parent flow - "
									+ "the resuming parent flow will now have access to attributes passed up by the completed subflow");
				}
				resumingState.getAttributesMapper().mapToResumingParentFlow(endingFlowSession,
						sessionExecution.getActiveFlowSession());
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("No attributes mapper is configured for the resuming state '" + getId()
							+ "' - note: as a result, no attributes in the ending subflow '"
							+ endingFlowSession.getFlowId() + "' scope will be passed to the resuming parent flow '"
							+ resumingParentFlow.getId() + "'");
				}
			}
			// treat the returned end state as a transitional event in the
			// resuming state, this is so cool!
			String eventId = endingFlowSession.getCurrentStateId();
			descriptor = resumingState.execute(eventId, sessionExecution, request, response);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Session execution for root flow '" + getFlow().getId() + "' has ended");
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning view descriptor '" + descriptor + "'");
		}
		return descriptor;
	}
}