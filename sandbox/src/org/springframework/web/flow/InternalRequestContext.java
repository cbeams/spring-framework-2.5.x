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
package org.springframework.web.flow;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeSource;
import org.springframework.binding.AttributeValueResolutionStrategy;
import org.springframework.binding.AttributeValueResolver;
import org.springframework.binding.NoSuchAttributeValueException;
import org.springframework.binding.support.DefaultAttributeValueResolutionStrategy;
import org.springframework.binding.support.EmptyAttributeSource;
import org.springframework.core.closure.support.Block;
import org.springframework.util.Assert;
import org.springframework.util.RandomGuid;
import org.springframework.util.StringUtils;

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
public class InternalRequestContext implements StateContext, TransactionSynchronizer {

	protected final Log logger = LogFactory.getLog(InternalRequestContext.class);

	private Event originatingEvent;

	private Event lastEvent;

	private ActionAttributes actionAttributes = new ActionAttributes();

	private StateAttributes stateAttributes = new StateAttributes();

	private FlowExecutionStack flowExecution;

	private Scope requestScope = new Scope(ScopeType.REQUEST);

	/**
	 * Create a new request context.
	 * 
	 * @param originatingEvent
	 *            the event at the origin of this request
	 * @param flowExecution
	 *            the owning flow execution
	 */
	public InternalRequestContext(Event originatingEvent, FlowExecutionStack flowExecution) {
		Assert.notNull(originatingEvent, "the originating event is required");
		Assert.notNull(flowExecution, "the flow execution is required");
		this.originatingEvent = originatingEvent;
		this.flowExecution = flowExecution;
	}

	private static class StateAttributes implements AttributeValueResolver {
		private AttributeSource stateAttributes = EmptyAttributeSource.INSTANCE;

		private AttributeValueResolutionStrategy propertyResolutionStrategy = new DefaultAttributeValueResolutionStrategy();

		public void setStateAttributes(AttributeSource stateAttributes) {
			if (stateAttributes != null) {
				this.stateAttributes = stateAttributes;
			}
			else {
				this.stateAttributes = EmptyAttributeSource.INSTANCE;
			}
		}

		public boolean isValuePlaceholder(String value) {
			return propertyResolutionStrategy.isValuePlaceholder(value);
		}

		public Object resolveAttributeValue(String placeholder) {
			try {
				return propertyResolutionStrategy.resolveAttributeValue(placeholder, stateAttributes);
			}
			catch (NoSuchAttributeValueException e) {
				return null;
			}
		}
	}

	private class ActionAttributes implements AttributeSource {
		private AttributeSource actionAttributes = EmptyAttributeSource.INSTANCE;

		public void setActionAttributes(AttributeSource actionAttributes) {
			if (actionAttributes != null) {
				this.actionAttributes = actionAttributes;
			}
			else {
				this.actionAttributes = EmptyAttributeSource.INSTANCE;
			}
		}

		public boolean containsAttribute(String attributeName) {
			if (!actionAttributes.containsAttribute(attributeName)) {
				return false;
			}
			return getAttribute(attributeName) != null;
		}

		public Object getAttribute(String attributeName) {
			Object attributeValue = actionAttributes.getAttribute(attributeName);
			if (attributeValue instanceof String
					&& getStateAttributeResolver().isValuePlaceholder((String)attributeValue)) {
				return getStateAttributeResolver().resolveAttributeValue((String)attributeValue);
			}
			else {
				return actionAttributes.getAttribute(attributeName);
			}
		}
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

	public boolean isFlowExecutionActive() {
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

	public AttributeSource getActionAttributes() {
		return actionAttributes;
	}

	public Scope getRequestScope() {
		return this.requestScope;
	}

	public Scope getFlowScope() {
		return getActiveFlowSession().getFlowScope();
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

	// implementing StateContext

	public void setCurrentState(State state) {
		State previousState = this.flowExecution.getCurrentState();
		this.flowExecution.setCurrentState(state);
		fireStateTransitioned(previousState);
	}

	public void setLastEvent(Event event) {
		this.lastEvent = event;
		this.flowExecution.setLastEvent(event);
		fireEventSignaled();
	}

	public void setActionAttributes(AttributeSource parameters) {
		this.actionAttributes.setActionAttributes(parameters);
	}

	public void setStateAttributes(AttributeSource parameters) {
		this.stateAttributes.setStateAttributes(parameters);
	}

	public AttributeValueResolver getStateAttributeResolver() {
		return stateAttributes;
	}

	public FlowSession getActiveFlowSession() {
		return this.flowExecution.getActiveFlowSession();
	}

	public FlowSession getParentFlowSession() throws IllegalStateException {
		return this.flowExecution.getParentFlowSession();
	}

	public FlowSession endActiveFlowSession() {
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

	public ViewDescriptor spawn(Flow subFlow, Map subFlowInput) {
		this.flowExecution.createAndActivateFlowSession(subFlow, subFlowInput);
		fireSubFlowSpawned();
		return subFlow.getStartState().enter(this);
	}

	public ViewDescriptor spawn(Flow subFlow, String stateId, Map subFlowInput) {
		this.flowExecution.createAndActivateFlowSession(subFlow, subFlowInput);
		fireSubFlowSpawned();
		return subFlow.getState(stateId).enter(this);
	}

	// lifecycle event management

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
			logger
					.debug("Publishing event signaled event to " + getFlowExecutionListenerList().size()
							+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).eventSignaled(InternalRequestContext.this);
			}
		});
	}

	/**
	 * Notify all interested listeners that a state transition happened in this
	 * flow execution.
	 */
	protected void fireStateTransitioned(final State previousState) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing state transitioned event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).stateTransitioned(InternalRequestContext.this, previousState,
						getCurrentState());
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
			logger
					.debug("Publishing sub flow ended event to " + getFlowExecutionListenerList().size()
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
		Assert.state(isEventTokenValid(getTransactionTokenAttributeName(), getTransactionTokenParameterName(), end),
				"The request is not executing in the context of an application transaction");
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
		return FlowConstants.TRANSACTION_TOKEN_ATTRIBUTE_NAME;
	}

	/**
	 * Get the name for the transaction token parameter in request events.
	 * Defaults to "_txToken".
	 */
	protected String getTransactionTokenParameterName() {
		return FlowConstants.TRANSACTION_TOKEN_PARAMETER_NAME;
	}

	/**
	 * Save a new transaction token in flow scope.
	 * 
	 * @param tokenName
	 *            the key used to save the token in the scope
	 */
	protected void setToken(String tokenName) {
		String txToken = new RandomGuid().toString();
		getFlowScope().setAttribute(tokenName, txToken);
	}

	/**
	 * Reset the saved transaction token in the flow scope. This indicates that
	 * transactional token checking will not be needed on the next request event
	 * that is submitted.
	 * 
	 * @param tokenName
	 *            the key used to save the token in the scope
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
	 * 
	 * @param tokenName
	 *            the key used to save the token in the scope
	 * @param tokenParameterName
	 *            name of the event parameter holding the token
	 * @param clear
	 *            indicates whether or not the token should be reset after
	 *            checking it
	 * @return true when the token is valid, false otherwise
	 */
	protected boolean isEventTokenValid(String tokenName, String tokenParameterName, boolean clear) {
		String tokenValue = (String)getLastEvent().getParameter(tokenParameterName);
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
	 * 
	 * @param tokenName
	 *            the key used to save the token in the model
	 * @param tokenValue
	 *            the token value to check
	 * @param clear
	 *            indicates whether or not the token should be reset after
	 *            checking it
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