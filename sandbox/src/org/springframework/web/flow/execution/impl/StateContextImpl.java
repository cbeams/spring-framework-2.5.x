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
package org.springframework.web.flow.execution.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeSource;
import org.springframework.binding.support.EmptyAttributeSource;
import org.springframework.util.Assert;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowContext;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.Scope;
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.State;
import org.springframework.web.flow.StateContext;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.ViewDescriptor;

/**
 * Default state context implementation used internally by the web flow
 * system.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class StateContextImpl implements StateContext {

	protected final Log logger = LogFactory.getLog(StateContextImpl.class);

	/**
	 * The owning flow execution.
	 */
	private FlowExecutionImpl flowExecution;
	
	/**
	 * The source (originating) event of the request context.
	 */
	private Event sourceEvent;

	/**
	 * The last event that occured in this context.
	 */
	private Event lastEvent;

	/**
	 * The last transition that executed in this context.
	 */
	private Transition lastTransition;
	
	/**
	 * Holder for contextual execution properties.
	 */
	private AttributeSource executionProperties = EmptyAttributeSource.INSTANCE;

	/**
	 * The request scope data map.
	 */
	private Scope requestScope = new Scope(ScopeType.REQUEST);

	/**
	 * Create a new request context.
	 * @param sourceEvent the event at the origin of this request
	 * @param flowExecution the owning flow execution
	 */
	public StateContextImpl(Event sourceEvent, FlowExecutionImpl flowExecution) {
		Assert.notNull(sourceEvent, "the source event is required");
		Assert.notNull(flowExecution, "the owning flow execution is required");
		this.sourceEvent = sourceEvent;
		this.flowExecution = flowExecution;
	}

	// implementing RequestContext

	public Event getSourceEvent() {
		return this.sourceEvent;
	}

	public FlowContext getFlowContext() {
		return this.flowExecution;
	}

	public Scope getRequestScope() {
		return this.requestScope;
	}

	public Scope getFlowScope() {
		return this.flowExecution.getActiveSession().getScope();
	}

	public Event getLastEvent() {
		if (lastEvent != null) {
			return lastEvent;
		}
		else {
			return sourceEvent;
		}
	}
	
	public Transition getLastTransition() {
		return lastTransition;
	}

	public AttributeSource getProperties() {
		return executionProperties;
	}

	public void setProperties(AttributeSource properties) {
		if (properties != null) {
			this.executionProperties = properties;
		}
		else {
			this.executionProperties = EmptyAttributeSource.INSTANCE;
		}
	}

	public boolean inTransaction(boolean end) {
		return this.flowExecution.getTransactionSynchronizer().inTransaction(this, end);
	}

	public void assertInTransaction(boolean end) throws IllegalStateException {
		this.flowExecution.getTransactionSynchronizer().assertInTransaction(this, end);
	}

	public void beginTransaction() {
		this.flowExecution.getTransactionSynchronizer().beginTransaction(this);
	}

	public void endTransaction() {
		this.flowExecution.getTransactionSynchronizer().endTransaction(this);
	}

	public Map getModel() {
		// merge request and flow scope
		Map model = new HashMap(getFlowScope().size() + getRequestScope().size());
		model.putAll(getFlowScope().getAttributeMap());
		model.putAll(getRequestScope().getAttributeMap());
		return model;
	}

	// implementing StateContext

	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
		this.flowExecution.setLastEvent(lastEvent);
		this.flowExecution.getListeners().fireEventSignaled(this);
	}

	public void setLastTransition(Transition lastTransition) {
		this.lastTransition = lastTransition;
	}

	public void setCurrentState(State state) {
		this.flowExecution.getListeners().fireStateEntering(this, state);
		State previousState = this.flowExecution.getCurrentState();
		this.flowExecution.setCurrentState(state);
		this.flowExecution.getListeners().fireStateEntered(this, previousState);
	}
	
	public ViewDescriptor spawn(State startState, Map input) throws IllegalStateException {
		this.flowExecution.getListeners().fireSessionStarting(this, startState, input);
		FlowSession session = this.flowExecution.activateSession(this, startState.getFlow(), input);
		ViewDescriptor viewDescriptor = startState.enter(this);
		this.flowExecution.getListeners().fireSessionStarted(this);
		return viewDescriptor;
	}

	public FlowSession endActiveSession() throws IllegalStateException {
		FlowSession endedSession = this.flowExecution.endActiveFlowSession();
		if (logger.isDebugEnabled()) {
			logger.debug("Session for flow '" + endedSession.getFlow().getId() + "' ended, session details = "
					+ endedSession);
		}
		this.flowExecution.getListeners().fireSessionEnded(this, endedSession);
		return endedSession;
	}
}