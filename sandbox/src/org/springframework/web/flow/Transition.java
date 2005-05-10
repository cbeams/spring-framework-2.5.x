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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A transition takes a flow from one state to another when executed. A
 * transition is associated with exactly one source <code>TransitionableState</code>.
 * Execution of a transition is guarded by a <code>TransitionCriteria</code> object,
 * which, when matched, makes the transition elligible for execution.
 * 
 * @see org.springframework.web.flow.TransitionableState
 * @see org.springframework.web.flow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class Transition extends AnnotatedObject {

	protected final Log logger = LogFactory.getLog(Transition.class);

	/**
	 * The source state that owns this transition.
	 */
	private TransitionableState sourceState;

	/**
	 * The criteria that determine whether or not this transition matches as
	 * elligible for execution.
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
	 * Default constructor for bean style usage. 
	 */
	public Transition() {
	}

	/**
	 * Create a new transition.
	 * @param matchingCriteria strategy object used to determine if this transition should be
	 *        matched as elligible for execution
	 */
	public Transition(TransitionCriteria matchingCriteria) {
		setMatchingCriteria(matchingCriteria);
	}

	/**
	 * Create a new transition.
	 * @param matchingCriteria strategy object used to determine if this transition should be
	 *        matched as elligible for execution
	 * @param targetStateId the id of the starget state of the transition
	 */
	public Transition(TransitionCriteria matchingCriteria, String targetStateId) {
		setMatchingCriteria(matchingCriteria);
		setTargetStateId(targetStateId);
	}

	/**
	 * Create a new transition.
	 * @param matchingCriteria strategy object used to determine if this transition should be
	 *        matched as elligible for execution
	 * @param properties additional properties describing this transition
	 */
	public Transition(TransitionCriteria matchingCriteria, Map properties) {
		setMatchingCriteria(matchingCriteria);
		setProperties(properties);
	}

	/**
	 * Create a new transition.
	 * @param matchingCriteria strategy object used to determine if this transition should be
	 *        matched as elligible for execution
	 * @param targetStateId the id of the starget state of the transition
	 * @param properties additional properties describing this transition
	 */
	public Transition(TransitionCriteria matchingCriteria, String targetStateId, Map properties) {
		setMatchingCriteria(matchingCriteria);
		setTargetStateId(targetStateId);
		setProperties(properties);
	}

	/**
	 * Returns the owning source (<i>from</i>) state of this transition.
	 * @return the source state
	 */
	public TransitionableState getSourceState() {
		return sourceState;
	}

	/**
	 * Set the owning source (<i>from</i>) state of this transition.
	 */
	public void setSourceState(TransitionableState owningState) {
		Assert.isTrue(getSourceState() == null, "This transition was already added to a source state");
		this.sourceState = owningState;
	}

	/**
	 * Returns the criteria that determine whether or not this transition matches as
	 * elligible for execution.
	 * @return the transition matching criteria
	 */
	public TransitionCriteria getMatchingCriteria() {
		return matchingCriteria;
	}
	
	/**
	 * Set the criteria that determine whether or not this transition matches as
	 * elligible for execution.
	 */
	public void setMatchingCriteria(TransitionCriteria matchingCriteria) {
		Assert.notNull(matchingCriteria, "The transition matching criteria are required");
		this.matchingCriteria = matchingCriteria;
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
	 * Set the criteria that determine whether or not this transition, once matched,
	 * should complete execution or should <i>roll back</i>.
	 */
	public void setExecutionCriteria(TransitionCriteria executionCriteria) {
		this.executionCriteria = executionCriteria;
	}
	
	/**
	 * Checks if this transition is elligible for execution given the state of the
	 * provided flow execution request context.
	 * @param context the flow execution request context
	 * @return true if this transition should execute, false otherwise
	 */
	public boolean matches(RequestContext context) {
		return this.matchingCriteria.test(context);
	}

	/**
	 * Returns the id of the target (<i>to</i>) state of this transition.
	 * @return the target state id
	 */
	public String getTargetStateId() {
		return targetStateId;
	}
	
	/**
	 * Set the id of the target (<i>to</i>) state of this transtion.
	 */
	public void setTargetStateId(String targetStateId) {
		this.targetStateId = targetStateId;
	}

	/**
	 * Returns the state this transition will transition <i>to</i> when executed
	 * with given request context. 
	 */
	public State getTargetState(RequestContext context) {
		// this implementation does not take the request context into
		// consideration when determining the target state
		return getTargetState();
	}

	/**
	 * Returns the target (<i>to</i>) state of this transition.
	 * @return the target state
	 * @throws NoSuchFlowStateException when the target state cannot be found
	 */
	protected State getTargetState() throws NoSuchFlowStateException {
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
	 * Execute this state transition. Will only be called if the {@link #matches(RequestContext)}
	 * method returns true for given context.
	 * @param context the flow execution request context
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the transition execution
	 * @throws CannotExecuteTransitionException when this transition cannot
	 *         be executed because the target state is invalid
	 */
	public ViewDescriptor execute(RequestContext context) throws CannotExecuteTransitionException {
		State state = null;
		try {
			state = getTargetState();
		}
		catch (NoSuchFlowStateException e) {
			throw new CannotExecuteTransitionException(this, e);
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
				logger.debug("Transition '" + this + "' executed; as a result, the flow execution has ended");
			}
		}
		return viewDescriptor;
	}

	public String toString() {
		return new ToStringCreator(this).append("on", matchingCriteria).append("properties", getProperties()).toString();
	}
}