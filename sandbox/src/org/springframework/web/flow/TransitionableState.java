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
 * transitions are triggered by events, specifically, when an occurence of a
 * supported event in this state is signaled.
 * @author Keith Donald
 * @author Erwin Vervaet
 * @see org.springframework.web.flow.Transition
 * @see org.springframework.web.flow.Flow
 */
public abstract class TransitionableState extends State {

	/**
	 * The set of possible transitions out of this state.
	 */
	private Set transitions = new LinkedHashSet();

	/**
	 * Create a new transitionable state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param transition The sole transition of this state
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public TransitionableState(Flow flow, String id, Transition transition) throws IllegalArgumentException {
		super(flow, id);
		add(transition);
	}

	/**
	 * Create a new transitionable state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param transitions The transitions of this state
	 * @throws IllegalArgumentException When this state cannot be added to given
	 *         flow
	 */
	public TransitionableState(Flow flow, String id, Transition[] transitions) throws IllegalArgumentException {
		super(flow, id);
		addAll(transitions);
	}

	/**
	 * Add a transition to this state.
	 * @param transition The transition to add
	 */
	protected void add(Transition transition) {
		transition.setSourceState(this);
		this.transitions.add(transition);
	}

	/**
	 * Add given list of transitions to this state.
	 * @param transitions The transitions to add
	 */
	protected void addAll(Transition[] transitions) {
		for (int i = 0; i < transitions.length; i++) {
			add(transitions[i]);
		}
	}

	/**
	 * Signal an occurence of the specified event in this state, triggering the
	 * execution of an appropriate state transition.
	 * @param event The event that occured in this state (e.g 'submit', 'next',
	 *        'back')
	 * @param context A flow execution context
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 * @throws EventNotSupportedException if the eventId does not map to a valid
	 *         transition for this state
	 * @throws CannotExecuteStateTransitionException if a state transition could
	 *         not be executed.
	 */
	protected ViewDescriptor executeTransitionOnEvent(Event event, StateContext context)
			throws EventNotSupportedException, CannotExecuteStateTransitionException {
		context.setLastEvent(event);
		Transition transition = getRequiredTransition(context);
		return transition.execute(context);
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
	 * Returns a collection of the supported transitional criteria (Constraint
	 * objects) used to match events with transitions in this state.
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
	 * Get a transition in this state for given id. Throws and exception when
	 * the event is not supported by this state, e.g. when there is no
	 * corresponding transition.
	 * @throws EventNotSupportedException When the event is not supported by
	 *         this state
	 */
	protected Transition getRequiredTransition(FlowExecutionContext context) throws EventNotSupportedException {
		Transition transition = getTransition(context);
		if (transition != null) {
			return transition;
		}
		else {
			throw new EventNotSupportedException(this, context.getLastEvent());
		}
	}

	/**
	 * Get a transition for given event id.
	 * @param eventId The event id of the transition to look up
	 * @return The transition associated with the event, or null if there is no
	 *         such transition in this state
	 */
	public Transition getTransition(FlowExecutionContext context) {
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
	 * Check if given event id is supported by this state. In other words, check
	 * if this state has a transition that executes on an occurence of the given
	 * eventId.
	 * @param eventId the event id to check
	 * @return true or false
	 */
	public boolean isTransitionForOccurrenceOf(Event event, StateContext context) {
		context.setLastEvent(event);
		return getTransition(context) != null;
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("transitions", this.transitions);
	}
}