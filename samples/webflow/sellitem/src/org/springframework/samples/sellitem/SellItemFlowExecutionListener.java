package org.springframework.samples.sellitem;

import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.State;
import org.springframework.web.flow.EnterStateVetoException;
import org.springframework.web.flow.support.FlowExecutionListenerAdapter;
import org.springframework.web.flow.support.StateConditionTester;

public class SellItemFlowExecutionListener extends FlowExecutionListenerAdapter {
	private StateConditionTester stateConditionTester = new SellItemStateConditionTester();

	public void stateEntering(RequestContext context, State nextState) throws EnterStateVetoException {
		stateConditionTester.testPreconditions(nextState, context);
	}
}
