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
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 * Terminates an active web flow session when entered. If the terminated session
 * is the root flow session, the entire flow execution ends. If the terminated
 * session is a subflow session, control returns to the parent flow session, and
 * this state is used as grounds the transition in that resuming parent.
 * <p>
 * An end state may optionally be configured with the name of a view. This view
 * will be rendered if the end state terminates the entire flow execution.
 * <p>
 * Note: if no</code> viewName</code> property is specified AND this EndState
 * terminates the entire flow execution, it is expected that some other action
 * has already written the response (or else a blank response will result.) On
 * the other hand, if no <code>viewName</code> is specified AND this EndState
 * reliniqushes control back to a parent flow, view rendering responsibility is
 * falls on the parent.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @author Erwin Vervaet
 */
public class EndState extends AbstractState {

	private String viewName;

	/**
	 * Create a new end state with no associated view.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public EndState(Flow flow, String id) throws IllegalArgumentException {
		super(flow, id);
	}

	/**
	 * Create a new end state with specified associated view.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param viewName The name of the view that should be rendered if this end
	 *        state terminates flow execution
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public EndState(Flow flow, String id, String viewName) throws IllegalArgumentException {
		super(flow, id);
		setViewName(viewName);
	}

	/**
	 * @param viewName The name of the view that should be rendered if this end
	 *        state terminates flow execution.
	 */
	protected void setViewName(String viewName) {
		this.viewName = viewName;
	}

	/**
	 * @return The name of the view that will be rendered if this end state
	 *         terminates flow execution, or null if there is no associated
	 *         view.
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * @return True if this end state has no associated view, false otherwise.
	 */
	public boolean isMarker() {
		return !StringUtils.hasText(viewName);
	}

	/**
	 * Hook method implementation that initiates state-specific processing.
	 * 
	 * This implementation pops the top (active) flow session off the execution
	 * stack, ending it, and resumes control in the spawning parent flow (if
	 * neccessary.) If the ended session is the root flow, a ModelAndView is
	 * returned (when viewName is not null, else null is returned.)
	 */
	protected ModelAndView doEnterState(FlowExecutionStack flowExecution, HttpServletRequest request,
			HttpServletResponse response) {
		FlowSession endingFlowSession = flowExecution.endActiveSession();
		if (logger.isDebugEnabled()) {
			logger.debug("Session for flow '" + getFlow().getId() + "' ended, session details = " + endingFlowSession);
		}
		if (flowExecution.isActive()) {
			// session execution is still active, resume in parent
			if (logger.isDebugEnabled()) {
				logger.debug("Resuming parent flow '" + flowExecution.getActiveFlowId() + "' in state '"
						+ flowExecution.getCurrentStateId() + "'");
			}
			Assert.isInstanceOf(FlowAttributesMapper.class, flowExecution.getCurrentState());
			FlowAttributesMapper resumingState = (FlowAttributesMapper)flowExecution.getCurrentState();
			resumingState.mapSubFlowOutputAttributes(endingFlowSession, flowExecution.getActiveFlowSession());
			// treat this end state id as a transitional event in the
			// resuming state, this is so cool!
			Assert.isInstanceOf(TransitionableState.class, resumingState);
			return ((TransitionableState)resumingState).signalEvent(getId(), flowExecution, request, response);
		}
		else {
			// entire flow execution has ended, return ending view if applicable
			if (logger.isDebugEnabled()) {
				logger.debug("Session execution for root flow '" + getFlow().getId() + "' has ended");
			}
			if (isMarker()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Returning a view descriptor null object; no view to render");
				}
				return null;
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Returning view name '" + viewName + "' to render");
				}
				return new ModelAndView(viewName, endingFlowSession.getAttributes());
			}
		}
	}
}