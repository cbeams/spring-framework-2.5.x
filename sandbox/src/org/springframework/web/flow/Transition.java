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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeSource;
import org.springframework.binding.support.EmptyAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A transition takes a flow from one state to another when executed. A
 * transition is associated with exactly one source
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
	 * The source state that owns this transition.
	 */
	private TransitionableState sourceState;

	/**
	 * The criteria that determine whether or not this transition matches as
	 * eligible for execution.
	 */
	private TransitionCriteria matchingCriteria;

	/**
	 * The criteria that determine whether or not this transition, once matched,
	 * should complete execution or should <i>roll back</i>.
	 */
	private TransitionCriteria executionCriteria;

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
	 * Additional properties further describing this transition. 
	 */
	private AttributeSource properties = EmptyAttributeSource.INSTANCE;

	/**
	 * Create a new transition.
	 * @param criteria strategy object used to determine if this transition should be
	 *        matched eligible for execution
	 * @param targetStateId the id of the state to transition to when this transition is
	 *        executed
	 */
	public Transition(TransitionCriteria criteria, String targetStateId) {
		this(criteria, targetStateId, null, null);
	}

	/**
	 * Create a new transition.
	 * @param criteria strategy object used to determine if this transition should be
	 *        matched eligible for execution
	 * @param targetStateId the id of the state to transition to when this transition is
	 *        executed
	 * @param properties additional properties describing this transition
	 */
	public Transition(TransitionCriteria criteria, String targetStateId, Map properties) {
		this(criteria, targetStateId, null, properties);
	}

	/**
	 * Create a new transition.
	 * @param criteria strategy object used to determine if this transition should be
	 *        matched eligible for execution
	 * @param targetStateId the id of the state to transition to when this transition is
	 *        executed
	 * @param executionCriteria strategy object used to determine if this transition, once
	 *        matched for execution, is allowed to complete execution or should roll back
	 */
	public Transition(TransitionCriteria criteria, String targetStateId, TransitionCriteria executionCriteria) {
		this(criteria, targetStateId, executionCriteria, null);
	}

	/**
	 * Create a new transition.
	 * @param criteria strategy object used to determine if this transition should be
	 *        matched eligible for execution
	 * @param targetStateId the id of the state to transition to when this transition is
	 *        executed
	 * @param executionCriteria strategy object used to determine if this transition, once
	 *        matched for execution, is allowed to complete execution or should roll back
	 * @param properties additional properties describing this transition
	 */
	public Transition(TransitionCriteria criteria, String targetStateId, TransitionCriteria executionCriteria, Map properties) {
		Assert.notNull(criteria, "The transition criteria property is required");
		Assert.notNull(targetStateId, "The targetStateId property is required");
		this.matchingCriteria = criteria;
		this.targetStateId = targetStateId;
		this.executionCriteria = executionCriteria;
		if (properties != null) {
			this.properties = new MapAttributeSource(new HashMap(properties));
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
	 * Returns the criteria that determine whether or not this transition matches as
	 * eligible for execution.
	 * @return the transition matching criteria
	 */
	public TransitionCriteria getMatchingCriteria() {
		return matchingCriteria;
	}
	
	/**
	 * Returns the criteria that determine whether or not this transition, once matched,
	 * should complete execution or should <i>roll back</i>.
	 * @return the transition execution criteria
	 */
	public TransitionCriteria getExecutionCriteria() {
		return executionCriteria;
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
	 * Returns the additional properties describing this transition.
	 */
	public AttributeSource getProperties() {
		return this.properties;
	}

	/**
	 * Returns the value of given additional state property, or null if
	 * if not found.
	 */
	public Object getProperty(String propertyName) {
		return this.properties.getAttribute(propertyName);
	}
	
	/**
	 * Checks if this transition is eligible for execution given the state of the
	 * provided flow execution request context.
	 * @param context the flow execution request context
	 * @return true if this transition should execute, false otherwise
	 */
	public boolean matches(RequestContext context) {
		return this.matchingCriteria.test(context);
	}

	/**
	 * Execute this state transition.
	 * @param context the flow execution request context
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the transition execution
	 * @throws CannotExecuteStateTransitionException when this transition cannot
	 *         be executed because the target state is invalid
	 */
	protected ViewDescriptor execute(StateContext context) throws CannotExecuteStateTransitionException {
		State state = null;
		try {
			state = getTargetState();
		}
		catch (NoSuchFlowStateException e) {
			throw new CannotExecuteStateTransitionException(this, e);
		}
		context.setLastTransition(this);
		ViewDescriptor viewDescriptor;
		if (this.executionCriteria != null && !this.executionCriteria.test(context)) {
			// 'roll back' and re-enter the source state
			viewDescriptor = getSourceState().enter(context);
		}
		else {
			// enter the target state (note: any exceptions are propagated)
			viewDescriptor = state.enter(context);
		}
		if (logger.isDebugEnabled()) {
			if (context.isActive()) {
				logger.debug("Transition '" + this + "' executed; as a result, the new state is '"
						+ context.getActiveSession().getState().getId() + "' in flow '" + context.getActiveSession().getFlow().getId() + "'");
			}
			else {
				logger.debug("Transition '" + this + "' executed; as a result, the flow '"
						+ context.getRootFlow().getId() + "' execution has ended");
			}
		}
		return viewDescriptor;
	}

	public String toString() {
		return new ToStringCreator(this).append("on", matchingCriteria).append("to", targetStateId).append(
				"executionCriteria", executionCriteria).append("properties", properties).toString();
	}
}