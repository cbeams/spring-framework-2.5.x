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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;
import org.springframework.util.closure.Constraint;
import org.springframework.util.closure.ProcessTemplate;
import org.springframework.util.closure.support.Block;
import org.springframework.web.flow.support.RandomGUID;
import org.springframework.web.servlet.ModelAndView;

/**
 * Default implementation of FlowExecution that uses a stack-based data
 * structure to manage flow sessions.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionStack implements FlowExecution, Serializable {

	private static final Log logger = LogFactory.getLog(FlowExecutionStack.class);

	private String id;

	private Flow rootFlow;

	private Stack executingFlowSessions = new Stack();

	private String lastEventId;

	private long lastEventTimestamp;

	public FlowExecutionStack(Flow rootFlow) {
		Assert.notNull(rootFlow, "The root flow definition is required");
		this.id = new RandomGUID().toString();
		this.rootFlow = rootFlow;
	}

	public String getId() {
		return id;
	}

	public ModelAndView start(Map input, HttpServletRequest request, HttpServletResponse response) {
		Assert.state(!isActive(), "This flow execution is already started");
		activate(new FlowSession(this.rootFlow, input));
		return this.rootFlow.getStartState().enter(this, request, response);
	}

	public ModelAndView signalEvent(String eventId, String stateId, HttpServletRequest request,
			HttpServletResponse response) {
		assertActive();
		if (stateId == null) {
			if (logger.isInfoEnabled()) {
				logger
						.info("Current state id was not provided in request to signal event '"
								+ eventId
								+ "' in flow "
								+ getCaption()
								+ "' - pulling current state id from session - "
								+ "note: if the user has been using the with browser back/forward buttons in browser, the currentState could be incorrect.");
			}
			stateId = getCurrentStateId();
		}
		fireRequestSubmitted(request);
		TransitionableState state = getActiveFlow().getRequiredTransitionableState(stateId);
		if (!state.equals(getCurrentState())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Event '" + eventId + "' in state '" + state.getId()
						+ "' was signaled by client; however the current flow execution state is '"
						+ getCurrentStateId() + "'; updating current state to '" + state.getId() + "'");
			}
			setCurrentState(state);
		}
		ModelAndView view = state.signalEvent(eventId, this, request, response);
		fireRequestProcessed(request);
		return view;
	}

	public ModelAndView spawn(Flow flow, Map input, HttpServletRequest request, HttpServletResponse response) {
		activate(new FlowSession(flow, input));
		return flow.getStartState().enter(this, request, response);
	}

	public ModelAndView spawn(Flow flow, String stateId, Map input, HttpServletRequest request,
			HttpServletResponse response) {
		activate(new FlowSession(flow, input));
		return flow.getRequiredTransitionableState(stateId).enter(this, request, response);
	}

	public void activate(FlowSession flowSession) {
		if (!executingFlowSessions.isEmpty()) {
			getActiveFlowSession().setStatus(FlowSessionStatus.SUSPENDED);
		}
		executingFlowSessions.push(flowSession);
		flowSession.setStatus(FlowSessionStatus.ACTIVE);
		if (isRootFlowActive()) {
			fireStarted();
		}
		else {
			fireSubFlowSpawned();
		}
	}

	public String getCaption() {
		return "[sessionId=" + getId() + ", " + getQualifiedActiveFlowId() + "]";
	}

	public boolean isActive() {
		return !isEmpty();
	}

	public boolean isEmpty() {
		return executingFlowSessions.isEmpty();
	}

	public String getRootFlowId() {
		return rootFlow.getId();
	}

	public Flow getRootFlow() {
		return rootFlow;
	}

	public FlowSession getRootFlowSession() {
		assertActive();
		return (FlowSession)executingFlowSessions.get(0);
	}

	private void assertActive() {
		if (!isActive()) {
			throw new IllegalStateException(
					"No active flow sessions executing - this flow session execution has ended (or has never been started)");
		}
	}

	/**
	 * Are we currently in the root flow? There can be any depth of nested
	 * subflows below this, but sometimes the first subflow below the root may
	 * require special treatment.
	 * @return whether we're in the root flow
	 */
	public boolean isRootFlowActive() {
		return executingFlowSessions.size() == 1;
	}

	public Flow getActiveFlow() {
		return getActiveFlowSession().getFlow();
	}

	public String getActiveFlowId() {
		return getActiveFlowSession().getFlowId();
	}

	public String getQualifiedActiveFlowId() {
		assertActive();
		Iterator it = executingFlowSessions.iterator();
		StringBuffer qualifiedName = new StringBuffer(128);
		while (it.hasNext()) {
			FlowSession session = (FlowSession)it.next();
			qualifiedName.append(session.getFlowId());
			if (it.hasNext()) {
				qualifiedName.append('.');
			}
		}
		return qualifiedName.toString();
	}

	public FlowSession getActiveFlowSession() {
		assertActive();
		return (FlowSession)executingFlowSessions.peek();
	}

	public String[] getFlowIdStack() {
		if (isEmpty()) {
			return new String[0];
		}
		else {
			Iterator it = executingFlowSessions.iterator();
			List stack = new ArrayList(executingFlowSessions.size());
			while (it.hasNext()) {
				FlowSession session = (FlowSession)it.next();
				stack.add(session.getFlowId());
			}
			return (String[])stack.toArray(new String[0]);
		}
	}

	public AbstractState getCurrentState() {
		return getActiveFlowSession().getCurrentState();
	}

	public String getCurrentStateId() {
		return getActiveFlowSession().getCurrentStateId();
	}

	protected void setCurrentState(AbstractState newState) {
		AbstractState previousState = getActiveFlowSession().getCurrentState();
		getActiveFlowSession().setCurrentState(newState);
		fireStateTransitioned(previousState);
	}

	public String getLastEventId() {
		return lastEventId;
	}

	public long getLastEventTimestamp() {
		return lastEventTimestamp;
	}

	public void setLastEventId(String eventId) {
		Assert.notNull(eventId, "The eventId is required");
		this.lastEventId = eventId;
		this.lastEventTimestamp = new Date().getTime();
		if (logger.isDebugEnabled()) {
			logger.debug("Event '" + eventId + "' within state '" + getCurrentStateId() + "' for flow '"
					+ getActiveFlowId() + "' signaled");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Set last event id to '" + eventId + "' and updated timestamp to " + this.lastEventTimestamp);
		}
		fireEventSignaled(eventId);
	}

	public Map getAttributes() {
		return getActiveFlowSession().getAttributes();
	}

	public boolean exists(String flowId) {
		Iterator it = executingFlowSessions.iterator();
		while (it.hasNext()) {
			FlowSession fs = (FlowSession)it.next();
			if (fs.getFlowId().equals(flowId)) {
				return true;
			}
		}
		return false;
	}

	public FlowSessionStatus getStatus(String flowId) throws IllegalArgumentException {
		Iterator it = executingFlowSessions.iterator();
		while (it.hasNext()) {
			FlowSession fs = (FlowSession)it.next();
			if (fs.getFlowId().equals(flowId)) {
				return fs.getStatus();
			}
		}
		throw new IllegalArgumentException("No such session for flow '" + flowId + "'");
	}

	public Object getAttribute(String attributeName) {
		if (attributeName.equals(ATTRIBUTE_NAME)) {
			return this;
		}
		else {
			return getActiveFlowSession().getAttribute(attributeName);
		}
	}

	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		if (attributeName.equals(ATTRIBUTE_NAME)) {
			Assert.isInstanceOf(requiredType, this);
			return this;
		}
		else {
			return getActiveFlowSession().getAttribute(attributeName, requiredType);
		}
	}

	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		if (attributeName.equals(ATTRIBUTE_NAME)) {
			return this;
		}
		else {
			return getActiveFlowSession().getRequiredAttribute(attributeName);
		}
	}

	public Object getRequiredAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		if (attributeName.equals(ATTRIBUTE_NAME)) {
			Assert.isInstanceOf(requiredType, this);
			return this;
		}
		else {
			return getActiveFlowSession().getRequiredAttribute(attributeName, requiredType);
		}
	}

	public boolean containsAttribute(String attributeName) {
		return getActiveFlowSession().containsAttribute(attributeName);
	}

	public boolean containsAttribute(String attributeName, Class requiredType) {
		return getActiveFlowSession().containsAttribute(attributeName, requiredType);
	}

	public void assertAttributePresent(String attributeName) {
		getActiveFlowSession().assertAttributePresent(attributeName);
	}

	public void assertAttributePresent(String attributeName, Class requiredType) {
		getActiveFlowSession().assertAttributePresent(attributeName, requiredType);
	}

	public Collection attributeNames() {
		return getActiveFlowSession().attributeNames();
	}

	public Collection attributeValues() {
		return getActiveFlowSession().attributeValues();
	}

	public Collection attributeEntries() {
		return getActiveFlowSession().attributeEntries();
	}

	public Collection findAttributes(Constraint criteria) {
		return getActiveFlowSession().findAttributes(criteria);
	}

	public void setAttribute(String attributeName, Object attributeValue) {
		if (attributeName.equals(ATTRIBUTE_NAME)) {
			throw new IllegalArgumentException("Attribute name '" + ATTRIBUTE_NAME
					+ "' is reserved for internal use only");
		}
		getActiveFlowSession().setAttribute(attributeName, attributeValue);
	}

	public void setAttributes(Map attributes) {
		getActiveFlowSession().setAttributes(attributes);
	}

	public void removeAttribute(String attributeName) {
		getActiveFlowSession().removeAttribute(attributeName);
	}

	public FlowSession endActiveSession() {
		FlowSession endingSession = (FlowSession)executingFlowSessions.pop();
		endingSession.setStatus(FlowSessionStatus.ENDED);
		if (!executingFlowSessions.isEmpty()) {
			getActiveFlowSession().setStatus(FlowSessionStatus.ACTIVE);
			fireSubFlowEnded(endingSession);
		}
		else {
			fireEnded(endingSession);
		}
		return endingSession;
	}

	// lifecycle event publishers

	protected ProcessTemplate getListenerIterator() {
		return getRootFlow().getFlowExecutionListenerIteratorTemplate();
	}

	protected int getListenerCount() {
		return getRootFlow().getFlowExecutionListenerCount();
	}

	protected void fireStarted() {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow session execution started event to " + getListenerCount() + " listener(s)");
		}
		getListenerIterator().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).started(FlowExecutionStack.this);
			}
		});
	}

	protected void fireRequestSubmitted(final HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request submitted event to " + getListenerCount() + " listener(s)");
		}
		getListenerIterator().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).requestSubmitted(FlowExecutionStack.this, request);
			}
		});
	}

	protected void fireRequestProcessed(final HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request processed event to " + getListenerCount() + " listener(s)");
		}
		getListenerIterator().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).requestProcessed(FlowExecutionStack.this, request);
			}
		});
	}

	protected void fireEventSignaled(final String eventId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing event signaled event to " + getListenerCount() + " listener(s)");
		}
		getListenerIterator().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).eventSignaled(FlowExecutionStack.this, eventId);
			}
		});
	}

	protected void fireStateTransitioned(final AbstractState previousState) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing state transitioned event to " + getListenerCount() + " listener(s)");
		}
		getListenerIterator().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).stateTransitioned(FlowExecutionStack.this, previousState, getCurrentState());
			}
		});
	}

	protected void fireSubFlowSpawned() {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing sub flow session execution started event to " + getListenerCount()
					+ " listener(s)");
		}
		getListenerIterator().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).subFlowSpawned(FlowExecutionStack.this);
			}
		});
	}

	protected void fireSubFlowEnded(final FlowSession endedSession) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing sub flow session ended event to " + getListenerCount() + " listener(s)");
		}
		getListenerIterator().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).subFlowEnded(FlowExecutionStack.this, endedSession);
			}
		});
	}

	protected void fireEnded(final FlowSession endingRootFlowSession) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow session execution ended event to "
					+ endingRootFlowSession.getFlow().getFlowExecutionListenerCount() + " listener(s)");
		}
		getListenerIterator().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).ended(FlowExecutionStack.this, endingRootFlowSession);
			}
		});
	}

	public String toString() {
		return executingFlowSessions.isEmpty() ? "[Empty FlowSessionExecutionStack " + getId()
				+ "; no flows are active]" : new ToStringCreator(this).append("id", getId()).append("activeFlowId",
				getActiveFlowId()).append("currentStateId", getCurrentStateId()).append("rootFlow", isRootFlowActive())
				.append("executingFlowSessions", executingFlowSessions).toString();
	}
}