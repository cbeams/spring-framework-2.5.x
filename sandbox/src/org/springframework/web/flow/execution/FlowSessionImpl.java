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
package org.springframework.web.flow.execution;

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
import org.springframework.web.flow.FlowExecutionInfo;
import org.springframework.web.flow.FlowLocator;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.FlowSessionStatus;
import org.springframework.web.flow.Scope;
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.State;

/**
 * A single client session instance for a <code>Flow</code> participating in a
 * <code>FlowExecution</code>. Also acts as a "flow-scope" data model.
 * <p>
 * The stack of executing flow sessions (managed within a
 * <code>FlowExecutionStack</code>) represents the complete state of an
 * ongoing flow execution.
 * <p>
 * A flow session will go through several statuses during its lifecycle.
 * Initially it will be {@link FlowSessionStatus#CREATED}. Once the flow
 * session is activated in a flow execution, it becomes
 * {@link FlowSessionStatus#ACTIVE}. If the flow session would spawn a sub flow
 * session, it will become {@link FlowSessionStatus#SUSPENDED} until the sub
 * flow returns (ends). When the flow session is ended by the flow execution,
 * its status becomes {@link FlowSessionStatus#ENDED}, ending its lifecycle.
 * <p>
 * Note that a flow <i>session</i> is in no way linked to an HTTP session! It
 * just uses the familiar request/session naming convention.
 * 
 * @see org.springframework.web.flow.execution.FlowExecution
 * @see org.springframework.web.flow.execution.FlowExecutionImpl
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class FlowSessionImpl implements FlowSession, Serializable {

	// static logger because FlowSession objects can be serialized and
	// then restored
	protected static final Log logger = LogFactory.getLog(FlowSessionImpl.class);
	
	/**
	 * The flow execution containing this flow session.
	 */
	private transient FlowExecutionInfo flowExecutionInfo;

	/**
	 * The flow definition (a singleton).
	 */
	private transient Flow flow;

	/**
	 * The current state of this flow session.
	 */
	private transient State currentState;

	/**
	 * The session status; may be CREATED, ACTIVE, SUSPENDED, or ENDED.
	 */
	private FlowSessionStatus status = FlowSessionStatus.CREATED;

	/**
	 * The session data model ("flow scope").
	 */
	private Scope flowScope = new Scope(ScopeType.FLOW);

	/**
	 * Set only on deserialization so this object can be fully reconstructed.
	 */
	private String flowId;

	/**
	 * Set only on deserialization so this object can be fully reconstructed.
	 */
	private String currentStateId;

	/**
	 * The parent session of this session (may be null if this is a root session.) 
	 */
	private transient FlowSession parent;
	
	/**
	 * Create a new flow session.
	 * @param flow the flow associated with this flow session
	 * @param input the input parameters used to populate the flow session
	 */
	public FlowSessionImpl(FlowExecutionImpl flowExecution, Flow flow, Map input, FlowSession parent) {
		Assert.notNull(flow, "The flow is required");
		Assert.notNull(flowExecution, "The flow execution is required");
		this.flow = flow;
		if (input != null) {
			this.flowScope.setAttributes(input);
		}
		this.parent = parent;
	}
	
	public FlowExecutionInfo getFlowExecutionInfo() {
		return flowExecutionInfo;
	}

	public Flow getFlow() {
		return flow;
	}

	public State getState() {
		return currentState;
	}

	/**
	 * Set the current state of this flow session.
	 * @param newState the state that is currently active in this flow session
	 */
	public void setState(State newState) {
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

	public FlowSession getParent() {
		return parent;
	}
	
	/**
	 * Set the parent of this flow session in the owning flow execution.
	 */
	public void setParent(FlowSession parent) {
		this.parent = parent;
	}
	
	public boolean isRoot() {
		return parent == null;
	}

	public Scope getFlowScope() {
		return this.flowScope;
	}
	
	// custom serialization

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(this.flow.getId());
		out.writeObject(this.currentState.getId());
		out.writeObject(this.status);
		out.writeObject(this.flowScope);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.flowId = (String)in.readObject();
		this.currentStateId = (String)in.readObject();
		this.status = (FlowSessionStatus)in.readObject();
		this.flowScope = (Scope)in.readObject();
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
				"attributesCount", (flowScope != null ? flowScope.size() : 0)).append("attributes", flowScope)
				.toString();
	}
}