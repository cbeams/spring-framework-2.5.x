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
package org.springframework.web.flow.execution.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowNavigationException;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.FlowSessionStatus;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.State;
import org.springframework.web.flow.StateContext;
import org.springframework.web.flow.TransitionableState;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.execution.FlowExecution;
import org.springframework.web.flow.execution.FlowExecutionListener;
import org.springframework.web.flow.execution.FlowExecutionListenerList;
import org.springframework.web.flow.execution.FlowLocator;
import org.springframework.web.flow.execution.TokenTransactionSynchronizer;
import org.springframework.web.flow.execution.TransactionSynchronizer;

/**
 * Default implementation of FlowExecution that uses a stack-based data
 * structure to manage
 * {@link org.springframework.web.flow.FlowSession flow sessions}.
 * <p>
 * This implementation of FlowExecution is serializable so it can be safely
 * stored in an HTTP session.
 * <p>
 * Note: this implementation synchronizes both execution entry points
 * {@link #start(Event)} and {@link #signalEvent(Event)}. They are locked on a
 * per client basis for this flow execution. Synchronization prevents a client
 * from being able to signal other events before previously signaled ones have
 * processed in-full, preventing possible race conditions.
 *
 * @see org.springframework.web.flow.FlowSession
 * @see org.springframework.web.flow.execution.impl.FlowSessionImpl 
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowExecutionImpl implements FlowExecution, Serializable {

	// static logger because FlowExecutionImpl objects can be serialized
	// and then restored
	protected static final Log logger = LogFactory.getLog(FlowExecutionImpl.class);

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
	 * The id of the last event that was signaled in this flow execution.
	 * <p>
	 * Note that we're not storing the event itself because that would cause
	 * serialisation related issues.
	 */
	private String lastEventId;

	/**
	 * The timestamp when the last request to manipulate this flow execution was processed.
	 */
	private long lastRequestTimestamp;

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
	 * The application transaction synchronization strategy to use.
	 */
	private transient TransactionSynchronizer transactionSynchronizer;
	
	/**
	 * Create a new flow execution executing the provided flow.
	 * @param rootFlow the root flow of this flow execution
	 */
	public FlowExecutionImpl(Flow rootFlow) {
		this(rootFlow, null, new TokenTransactionSynchronizer());
	}
	
	/**
	 * Create a new flow execution executing the provided flow.
	 * @param rootFlow the root flow of this flow execution
	 * @param listeners the listeners interested in flow execution lifecycle events
	 * @param transactionSynchronizer the application transaction synchronization
	 *        strategy to use
	 */
	public FlowExecutionImpl(Flow rootFlow, FlowExecutionListener[] listeners,
			TransactionSynchronizer transactionSynchronizer) {
		Assert.notNull(rootFlow, "The root flow definition is required");
		Assert.notNull(transactionSynchronizer, "The transaction synchronizer is required");
		this.creationTimestamp = System.currentTimeMillis();
		this.rootFlow = rootFlow;
		this.getListeners().add(listeners);
		this.transactionSynchronizer = transactionSynchronizer;
		if (logger.isDebugEnabled()) {
			logger.debug("Created new client execution for flow '" + rootFlow.getId() + "'");
		}
	}
	
	/**
	 * Returns the transaction synchronizer in use.
	 */
	public TransactionSynchronizer getTransactionSynchronizer() {
		return transactionSynchronizer;
	}
	
	/**
	 * Set the transaction synchronization strategy to use.
	 */
	protected void setTransactionSynchronizer(TransactionSynchronizer transactionSynchronizer) {
		this.transactionSynchronizer = transactionSynchronizer;
	}
	
	// implementing FlowContext

	public String getCaption() {
		if (isActive()) {
			return "Active execution for flow '" + getRootFlow() + "': [" + getSessionPath() + "]";
		}
		else {
			return "Inactive execution for flow '" + getRootFlow() + "'";
		}
	}

	/**
	 * Helper that return a string representation of the current
	 * flow session stack.
	 */
	private String getSessionPath() {
		if (isActive()) {
			StringBuffer qualifiedName = new StringBuffer(128);
			Iterator it = executingFlowSessions.iterator();
			while (it.hasNext()) {
				FlowSession session = (FlowSession)it.next();
				qualifiedName.append(session.getFlow().getId());
				if (it.hasNext()) {
					qualifiedName.append('.');
				}
			}
			return qualifiedName.toString();
		}
		else {
			return "";
		}
	}

	public long getCreationTimestamp() {
		return this.creationTimestamp;
	}

	public long getUptime() {
		return System.currentTimeMillis() - this.creationTimestamp;
	}
	
	public Flow getRootFlow() {
		return rootFlow;
	}
	
	public Flow getActiveFlow() {
		return getActiveSession().getFlow();
	}

	public State getCurrentState() {
		return getActiveSession().getCurrentState();
	}

	public long getLastRequestTimestamp() {
		return this.lastRequestTimestamp;
	}

	/**
	 * Update the last request timestamp to now.
	 */
	protected void updateLastRequestTimestamp() {
		this.lastRequestTimestamp = System.currentTimeMillis();
	}

	public String getLastEventId() {
		return lastEventId;
	}

	/**
	 * Set the last event processed by this flow execution.
	 * @param lastEvent the last event to set
	 */
	protected void setLastEvent(Event lastEvent) {
		Assert.notNull(lastEvent, "The last event is required");
		this.lastEventId = lastEvent.getId();
		if (logger.isDebugEnabled()) {
			logger.debug("Set last event id to '" + this.lastEventId + "'");
		}
	}

	public FlowSession getActiveSession() {
		return getActiveSessionInternal();
	}
	
	public boolean isActive() {
		return !executingFlowSessions.isEmpty();
	}
	
	public boolean isRootFlowActive() {
		if (isActive()) {
			return getActiveSession().isRoot();
		}
		else {
			return false;
		}
	}

	/**
	 * Check that this flow execution is active and throw an exception if it's not.
	 */
	protected void assertActive() throws IllegalStateException {
		if (!isActive()) {
			throw new IllegalStateException(
					"No active flow sessions executing - this flow execution has ended (or has never been started)");
		}
	}

	// methods implementing FlowExecution

	public synchronized ViewDescriptor start(Event event) throws IllegalStateException {
		Assert.state(!isActive(), "This flow is already executing -- you cannot call start more than once");
		updateLastRequestTimestamp();
		StateContext context = createStateContext(event);
		getListeners().fireRequestSubmitted(context);
		ViewDescriptor viewDescriptor = context.spawn(rootFlow.getStartState(), new HashMap());
		if (isActive()) {
			getActiveSessionInternal().setStatus(FlowSessionStatus.PAUSED);
			getListeners().firePaused(context);
		}
		getListeners().fireRequestProcessed(context);
		return viewDescriptor;
	}

	public synchronized ViewDescriptor signalEvent(Event event) throws FlowNavigationException, IllegalStateException {
		assertActive();
		updateLastRequestTimestamp();
		String stateId = event.getStateId();
		if (!StringUtils.hasText(stateId)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Current state id was not provided in request to signal event '"
						+ event.getId()
						+ "' in flow "
						+ getCaption()
						+ "' -- pulling current state id from session -- "
						+ "note: if the user has been using the browser back/forward buttons, the currentState could be incorrect.");
			}
			stateId = getCurrentState().getId();
		}
		TransitionableState state = getActiveFlow().getRequiredTransitionableState(stateId);
		if (!state.equals(getCurrentState())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Event '" + event.getId() + "' in state '" + state.getId()
						+ "' was signaled by client; however the current flow execution state is '"
						+ getCurrentState().getId() + "'; updating current state to '" + state.getId() + "'");
			}
			setCurrentState(state);
		}
		// execute the event
		StateContextImpl context = createStateContext(event);
		getListeners().fireRequestSubmitted(context);
		getActiveSessionInternal().setStatus(FlowSessionStatus.ACTIVE);
		getListeners().fireResumed(context);
		ViewDescriptor viewDescriptor = state.onEvent(event, context);
		if (isActive()) {
			getActiveSessionInternal().setStatus(FlowSessionStatus.PAUSED);
			getListeners().firePaused(context);
		}
		getListeners().fireRequestProcessed(context);
		return viewDescriptor;
	}
	
	public FlowExecutionListenerList getListeners() {
		return listenerList;
	}

	// flow session management helpers
	
	/**
	 * Create a flow execution state context for given event.
	 * <p>
	 * The default implementation uses the <code>InternalStateContext</code>
	 * class. Subclasses can override this to use a custom class.
	 * @param originatingEvent the event at the origin of this request
	 */
	protected StateContextImpl createStateContext(Event originatingEvent) {
		return new StateContextImpl(originatingEvent, this);
	}
	
	/**
	 * Returns the currently active flow session.
	 * @throws IllegalStateException this execution is not active
	 */
	protected FlowSessionImpl getActiveSessionInternal() throws IllegalStateException {
		assertActive();
		return (FlowSessionImpl)executingFlowSessions.peek();
	}

	/**
	 * Returns the parent flow session of the currently active flow session.
	 * @return the parent flow session
	 * @throws IllegalArgumentException when this execution is not active or
	 *         when the current flow session has no parent (e.g. is the root
	 *         flow session)
	 */
	protected FlowSession getParentSession() throws IllegalArgumentException {
		assertActive();
		Assert.state(!getActiveSession().isRoot(), "There is no parent flow session for the currently active flow session");
		return (FlowSession)executingFlowSessions.get(executingFlowSessions.size() - 2);
	}

	/**
	 * Returns the flow session associated with the root flow.
	 * @throws IllegalStateException this execution is not active
	 */
	protected FlowSession getRootSession() throws IllegalStateException {
		assertActive();
		return (FlowSession)executingFlowSessions.get(0);
	}

	/**
	 * Set the state that is currently active in this flow execution.
	 * @param newState the new current state
	 */
	protected void setCurrentState(State newState) {
		getActiveSessionInternal().setCurrentState(newState);
	}

	/**
	 * Create a new flow session and activate it in this flow execution. This
	 * will push the flow session onto the stack and mark it as the active flow
	 * session.
	 * @param subflow the flow that should be associated with the flow session
	 * @param input the input parameters used to populate the flow session
	 * @return the created and activated flow session
	 */
	protected FlowSession activateSession(RequestContext context, Flow subflow, Map input) {
		FlowSessionImpl session;
		if (!executingFlowSessions.isEmpty()) {
			FlowSessionImpl parent = getActiveSessionInternal();
			parent.setStatus(FlowSessionStatus.SUSPENDED);
			session = createFlowSession(subflow, input, parent);
		}
		else {
			session = createFlowSession(subflow, input, null);
		}
		executingFlowSessions.push(session);
		session.setStatus(FlowSessionStatus.ACTIVE);
		return session;
	}

	/**
	 * Create a new flow session object. Subclasses can override this to return
	 * a special implementation if required.
	 * @param flow the flow that should be associated with the flow session
	 * @param input the input parameters used to populate the flow session
	 * @return the newly created flow session
	 */
	protected FlowSessionImpl createFlowSession(Flow flow, Map input, FlowSession parent) {
		return new FlowSessionImpl(flow, input, parent);
	}

	/**
	 * End the active flow session of this flow execution. This will pop the top
	 * element from the stack and activate the new top flow session.
	 * @return the flow session that ended
	 */
	protected FlowSession endActiveFlowSession() {
		FlowSessionImpl endingSession = (FlowSessionImpl)executingFlowSessions.pop();
		endingSession.setStatus(FlowSessionStatus.ENDED);
		if (!executingFlowSessions.isEmpty()) {
			getActiveSessionInternal().setStatus(FlowSessionStatus.ACTIVE);
		}
		return endingSession;
	}

	// custom serialization

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeLong(this.creationTimestamp);
		out.writeObject(this.getRootFlow().getId());
		out.writeObject(this.lastEventId);
		out.writeLong(this.lastRequestTimestamp);
		out.writeObject(this.executingFlowSessions);
	}

	private void readObject(ObjectInputStream in) throws OptionalDataException, ClassNotFoundException, IOException {
		this.creationTimestamp = in.readLong();
		this.rootFlowId = (String)in.readObject();
		this.lastEventId = (String)in.readObject();
		this.lastRequestTimestamp = in.readLong();
		this.executingFlowSessions = (Stack)in.readObject();
	}

	public synchronized void rehydrate(FlowLocator flowLocator, FlowExecutionListener[] listeners,
			TransactionSynchronizer transactionSynchronizer) {
		// implementation note: we cannot integrate this code into the
		// readObject() method since we need the flow locator, listener list and tx synchronizer!
		if (this.rootFlow != null) {
			// nothing to do, we're already hydrated
			return;
		}
		Assert.notNull(rootFlowId, "The root flow id was not set during deserialization: cannot restore"
				+ " -- was this flow execution deserialized properly?");
		this.rootFlow = flowLocator.getFlow(rootFlowId);
		this.rootFlowId = null;
		// rehydrate all flow sessions
		ListIterator it = this.executingFlowSessions.listIterator();
		while (it.hasNext()) {
			FlowSessionImpl session = (FlowSessionImpl)it.next();
			FlowSessionImpl parent = null;
			if (it.hasNext()) {
				parent = (FlowSessionImpl)it.next();
				it.previous();
			}
			session.rehydrate(flowLocator, parent);
		}
		if (isActive()) {
			// sanity check
			Assert.isTrue(getRootFlow() == getRootSession().getFlow(),
					"The root flow of the execution should be the same as the flow in the root flow session");
		}
		this.listenerList = new FlowExecutionListenerList();
		this.listenerList.add(listeners);
		this.transactionSynchronizer = transactionSynchronizer;
	}

	public String toString() {
		if (!isActive()) {
			return "[Empty FlowExecutionStack; no flows are active]";
		}
		else {
			return new ToStringCreator(this).append("activeFlowId", getActiveSession().getFlow().getId()).append("currentStateId",
					getCurrentState().getId()).append("rootFlow", getRootFlow()).append("executingFlowSessions",
					executingFlowSessions).toString();
		}
	}
}