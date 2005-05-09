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
import java.util.Map;
import java.util.Set;

import org.springframework.core.CollectionFactory;
import org.springframework.core.ToStringCreator;

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
	private Set transitions = CollectionFactory.createLinkedSetIfPossible(6);

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public TransitionableState(Flow flow, String id, Transition transition) throws IllegalArgumentException {
		super(flow, id);
		add(transition);
	}

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transition the sole transition of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public TransitionableState(Flow flow, String id, Transition transition, Map properties) throws IllegalArgumentException {
		super(flow, id, properties);
		add(transition);
	}

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transitions the transitions of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public TransitionableState(Flow flow, String id, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id);
		addAll(transitions);
	}

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transitions the transitions of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	public TransitionableState(Flow flow, String id, Transition[] transitions, Map properties) throws IllegalArgumentException {
		super(flow, id, properties);
		addAll(transitions);
	}

	/**
	 * Add a transition to this state.
	 * @param transition the transition to add
	 */
	protected void add(Transition transition) {
		transition.setSourceState(this);
		if (transition.getTargetStateId().equals(this.getId())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Loop detected: the source and target state of transition '" + transition
						+ "' are the same" + " -- make sure this is not a bug!");
			}
		}
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
	 * Returns a collection of the supported transitional criteria ({@link TransitionCriteria}
	 * objects) used to fire transitions in this state.
	 * @return the collection of transitional conditions
	 */
	public Collection getTransitionMatchingCriteria() {
		if (transitions.isEmpty()) {
			return Collections.EMPTY_SET;
		}
		Set criteria = new LinkedHashSet(transitions.size());
		Iterator it = transitionsIterator();
		while (it.hasNext()) {
			criteria.add(((Transition)it.next()).getMatchingCriteria());
		}
		return Collections.unmodifiableSet(criteria);
	}

	/**
	 * Get a transition in this state for given flow execution request context.
	 * Throws and exception when when there is no corresponding transition.
	 * @throws NoMatchingTransitionException
	 *             when the transition cannot be found
	 */
	public Transition transitionFor(RequestContext context) throws NoMatchingTransitionException {
		Transition transition = getTransition(context);
		if (transition != null) {
			return transition;
		}
		else {
			throw new NoMatchingTransitionException(this, context);
		}
	}

	/**
	 * Returns whether or not this state has a transition that will fire for
	 * given flow execution request context.
	 * @param context a flow execution context
	 */
	public boolean hasTransitionFor(RequestContext context) {
		return getTransition(context) != null;
	}

	/**
	 * Internal helper method that gets a transition for given flow execution
	 * request context.
	 * @param context a flow execution context
	 * @return the transition, or null if not found
	 */
	protected Transition getTransition(RequestContext context) {
		Iterator it = transitionsIterator();
		while (it.hasNext()) {
			Transition transition = (Transition)it.next();
			if (transition.matches(context)) {
				return transition;
			}
		}
		return null;
	}

	/**
	 * Notify this state that the specified Event was signaled within it. By
	 * default, receipt of the event will trigger a search for a matching state
	 * transition. If a valid transition is matched, its execution will be
	 * requested. If a transition could not be matched, or the transition
	 * execution failed, an exception will be thrown.
	 * @param event the event that occured
	 * @param context the state context associated with this request
	 * @return the view descriptor
	 * @throws NoMatchingTransitionException when no matching transition can be found
	 * @throws CannotExcuteStateTransitionException when a transition could
	 *         not be executed on receipt of the event
	 */
	public ViewDescriptor onEvent(Event event, StateContext context)
			throws NoMatchingTransitionException, CannotExecuteStateTransitionException {
		context.setLastEvent(event);
		return transitionFor(context).execute(context);
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("transitions", this.transitions);
	}
}