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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.RandomGuid;
import org.springframework.util.StringUtils;
import org.springframework.util.closure.support.Block;

public class LocalFlowExecutionContext implements StateContext {
	protected static final Log logger = LogFactory.getLog(FlowExecutionStack.class);

	private Event event;

	private FlowExecutionStack flowExecutionStack;

	private Scope requestAttributes = new Scope();

	private TransactionSynchronizer transactionSynchronizer = new LocalTransactionSynchronizer();

	public LocalFlowExecutionContext(Event event, FlowExecutionStack executionStack) {
		this.event = event;
		this.flowExecutionStack = executionStack;
	}

	public Flow getActiveFlow() {
		return this.flowExecutionStack.getActiveFlow();
	}

	public FlowExecutionListenerList getFlowExecutionListenerList() {
		return this.flowExecutionStack.getListenerList();
	}

	public Flow getRootFlow() {
		return this.flowExecutionStack.getRootFlow();
	}

	public State getCurrentState() {
		return this.flowExecutionStack.getCurrentState();
	}

	public boolean isFlowExecutionActive() {
		return this.flowExecutionStack.isActive();
	}

	public Event getEvent() {
		return event;
	}

	public void setCurrentState(State state) {
		State previousState = this.flowExecutionStack.getCurrentState();
		this.flowExecutionStack.setCurrentState(state);
		fireStateTransitioned(previousState);
	}

	public void setEvent(Event event) {
		this.event = event;
		this.flowExecutionStack.setEventId(event.getId());
		fireEventSignaled(event);
	}

	public ViewDescriptor spawn(Flow subFlow, Map subFlowInput) {
		this.flowExecutionStack.activateFlowSession(subFlow, subFlowInput);
		fireSubFlowSpawned();
		return subFlow.getStartState().enter(this);
	}

	public ViewDescriptor spawn(Flow subFlow, String stateId, Map subFlowInput) {
		this.flowExecutionStack.activateFlowSession(subFlow, subFlowInput);
		fireSubFlowSpawned();
		return subFlow.getState(stateId).enter(this);
	}

	public FlowSession getActiveFlowSession() {
		return this.flowExecutionStack.getActiveFlowSession();
	}

	public FlowSession endActiveFlowSession() {
		FlowSession endedSession = this.flowExecutionStack.endActiveFlowSession();
		if (this.flowExecutionStack.isActive()) {
			fireSubFlowEnded(endedSession);
		}
		else {
			fireEnded(endedSession);
		}
		return endedSession;
	}

	public Scope requestScope() {
		return this.requestAttributes;
	}

	public Scope flowScope() {
		return getActiveFlowSession().flowScope();
	}

	public TransactionSynchronizer getTransactionSynchronizer() {
		return this.transactionSynchronizer;
	}
	
	public Map getModel() {
		Map model = this.flowExecutionStack.getModel();
		model.putAll(requestAttributes.getAttributeMap());
		return model;
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
				((FlowExecutionListener)o).started(LocalFlowExecutionContext.this);
			}
		});
	}

	/**
	 * Notify all interested listeners that a request was submitted to this flow
	 * execution.
	 */
	protected void fireRequestSubmitted(final Event event) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request submitted event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).requestSubmitted(LocalFlowExecutionContext.this, event);
			}
		});
	}

	/**
	 * Notify all interested listeners that this flow execution finished
	 * processing a request.
	 */
	protected void fireRequestProcessed(final Event event) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing request processed event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).requestProcessed(LocalFlowExecutionContext.this, event);
			}
		});
	}

	/**
	 * Notify all interested listeners that an event was signaled in this flow
	 * execution.
	 */
	protected void fireEventSignaled(final Event event) {
		if (logger.isDebugEnabled()) {
			logger
					.debug("Publishing event signaled event to " + getFlowExecutionListenerList().size()
							+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).eventSignaled(LocalFlowExecutionContext.this, event);
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
				((FlowExecutionListener)o).stateTransitioned(LocalFlowExecutionContext.this, previousState,
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
			logger.debug("Publishing sub flow session execution started event to "
					+ getFlowExecutionListenerList().size() + " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).subFlowSpawned(LocalFlowExecutionContext.this);
			}
		});
	}

	/**
	 * Notify all interested listeners that a sub flow ended in this flow
	 * execution.
	 */
	protected void fireSubFlowEnded(final FlowSession endedSession) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing sub flow session ended event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).subFlowEnded(LocalFlowExecutionContext.this, endedSession);
			}
		});
	}

	/**
	 * Notify all interested listeners that flow execution has ended.
	 */
	protected void fireEnded(final FlowSession endingRootFlowSession) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing flow session execution ended event to " + getFlowExecutionListenerList().size()
					+ " listener(s)");
		}
		getFlowExecutionListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).ended(LocalFlowExecutionContext.this, endingRootFlowSession);
			}
		});
	}

	private class LocalTransactionSynchronizer implements TransactionSynchronizer {
		public boolean inTransaction(boolean clear) {
			return isEventTokenValid(getTransactionTokenAttributeName(), getTransactionTokenParameterName(), clear);
		}

		public void assertInTransaction(boolean clear) throws IllegalStateException {
			Assert.state(isEventTokenValid(getTransactionTokenAttributeName(), getTransactionTokenParameterName(),
					clear), "The request is not running in the context of an application transaction");
		}

		public void beginTransaction() {
			setToken(getTransactionTokenAttributeName());
		}

		public void endTransaction() {
			clearToken(getTransactionTokenAttributeName());
		}

		/**
		 * Get the name for the transaction token attribute. Defaults to
		 * "txToken".
		 */
		protected String getTransactionTokenAttributeName() {
			return FlowConstants.TRANSACTION_TOKEN_ATTRIBUTE_NAME;
		}

		/**
		 * Get the name for the transaction token parameter in requests.
		 * Defaults to "_txToken".
		 */
		protected String getTransactionTokenParameterName() {
			return FlowConstants.TRANSACTION_TOKEN_PARAMETER_NAME;
		}

		/**
		 * Save a new transaction token in given model.
		 * @param model the model where the generated token should be saved
		 * @param tokenName the key used to save the token in the model
		 */
		public void setToken(String tokenName) {
			String txToken = new RandomGuid().toString();
			flowScope().setAttribute(tokenName, txToken);
		}

		/**
		 * Reset the saved transaction token in given model. This indicates that
		 * transactional token checking will not be needed on the next request
		 * that is submitted.
		 * @param model the model where the generated token should be saved
		 * @param tokenName the key used to save the token in the model
		 */
		public void clearToken(String tokenName) {
			flowScope().removeAttribute(tokenName);
		}

		/**
		 * Return <code>true</code> if there is a transaction token stored in
		 * given model, and the value submitted as a request parameter matches
		 * it. Returns <code>false</code> when
		 * <ul>
		 * <li>there is no transaction token saved in the model</li>
		 * <li>there is no transaction token included as a request parameter</li>
		 * <li>the included transaction token value does not match the
		 * transaction token in the model</li>
		 * </ul>
		 * @param model the model where the token is stored
		 * @param tokenName the key used to save the token in the model
		 * @param request current HTTP request
		 * @param requestParameterName name of the request parameter holding the
		 *        token
		 * @param clear indicates whether or not the token should be reset after
		 *        checking it
		 * @return true when the token is valid, false otherwise
		 */
		public boolean isEventTokenValid(String tokenName, String tokenParameterName, boolean clear) {
			String tokenValue = (String)getEvent().getParameter(tokenParameterName);
			return isTokenValid(tokenName, tokenValue, clear);
		}

		/**
		 * Return <code>true</code> if there is a transaction token stored in
		 * given model and the given value matches it. Returns
		 * <code>false</code> when
		 * <ul>
		 * <li>there is no transaction token saved in the model</li>
		 * <li>given token value is empty</li>
		 * <li>the given transaction token value does not match the transaction
		 * token in the model</li>
		 * </ul>
		 * @param model the model where the token is stored
		 * @param tokenName the key used to save the token in the model
		 * @param tokenValue the token value to check
		 * @param clear indicates whether or not the token should be reset after
		 *        checking it
		 * @return true when the token is valid, false otherwise
		 */
		private boolean isTokenValid(String tokenName, String tokenValue, boolean clear) {
			if (!StringUtils.hasText(tokenValue)) {
				return false;
			}
			String txToken = (String)flowScope().getAttribute(tokenName);
			if (!StringUtils.hasText(txToken)) {
				return false;
			}
			if (clear) {
				clearToken(tokenName);
			}
			return txToken.equals(tokenValue);
		}
	}
}