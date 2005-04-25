package org.springframework.web.flow;

public interface StateCondition {
	public boolean test(State state, RequestContext context);
}