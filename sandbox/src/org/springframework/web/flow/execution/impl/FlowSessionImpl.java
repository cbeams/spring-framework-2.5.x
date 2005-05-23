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
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.FlowSessionStatus;
import org.springframework.web.flow.Scope;
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.State;
import org.springframework.web.flow.execution.FlowLocator;

/**
 * Implementation of the FlowSession interfaced used internally by
 * the FlowSessionImpl.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowSessionImpl implements FlowSession, Serializable {

	// static logger because FlowSessionImpl objects can be serialized and
	// then restored
	protected static final Log logger = LogFactory.getLog(FlowSessionImpl.class);
	
	/**
	 * The flow definition (a singleton).
	 */
	private transient Flow flow;

	/**
	 * Set only on deserialization so this object can be fully reconstructed.
	 */
	private String flowId;

	/**
	 * The current state of this flow session.
	 */
	private transient State currentState;

	/**
	 * Set only on deserialization so this object can be fully reconstructed.
	 */
	private String currentStateId;

	/**
	 * The session status; may be CREATED, ACTIVE, SUSPENDED, or ENDED.
	 */
	private FlowSessionStatus status = FlowSessionStatus.CREATED;

	/**
	 * The session data model ("flow scope").
	 */
	private Scope flowScope = new Scope(ScopeType.FLOW);

	/**
	 * The parent session of this session (may be null if this is a root session.) 
	 */
	private FlowSessionImpl parent;
	
	/**
	 * Create a new flow session.
	 * @param flow the flow associated with this flow session
	 * @param input the input parameters used to populate the flow session
	 * @param parent the parent flow session of the created flow session in the
	 *        owning flow execution
	 */
	public FlowSessionImpl(Flow flow, Map input, FlowSessionImpl parent) {
		Assert.notNull(flow, "The flow is required");
		this.flow = flow;
		if (input != null) {
			this.flowScope.setAttributes(input);
		}
		this.parent = parent;
	}
	
	public Flow getFlow() {
		return flow;
	}

	public State getCurrentState() {
		return currentState;
	}

	/**
	 * Set the current state of this flow session.
	 * @param newState the state that is currently active in this flow session
	 */
	public void setCurrentState(State newState) {
		Assert.notNull(newState, "The newState is required");
		Assert.isTrue(this.flow == newState.getFlow(),
				"The newState belongs to the flow associated with this flow session");
		if (logger.isDebugEnabled()) {
			logger.debug("Setting current state of this '" + getFlow().getId() + "' flow session to '" + newState + "'");
		}
		this.currentState = newState;
	}

	public FlowSessionStatus getStatus() {
		return status;
	}

	/**
	 * Set the status of this flow session.
	 * @param status the new status to set
	 */
	protected void setStatus(FlowSessionStatus status) {
		Assert.notNull(status);
		this.status = status;
	}

	public Scope getScope() {
		return this.flowScope;
	}

	public FlowSession getParent() {
		return parent;
	}
	
	public boolean isRoot() {
		return parent == null;
	}

	// custom serialization

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(this.flow.getId());
		out.writeObject(this.currentState.getId());
		out.writeObject(this.status);
		out.writeObject(this.flowScope);
		out.writeObject(this.parent);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.flowId = (String)in.readObject();
		this.currentStateId = (String)in.readObject();
		this.status = (FlowSessionStatus)in.readObject();
		this.flowScope = (Scope)in.readObject();
		this.parent = (FlowSessionImpl)in.readObject();
	}

	/**
	 * Restore this <code>Flow Session</code> for use after deserialization.
	 * @param flowLocator the flow locator
	 */
	protected void rehydrate(FlowLocator flowLocator) {
		// implementation note: we cannot integrate this code into the
		// readObject() method since we need the flow locator!
		Assert.state(this.flow == null, "The flow is already set -- already restored");
		Assert.state(this.currentState == null, "The current state is already set -- already restored");
		Assert.notNull(flowId,
				"The flow id was not set during deserialization: cannot restore -- was this flow session deserialized properly?");
		this.flow = flowLocator.getFlow(this.flowId);
		this.flowId = null;
		Assert.notNull(currentStateId,
				"The current state id was not set during deserialization: cannot restore -- was this flow session deserialized properly?");
		this.currentState = this.flow.getRequiredState(this.currentStateId);
		this.currentStateId = null;
	}

	public String toString() {
		return new ToStringCreator(this).append("flow", flow).append("currentState", currentState).append(
				"attributesCount", flowScope.size()).append("attributes", flowScope)
				.toString();
	}
}