package org.springframework.web.flow.support;

import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.State;

public interface StateCondition {
	public boolean test(State state, RequestContext context);
}