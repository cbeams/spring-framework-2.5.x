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
package org.springframework.mock.web.flow;

import org.springframework.util.Assert;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.FlowSessionStatus;
import org.springframework.web.flow.Scope;
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.State;

/**
 * Mock implementation of the <code>FlowSession</code> interface.
 * 
 * TODO - belongs in the spring-mock.jar
 * 
 * @author Erwin Vervaet
 */
public class MockFlowSession implements FlowSession {
	
	private Flow flow;
	
	private State state;
	
	private FlowSessionStatus status;
	
	private Scope scope = new Scope(ScopeType.FLOW);
	
	private FlowSession parent;
	
	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public FlowSession getParent() {
		return parent;
	}

	public void setParent(FlowSession parent) {
		this.parent = parent;
	}

	public boolean isRoot() {
		return this.parent == null;
	}
	
	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		Assert.notNull(scope, "The flow scope is required");
		Assert.isTrue(scope.getScopeType() == ScopeType.FLOW, "The session maintains flow scope data");
		this.scope = scope;
	}

	public State getCurrentState() {
		return state;
	}

	public void setCurrentState(State state) {
		this.state = state;
	}

	public FlowSessionStatus getStatus() {
		return status;
	}

	public void setStatus(FlowSessionStatus status) {
		this.status = status;
	}	
}