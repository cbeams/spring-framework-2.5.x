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

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;
import org.springframework.util.closure.Constraint;
import org.springframework.util.closure.support.AbstractConstraint;

/**
 * @author Keith Donald
 */
public class Transition implements Serializable {
	private static final Log logger = LogFactory.getLog(Transition.class);

	public static final String EVENT_ID_ATTRIBUTE = "_eventId";

	private Constraint eventIdCriteria;

	private String toState;

	// constant that says match on any event
	public static final Constraint WILDCARD_EVENT_CRITERIA = new Constraint() {
		public boolean test(Object o) {
			return true;
		}

		public String toString() {
			return "*";
		}
	};

	public Transition(String id, String toState) {
		Assert.notNull(id, "The id property is required");
		Assert.notNull(toState, "The toState property is required");
		this.eventIdCriteria = createDefaultEventIdCriteria(id);
		this.toState = toState;
	}

	public Transition(Constraint eventIdCriteria, String toState) {
		Assert.notNull(eventIdCriteria, "The eventIdCriteria property is required");
		Assert.notNull(toState, "The toState property is required");
		this.eventIdCriteria = eventIdCriteria;
		this.toState = toState;
	}

	protected Constraint createDefaultEventIdCriteria(final String id) {
		return new AbstractConstraint() {
			public boolean test(Object eventId) {
				return id.equals(eventId);
			}

			public String toString() {
				return id;
			}
		};
	}

	public Constraint getEventIdCriteria() {
		return this.eventIdCriteria;
	}

	public String getToState() {
		return toState;
	}

	public boolean matches(String eventId) {
		return eventIdCriteria.test(eventId);
	}

	public ViewDescriptor execute(String eventId, TransitionableState fromState,
			FlowSessionExecutionStack sessionExecution, HttpServletRequest request, HttpServletResponse response) {
		Flow activeFlow = sessionExecution.getActiveFlow();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Executing transition from state '" + fromState.getId() + "' to state '" + getToState()
						+ "' in flow '" + sessionExecution.getQualifiedActiveFlowId() + "'");
			}
			return activeFlow.getRequiredState(getToState()).enter(sessionExecution, request, response);
		}
		catch (NoSuchFlowStateException e) {
			throw new CannotExecuteStateTransitionException(this, eventId, activeFlow, fromState.getId(), e);
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("eventIdCriteria", eventIdCriteria).append("toState", toState)
				.toString();
	}

}