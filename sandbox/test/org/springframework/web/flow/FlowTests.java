/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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