package org.springframework.mock.web.flow;

import org.springframework.binding.AttributeSource;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.FlowSessionStatus;
import org.springframework.web.flow.Scope;
import org.springframework.web.flow.ScopeType;
import org.springframework.web.flow.State;

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

	public boolean hasParent() {
		return this.parent != null;
	}
	
	public void setParent(FlowSession parent) {
		this.parent = parent;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public FlowSessionStatus getStatus() {
		return status;
	}

	public AttributeSource getAttributes() {
		return scope;
	}
	
	public void setStatus(FlowSessionStatus status) {
		this.status = status;
	}	
}