/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeSetter;
import org.springframework.util.Assert;
import org.springframework.util.closure.support.Block;
import org.springframework.web.flow.support.FlowUtils;
import org.springframework.web.flow.support.MapAttributeSetterAdapter;

public class LocalFlowExecutionContext implements StateContext {
	protected static final Log logger = LogFactory.getLog(FlowExecutionStack.class);

	private Event event;

	private FlowExecutionStack flowExecutionStack;

	private MapAttributeSetterAdapter requestAttributes = new MapAttributeSetterAdapter();

	public LocalFlowExecutionContext(Event event, FlowExecutionStack executionStack) {
		this.event = event;
		this.flowExecutionStack = executionStack;
	}

	public Flow getActiveFlow() {
		return this.flowExecutionStack.getActiveFlow();
	}

	public FlowExecutionListenerList getListenerList() {
		return this.flowExecutionStack.getListenerList();
	}

	public Flow getRootFlow() {
		return this.flowExecutionStack.getRootFlow();
	}

	public AbstractState getCurrentState() {
		return this.flowExecutionStack.getCurrentState();
	}

	public boolean isFlowExecutionActive() {
		return this.flowExecutionStack.isActive();
	}

	public Event getEvent() {
		return event;
	}

	public boolean containsAttribute(String attributeName) {
		boolean containsAttribute = getActiveFlowSession().containsAttribute(attributeName);
		if (!containsAttribute) {
			if (this.requestAttributes != null) {
				containsAttribute = this.requestAttributes.containsAttribute(attributeName);
			}
		}
		return containsAttribute;
	}

	public Object getAttribute(String attributeName) {
		if (getActiveFlowSession().containsAttribute(attributeName)) {
			return getActiveFlowSession().getAttribute(attributeName);
		}
		else {
			if (this.requestAttributes != null) {
				return this.requestAttributes.getAttribute(attributeName);
			}
			else {
				return null;
			}
		}
	}

	public void setCurrentState(AbstractState state) {
		AbstractState previousState = this.flowExecutionStack.getCurrentState();
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

	public boolean inTransaction(boolean clear) {
		return FlowUtils.isEventTokenValid(this, getTransactionTokenAttributeName(),
				getTransactionTokenParameterName(), clear);
	}

	public void assertInTransaction(boolean clear) throws IllegalStateException {
		Assert.state(FlowUtils.isEventTokenValid(this, getTransactionTokenAttributeName(),
				getTransactionTokenParameterName(), clear),
				"The request is not running in the context of an application transaction");
	}

	/**
	 * Get the name for the transaction token attribute. Defaults to "txToken".
	 */
	protected String getTransactionTokenAttributeName() {
		return FlowConstants.TRANSACTION_TOKEN_ATTRIBUTE_NAME;
	}

	/**
	 * Get the name for the transaction token parameter in requests. Defaults to
	 * "_txToken".
	 */
	protected String getTransactionTokenParameterName() {
		return FlowConstants.TRANSACTION_TOKEN_PARAMETER_NAME;
	}

	public void beginTransaction() {
		FlowUtils.setToken(this, getTransactionTokenAttributeName());
	}

	public void endTransaction() {
		FlowUtils.clearToken(this, getTransactionTokenAttributeName());
	}

	public AttributeSetter getRequestAttributeAccessor() {
		return this.requestAttributes;
	}

	public Object getRequestAttribute(String attributeName) {
		return this.requestAttributes.getAttribute(attributeName);
	}

	public Object getRequestAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		return this.requestAttributes.getAttribute(attributeName, requiredType);
	}

	public Object getRequiredRequestAttribute(String attributeName) throws IllegalStateException {
		return this.requestAttributes.getRequiredAttribute(attributeName);
	}

	public Object getRequiredRequestAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		return this.requestAttributes.getRequiredAttribute(attributeName, requiredType);
	}

	public void setRequestAttribute(String attributeName, Object attributeValue) {
		this.requestAttributes.setAttribute(attributeName, attributeValue);
	}

	public void setRequestAttributes(Map attributes) {
		this.requestAttributes.setAttributes(attributes);
	}

	public Object removeRequestAttribute(String attributeName) {
		return this.requestAttributes.removeAttribute(attributeName);
	}

	public AttributeSetter getFlowAttributeAccessor() {
		return getActiveFlowSession();
	}

	public Object getFlowAttribute(String attributeName) {
		return getActiveFlowSession().getAttribute(attributeName);
	}

	public Object getFlowAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		return getActiveFlowSession().getAttribute(attributeName, requiredType);
	}

	public Object getRequiredFlowAttribute(String attributeName) throws IllegalStateException {
		return getActiveFlowSession().getRequiredAttribute(attributeName);
	}

	public Object getRequiredFlowAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		return getActiveFlowSession().getRequiredAttribute(attributeName, requiredType);
	}

	public void setFlowAttribute(String attributeName, Object attributeValue) {
		getActiveFlowSession().setAttribute(attributeName, attributeValue);
	}

	public void setFlowAttributes(Map attributes) {
		getActiveFlowSession().setAttributes(attributes);
	}

	public Object removeFlowAttribute(String attributeName) {
		return getActiveFlowSession().removeAttribute(attributeName);
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
			logger.debug("Publishing flow session execution started event to " + getListenerList().size()
					+ " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
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
			logger.debug("Publishing request submitted event to " + getListenerList().size() + " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
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
			logger.debug("Publishing request processed event to " + getListenerList().size() + " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
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
			logger.debug("Publishing event signaled event to " + getListenerList().size() + " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).eventSignaled(LocalFlowExecutionContext.this, event);
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
			logger.debug("Publishing sub flow session execution started event to " + getListenerList().size()
					+ " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
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
			logger.debug("Publishing sub flow session ended event to " + getListenerList().size() + " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
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
			logger.debug("Publishing flow session execution ended event to " + getListenerList().size()
					+ " listener(s)");
		}
		getListenerList().iteratorTemplate().run(new Block() {
			protected void handle(Object o) {
				((FlowExecutionListener)o).ended(LocalFlowExecutionContext.this, endingRootFlowSession);
			}
		});
	}
}