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
import org.springframework.util.closure.Constraint;
import org.springframework.web.servlet.ModelAndView;

/**
 * A transition takes a flow execution from one state to another when executed.
 * A transition is associated with exactly one source
 * <code>TransitionableState</code> managed by exactly one <code>Flow</code>
 * definition.
 * 
 * @see org.springframework.web.flow.TransitionableState
 * @see org.springframework.web.flow.Flow
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class Transition implements Serializable {

	protected final Log logger = LogFactory.getLog(Transition.class);

	/**
	 * Event id value ("*") that will cause this transition to match on any
	 * event.
	 */
	public static final String WILDCARD_EVENT_ID = "*";

	/**
	 * Event matching criteria that matches on any event.
	 */
	public static final Constraint WILDCARD_EVENT_CRITERIA = new Constraint() {
		public boolean test(Object o) {
			return true;
		}

		public String toString() {
			return WILDCARD_EVENT_ID;
		}
	};

	/**
	 * The criteria that determines whether or not this criteria handles a given
	 * event. The event is identified by a String identifier.
	 */
	private Constraint eventIdCriteria;

	/**
	 * The source state that owns this transition.
	 */
	private TransitionableState sourceState;

	/**
	 * The target state that this transition should transition to when executed.
	 */
	private AbstractState targetState;

	/**
	 * The state id for the target state; needed to lazily resolve the target
	 * state once on first execution (after configuration.)
	 */
	private String targetStateId;

	/**
	 * Create a new transition.
	 * @param eventId Id of the event on which this transition should be
	 *        executed, or "*" if it should execute on any event
	 * @param targetStateId The id of the state to transition to when this
	 *        transition is executed
	 */
	public Transition(String eventId, String targetStateId) {
		Assert.notNull(eventId, "The event id property is required");
		Assert.notNull(targetStateId, "The targetStateId property is required");
		this.eventIdCriteria = createDefaultEventIdCriteria(eventId);
		this.targetStateId = targetStateId;
	}

	/**
	 * Create a new transition.
	 * @param eventIdCriteria Constraint object used to determine if this
	 *        transition should be executed for a particular event id
	 * @param targetStateId The id of the state to transition to when this
	 *        transition is executed
	 */
	public Transition(Constraint eventIdCriteria, String targetStateId) {
		Assert.notNull(eventIdCriteria, "The eventIdCriteria property is required");
		Assert.notNull(targetStateId, "The targetStateId property is required");
		this.eventIdCriteria = eventIdCriteria;
		this.targetStateId = targetStateId;
	}

	/**
	 * @return The owning source ( <i>from </i>) state of this transition.
	 */
	protected TransitionableState getSourceState() {
		return sourceState;
	}

	/**
	 * @param owningState The owning source ( <i>from </i>) state of this
	 *        transition.
	 */
	protected void setSourceState(TransitionableState owningState) {
		this.sourceState = owningState;
	}

	/**
	 * @return The id of the target ( <i>to </i>) state of this transition.
	 */
	public String getTargetStateId() {
		return targetStateId;
	}

	/**
	 * @return The target ( <i>to </i>) state of this transition.
	 * @throws NoSuchFlowStateException When the target state cannot be found
	 */
	protected AbstractState getTargetState() throws NoSuchFlowStateException {
		synchronized (this) {
			if (this.targetState != null) {
				return this.targetState;
			}
		}
		AbstractState targetState = getSourceState().getFlow().getRequiredState(getTargetStateId());
		synchronized (this) {
			this.targetState = targetState;
		}
		return this.targetState;
	}

	/**
	 * Create a constraint object used to match event ids with this transition
	 * based on given event id.
	 * <p>
	 * If the given event id is "*", a wildcard event criteria object will be
	 * returned that matches any event. Otherwise you get a criteria object that
	 * matches given event id exactly.
	 */
	protected Constraint createDefaultEventIdCriteria(final String eventId) {
		if (WILDCARD_EVENT_ID.equals(eventId)) {
			return WILDCARD_EVENT_CRITERIA;
		}
		else {
			//implementation note: this inner class is not a class constant
			//because we need the eventId
			return new Constraint() {
				public boolean test(Object argument) {
					return eventId.equals(argument);
				}

				public String toString() {
					return eventId;
				}
			};
		}
	}

	/**
	 * @return The strategy used to match event ids with this transition
	 */
	public Constraint getEventIdCriteria() {
		return this.eventIdCriteria;
	}

	/**
	 * Checks if this transition is executed (triggered by) given event id.
	 * @param eventId The event id
	 * @return true or false
	 */
	public boolean executesOn(String eventId) {
		return eventIdCriteria.test(eventId);
	}

	/**
	 * Execute this transition.
	 * @param flowExecution A flow execution stack, tracking any suspended
	 *        parent flows that spawned this flow (as a subflow)
	 * @param request the client http request
	 * @param response the server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the transition execution.
	 * @throws CannotExecuteStateTransitionException thrown when this transition
	 *         cannot be executed
	 */
	protected ModelAndView execute(FlowExecutionStack flowExecution, HttpServletRequest request,
			HttpServletResponse response) throws CannotExecuteStateTransitionException {
		try {
			ModelAndView viewDescriptor = getTargetState().enter(flowExecution, request, response);
			if (logger.isDebugEnabled()) {
				if (flowExecution.isActive()) {
					logger
							.debug("Transition '" + this + "' executed; as a result, the new state is '"
									+ flowExecution.getCurrentStateId() + "' in flow '"
									+ flowExecution.getActiveFlowId() + "'");
				}
				else {
					logger.debug("Transition '" + this + "' executed; as a result, the flow '"
							+ flowExecution.getRootFlowId() + "' execution has ended");
				}
			}
			return viewDescriptor;
		}
		catch (NoSuchFlowStateException e) {
			throw new CannotExecuteStateTransitionException(this, e);
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("eventIdCriteria", eventIdCriteria).append("toState", targetStateId)
				.toString();
	}

}