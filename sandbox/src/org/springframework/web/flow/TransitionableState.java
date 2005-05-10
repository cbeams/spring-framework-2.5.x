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

import java.util.Iterator;
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
	 * Default constructor for bean style usage.
	 */
	protected TransitionableState() {
	}

	/**
	 * Create a new transitionable state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param transition the sole transition of this state
	 * @throws IllegalArgumentException when this state cannot be added to given flow
	 */
	protected TransitionableState(Flow flow, String id, Transition transition) throws IllegalArgumentException {
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
	protected TransitionableState(Flow flow, String id, Transition transition, Map properties)
			throws IllegalArgumentException {
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
	protected TransitionableState(Flow flow, String id, Transition[] transitions) throws IllegalArgumentException {
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
	protected TransitionableState(Flow flow, String id, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		super(flow, id, properties);
		addAll(transitions);
	}

	/**
	 * Add a transition to this state.
	 * @param transition the transition to add
	 */
	public void add(Transition transition) {
		transition.setSourceState(this);
		this.transitions.add(transition);
	}

	/**
	 * Add given list of transitions to this state.
	 * @param transitions the transitions to add
	 */
	public void addAll(Transition[] transitions) {
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
	 * Returns a list of the supported transitional criteria used to match
	 * transitions in this state.
	 * @return the list of transitional criteria
	 */
	public TransitionCriteria[] getTransitionCriterias() {
		TransitionCriteria[] res = new TransitionCriteria[transitions.size()];
		Iterator it = transitionsIterator();
		int i = 0;
		while (it.hasNext()) {
			res[i++] = ((Transition)it.next()).getMatchingCriteria();
		}
		return res;
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
	 * Gets a transition for given flow execution request context.
	 * @param context a flow execution context
	 * @return the transition, or null if not found
	 */
	public Transition getTransition(RequestContext context) {
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
	 * Get a transition in this state for given flow execution request context.
	 * Throws and exception when when there is no corresponding transition.
	 * @throws NoMatchingTransitionException when the transition cannot be found
	 */
	public Transition getRequiredTransition(RequestContext context) throws NoMatchingTransitionException {
		Transition transition = getTransition(context);
		if (transition == null) {
			throw new NoMatchingTransitionException(this, context);
		}
		return transition;
	}

	/**
	 * Notify this state that the specified Event was signaled within it. By
	 * default, receipt of the event will trigger a search for a matching state
	 * transition. If a valid transition is matched, its execution will be
	 * requested. If a transition could not be matched, or the transition
	 * execution failed, an exception will be thrown.
	 * @param event the event that occured
	 * @param context the context associated with this request
	 * @return the view descriptor
	 * @throws NoMatchingTransitionException when no matching transition can be found
	 * @throws CannotExecuteStateTransitionException when a transition could
	 *         not be executed on receipt of the event
	 */
	public ViewDescriptor onEvent(Event event, RequestContext context)
			throws NoMatchingTransitionException, CannotExecuteTransitionException {
		context.setLastEvent(event);
		Transition transition = getRequiredTransition(context);
		if (transition.getTargetState(context).getId().equals(this.getId())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Loop detected: the source and target state of transition '" + transition
						+ "' are the same" + " -- make sure this is not a bug!");
			}
		}
		return transition.execute(context);
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("transitions", this.transitions);
	}
}