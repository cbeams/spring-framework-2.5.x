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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.util.ToStringCreator;

/**
 * Abstract superclass for states that have one or more transitions. State
 * transitions are typically triggered by events.
 * 
 * @see org.springframework.web.flow.Transition
 * @see org.springframework.web.flow.TransitionCriteria
 * @see org.springframework.web.flow.Flow
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class TransitionableState extends State {

	/**
	 * The set of possible transitions out of this state.
	 */
	private Set transitions = new LinkedHashSet();

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public TransitionableState(Flow flow, String id, Transition transition) throws IllegalArgumentException {
		super(flow, id);
		add(transition);
	}

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public TransitionableState(Flow flow, String id, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id);
		addAll(transitions);
	}

	/**
	 * Add a transition to this state.
	 * @param transition the transition to add
	 */
	protected void add(Transition transition) {
		transition.setSourceState(this);
		this.transitions.add(transition);
	}

	/**
	 * Add given list of transitions to this state.
	 * @param transitions the transitions to add
	 */
	protected void addAll(Transition[] transitions) {
		for (int i = 0; i < transitions.length; i++) {
			add(transitions[i]);
		}
	}

	/**
	 * Returns an iterator looping over all transitions in this state.
	 */
	public Iterator transitionsIterator() {
		return transitions.iterator();
	}

	/**
	 * Returns the list of transitions owned by this state.
	 */
	public Transition[] getTransitions() {
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	/**
	 * Returns a collection of the supported transitional criteria
	 * ({@link TransitionCriteria} objects) used to fire transitions
	 * in this state.
	 * @return the collection of transitional conditions
	 */
	public Collection getTransitionalCriteria() {
		if (transitions.isEmpty()) {
			return Collections.EMPTY_SET;
		}
		Set criteria = new LinkedHashSet(transitions.size());
		Iterator it = transitionsIterator();
		while (it.hasNext()) {
			criteria.add(((Transition)it.next()).getCriteria());
		}
		return Collections.unmodifiableSet(criteria);
	}

	/**
	 * Get a transition in this state for given flow execution request context.
	 * Throws and exception when when there is no corresponding transition.
	 * @throws NoSuchTransitionException when the transition cannot be found
	 */
	protected Transition getRequiredTransition(RequestContext context) throws NoSuchTransitionException {
		Transition transition = getTransition(context);
		if (transition != null) {
			return transition;
		}
		else {
			throw new NoSuchTransitionException(this, context.getLastEvent());
		}
	}

	/**
	 * Get a transition for given flow execution request context.
	 * @param context a flow execution context
	 */
	public Transition getTransition(RequestContext context) {
		Iterator it = transitionsIterator();
		while (it.hasNext()) {
			Transition transition = (Transition)it.next();
			if (transition.shouldExecute(context)) {
				return transition;
			}
		}
		return null;
	}
	
	/**
	 * Returns whether or not this state has a transition that will fire
	 * for given flow execution request context.
	 * @param context a flow execution context
	 */
	public boolean hasTransitionFor(RequestContext context) {
		return getTransition(context) != null;
	}

	/**
	 * Execute the transition that fires for given flow execution request context.
	 * @param context a flow execution context
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the transition execution
	 * @throws NoSuchTransitionException when no transition can be found
	 * @throws CannotExecuteStateTransitionException if a state transition could
	 *         not be executed
	 */
	public ViewDescriptor executeTransition(StateContext context)
			throws NoSuchTransitionException, CannotExecuteStateTransitionException {
		Transition transition = getRequiredTransition(context);
		return transition.execute(context);
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("transitions", this.transitions);
	}
}