package org.springframework.samples.sellitem;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.State;
import org.springframework.web.flow.execution.EnterStateVetoException;
import org.springframework.web.flow.execution.servlet.ServletEvent;
import org.springframework.web.flow.support.FlowExecutionListenerAdapter;

public class SellItemFlowExecutionListener extends FlowExecutionListenerAdapter {
	public void stateEntering(RequestContext context, State nextState) throws EnterStateVetoException {
		HttpServletRequest request = ((ServletEvent)context.getSourceEvent()).getRequest();
		String role = (String)nextState.getProperty("role");
		if (StringUtils.hasText(role)) {
			if (!request.isUserInRole(role)) {
				throw new EnterStateVetoException(nextState, "State requires role '" + role
						+ "', but the authenticated user doesn't have it!");
			}
		}
	}
}
