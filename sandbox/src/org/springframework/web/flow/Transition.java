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
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Keith Donald
 */
public class Transition implements Serializable {
	
	private static final Log logger = LogFactory.getLog(Transition.class);

	public static final String WILDCARD_EVENT_ID="*";
	
	// constant that says match on any event
	public static final Constraint WILDCARD_EVENT_CRITERIA = new Constraint() {
		public boolean test(Object o) {
			return true;
		}

		public String toString() {
			return WILDCARD_EVENT_ID;
		}
	};
	
	private Constraint eventIdCriteria;

	private TransitionableState sourceState;

	private AbstractState targetState;

	private String targetStateId;

	public Transition(String id, String targetStateId) {
		Assert.notNull(id, "The id property is required");
		Assert.notNull(targetStateId, "The targetStateId property is required");
		this.eventIdCriteria = createDefaultEventIdCriteria(id);
		this.targetStateId = targetStateId;
	}

	public Transition(Constraint eventIdCriteria, String targetStateId) {
		Assert.notNull(eventIdCriteria, "The eventIdCriteria property is required");
		Assert.notNull(targetStateId, "The targetStateId property is required");
		this.eventIdCriteria = eventIdCriteria;
		this.targetStateId = targetStateId;
	}

	protected TransitionableState getSourceState() {
		return sourceState;
	}

	protected void setSourceState(TransitionableState owningState) {
		if (this.sourceState!=null && this.sourceState!=owningState) {
			throw new IllegalArgumentException("You cannot change the owning state of a transition");
		}
		this.sourceState = owningState;
	}

	protected AbstractState getTargetState() throws NoSuchFlowStateException {
		synchronized (this) {
			if (this.targetState != null) {
				return this.targetState;
			}
		}
		AbstractState targetState = getSourceState().getFlow().getRequiredState(getTargetStateId());
		synchronized (this) {
			this.targetState = targetState;
		}
		return this.targetState;
	}

	protected Constraint createDefaultEventIdCriteria(final String id) {
		if (WILDCARD_EVENT_ID.equals(id)) {
			return WILDCARD_EVENT_CRITERIA;
		}
		else {
			return new Constraint() {
				public boolean test(Object eventId) {
					return id.equals(eventId);
				}
	
				public String toString() {
					return id;
				}
			};
		}
	}

	public Constraint getEventIdCriteria() {
		return this.eventIdCriteria;
	}

	public String getTargetStateId() {
		return targetStateId;
	}

	public boolean executesOn(String eventId) {
		return eventIdCriteria.test(eventId);
	}

	protected ModelAndView execute(FlowExecutionStack flowExecution, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			ModelAndView viewDescriptor = getTargetState().enter(flowExecution, request, response);
			if (logger.isDebugEnabled()) {
				if (flowExecution.isActive()) {
					logger.debug("Transition '" + this + "' executed; as a result,  the new state is '"
							+ flowExecution.getCurrentStateId() + "' in flow '"
							+ flowExecution.getActiveFlowId() + "'");
				}
				else {
					logger.debug("Transition '" + this + "' executed; as a result, the flow '"
							+ flowExecution.getRootFlowId() + "' execution has ended");
				}
			}
			return viewDescriptor;
		}
		catch (NoSuchFlowStateException e) {
			throw new CannotExecuteStateTransitionException(this, e);
		}
	}

	public String toString() {
		return new ToStringCreator(this).append("eventIdCriteria", eventIdCriteria).append("toState", targetStateId)
				.toString();
	}

}