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
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;

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
 * @author Keith Donald
 * @author Erwin Vervaet
 * @see org.springframework.web.flow.FlowExecution
 * @see org.springframework.web.flow.FlowExecutionStack
 */
public class FlowSession implements Serializable {

	private static final long serialVersionUID = 3834024745107862072L;

	protected static final Log logger = LogFactory.getLog(FlowSession.class);

	/**
	 * The flow definition (a singleton)
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
	private Scope attributes = new Scope();

	/**
	 * Set only on deserialization so this object can be fully reconstructed.
	 */
	private String flowId;

	/**
	 * Set only on deserialization so this object can be fully reconstructed.
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
			this.attributes.setAttributes(input);
		}
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
	 * Returns the state that is currently active in this flow session
	 */
	public State getCurrentState() {
		return currentState;
	}

	/**
	 * Set the current state of this flow session.
	 * @param newState The state that is currently active in this flow session
	 */
	protected void setCurrentState(State newState) {
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
			logger
					.debug("Setting current state of this '" + getFlow().getId() + "' flow session to '" + newState
							+ "'");
		}
		this.currentState = newState;
	}

	public Scope flowScope() {
		return this.attributes;
	}
	
	public Map getModel() {
		return this.attributes.getAttributeMap();
	}
	
	// custom serialization
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(this.flow.getId());
		out.writeObject(this.currentState.getId());
		out.writeObject(this.status);
		out.writeObject(this.attributes);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.flowId = (String)in.readObject();
		this.currentStateId = (String)in.readObject();
		this.status = (FlowSessionStatus)in.readObject();
		this.attributes = (Scope)in.readObject();
	}

	/**
	 * Restore this <code>Flow Session</code> for use after deserialization.
	 * @param flowLocator the flow locator
	 */
	protected void rehydrate(FlowLocator flowLocator) {
		// implementation note: we cannot integrate this code into the
		// readObject() method since we need the flow locator!
		Assert.state(this.flow == null, "The flow is already set - already restored");
		Assert.state(this.currentState == null, "The current state is already set - already restored");
		Assert
				.notNull(flowId,
						"The flow id was not set during deserialization: cannot restore--was this flow session deserialized properly?");
		this.flow = flowLocator.getFlow(this.flowId);
		this.flowId = null;
		Assert
				.notNull(currentStateId,
						"The current state id was not set during deserialization: cannot restore--was this flow session deserialized properly?");
		this.currentState = this.flow.getRequiredState(this.currentStateId);
		this.currentStateId = null;
	}

	public String toString() {
		return new ToStringCreator(this).append("flow", flow).append("currentState", currentState).append(
				"attributesCount", (attributes != null ? attributes.size() : 0)).append("attributes", attributes)
				.toString();
	}
}