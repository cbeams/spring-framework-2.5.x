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

import org.springframework.core.ToStringCreator;

/**
 * A simple decision state that when entered, will execute the first transition whose
 * criteria evaluates to <code>true</code> in the context of the current request.
 * @author Keith Donald
 */
public class DecisionState extends TransitionableState {

	/**
	 * Creates a new decision state.
	 * 
	 * @param flow
	 * @param stateId
	 * @param condition
	 * @param ifTrueStateId
	 * @param elseStateId
	 */
	public DecisionState(Flow flow, String stateId, TransitionCriteria condition, String ifTrueStateId,
			String elseStateId) {
		super(flow, stateId, new Transition(condition, ifTrueStateId));
		add(new Transition(TransitionCriteria.WILDCARD_TRANSITION_CRITERIA, elseStateId));
	}

	/**
	 * Creates a new decision state.
	 * 
	 * @param flow
	 * @param stateId
	 * @param condition
	 * @param trueStateId
	 * @param falseStateId
	 */
	public DecisionState(Flow flow, String stateId, Transition[] transitions) {
		super(flow, stateId, transitions);
	}

	/**
	 * Specialization of State's <code>doEnterState</code> template method
	 * that executes behaviour specific to this state type in polymorphic
	 * fashion.
	 * <p>
	 * @param context
	 *            the state execution context
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the state execution
	 */
	protected ViewDescriptor doEnterState(StateContext context) {
		return transitionFor(context).execute(context);
	}

	protected void createToString(ToStringCreator creator) {
		super.createToString(creator);
	}
}