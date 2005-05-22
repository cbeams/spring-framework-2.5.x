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
 * <p>
 * This class provides a simple implementation of a Transition that offers
 * the following functionality:
 * <ul>
 * <li>Execution of a transition is guarded by a <code>TransitionCriteria</code> object,
 * the so called "matching criteria", which, when matched, makes the transition
 * elligible for execution.</li>
 * <li>Optionally, completion of transition execution is guarded by a
 * <code>TransitionCriteria</code> object, the so called "execution criteria". When the
 * execution criteria test fails, the transition will <i>roll back</i>, transitioning
 * back into its source state. When the execution criteria test succeeds, the transition
 * continues onto the target state.</li>
 * <li>The target state of the transition is specified at configuration time using the
 * target state id.</li>
 * </ul>
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
	 * The state id for the target state.
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
	 * Create a new local transition programatically
	 * @param the source state of the transition
	 * @param matchingCriteria strategy object used to determine if this transition should be
	 *        matched as elligible for execution
	 * @param targetStateId the target state id
	 */
	public Transition(TransitionableState sourceState, TransitionCriteria matchingCriteria, String targetStateId) {
		setSourceState(sourceState);
		setMatchingCriteria(matchingCriteria);
		setTargetStateId(targetStateId);
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
	public void setSourceState(TransitionableState sourceState) {
		Assert.isTrue(getSourceState() == null, "This transition was already added to a source state");
		Assert.notNull(sourceState, "The source state of this transition is required");		
		this.sourceState = sourceState;
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
		Assert.notNull(matchingCriteria, "The transition matching criteria is required");
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
		Assert.hasText(targetStateId, "The id of the target state of the transition is required");		
		this.targetStateId = targetStateId;
	}

	/**
	 * Returns the state this transition will transition <i>to</i> when executed
	 * with given request context. Subclasses can override this to implement
	 * specialized behaviour, e.g. a transition with a "variable" target state. 
	 * @param context the flow execution request context
	 * @return the target state of the transition
	 * @throws NoSuchFlowStateException when the target state cannot be found
	 */
	public State getTargetState(RequestContext context) throws NoSuchFlowStateException {
		// this implementation does not take the request context into
		// consideration when determining the target state
		return getSourceState().getFlow().getRequiredState(getTargetStateId());
	}
	
	/**
	 * Checks if this transition is elligible for execution given the state of the
	 * provided flow execution request context.
	 * @param context the flow execution request context
	 * @return true if this transition should execute, false otherwise
	 */
	public boolean matches(RequestContext context) {
		return getMatchingCriteria().test(context);
	}

	/**
	 * Checks if this transition can complete its execution or should be rolled back,
	 * given the state of the flow execution request context.
	 * @param context the flow execution request context
	 * @return true if this transition can complete execution, false if it should
	 *         roll back
	 */
	public boolean canExecute(RequestContext context) {
		return getExecutionCriteria() != null ? getExecutionCriteria().test(context) : true;
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
	public ViewDescriptor execute(StateContext context) throws CannotExecuteTransitionException {
		State targetState = null;
		try {
			targetState = getTargetState(context);
			if (targetState.getId().equals(sourceState.getId())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Loop detected: the source and target state of transition '" + this
							+ "' are the same -- make sure this is not a bug!");
				}
			}
		}
		catch (NoSuchFlowStateException e) {
			throw new CannotExecuteTransitionException(this, e);
		}
		context.setLastTransition(this);
		ViewDescriptor viewDescriptor;
		if (canExecute(context)) {
			// enter the target state (note: any exceptions are propagated)
			viewDescriptor = targetState.enter(context);
		}
		else {
			// 'roll back' and re-enter the source state
			viewDescriptor = getSourceState().enter(context);
		}
		if (logger.isDebugEnabled()) {
			if (context.getFlowContext().isActive()) {
				logger.debug("Transition '" + this + "' executed; as a result, the new state is '"
						+ context.getFlowContext().getCurrentState().getId() + "' in flow '"
						+ context.getFlowContext().getActiveFlow().getId() + "'");
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