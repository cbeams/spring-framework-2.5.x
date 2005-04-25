package org.springframework.web.flow;

public interface StateConditionTester {
	public void testPreconditions(State state, RequestContext context) throws StateConditionViolationException;

	public void testPostconditons(State state, RequestContext context) throws StateConditionViolationException;
}