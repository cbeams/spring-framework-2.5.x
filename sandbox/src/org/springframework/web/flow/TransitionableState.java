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
 */
public abstract class TransitionableState extends AbstractState {
	
	private Set transitions = new LinkedHashSet();

	public TransitionableState(Flow flow, String id, Transition transition) {
		super(flow, id);
		add(transition);
	}

	public TransitionableState(Flow flow, String id, Transition[] transitions) {
		super(flow, id);
		addAll(transitions);
	}

	public boolean isTransitionable() {
		return true;
	}

	protected void add(Transition transition) {
		if (transition.getSourceState()!=null && transition.getSourceState()!=this) {
			throw new IllegalArgumentException("Given transition already belongs to another state");
		}
		transition.setSourceState(this);
		this.transitions.add(transition);
	}

	protected void addAll(Transition[] transitions) {
		for (int i = 0; i < transitions.length; i++) {
			add(transitions[i]);
		}
	}

	/**
	 * Execute the event identified by <code>eventId</code> in this state.
	 * 
	 * @param eventId The id of the event to execute (e.g 'submit', 'next',
	 *        'back')
	 * @param flowExecution A flow session execution stack, tracking any
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
			HttpServletResponse response) throws CannotExecuteStateTransitionException {
		Transition transition = getRequiredTransition(eventId);
		flowExecution.setLastEventId(eventId);
		return transition.execute(flowExecution, request, response);
	}

	protected Iterator transitionsIterator() {
		return transitions.iterator();
	}

	protected Transition[] getTransitions() {
		return (Transition[])transitions.toArray(new Transition[transitions.size()]);
	}

	protected Collection getEventIdCriteria() {
		if (transitions.isEmpty()) {
			return Collections.EMPTY_SET;
		}
		Set criterion = new LinkedHashSet(transitions.size());
		Iterator it = transitionsIterator();
		while (it.hasNext()) {
			criterion.add(((Transition)it.next()).getEventIdCriteria());
		}
		return Collections.unmodifiableSet(criterion);
	}

	protected Transition getRequiredTransition(String eventId) throws EventNotSupportedException {
		Transition transition = getTransition(eventId);
		if (transition != null) {
			return transition;
		}
		else {
			throw new EventNotSupportedException(this, eventId);
		}
	}

	protected Transition getTransition(String eventId) throws EventNotSupportedException {
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