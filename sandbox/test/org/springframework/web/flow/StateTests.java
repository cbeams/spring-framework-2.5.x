/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests that each of the Flow state types execute as expected when entered.
 * @author Keith Donald
 */
public class StateTests extends TestCase {

	public void testActionStateSingleAction() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState", new ExecutionCounterAction(), new Transition(
				"success", "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelAndView view = flowExecution.start(null, request, null);
		assertNull(view);
		assertEquals("success", flowExecution.getLastEventId());
		assertEquals(1, ((ExecutionCounterAction)state.getAction()).getExecutionCount());
	}

	public void testActionStateActionChain() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState", new Action[] {
				new ExecutionCounterAction("not mapped result"), new ExecutionCounterAction(null),
				new ExecutionCounterAction(""), new ExecutionCounterAction("success") }, new Transition("success",
				"finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelAndView view = flowExecution.start(null, request, null);
		assertNull(view);
		assertEquals("success", flowExecution.getLastEventId());
		Action[] actions = state.getActions();
		for (int i = 0; i < actions.length; i++) {
			assertEquals(1, ((ExecutionCounterAction)actions[i]).getExecutionCount());
		}
	}

	public void testActionStateActionChainNoMatchingTransition() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState", new Action[] {
				new ExecutionCounterAction("not mapped result"), new ExecutionCounterAction(null),
				new ExecutionCounterAction(""), new ExecutionCounterAction("yet another not mapped result") },
				new Transition("success", "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		MockHttpServletRequest request = new MockHttpServletRequest();
		try {
			ModelAndView view = flowExecution.start(null, request, null);
			fail("Should not have matched to another state transition");
		}
		catch (CannotExecuteStateTransitionException e) {
			// expected
		}
	}

	public void testActionStateActionChainNamedActions() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState", new String[] { null, null, "action3", "action4" },
				new Action[] { new ExecutionCounterAction("not mapped result"), new ExecutionCounterAction(null),
						new ExecutionCounterAction(""), new ExecutionCounterAction("success") }, new Transition(
						"action4.success", "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelAndView view = flowExecution.start(null, request, null);
		assertNull(view);
		assertEquals("action4.success", flowExecution.getLastEventId());
		Action[] actions = state.getActions();
		for (int i = 0; i < actions.length; i++) {
			assertEquals(1, ((ExecutionCounterAction)actions[i]).getExecutionCount());
		}
	}

	public void testViewState() {

	}

	public void testViewStateMarker() {

	}

	public static class ExecutionCounterAction implements Action {
		private String result = "success";

		private int executionCount;

		public ExecutionCounterAction() {

		}

		public ExecutionCounterAction(String result) {
			this.result = result;
		}

		public int getExecutionCount() {
			return executionCount;
		}

		public String execute(HttpServletRequest request, HttpServletResponse response,
				MutableAttributesAccessor attributes) throws Exception {
			executionCount++;
			return result;
		}
	}
}