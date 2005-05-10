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

import org.springframework.util.StringUtils;

/**
 * A simple decision state that when entered, will execute the first transition whose
 * criteria evaluates to <code>true</code> in the context of the current request.
 * <p>
 * A decision state is a convenient way to encapsulate reusable state transition logic in
 * one place.
 * 
 * @author Keith Donald
 */
public class DecisionState extends TransitionableState {
	
	/**
	 * Default constructor for bean style usage.
	 */
	public DecisionState() {
	}

	/**
	 * Create a new decision state with an if/then/else transition set.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param criteria the expression
	 * @param ifTrueStateId the state to go to if the expression is true
	 * @param elseStateId the state to go to if the expression is false (optional)
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public DecisionState(Flow flow, String id, TransitionCriteria criteria, String ifTrueStateId, String elseStateId)
			throws IllegalArgumentException {
		super(flow, id, new Transition(criteria, ifTrueStateId));
		if (StringUtils.hasText(elseStateId)) {
			add(new Transition(new WildcardTransitionCriteria(), elseStateId));
		}
	}

	/**
	 * Create a new decision state with an if/then/else transition set.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param criteria the expression
	 * @param ifTrueStateId the state to go to if the expression is true
	 * @param elseStateId the state to go to if the expression is false
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public DecisionState(Flow flow, String id, TransitionCriteria criteria, String ifTrueStateId, String elseStateId,
			Map properties) throws IllegalArgumentException {
		super(flow, id, new Transition(criteria, ifTrueStateId), properties);
		if (StringUtils.hasText(elseStateId)) {
			add(new Transition(new WildcardTransitionCriteria(), elseStateId));
		}
	}

	/**
	 * Creates a new decision state with the supported set of transitions.
	 * @param flow the owning flow
	 * @param stateId the state identifier (must be unique to the flow)
	 * @param transitions the transitions
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public DecisionState(Flow flow, String stateId, Transition[] transitions) throws IllegalArgumentException {
		super(flow, stateId, transitions);
	}

	/**
	 * Creates a new decision state with the supported set of transitions.
	 * @param flow the owning flow
	 * @param stateId the state identifier (must be unique to the flow)
	 * @param transitions the transitions
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public DecisionState(Flow flow, String stateId, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		super(flow, stateId, transitions, properties);
	}

	/**
	 * Specialization of State's <code>doEnter</code> template method
	 * that executes behaviour specific to this state type in polymorphic
	 * fashion.
	 * <p>
	 * Simply looks up the first transition that matches the state of the
	 * StateContext and executes it.
	 * @param context the request execution context
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the state execution
	 */
	protected ViewDescriptor doEnter(RequestContext context) {
		return getRequiredTransition(context).execute(context);
	}
}