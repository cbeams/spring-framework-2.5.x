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

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;

/**
 * A transition takes a flow from one state to another when executed.
 * A transition is associated with exactly one source
 * <code>TransitionableState</code> managed by exactly one <code>Flow</code>
 * definition.
 * 
 * @see org.springframework.web.flow.TransitionableState
 * @see org.springframework.web.flow.Flow
 * @see org.springframework.web.flow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class Transition {

	protected final Log logger = LogFactory.getLog(Transition.class);

	/**
	 * Event id value ("*") that will cause this transition to match on any
	 * event.
	 */
	public static final String WILDCARD_EVENT_ID = "*";

	/**
	 * The source state that owns this transition.
	 */
	private TransitionableState sourceState;

	/**
	 * The criteria that determine whether or not this transition should
	 * execute.
	 */
	private TransitionCriteria criteria;

	/**
	 * The target state that this transition should transition to when executed.
	 */
	private State targetState;

	/**
	 * The state id for the target state; needed to lazily resolve the target
	 * state once on first execution (after configuration).
	 */
	private String targetStateId;

	/**
	 * Create a new transition.
	 * @param eventId id of the event on which this transition should be
	 *        executed, or "*" if it should execute on any event
	 * @param targetStateId the id of the state to transition to when this
	 *        transition is executed
	 */
	public Transition(String eventId, String targetStateId) {
		Assert.notNull(eventId, "The event id property is required");
		Assert.notNull(targetStateId, "The targetStateId property is required");
		this.criteria = createDefaultTransitionCriteria(eventId);
		this.targetStateId = targetStateId;
	}

	/**
	 * Create a new transition.
	 * @param criteria strategy object used to determine if this transition
	 *        should be executed given contextual information
	 * @param targetStateId the id of the state to transition to when this
	 *        transition is executed
	 */
	public Transition(TransitionCriteria criteria, String targetStateId) {
		Assert.notNull(criteria, "The transition criteria property is required");
		Assert.notNull(targetStateId, "The targetStateId property is required");
		this.criteria = criteria;
		this.targetStateId = targetStateId;
	}

	/**
	 * Create a default constraint implementation that will match true on events
	 * with the provided event id.
	 * <p>
	 * If the given event id is "*", a wildcard event criteria object will be
	 * returned that matches any event. Otherwise you get a criteria object that
	 * matches given event id exactly.
	 */
	protected TransitionCriteria createDefaultTransitionCriteria(String eventId) {
		if (WILDCARD_EVENT_ID.equals(eventId)) {
			return WILDCARD_TRANSITION_CRITERIA;
		}
		else {
			// implementation note: this inner class is not a class constant
			// because we need the eventId
			return new EventIdTransitionCriteria(eventId);
		}
	}

	/**
	 * Returns the owning source (<i>from</i>) state of this transition.
	 * @return the source state
	 * @throws IllegalStateException if the source state has not been set
	 */
	public TransitionableState getSourceState() throws IllegalStateException {
		Assert.state(sourceState != null,
			"The source state is not yet been set -- this transition must be added to exactly one owning state definition!");
		return sourceState;
	}

	/**
	 * Set the owning source (<i>from</i>) state of this transition.
	 */
	protected void setSourceState(TransitionableState owningState) {
		this.sourceState = owningState;
	}

	/**
	 * Returns the id of the target (<i>to</i>) state of this transition.
	 * @return the target state id
	 */
	public String getTargetStateId() {
		return targetStateId;
	}

	/**
	 * Returns the target (<i>to</i>) state of this transition.
	 * @return the target state
	 * @throws NoSuchFlowStateException when the target state cannot be found
	 */
	public State getTargetState() throws NoSuchFlowStateException {
		synchronized (this) {
			if (this.targetState != null) {
				return this.targetState;
			}
		}
		State targetState = getSourceState().getFlow().getRequiredState(getTargetStateId());
		synchronized (this) {
			this.targetState = targetState;
		}
		return this.targetState;
	}

	/**
	 * Returns the strategy used to determine if this transition should execute
	 * given an execution context.
	 * @return the constraint
	 */
	public TransitionCriteria getCriteria() {
		return this.criteria;
	}

	/**
	 * Checks if this transition should be executed given the state of the
	 * provided flow execution request context.
	 * @param context the flow execution request context
	 * @return true if this transition should execute, false otherwise
	 */
	public boolean shouldExecute(RequestContext context) {
		return this.criteria.test(context);
	}

	/**
	 * Execute this transition.
	 * @param context the flow execution request context
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the transition execution
	 * @throws CannotExecuteStateTransitionException when this transition
	 *         cannot be executed
	 */
	protected ViewDescriptor execute(StateContext context) throws CannotExecuteStateTransitionException {
		try {
			ViewDescriptor viewDescriptor = getTargetState().enter(context);
			if (logger.isDebugEnabled()) {
				if (context.isFlowExecutionActive()) {
					logger.debug("Transition '" + this + "' executed; as a result, the new state is '"
							+ context.getCurrentState().getId() + "' in flow '"
							+ context.getActiveFlow().getId() + "'");
				}
				else {
					logger.debug("Transition '" + this + "' executed; as a result, the flow '"
							+ context.getRootFlow().getId() + "' execution has ended");
				}
			}
			return viewDescriptor;
		}
		catch (NoSuchFlowStateException e) {
			throw new CannotExecuteStateTransitionException(this, e);
		}
	}

	/**
	 * Event matching criteria that matches on any event.
	 */
	public static final TransitionCriteria WILDCARD_TRANSITION_CRITERIA = new TransitionCriteria() {
		public boolean test(RequestContext context) {
			return true;
		}

		public String toString() {
			return WILDCARD_EVENT_ID;
		}
	};

	/**
	 * Simple, default transition criteria that matches on an eventId and nothing
	 * else. Specifically, if the last event that occured has id ${eventId},
	 * this criteria will return true.
	 */
	public static class EventIdTransitionCriteria implements TransitionCriteria, Serializable {
		
		private String eventId;

		public EventIdTransitionCriteria(String eventId) {
			this.eventId = eventId;
		}

		public boolean test(RequestContext context) {
			return context.getLastEvent().getId().equals(eventId);
		}

		public String toString() {
			return eventId;
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("criteria", criteria).append("targetStateId", targetStateId).toString();
	}
}