/*
 * Copyright 2002-2004 the original author or authors.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.ToStringCreator;
import org.springframework.web.servlet.ModelAndView;

/**
 * A state that has one or more transitions. State transitions are triggered by
 * events, specifically, when execution of an event in this state is requested.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class TransitionableState extends AbstractState {
	
	private Set transitions = new LinkedHashSet();

	/**
	 * Create a new transitionable state.
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @param transition The sole transition of this state
	 * @throws IllegalArgumentException When this state cannot be added to given flow
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
	 * @throws IllegalArgumentException When this state cannot be added to given flow
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
	 * Signal an occurence of the event identified by <code>eventId</code> in this state.
	 * 
	 * @param eventId The id of the event to execute (e.g 'submit', 'next',
	 *        'back')
	 * @param flowExecution A flow execution stack, tracking any
	 *        suspended parent flows that spawned this flow (as a subflow)
	 * @param request the client http request
	 * @param response the server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 * @throws EventNotSupportedException if the <ode>eventId>does not map to
	 *         valid transition for this state
	 * @throws CannotExecuteStateTransitionException if a state transition could
	 *         not be executed.
	 */
	protected ModelAndView signalEvent(String eventId, FlowExecutionStack flowExecution, HttpServletRequest request,
			HttpServletResponse response) throws EventNotSupportedException, CannotExecuteStateTransitionException {
		Transition transition = getRequiredTransition(eventId);
		flowExecution.setLastEventId(eventId);
		return transition.execute(flowExecution, request, response);
	}

	/**
	 * @return An iterator looping over all transitions in this state
	 */
	public Iterator transitionsIterator() {
		return transitions.iterator();
	}

	/**
	 * @return The list of transitions owned by this state
	 */
	public Transition[] getTransitions() {
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	/**
	 * @return A collection of all the criteria (Constraint objects) used
	 *         to match events with transitions in this state.
	 */
	public Collection getEventIdCriteria() {
		if (transitions.isEmpty()) {
			return Collections.EMPTY_SET;
		}
		Set criteria = new LinkedHashSet(transitions.size());
		Iterator it = transitionsIterator();
		while (it.hasNext()) {
			criteria.add(((Transition)it.next()).getEventIdCriteria());
		}
		return Collections.unmodifiableSet(criteria);
	}

	/**
	 * Get a transition in this state for given id. Throws and exception
	 * when the event is not supported by this state, e.g. when there is no
	 * corresponding transition.
	 */
	protected Transition getRequiredTransition(String eventId) throws EventNotSupportedException {
		Transition transition = getTransition(eventId);
		if (transition != null) {
			return transition;
		}
		else {
			throw new EventNotSupportedException(this, eventId);
		}
	}

	/**
	 * @param eventId The event id of the transition to look up
	 * @return The transition associated with the event, or null if there
	 *         is no such transition in this state
	 */
	public Transition getTransition(String eventId) {
		Iterator it = transitionsIterator();
		while (it.hasNext()) {
			Transition transition = (Transition)it.next();
			if (transition.executesOn(eventId)) {
				return transition;
			}
		}
		return null;
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("transitions", this.transitions);
	}
}