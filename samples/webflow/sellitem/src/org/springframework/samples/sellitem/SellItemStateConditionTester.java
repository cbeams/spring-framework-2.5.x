package org.springframework.samples.sellitem;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.State;
import org.springframework.web.flow.execution.servlet.HttpServletRequestEvent;
import org.springframework.web.flow.support.StateConditionTester;
import org.springframework.web.flow.support.StateConditionViolationException;

public class SellItemStateConditionTester implements StateConditionTester {
	public void testPreconditions(State state, RequestContext context) throws StateConditionViolationException {
		HttpServletRequest request = ((HttpServletRequestEvent)context.getOriginatingEvent()).getRequest();
		String role = (String)state.getProperty("role");
		if (StringUtils.hasText(role)) {
			if (!request.isUserInRole(role)) {
				throw new StateConditionViolationException(state, "State requires role '" + role
						+ "', but the authenticated user doesn't have it!");
			}
		}
	}
	
	public void testPostconditons(State state, RequestContext context) throws StateConditionViolationException {
	}
	
}
