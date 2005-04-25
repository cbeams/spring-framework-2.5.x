package org.springframework.samples.sellitem;

import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.State;
import org.springframework.web.flow.StateConditionTester;
import org.springframework.web.flow.StateEventVetoedException;
import org.springframework.web.flow.support.FlowExecutionListenerAdapter;

public class SellItemFlowExecutionListener extends FlowExecutionListenerAdapter {
	private StateConditionTester stateConditionTester = new SellItemStateConditionTester();

	public void stateEntering(RequestContext context, State nextState) throws StateEventVetoedException {
		stateConditionTester.testPreconditions(nextState, context);
	}
}
