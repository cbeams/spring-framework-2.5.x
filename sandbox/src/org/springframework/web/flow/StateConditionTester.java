package org.springframework.web.flow;

public interface StateConditionTester {
	public void testPreconditions(State state) throws StateConditionViolationException;

	public void testPostconditons(State state) throws StateConditionViolationException;
}