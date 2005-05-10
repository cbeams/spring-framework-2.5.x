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
package org.springframework.web.flow.execution;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeSource;
import org.springframework.binding.support.EmptyAttributeSource;
import org.springframework.core.closure.support.Block;
import org.springframework.util.Assert;
import org.springframework.util.RandomGuid;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.Scope;
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.State;
import org.springframework.web.flow.TransactionSynchronizer;
import org.springframework.web.flow.Transition;

/**
 * Default request context implementation used internally by the web flow
 * system.
 * <p>
 * This implementation uses a <i>synchronizer token</i> to implement
 * application transaction functionality. The token will be stored in flow scope
 * for the duration of an application transaction. This implies that there needs
 * to be a unique flow execution for each running application transaction.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class InternalRequestContext implements RequestContext, TransactionSynchronizer {

	protected final Log logger = LogFactory.getLog(InternalRequestContext.class);

	/**
	 * The transaction synchronizer token will be stored in the model using an
	 * attribute with this name ("txToken").
	 */
	public static final String TRANSACTION_TOKEN_ATTRIBUTE_NAME = "txToken";

	/**
	 * A client can send the transaction synchronizer token to a controller
	 * using a request parameter with this name ("_txToken").
	 */
	public static final String TRANSACTION_TOKEN_PARAMETER_NAME = "_txToken";

	private Event originatingEvent;

	private Event lastEvent;

	private Transition lastTransition;
	
	private AttributeSource executionProperties = EmptyAttributeSource.INSTANCE;

	private FlowExecutionImpl flowExecution;

	private Scope requestScope = new Scope(ScopeType.REQUEST);

	/**
	 * Create a new request context.
	 * @param originatingEvent the event at the origin of this request
	 * @param flowExecution the owning flow execution
	 */
	public InternalRequestContext(Event originatingEvent, FlowExecutionImpl flowExecution) {
		Assert.notNull(originatingEvent, "the originating event is required");
		Assert.notNull(flowExecution, "the flow execution is required");
		this.originatingEvent = originatingEvent;
		this.flowExecution = flowExecution;
	}

	// implementing RequestContext

	public Flow getRootFlow() {
		return this.flowExecution.getRootFlow();
	}

	public boolean isRootFlowActive() {
		return this.flowExecution.isRootFlowActive();
	}

	public Flow getActiveFlow() {
		return this.flowExecution.getActiveFlow();
	}

	public FlowExecutionListenerList getFlowExecutionListenerList() {
		return this.flowExecution.getListenerList();
	}

	public FlowSession getActiveSession() throws IllegalStateException {
		return this.flowExecution.getActiveSession();
	}

	public boolean isActive() {
		return this.flowExecution.isActive();
	}

	public State getCurrentState() {
		return this.flowExecution.getCurrentState();
	}

	public Event getOriginatingEvent() {
		return this.originatingEvent;
	}

	public Event getLastEvent() {
		if (lastEvent != null) {
			return lastEvent;
		}
		else {
			return originatingEvent;
		}
	}
	
	public Transition getLastTransition() {
		return lastTransition;
	}

	public AttributeSource getExecutionProperties() {
		return executionProperties;
	}

	public Scope getRequestScope() {
		return this.requestScope;
	}

	public Scope getFlowScope() {
		return getActiveSession().getScope();
	}

	public Map getModel() {
		// merge request and flow scope
		Map model = new HashMap(getFlowScope().size() + getRequestScope().size());
		model.putAll(getFlowScope().getAttributeMap());
		model.putAll(getRequestScope().getAttributeMap());
		return model;
	}

	public TransactionSynchronizer getTransactionSynchronizer() {
		return this;
	}

	public void setCurrentState(State state) {
		fireStateEntering(state);
		State previousState = this.flowExecution.getCurrentState();
		this.flowExecution.setCurrentState(state);
		fireStateEntered(previousState);
	}

	public void setLastEvent(Event lastEvent) {
		this.lastEvent = lastEvent;
		this.flowExecution.setLastEvent(lastEvent);
		fireEventSignaled();
	}
	
	public void setLastTransition(Transition lastTransition) {
		this.lastTransition = lastTransition;
	}

	public void setExecutionProperties(AttributeSource properties) {
		if (properties != null) {
			this.executionProperties = properties;
		}
		else {
			this.executionProperties = EmptyAttributeSource.INSTANCE;
		}
	}

	public FlowSession getParentSession() throws IllegalStateException {
		return this.flowExecution.getParentSession();
	}

	public FlowSession endActiveSession() {
		FlowSession endedSession = this.flowExecution.endActiveFlowSession();
		if (logger.isDebugEnabled()) {
			logger.debug("Session for flow '" + endedSession.getFlow().getId() + "' ended, session details = "
					+ endedSession);
		}
		if (this.flowExecution.isActive()) {
			fireSubFlowEnded(endedSession);
		}
		else {
			fireEnded(endedSession);
		}
		return endedSession;
	}

	public FlowSession spawn(Flow subFlow, Map subFlowInput) {
		FlowSession session = this.flowExecution.createAndActivateFlowSession(subFlow, subFlowInput);
		fireSubFlowSpawned();
		return session;
	}

	// lifecycle event management

	/**
	 * Notify all interested listeners that a request was submitted to this flow
	 * execution.
	 */
	protected void fireRequestSubmitted() {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request submitted event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).requestSubmitted(InternalRequestContext.this);
			}
		});
	}

	/**
	 * Notify all interested listeners that flow execution is starting.
	 */
	protected void fireStarting(final State startState) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow session execution starting event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).starting(InternalRequestContext.this, startState);
			}
		});
	}

	/**
	 * Notify all interested listeners that flow execution has started.
	 */
	protected void fireStarted() {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow session execution started event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).started(InternalRequestContext.this);
			}
		});
	}

	/**
	 * Notify all interested listeners that this flow execution finished
	 * processing a request.
	 */
	protected void fireRequestProcessed() {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request processed event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).requestProcessed(InternalRequestContext.this);
			}
		});
	}

	/**
	 * Notify all interested listeners that an event was signaled in this flow
	 * execution.
	 */
	protected void fireEventSignaled() {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing event signaled event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).eventSignaled(InternalRequestContext.this);
			}
		});
	}

	/**
	 * Notify all interested listeners that a state is being entered in this
	 * flow execution.
	 */
	protected void fireStateEntering(final State nextState) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing state entering event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).stateEntering(InternalRequestContext.this, nextState);
			}
		});
	}

	/**
	 * Notify all interested listeners that a state was entered in this
	 * flow execution.
	 */
	protected void fireStateEntered(final State previousState) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing state entered event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).stateEntered(InternalRequestContext.this, previousState, getCurrentState());
			}
		});
	}

	/**
	 * Notify all interested listeners that a sub flow was spawned in this flow
	 * execution.
	 */
	protected void fireSubFlowSpawned() {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing sub flow execution started event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).subFlowSpawned(InternalRequestContext.this);
			}
		});
	}

	/**
	 * Notify all interested listeners that a sub flow ended in this flow
	 * execution.
	 */
	protected void fireSubFlowEnded(final FlowSession endedSession) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing sub flow ended event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).subFlowEnded(InternalRequestContext.this, endedSession);
			}
		});
	}

	/**
	 * Notify all interested listeners that flow execution has ended.
	 */
	protected void fireEnded(final FlowSession endingRootFlowSession) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow execution ended event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).ended(InternalRequestContext.this, endingRootFlowSession);
			}
		});
	}

	// implementing TransactionSynchronizer

	public boolean inTransaction(boolean end) {
		return isEventTokenValid(getTransactionTokenAttributeName(), getTransactionTokenParameterName(), end);
	}

	public void assertInTransaction(boolean end) throws IllegalStateException {
		Assert.state(inTransaction(end), "The request is not executing in the context of an application transaction");
	}

	public void beginTransaction() {
		setToken(getTransactionTokenAttributeName());
	}

	public void endTransaction() {
		clearToken(getTransactionTokenAttributeName());
	}

	/**
	 * Get the name for the transaction token attribute. Defaults to "txToken".
	 */
	protected String getTransactionTokenAttributeName() {
		return TRANSACTION_TOKEN_ATTRIBUTE_NAME;
	}

	/**
	 * Get the name for the transaction token parameter in request events.
	 * Defaults to "_txToken".
	 */
	protected String getTransactionTokenParameterName() {
		return TRANSACTION_TOKEN_PARAMETER_NAME;
	}

	/**
	 * Save a new transaction token in flow scope.
	 * @param tokenName the key used to save the token in the scope
	 */
	protected void setToken(String tokenName) {
		String txToken = new RandomGuid().toString();
		getFlowScope().setAttribute(tokenName, txToken);
	}

	/**
	 * Reset the saved transaction token in the flow scope. This indicates that
	 * transactional token checking will not be needed on the next request event
	 * that is submitted.
	 * @param tokenName the key used to save the token in the scope
	 */
	protected void clearToken(String tokenName) {
		getFlowScope().removeAttribute(tokenName);
	}

	/**
	 * Return <code>true</code> if there is a transaction token stored in the
	 * flow scope, and the value submitted as a event parameter matches it.
	 * Returns <code>false</code> when
	 * <ul>
	 * <li>there is no transaction token saved in the flow scope</li>
	 * <li>there is no transaction token included as an event parameter</li>
	 * <li>the included transaction token value does not match the transaction
	 * token in the flow scope</li>
	 * </ul>
	 * @param tokenName the key used to save the token in the scope
	 * @param tokenParameterName name of the event parameter holding the token
	 * @param clear indicates whether or not the token should be reset after
	 *        checking it
	 * @return true when the token is valid, false otherwise
	 */
	protected boolean isEventTokenValid(String tokenName, String tokenParameterName, boolean clear) {
		// we use the originating event because we want to check that the
		// client request that came into the system has a transaction token
		String tokenValue = (String)getOriginatingEvent().getParameter(tokenParameterName);
		return isTokenValid(tokenName, tokenValue, clear);
	}

	/**
	 * Return <code>true</code> if there is a transaction token stored in the
	 * flow scope and the given value matches it. Returns <code>false</code>
	 * when
	 * <ul>
	 * <li>there is no transaction token saved in the flow scope</li>
	 * <li>given token value is empty</li>
	 * <li>the given transaction token value does not match the transaction
	 * token in the flow scope</li>
	 * </ul>
	 * @param tokenName the key used to save the token in the model
	 * @param tokenValue the token value to check
	 * @param clear indicates whether or not the token should be reset after
	 *        checking it
	 * @return true when the token is valid, false otherwise
	 */
	protected boolean isTokenValid(String tokenName, String tokenValue, boolean clear) {
		if (!StringUtils.hasText(tokenValue)) {
			return false;
		}
		String txToken = (String)getFlowScope().getAttribute(tokenName);
		if (!StringUtils.hasText(txToken)) {
			return false;
		}
		if (clear) {
			clearToken(tokenName);
		}
		return txToken.equals(tokenValue);
	}
}