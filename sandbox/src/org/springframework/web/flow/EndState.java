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
		this(id, null);
	}

	public EndState(String id, String viewName) {
		super(id);
		this.viewName = viewName;
	}

	public boolean isEndState() {
		return true;
	}

	protected ViewDescriptor doEnterState(FlowSessionExecutionStack sessionExecution, HttpServletRequest request,
			HttpServletResponse response) {
		ViewDescriptor descriptor;
		if (viewName != null) {
			descriptor = new ViewDescriptor(viewName, sessionExecution.getAttributes());
		}
		else {
			descriptor = ViewDescriptor.NULL_OBJECT;
		}
		FlowSession endingFlowSession = sessionExecution.endActiveSession();
		Assert.isTrue(endingFlowSession.getCurrentState().equals(this),
				"The ending flow session current state should equal this end state - this should not happen");
		if (logger.isDebugEnabled()) {
			logger.debug("Flow session '" + endingFlowSession.getFlowId() + "' ended, details=" + endingFlowSession);
		}
		if (sessionExecution.isActive()) {
			// session is still active, resume in parent
			if (logger.isDebugEnabled()) {
				logger.debug("Resuming parent flow '" + sessionExecution.getQualifiedActiveFlowId() + "' in state '"
						+ sessionExecution.getCurrentStateId() + "'");
			}
			Flow resumingParentFlow = sessionExecution.getActiveFlow();
			SubFlowState resumingState = (SubFlowState)sessionExecution.getCurrentState();
			if (resumingState.getAttributesMapper(resumingParentFlow) != null) {
				if (logger.isDebugEnabled()) {
					logger
							.debug("Messaging the configured attributes mapper to map subflow attributes back up to the resuming parent flow - "
									+ "the resuming parent flow will now have access to attributes passed up by the completed subflow");
				}
				resumingState.getAttributesMapper(resumingParentFlow).mapToResumingParentFlow(endingFlowSession,
						sessionExecution.getActiveFlowSession());
			}
			else {
				if (logger.isInfoEnabled()) {
					logger.info("No attributes mapper is configured for the resuming state '" + getId()
							+ "' - note: as a result, no attributes in the ending subflow '"
							+ endingFlowSession.getFlowId() + "' scope will be passed to the resuming parent flow '"
							+ resumingParentFlow.getId() + "'");
				}
			}
			// treat the returned end state as a transitional event in the
			// resuming state, this is so cool!
			String eventId = endingFlowSession.getCurrentStateId();
			descriptor = ((TransitionableState)sessionExecution.getCurrentState()).execute(eventId, sessionExecution,
					request, response);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Root flow '" + endingFlowSession.getFlowId() + "' ended");
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning view descriptor '" + descriptor + "'");
		}
		return descriptor;
	}
}