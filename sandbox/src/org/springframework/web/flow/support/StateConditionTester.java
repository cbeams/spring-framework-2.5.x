package org.springframework.web.flow.support;

import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.State;

public interface StateConditionTester {
	public void testPreconditions(State state, RequestContext context) throws StateConditionViolationException;

	public void testPostconditons(State state, RequestContext context) throws StateConditionViolationException;
}