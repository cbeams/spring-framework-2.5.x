/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.flow.StateTests.ExecutionCounterAction;
import org.springframework.web.flow.StateTests.InputOutputMapper;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Keith Donald
 */
public class FlowTests extends TestCase {
	public void testFlowExecutionListener() {
		Flow subFlow = new Flow("mySubFlow");
		ViewState subFlowState = new ViewState(subFlow, "subFlowViewState", "mySubFlowViewName", new Transition(
				"submit", "finish"));
		new EndState(subFlow, "finish");
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState", new ExecutionCounterAction(), new Transition(
				"success", "viewState"));
		ViewState viewState = new ViewState(flow, "viewState", "myView", new Transition("submit", "subFlowState"));
		new SubFlowState(flow, "subFlowState", subFlow, new InputOutputMapper(), new Transition("finish", "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		MockFlowExecutionListener flowExecutionListener = new MockFlowExecutionListener();
		flowExecution.getListenerList().add(flowExecutionListener);
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelAndView view = flowExecution.start(null, request, null);
		assertEquals(1, flowExecutionListener.flowExecutionsStarted);
		assertEquals(2, flowExecutionListener.stateTransitions);
		view = flowExecution.signalEvent("submit", null, request, null);
		assertEquals(2, flowExecutionListener.flowExecutionsStarted);
		assertEquals(4, flowExecutionListener.stateTransitions);
		view = flowExecution.signalEvent("submit", null, request, null);
		assertEquals(0, flowExecutionListener.flowExecutionsStarted);
		assertEquals(6, flowExecutionListener.stateTransitions);
	}

	private class MockFlowExecutionListener implements FlowExecutionListener {
		private int flowExecutionsStarted;

		private boolean requestSubmitted;

		private boolean requestProcessed;

		private int stateTransitions;

		private boolean eventSignaled;

		public void ended(FlowExecution flowExecution, FlowSession endedRootFlowSession) {
			flowExecutionsStarted--;
			assertTrue(flowExecutionsStarted == 0);
		}

		public void eventSignaled(FlowExecution flowExecution, String eventId) {
			assertTrue(flowExecutionsStarted > 0);
			eventSignaled = true;
		}

		public void requestProcessed(FlowExecution flowExecution, HttpServletRequest request) {
			requestProcessed = true;
			requestSubmitted = false;
		}

		public void requestSubmitted(FlowExecution flowExecution, HttpServletRequest request) {
			assertTrue(flowExecutionsStarted > 0);
			requestSubmitted = true;
			requestProcessed = false;
		}

		public void started(FlowExecution flowExecution) {
			assertTrue(flowExecutionsStarted == 0);
			flowExecutionsStarted++;
			stateTransitions = 0;
		}

		public void stateTransitioned(FlowExecution flowExecution, AbstractState previousState, AbstractState newState) {
			assertTrue(flowExecutionsStarted > 0);
			stateTransitions++;
			eventSignaled = false;
		}

		public void subFlowEnded(FlowExecution flowExecution, FlowSession endedSession) {
			assertTrue(flowExecutionsStarted > 0);
			flowExecutionsStarted--;
			assertTrue(flowExecutionsStarted > 0);
		}

		public void subFlowSpawned(FlowExecution flowExecution) {
			assertTrue(flowExecutionsStarted > 0);
			flowExecutionsStarted++;
		}
	}
}