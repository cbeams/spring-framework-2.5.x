/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.test.web.flow;

import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowExecutionListenerList;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.Scope;
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.State;
import org.springframework.web.flow.TransactionSynchronizer;

/**
 * Mock implementation of the <code>RequestContext</code> interface to
 * facilitate standalone Action unit tests.
 * <p>
 * NOT intended to be used for anything but standalone action unit tests. This
 * is a simple state holder, a stub implementation.
 * <p>
 * Note that this is really a <i>stub</i> implementation of the RequestContext
 * interface, at least if you follow
 * <a href="http://www.martinfowler.com/articles/mocksArentStubs.html">Martin
 * Fowler's</a> reasoning. This class is called <i>Mock</i>RequestContext to
 * be consistent with the naming convention in the rest of the Spring framework
 * (e.g. MockHttpServletRequest, ...).
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MockRequestContext implements RequestContext, TransactionSynchronizer {

	private Flow activeFlow;

	private Flow rootFlow;

	private State currentState;

	private Event originatingEvent;

	private Event lastEvent;

	private Scope requestScope = new Scope(ScopeType.REQUEST);

	private Scope flowScope = new Scope(ScopeType.FLOW);

	private FlowExecutionListenerList listenerList = new FlowExecutionListenerList();

	private boolean inTransaction;

	/**
	 * Create a new stub request context.
	 */
	public MockRequestContext() {
	}

	/**
	 * Create a new stub request context.
	 * @param activeFlow the active flow
	 * @param currentState the current state
	 * @param originatingEvent the event originating this request context
	 */
	public MockRequestContext(Flow activeFlow, State currentState, Event originatingEvent) {
		setActiveFlow(activeFlow);
		setCurrentState(currentState);
		setOriginatingEvent(originatingEvent);
		setLastEvent(originatingEvent);
	}

	/**
	 * Set the root flow of this request context.
	 * @param rootFlow the rootFlow to set
	 */
	public void setRootFlow(Flow rootFlow) {
		this.rootFlow = rootFlow;
		if (this.activeFlow == null) {
			this.activeFlow = rootFlow;
		}
	}

	/**
	 * Set the active flow of this request context.
	 * @param activeFlow the activeFlow to set
	 */
	public void setActiveFlow(Flow activeFlow) {
		this.activeFlow = activeFlow;
		if (this.rootFlow == null) {
			this.rootFlow = activeFlow;
		}
	}

	/**
	 * Set the current state of this request context.
	 * @param currentState the currentState to set
	 */
	public void setCurrentState(State currentState) {
		Assert.state(currentState.getFlow() == this.activeFlow, "The current state must be in the active flow");
		this.currentState = currentState;
	}

	/**
	 * Set the last event that occured in this request context.
	 * @param lastEvent the lastEvent to set
	 */
	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
	}

	/**
	 * Set the event originating this request context.
	 * @param originatingEvent the originatingEvent to set
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