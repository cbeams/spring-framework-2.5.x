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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.DefaultObjectStyler;
import org.springframework.util.ToStringCreator;
import org.springframework.util.closure.Constraint;
import org.springframework.web.flow.support.FlowUtils;

/**
 * A single client session instance for a <code>Flow</code> participating in a
 * <code>FlowExecution</code>. Also a <code>MutableFlowModel</code>, as
 * the flow session acts as a "flow-scope" data model.
 * <p>
 * The stack of executing flow sessions (managed within
 * <code>FlowExecutionStack</code>) represents the complete state of an
 * ongoing flow execution.
 * <p>
 * A flow session will go through several statuses during its lifecycle.
 * Initially it will be {@link FlowSessionStatus#CREATED}. Once the flow
 * session is activated in a flow execution, it becomes
 * {@link FlowSessionStatus#ACTIVE}. If the flow session would spawn a sub flow
 * session, it will become {@link FlowSessionStatus#SUSPENDED} untill the sub
 * flow returns (ends). When the flow session is ended by the flow execution,
 * its status becomes {@link FlowSessionStatus#ENDED}, ending its lifecycle.
 * 
 * @see org.springframework.web.flow.FlowExecution
 * @see org.springframework.web.flow.FlowExecutionStack
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowSession implements MutableFlowModel, Serializable {

	private static final long serialVersionUID = 3834024745107862072L;

	protected final Log logger = LogFactory.getLog(FlowSession.class);

	/**
	 * The flow definition (a singleton)
	 */
	private transient Flow flow;

	/**
	 * The current state of this flow session.
	 */
	private transient AbstractState currentState;

	/**
	 * The session status; may be CREATED, ACTIVE, SUSPENDED, or ENDED.
	 */
	private FlowSessionStatus status = FlowSessionStatus.CREATED;

	/**
	 * The session data model ("flow scope");
	 */
	private Map attributes = new HashMap();

	/**
	 * Set only on deserialization so this object can be fully reconstructed
	 */
	private String flowId;

	/**
	 * Set only on deserialization so this object can be fully reconstructed
	 */
	private String currentStateId;

	/**
	 * Create a new flow session.
	 * @param flow The flow associated with this session
	 */
	public FlowSession(Flow flow) {
		this(flow, null);
	}

	/**
	 * Create a new flow session.
	 * @param flow The flow associated with this flow session
	 * @param input The input parameters used to populate the flow session
	 */
	public FlowSession(Flow flow, Map input) {
		Assert.notNull(flow, "The flow is required");
		this.flow = flow;
		if (input != null) {
			setAttributes(input);
		}
	}

	/**
	 * Returns the id of the flow associated with this flow session.
	 */
	public String getFlowId() {
		return getFlow().getId();
	}

	/**
	 * Returns the flow associated with this flow session.
	 */
	public Flow getFlow() {
		return flow;
	}

	/**
	 * Returns the current status of this flow session.
	 */
	public FlowSessionStatus getStatus() {
		return status;
	}

	/**
	 * Set the status of this flow session.
	 * @param status The new status to set
	 */
	public void setStatus(FlowSessionStatus status) {
		Assert.notNull(status);
		this.status = status;
	}

	/**
	 * Returns the id of the state that is currently active in this flow
	 * session.
	 */
	public String getCurrentStateId() {
		return currentState.getId();
	}

	/**
	 * Returns the state that is currently active in this flow session
	 */
	public AbstractState getCurrentState() {
		return currentState;
	}

	/**
	 * Set the current state of this flow session.
	 * @param newState The state that is currently active in this flow session
	 */
	protected void setCurrentState(AbstractState newState) {
		Assert.notNull(newState, "The newState is required");
		Assert.isTrue(this.flow == newState.getFlow(),
				"The newState belongs to the flow associated with this flow session");
		if (this.currentState != null) {
			if (this.currentState.equals(newState)) {
				throw new IllegalArgumentException("The current state is already set to '" + newState
						+ "' - this should not happen!");
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Setting current state of this '" + getFlowId() + "' flow session to '" + newState + "'");
		}
		this.currentState = newState;
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

	// methods implementing FlowModel

	/**
	 * Returns all the attributes stored in this flow session as a model map.
	 * @return the flow session data model
	 */
	public Map getModel() {
		return attributes;
	}

	public Object getAttribute(String attributeName) {
		return attributes.get(attributeName);
	}

	public Object getAttribute(String attributeName, Class requiredType) throws IllegalStateException {
		Object value = getAttribute(attributeName);
		if (requiredType != null && value != null) {
			Assert.isInstanceOf(requiredType, value);
		}
		return value;
	}

	public Object getRequiredAttribute(String attributeName) throws IllegalStateException {
		Object value = getAttribute(attributeName);
		if (value == null) {
			throw new IllegalStateException("Required attribute '" + attributeName
					+ "' is not present in flow scope for flow '" + getFlowId()
					+ "'; attributes currently in scope are = " + DefaultObjectStyler.call(attributes));
		}
		return value;
	}

	public Object getRequiredAttribute(String attributeName, Class clazz) throws IllegalStateException {
		Object value = getRequiredAttribute(attributeName);
		if (clazz != null) {
			Assert.isInstanceOf(clazz, value);
		}
		return value;
	}

	public void assertAttributePresent(String attributeName, Class requiredType) throws IllegalStateException {
		getRequiredAttribute(attributeName, requiredType);
	}

	public void assertAttributePresent(String attributeName) throws IllegalStateException {
		getRequiredAttribute(attributeName);
	}

	public void assertInTransaction(HttpServletRequest request, boolean clear) throws IllegalStateException {
		Assert.state(FlowUtils.isTokenValid(this, request, getTransactionTokenAttributeName(),
				getTransactionTokenParameterName(), clear),
				"The request is not running in the context of an application transaction");
	}

	public boolean containsAttribute(String attributeName) {
		return attributes.containsKey(attributeName);
	}

	public boolean containsAttribute(String attributeName, Class requiredType) {
		try {
			getRequiredAttribute(attributeName, requiredType);
			return true;
		}
		catch (IllegalStateException e) {
			return false;
		}
	}

	public boolean inTransaction(HttpServletRequest request, boolean clear) {
		return FlowUtils.isTokenValid(this, request, getTransactionTokenAttributeName(),
				getTransactionTokenParameterName(), clear);
	}

	public Collection attributeNames() {
		return Collections.unmodifiableCollection(attributes.keySet());
	}

	public Collection attributeValues() {
		return Collections.unmodifiableCollection(attributes.values());
	}

	public Collection attributeEntries() {
		return Collections.unmodifiableCollection(attributes.entrySet());
	}

	public Collection findAttributes(Constraint criteria) {
		Iterator it = attributeEntries().iterator();
		Collection filteredEntries = new LinkedHashSet();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			if (criteria.test(entry)) {
				filteredEntries.add(entry);
			}
		}
		return filteredEntries;
	}

	// methods implementing MutableFlowModel

	public void setAttribute(String attributeName, Object attributeValue) {
		if (logger.isDebugEnabled()) {
			logger.debug("Setting flow '" + getFlowId() + "' attribute '" + attributeName + "' to '" + attributeValue
					+ "'");
		}
		this.attributes.put(attributeName, attributeValue);
	}

	public void setAttributes(Map attributes) {
		if (attributes == null) {
			return;
		}
		Iterator it = attributes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry)it.next();
			Assert.isInstanceOf(String.class, e.getKey());
			setAttribute((String)e.getKey(), e.getValue());
		}
	}

	public void removeAttribute(String attributeName) {
		if (logger.isDebugEnabled()) {
			logger.debug("Removing flow '" + getFlowId() + "' attribute '" + attributeName);
		}
		this.attributes.remove(attributeName);
	}

	public void beginTransaction() {
		FlowUtils.setToken(this, getTransactionTokenAttributeName());
	}

	public void endTransaction() {
		FlowUtils.clearToken(this, getTransactionTokenAttributeName());
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(this.flow.getId());
		out.writeObject(this.currentState.getId());
		out.writeObject(this.status);
		out.writeObject(this.attributes);
	}

	private void readObject(ObjectInputStream in) throws OptionalDataException, ClassNotFoundException, IOException {
		this.flowId = (String)in.readObject();
		this.currentStateId = (String)in.readObject();
		this.status = (FlowSessionStatus)in.readObject();
		this.attributes = (Map)in.readObject();
	}

	/**
	 * Restore this <code>Flow Session</code> for use after deserialization
	 * @param flowLocator the flow locator
	 */
	protected void rehydrate(FlowLocator flowLocator) {
		Assert.state(this.flow == null, "The flow is already set - already restored");
		Assert.state(this.currentState == null, "The current state is already set - already restored");
		Assert
				.notNull(flowId,
						"The flow id was not set during deserialization: cannot restore--was this flow session deserialized properly?");
		this.flow = flowLocator.getFlow(this.flowId);
		Assert
				.notNull(flowId,
						"The current state id was not set during deserialization: cannot restore--was this flow session deserialized properly?");
		this.currentState = this.flow.getRequiredState(this.currentStateId);
		this.flowId = null;
		this.currentStateId = null;
	}

	public String toString() {
		return new ToStringCreator(this).append("flow", flow).append("currentState", currentState).append(
				"attributesCount", (attributes != null ? attributes.size() : 0)).append("attributes", attributes)
				.toString();
	}
}