/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.web.flow;

import java.util.Map;

import org.springframework.util.Assert;

/**
 * Stub implementation of the <code>RequestContext</code> interface to
 * facilitate standalone Action unit tests.
 * <p>
 * NOT intended to be used for anything but standalone action unit tests. This
 * is a simple state holder, a stub implementation.
 * 
 * @author Keith Donald
 */
public class StubRequestContext implements RequestContext, TransactionSynchronizer {

	private Flow activeFlow;

	private Flow rootFlow;

	private State currentState;

	private Event originatingEvent;

	private Event lastEvent;

	private Scope requestScope = new Scope(ScopeType.REQUEST);

	private Scope flowScope = new Scope(ScopeType.FLOW);

	private FlowExecutionListenerList listenerList = new FlowExecutionListenerList();

	private boolean inTransaction;

	public StubRequestContext() {

	}

	public StubRequestContext(Flow activeFlow, State currentState, Event originatingEvent) {
		setActiveFlow(activeFlow);
		setCurrentState(currentState);
		setOriginatingEvent(originatingEvent);
		setLastEvent(originatingEvent);
	}

	/**
	 * @param rootFlow The rootFlow to set.
	 */
	public void setRootFlow(Flow rootFlow) {
		this.rootFlow = rootFlow;
		if (this.activeFlow == null) {
			this.activeFlow = rootFlow;
		}
	}

	/**
	 * @param activeFlow The activeFlow to set.
	 */
	public void setActiveFlow(Flow activeFlow) {
		this.activeFlow = activeFlow;
		if (this.rootFlow == null) {
			this.rootFlow = activeFlow;
		}
	}

	/**
	 * @param currentState The currentState to set.
	 */
	public void setCurrentState(State currentState) {
		Assert.state(currentState.getFlow() == this.activeFlow, "The current state must equal the active flow");
		this.currentState = currentState;
	}

	/**
	 * @param lastEvent The lastEvent to set.
	 */
	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
	}

	/**
	 * @param originatingEvent The originatingEvent to set.
	 */
	public void setOriginatingEvent(Event originatingEvent) {
		this.originatingEvent = originatingEvent;
	}

	public Flow getRootFlow() {
		return rootFlow;
	}

	public Flow getActiveFlow() throws IllegalStateException {
		return activeFlow;
	}

	public State getCurrentState() throws IllegalStateException {
		return currentState;
	}

	public FlowExecutionListenerList getFlowExecutionListenerList() {
		return listenerList;
	}

	public Event getOriginatingEvent() {
		return originatingEvent;
	}

	public Scope getFlowScope() {
		return flowScope;
	}

	public Event getLastEvent() {
		return lastEvent;
	}

	public Scope getRequestScope() {
		return requestScope;
	}

	public TransactionSynchronizer getTransactionSynchronizer() {
		return this;
	}

	public boolean isFlowExecutionActive() {
		return activeFlow != null;
	}

	public boolean isRootFlowActive() {
		return rootFlow != null && rootFlow == activeFlow;
	}

	/*
	 * Not supported, actions should really never call this
	 */
	public Map getModel() {
		throw new UnsupportedOperationException();
	}

	// transaction synchronizer stub methods
	
	public void assertInTransaction(boolean end) throws IllegalStateException {
		Assert.state(inTransaction, "Not in application transaction but is expected to be");
		if (end) {
			inTransaction = false;
		}
	}

	public void beginTransaction() {
		inTransaction = true;
	}

	public void endTransaction() {
		inTransaction = false;
	}

	public boolean inTransaction(boolean end) {
		boolean inTransaction = this.inTransaction;
		if (end) {
			this.inTransaction = false;
		}
		return inTransaction;
	}
}