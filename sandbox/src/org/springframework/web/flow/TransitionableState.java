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

	public TransitionableState(Flow flow, String id) {
		super(flow, id);
	}

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
		transition.setSourceState(this);
		transitions.add(transition);
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
	 * @param sessionExecutionStack A flow session execution stack, tracking any
	 *        suspended parent flows that spawned this flow (as a subflow)
	 * @param request the client http request
	 * @param response the server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 * @throws CannotExecuteStateTransitionException if the <code>eventId</code>
	 *         does not map to a valid transition for this state.
	 */
	protected ModelAndView execute(String eventId, FlowExecutionStack sessionExecution, HttpServletRequest request,
			HttpServletResponse response) throws CannotExecuteStateTransitionException {
		updateCurrentStateIfNeccessary(eventId, sessionExecution);
		if (logger.isDebugEnabled()) {
			logger.debug("Event '" + eventId + "' within state '" + getId() + "' for flow '" + getFlow().getId()
					+ "' signaled");
		}
		sessionExecution.setLastEventId(eventId);
		ModelAndView viewDescriptor = getTransition(eventId).execute(sessionExecution, request, response);
		if (logger.isDebugEnabled()) {
			if (sessionExecution.isActive()) {
				logger.debug("Event '" + eventId + "' within state '" + this + "' for flow '" + getFlow().getId()
						+ "' processed; as a result, the new state is '" + sessionExecution.getCurrentStateId()
						+ "' in flow '" + sessionExecution.getActiveFlowId() + "'");
			}
			else {
				logger.debug("Event '" + eventId + "' within state '" + this + "' for flow '" + getFlow().getId()
						+ "' processed; as a result, the flow session execution has ended");
			}
		}
		return viewDescriptor;
	}

	protected Iterator transitionsIterator() {
		return transitions.iterator();
	}

	public Collection getEventIdCriterion() {
		if (transitions.isEmpty()) {
			return Collections.EMPTY_SET;
		}
		Iterator it = transitionsIterator();
		Set criterion = new LinkedHashSet(transitions.size());
		while (it.hasNext()) {
			criterion.add(((Transition)it.next()).getEventIdCriteria());
		}
		return Collections.unmodifiableSet(criterion);
	}

	protected Transition getTransition(String eventId) throws EventNotSupportedException {
		Iterator it = transitionsIterator();
		while (it.hasNext()) {
			Transition transition = (Transition)it.next();
			if (transition.executesOn(eventId)) {
				return transition;
			}
		}
		throw new EventNotSupportedException(this, eventId);
	}

	protected void updateCurrentStateIfNeccessary(String eventId, FlowExecutionStack sessionExecution) {
		if (!this.equals(sessionExecution.getCurrentState())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Event '" + eventId + "' in state '" + getId()
						+ "' was signaled by client; however the current flow session execution state is '"
						+ sessionExecution.getCurrentStateId() + "'; updating current state to '" + getId() + "'");
			}
			sessionExecution.setCurrentState(this);
		}
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("transitions", transitions);
	}

}