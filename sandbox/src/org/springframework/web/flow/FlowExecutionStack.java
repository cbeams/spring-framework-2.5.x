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
import java.util.HashMap;
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
import org.springframework.util.closure.support.Block;
import org.springframework.web.flow.support.RandomGuid;
import org.springframework.web.servlet.ModelAndView;

/**
 * Default implementation of FlowExecution that uses a stack-based data
 * structure to manage flow executions.
 * <p>
 * A flow execution is managed by a client object, typically a web controller.
 * As a result, this client object is responsable for the creation and
 * maintenance of the flow execution.
 * <p>
 * This implementation of FlowExecution is Serializable so it can be safely
 * stored in an HTTP session.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionStack implements FlowExecution, Serializable {

	protected final Log logger = LogFactory.getLog(FlowExecutionStack.class);

	/**
	 * The unique, randomly machine generated flow execution identifier.
	 */
	private String id;

	/**
	 * The execution's root flow; the top level flow that acts as the starting
	 * point for this flow execution.
	 */
	private Flow rootFlow;

	/**
	 * The stack of active, currently executing flow sessions. As subflows are
	 * spawned, they are pushed onto the stack. As they end, they are popped off
	 * the stack.
	 */
	private Stack executingFlowSessions = new Stack();

	/**
	 * The id of the last valid event that was signaled in this flow execution.
	 * Valid means the event indeed maps to a state transition (it is
	 * supported).
	 */
	private String lastEventId;

	/**
	 * The timestamp when the last valid event was signaled.
	 */
	private long lastEventTimestamp;

	/**
	 * A thread-safe listener list, holding listeners monitoring the lifecycle
	 * of this flow execution.
	 */
	private FlowExecutionListenerList listenerList = new FlowExecutionListenerList();

	/**
	 * Create a new flow execution executing for the provided flow.
	 * <p>
	 * The default list of flow execution listeners configured for given flow
	 * will also be notified of this flow execution.
	 * 
	 * @param rootFlow the root flow of this flow execution
	 */
	public FlowExecutionStack(Flow rootFlow) {
		Assert.notNull(rootFlow, "The root flow definition is required");
		this.id = new RandomGuid().toString();
		this.rootFlow = rootFlow;
		//add the list of default execution listeners configured for the flow
		listenerList.add(rootFlow.getFlowExecutionListenerList());
		if (logger.isDebugEnabled()) {
			logger.debug("Created new client execution for flow '" + rootFlow.getId() + "' with id '" + getId() + "'");
		}
	}

	//methods implementing FlowExecutionInfo

	public String getId() {
		return id;
	}

	public String getCaption() {
		return "[sessionId=" + getId() + ", " + getQualifiedActiveFlowId() + "]";
	}

	/**
	 * @return Whether or not this flow execution stack is empty
	 */
	public boolean isEmpty() {
		return executingFlowSessions.isEmpty();
	}

	public boolean isActive() {
		return !isEmpty();
	}

	/**
	 * Check that this flow execution is active and throw an exception it it's
	 * not.
	 */
	protected void assertActive() throws IllegalStateException {
		if (!isActive()) {
			throw new IllegalStateException(
					"No active flow sessions executing - this flow execution has ended (or has never been started)");
		}
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

	public String getRootFlowId() {
		return rootFlow.getId();
	}

	public boolean isRootFlowActive() {
		return executingFlowSessions.size() == 1;
	}

	public String getCurrentStateId() {
		return getActiveFlowSession().getCurrentStateId();
	}

	public String getLastEventId() {
		return lastEventId;
	}

	/**
	 * Set the last event id processed by this flow execution. This will also
	 * update the last event timestamp.
	 * @param eventId The last event id to set
	 */
	public void setLastEventId(String eventId) {
		Assert.notNull(eventId, "The eventId is required");
		this.lastEventId = eventId;
		this.lastEventTimestamp = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug("Event '" + eventId + "' within state '" + getCurrentStateId() + "' for flow '"
					+ getActiveFlowId() + "' signaled");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Set last event id to '" + eventId + "' and updated timestamp to " + this.lastEventTimestamp);
		}
		fireEventSignaled(eventId);
	}

	public long getLastEventTimestamp() {
		return lastEventTimestamp;
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

	//methods implementing FlowExecution

	public FlowExecutionListenerList getListenerList() {
		return listenerList;
	}

	public Flow getActiveFlow() {
		return getActiveFlowSession().getFlow();
	}

	public Flow getRootFlow() {
		return rootFlow;
	}

	public AbstractState getCurrentState() {
		return getActiveFlowSession().getCurrentState();
	}

	/**
	 * Set the state that is currently active in this flow execution.
	 * @param newState The new current state
	 */
	protected void setCurrentState(AbstractState newState) {
		AbstractState previousState = getActiveFlowSession().getCurrentState();
		getActiveFlowSession().setCurrentState(newState);
		fireStateTransitioned(previousState);
	}

	public ModelAndView start(Map input, HttpServletRequest request, HttpServletResponse response) {
		Assert.state(!isActive(), "This flow execution is already started");
		this.lastEventTimestamp = System.currentTimeMillis();
		activate(createFlowSession(this.rootFlow, input));
		return this.rootFlow.getStartState().enter(this, request, response);
	}

	public synchronized ModelAndView signalEvent(String eventId, String stateId, HttpServletRequest request,
			HttpServletResponse response) {
		assertActive();
		if (stateId == null) {
			if (logger.isDebugEnabled()) {
				logger
						.debug("Current state id was not provided in request to signal event '"
								+ eventId
								+ "' in flow "
								+ getCaption()
								+ "' - pulling current state id from session - "
								+ "note: if the user has been using the browser back/forward buttons, the currentState could be incorrect.");
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

	//flow session management helpers

	/**
	 * Spawn a new sub flow in this flow execution stack. This will
	 * <ol>
	 * <li>create a new flow session for given sub flow</li>
	 * <li>activate this new flow session</li>
	 * <li>start the sub flow in its start state</li>
	 * </ol>
	 * @param subFlow The sub flow to spawn
	 * @param input The input parameters used to populate the flow session for
	 *        the subflow
	 * @param request The current HTTP request
	 * @param response The current HTTP response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the newly spawned sub flow.
	 */
	public ModelAndView spawn(Flow subFlow, Map input, HttpServletRequest request, HttpServletResponse response) {
		activate(createFlowSession(subFlow, input));
		return subFlow.getStartState().enter(this, request, response);
	}

	/**
	 * Spawn a new sub flow in this flow execution stack. This will
	 * <ol>
	 * <li>create a new flow session for given sub flow</li>
	 * <li>activate this new flow session</li>
	 * <li>start the sub flow in specified state</li>
	 * </ol>
	 * @param subFlow The sub flow to spawn
	 * @param stateId The id of the state in which the sub flow will start
	 * @param input The input parameters used to populate the flow session for
	 *        the subflow
	 * @param request The current HTTP request
	 * @param response The current HTTP response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the newly spawned sub flow.
	 * @throws NoSuchFlowStateException If there is no state with specified id
	 *         in the subflow
	 */
	public ModelAndView spawn(Flow subFlow, String stateId, Map input, HttpServletRequest request,
			HttpServletResponse response) throws NoSuchFlowStateException {
		activate(createFlowSession(subFlow, input));
		return subFlow.getRequiredTransitionableState(stateId).enter(this, request, response);
	}

	/**
	 * Create a new flow session object. Subclasses can override this to return
	 * a special implementation if required.
	 * @param flow The flow that should be associated with the flow session
	 * @param input The input parameters used to populate the flow session
	 * @return The newly created flow session
	 */
	public FlowSession createFlowSession(Flow flow, Map input) {
		return new FlowSession(flow, input);
	}

	/**
	 * Activate given flow session in this flow execution stack. This will push
	 * the flow session onto the stack and mark it as the active flow session.
	 * @param flowSession the flow session to activate
	 */
	public void activate(FlowSession flowSession) {
		if (executingFlowSessions.contains(flowSession)) {
			throw new IllegalArgumentException("Flow session '" + flowSession + "' has already been activated before");
		}
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

	/**
	 * End the active flow session of this flow execution. This will pop the top
	 * element from the stack and activate the now top flow session.
	 * @return the flow session that ended
	 */
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

	/**
	 * @return The flow session associated with the root flow
	 */
	public FlowSession getRootFlowSession() {
		assertActive();
		return (FlowSession)executingFlowSessions.get(0);
	}

	/**
	 * @return The currently active flow session
	 */
	public FlowSession getActiveFlowSession() {
		assertActive();
		return (FlowSession)executingFlowSessions.peek();
	}

	//

	/**
	 * Returns the name of the flow execution attribute, a special index to
	 * lookup this flow execution as an attribute.
	 * <p>
	 * The flow execution will also be exposed in the model returned from the
	 * <code>getModel()</code> method under this name.
	 * @return This flow execution's name
	 */
	protected Object getFlowExecutionAttributeName() {
		return ATTRIBUTE_NAME;
	}

	/**
	 * The flow execution id will be exposed in the model returned from the
	 * <code>getModel()</code> method under this name.
	 * @return This flow execution's id's name
	 */
	protected Object getFlowExecutionIdAttributeName() {
		return FlowConstants.FLOW_EXECUTION_ID_ATTRIBUTE;
	}

	/**
	 * The current state of the flow execution will be exposed in the model
	 * returned from the <code>getModel()</code> method under this name.
	 * @return This flow execution's current state name
	 */
	protected Object getCurrentStateIdAttributeName() {
		return FlowConstants.CURRENT_STATE_ID_ATTRIBUTE;
	}

	/**
	 * Returns the data model for this flow execution, suitable for exporting to
	 * web views.
	 * @return Map of model attributes for this flow execution.
	 */
	public Map getModel() {
		Map model = new HashMap(getActiveFlowSession().getModel());
		//the flow execution itself is available in the model
		model.put(getFlowExecutionAttributeName(), this);
		// these are added for convenience for views that aren't easily
		// javabean aware
		model.put(getFlowExecutionIdAttributeName(), getId());
		model.put(getCurrentStateIdAttributeName(), getCurrentStateId());
		return model;
	}

	//methods implementing AttributesAccessor

	public Object getAttribute(String attributeName) {
		if (attributeName.equals(getFlowExecutionAttributeName())) {
			return this;
		}
		else {
			return getActiveFlowSession().getAttribute(attributeName);
		}
	}

	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		if (attributeName.equals(getFlowExecutionAttributeName())) {
			Assert.isInstanceOf(requiredType, this);
			return this;
		}
		else {
			return getActiveFlowSession().getAttribute(attributeName, requiredType);
		}
	}

	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		if (attributeName.equals(getFlowExecutionAttributeName())) {
			return this;
		}
		else {
			return getActiveFlowSession().getRequiredAttribute(attributeName);
		}
	}

	public Object getRequiredAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		if (attributeName.equals(getFlowExecutionAttributeName())) {
			Assert.isInstanceOf(requiredType, this);
			return this;
		}
		else {
			return getActiveFlowSession().getRequiredAttribute(attributeName, requiredType);
		}
	}

	public void assertAttributePresent(String attributeName) {
		getActiveFlowSession().assertAttributePresent(attributeName);
	}

	public void assertAttributePresent(String attributeName, Class requiredType) {
		getActiveFlowSession().assertAttributePresent(attributeName, requiredType);
	}

	public void assertInTransaction(HttpServletRequest request, boolean reset) throws IllegalStateException {
		getActiveFlowSession().assertInTransaction(request, reset);
	}

	public boolean containsAttribute(String attributeName) {
		return getActiveFlowSession().containsAttribute(attributeName);
	}

	public boolean containsAttribute(String attributeName, Class requiredType) {
		return getActiveFlowSession().containsAttribute(attributeName, requiredType);
	}

	public boolean inTransaction(HttpServletRequest request, boolean reset) {
		return getActiveFlowSession().inTransaction(request, reset);
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

	//methods implementing MutableAttributesAccessor

	public void setAttribute(String attributeName, Object attributeValue) {
		if (getFlowExecutionAttributeName().equals(attributeName)) {
			throw new IllegalArgumentException("Attribute name '" + getFlowExecutionAttributeName()
					+ "' is reserved for internal use only");
		}
		getActiveFlowSession().setAttribute(attributeName, attributeValue);
	}

	public void setAttributes(Map attributes) {
		if (attributes.containsKey(getFlowExecutionAttributeName())) {
			throw new IllegalArgumentException("Attribute name '" + getFlowExecutionAttributeName()
					+ "' is reserved for internal use only");
		}
		getActiveFlowSession().setAttributes(attributes);
	}

	public void removeAttribute(String attributeName) {
		if (getFlowExecutionAttributeName().equals(attributeName)) {
			throw new IllegalArgumentException("Attribute name '" + getFlowExecutionAttributeName()
					+ "' is reserved for internal use only");
		}
		getActiveFlowSession().removeAttribute(attributeName);
	}

	public void beginTransaction() {
		getActiveFlowSession().beginTransaction();
	}

	public void endTransaction() {
		getActiveFlowSession().endTransaction();
	}

	//lifecycle event management

	/**
	 * Notify all interested listeners that flow execution has started.
	 */
	protected void fireStarted() {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow session execution started event to " + getListenerList().size()
					+ " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).started(FlowExecutionStack.this);
			}
		});
	}

	/**
	 * Notify all interested listeners that a request was submitted to this flow
	 * execution.
	 */
	protected void fireRequestSubmitted(final HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request submitted event to " + getListenerList().size() + " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).requestSubmitted(FlowExecutionStack.this, request);
			}
		});
	}

	/**
	 * Notify all interested listeners that this flow execution finished
	 * processing a request.
	 */
	protected void fireRequestProcessed(final HttpServletRequest request) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request processed event to " + getListenerList().size() + " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).requestProcessed(FlowExecutionStack.this, request);
			}
		});
	}

	/**
	 * Notify all interested listeners that an event was signaled in this flow
	 * execution.
	 */
	protected void fireEventSignaled(final String eventId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing event signaled event to " + getListenerList().size() + " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).eventSignaled(FlowExecutionStack.this, eventId);
			}
		});
	}

	/**
	 * Notify all interested listeners that a state transition happened in this
	 * flow execution.
	 */
	protected void fireStateTransitioned(final AbstractState previousState) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing state transitioned event to " + getListenerList().size() + " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).stateTransitioned(FlowExecutionStack.this, previousState, getCurrentState());
			}
		});
	}

	/**
	 * Notify all interested listeners that a sub flow was spawned in this flow
	 * execution.
	 */
	protected void fireSubFlowSpawned() {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing sub flow session execution started event to " + getListenerList().size()
					+ " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).subFlowSpawned(FlowExecutionStack.this);
			}
		});
	}

	/**
	 * Notify all interested listeners that a sub flow ended in this flow
	 * execution.
	 */
	protected void fireSubFlowEnded(final FlowSession endedSession) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing sub flow session ended event to " + getListenerList().size() + " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).subFlowEnded(FlowExecutionStack.this, endedSession);
			}
		});
	}

	/**
	 * Notify all interested listeners that flow execution has ended.
	 */
	protected void fireEnded(final FlowSession endingRootFlowSession) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow session execution ended event to " + getListenerList().size()
					+ " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).ended(FlowExecutionStack.this, endingRootFlowSession);
			}
		});
	}

	public String toString() {
		return executingFlowSessions.isEmpty() ? "[Empty FlowExecutionStack " + getId() + "; no flows are active]"
				: new ToStringCreator(this).append("id", getId()).append("activeFlowId", getActiveFlowId()).append(
						"currentStateId", getCurrentStateId()).append("rootFlow", isRootFlowActive()).append(
						"executingFlowSessions", executingFlowSessions).toString();
	}
}