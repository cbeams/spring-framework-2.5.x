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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.RandomGuid;
import org.springframework.util.StringUtils;
import org.springframework.util.ToStringCreator;

/**
 * Default implementation of FlowExecution that uses a stack-based data
 * structure to manage {@link org.springframework.web.flow.FlowSession flow sessions}.
 * <p>
 * This implementation of FlowExecution is serializable so it can be safely
 * stored in an HTTP session.
 * <p>
 * Note: this implementation synchronizes both execution entry points
 * (@link #start(Event)) and {@link #signalEvent(Event)}.
 * They are locked on a per client basis for this flow execution.
 * Synchronization prevents a client from being able to signal other events
 * before previously signaled ones have processed in-full, preventing possible
 * race conditions.
 * 
 * @see org.springframework.web.flow.FlowSession
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionStack implements FlowExecutionMBean, FlowExecution, Serializable {

	private static final long serialVersionUID = 3258688806151469104L;

	protected final Log logger = LogFactory.getLog(FlowExecutionStack.class);

	/**
	 * The unique, random, machine-generated flow execution identifier.
	 */
	private String id;

	/**
	 * The time at which this object was created.
	 */
	private long creationTimestamp;

	/**
	 * The execution's root flow; the top level flow that acts as the starting
	 * point for this flow execution.
	 */
	private transient Flow rootFlow;

	/**
	 * Set only on deserialization so this object can be fully reconstructed.
	 */
	private String rootFlowId;

	/**
	 * The id of the last valid event that was signaled in this flow execution.
	 * Valid means the event indeed mapped to a state transition (it is
	 * supported).
	 * <p>
	 * Note that we're not storing the event itself because that would cause
	 * serialisation related issues.
	 */
	private String lastEventId;

	/**
	 * The timestamp when the last valid event was signaled.
	 */
	private long lastEventTimestamp;

	/**
	 * The stack of active, currently executing flow sessions. As subflows are
	 * spawned, they are pushed onto the stack. As they end, they are popped off
	 * the stack.
	 */
	private Stack executingFlowSessions = new Stack();

	/**
	 * A thread-safe listener list, holding listeners monitoring the lifecycle
	 * of this flow execution.
	 */
	private transient FlowExecutionListenerList listenerList = new FlowExecutionListenerList();

	/**
	 * Create a new flow execution executing the provided flow.
	 * <p>
	 * The default list of flow execution listeners configured for given flow
	 * will also be notified of this flow execution.
	 * @param rootFlow the root flow of this flow execution
	 */
	public FlowExecutionStack(Flow rootFlow) {
		Assert.notNull(rootFlow, "The root flow definition is required");
		this.id = new RandomGuid().toString();
		this.creationTimestamp = System.currentTimeMillis();
		this.rootFlow = rootFlow;
		// add the list of default execution listeners configured for the flow
		listenerList.add(rootFlow.getFlowExecutionListenerList());
		if (logger.isDebugEnabled()) {
			logger.debug("Created new client execution for flow '" + rootFlow.getId() + "' with id '" + getId() + "'");
		}
	}

	// methods implementing FlowExecutionMBean
	
	public String getId() {
		return id;
	}

	public long getCreationTimestamp() {
		return this.creationTimestamp;
	}

	public long getUptime() {
		return System.currentTimeMillis() - this.creationTimestamp;
	}

	public String getCaption() {
		return "[" + FlowConstants.FLOW_EXECUTION_ID_ATTRIBUTE + "=" + getId() + ", " + getQualifiedActiveFlowId() + "]";
	}

	/**
	 * Returns whether or not this flow execution stack is empty.
	 */
	public boolean isEmpty() {
		return executingFlowSessions.isEmpty();
	}

	public boolean isActive() {
		return !isEmpty();
	}

	/**
	 * Check that this flow execution is active and throw an exception
	 * if it's not.
	 */
	protected void assertActive() throws IllegalStateException {
		if (!isActive()) {
			throw new IllegalStateException(
					"No active flow sessions executing - this flow execution has ended (or has never been started)");
		}
	}

	public String getActiveFlowId() throws IllegalStateException {
		return getActiveFlowSession().getFlow().getId();
	}

	public String getQualifiedActiveFlowId() throws IllegalStateException {
		assertActive();
		Iterator it = executingFlowSessions.iterator();
		StringBuffer qualifiedName = new StringBuffer(128);
		while (it.hasNext()) {
			FlowSession session = (FlowSession)it.next();
			qualifiedName.append(session.getFlow().getId());
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
				stack.add(session.getFlow().getId());
			}
			return (String[])stack.toArray(new String[stack.size()]);
		}
	}

	public String getRootFlowId() {
		return rootFlow.getId();
	}

	public boolean isRootFlowActive() {
		return executingFlowSessions.size() == 1;
	}

	public String getCurrentStateId() throws IllegalStateException {
		return getActiveFlowSession().getCurrentState().getId();
	}

	public String getLastEventId() {
		return this.lastEventId;
	}

	/**
	 * Set the last event processed by this flow execution. This will also
	 * update the last event timestamp, which management clients can use to
	 * monitor the activity of this execution to detect idle status.
	 * @param lastEvent the last event to set
	 */
	public void setLastEvent(Event lastEvent) {
		Assert.notNull(lastEvent, "The event is required");
		this.lastEventId = lastEvent.getId();
		this.lastEventTimestamp = lastEvent.getTimestamp();
		if (logger.isDebugEnabled()) {
			logger.debug("Set last event id to '" + this.lastEventId + "' and updated timestamp to " + this.lastEventTimestamp);
		}
	}

	public long getLastEventTimestamp() {
		return this.lastEventTimestamp;
	}

	// methods implementing FlowExecution
	
	public FlowExecutionListenerList getListenerList() {
		return listenerList;
	}

	public synchronized ViewDescriptor start(Event event) throws IllegalStateException {
		Assert.state(!isActive(), "This flow execution is already started");
		// create a new flow session for the root flow and activate it
		createAndActivateFlowSession(this.rootFlow, event.getParameters());
		// execute the event
		InternalRequestContext context = new InternalRequestContext(event, this);
		context.fireRequestSubmitted(event);
		context.fireStarted();
		setLastEvent(event);
		ViewDescriptor viewDescriptor = this.rootFlow.getStartState().enter(context);
		context.fireRequestProcessed(event);
		return viewDescriptor;
	}

	public synchronized ViewDescriptor signalEvent(Event event) throws NoSuchTransitionException,
			IllegalStateException {
		assertActive();
		String eventId = event.getId();
		String stateId = event.getStateId();
		if (!StringUtils.hasText(stateId)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Current state id was not provided in request to signal event '"
						+ eventId
						+ "' in flow "
						+ getCaption()
						+ "' -- pulling current state id from session -- "
						+ "note: if the user has been using the browser back/forward buttons, the currentState could be incorrect.");
			}
			stateId = getCurrentStateId();
		}
		TransitionableState state = getActiveFlow().getRequiredTransitionableState(stateId);
		if (!state.equals(getCurrentState())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Event '" + eventId + "' in state '" + state.getId()
						+ "' was signaled by client; however the current flow execution state is '"
						+ getCurrentStateId() + "'; updating current state to '" + state.getId() + "'");
			}
			setCurrentState(state);
		}
		// execute the event
		InternalRequestContext context = new InternalRequestContext(event, this);
		context.fireRequestSubmitted(event);
		context.setLastEvent(event);
		ViewDescriptor viewDescriptor = state.executeTransition(context);
		context.fireRequestProcessed(event);
		return viewDescriptor;
	}

	// flow session management helpers

	/**
	 * Returns the flow of the currently active flow session.
	 */
	public Flow getActiveFlow() {
		return getActiveFlowSession().getFlow();
	}

	/**
	 * Returns the currently active flow session.
	 * @throws IllegalStateException this execution is not active
	 */
	public FlowSession getActiveFlowSession() throws IllegalStateException {
		assertActive();
		return (FlowSession)executingFlowSessions.peek();
	}
	
	/**
	 * Returns the parent flow session of the currently active flow session.
	 * @return the parent flow session
	 * @throws IllegalArgumentException when this execution is not active
	 *         or when the current flow session has no parent (e.g. is the
	 *         root flow session)
	 */
	public FlowSession getParentFlowSession() throws IllegalArgumentException {
		assertActive();
		Assert.state(!isRootFlowActive(), "There is no parent flow session for the currently active flow session");
		return (FlowSession)executingFlowSessions.get(executingFlowSessions.size() - 2);
	}
	
	/**
	 * Returns the root flow of this flow execution.
	 */
	public Flow getRootFlow() {
		return rootFlow;
	}

	/**
	 * Returns the flow session associated with the root flow.
	 * @throws IllegalStateException this execution is not active
	 */
	public FlowSession getRootFlowSession() throws IllegalStateException {
		assertActive();
		return (FlowSession)executingFlowSessions.get(0);
	}
	
	/**
	 * Returns the current state of the active flow session.
	 */
	public State getCurrentState() {
		return getActiveFlowSession().getCurrentState();
	}

	/**
	 * Set the state that is currently active in this flow execution.
	 * @param newState the new current state
	 */
	protected void setCurrentState(State newState) {
		getActiveFlowSession().setCurrentState(newState);
	}

	/**
	 * Create a new flow session object. Subclasses can override this to return
	 * a special implementation if required.
	 * @param flow the flow that should be associated with the flow session
	 * @param input the input parameters used to populate the flow session
	 * @return the newly created flow session
	 */
	protected FlowSession createFlowSession(Flow flow, Map input) {
		return new FlowSession(flow, input);
	}

	/**
	 * Create a new flow session and activate in this flow execution stack.
	 * This will push the flow session onto the stack and mark it as the
	 * active flow session.
	 * @param subFlow the flow that should be associated with the flow session
	 * @param input the input parameters used to populate the flow session
	 * @return the created and activated flow session
	 */
	protected FlowSession createAndActivateFlowSession(Flow subFlow, Map input) {
		FlowSession flowSession = createFlowSession(subFlow, input);
		if (!executingFlowSessions.isEmpty()) {
			getActiveFlowSession().setStatus(FlowSessionStatus.SUSPENDED);
		}
		executingFlowSessions.push(flowSession);
		flowSession.setStatus(FlowSessionStatus.ACTIVE);
		return flowSession;
	}

	/**
	 * End the active flow session of this flow execution. This will pop the top
	 * element from the stack and activate the new top flow session.
	 * @return the flow session that ended
	 */
	protected FlowSession endActiveFlowSession() {
		FlowSession endingSession = (FlowSession)executingFlowSessions.pop();
		endingSession.setStatus(FlowSessionStatus.ENDED);
		if (!executingFlowSessions.isEmpty()) {
			getActiveFlowSession().setStatus(FlowSessionStatus.ACTIVE);
		}
		return endingSession;
	}
	
	// custom serialization

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(this.id);
		out.writeObject(this.getRootFlow().getId());
		out.writeObject(this.lastEventId);
		out.writeLong(this.lastEventTimestamp);
		out.writeObject(this.executingFlowSessions);
	}

	private void readObject(ObjectInputStream in) throws OptionalDataException, ClassNotFoundException, IOException {
		this.id = (String)in.readObject();
		this.rootFlowId = (String)in.readObject();
		this.lastEventId = (String)in.readObject();
		this.lastEventTimestamp = in.readLong();
		this.executingFlowSessions = (Stack)in.readObject();
	}

	public synchronized void rehydrate(FlowLocator flowLocator, FlowExecutionListener[] listeners) {
		// implementation note: we cannot integrate this code into the
		// readObject() method since we need the flow locator and listener list!
		if (this.rootFlow != null) {
			// nothing to do, we're already hydrated
			return;
		}
		Assert.notNull(rootFlowId,
				"The root flow id was not set during deserialization: cannot restore"
				+ " -- was this flow execution deserialized properly?");
		this.rootFlow = flowLocator.getFlow(rootFlowId);
		this.rootFlowId = null;
		// rehydrate all flow sessions
		Iterator it = this.executingFlowSessions.iterator();
		while (it.hasNext()) {
			FlowSession session = (FlowSession)it.next();
			session.rehydrate(flowLocator);
		}
		if (isActive()) {
			// sanity check
			Assert.isTrue(getRootFlow() == getRootFlowSession().getFlow(),
					"The root flow of the execution should be the same as the flow in the root flow session");
		}
		this.listenerList = new FlowExecutionListenerList();
		this.listenerList.add(this.rootFlow.getFlowExecutionListenerList());
		this.listenerList.add(listeners);
	}

	public String toString() {
		if (isEmpty()) {
			return "[Empty FlowExecutionStack '" + getId() + "'; no flows are active]";
		}
		else {
			return new ToStringCreator(this).append("id", getId()).append("activeFlowId", getActiveFlowId())
				.append("currentStateId", getCurrentStateId()).append("rootFlow", getRootFlow())
				.append("executingFlowSessions", executingFlowSessions).toString();
		}
	}
}