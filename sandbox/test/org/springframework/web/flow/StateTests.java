/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.util.HashMap;
import java.util.Map;

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
		Flow flow = new Flow("myFlow");
		ViewState state = new ViewState(flow, "viewState", "myViewName", new Transition("submit", "finish"));
		assertTrue(state.isTransitionable());
		assertTrue(!state.isMarker());
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelAndView view = flowExecution.start(null, request, null);
		assertEquals("viewState", flowExecution.getCurrentStateId());
		assertNotNull(view);
		assertEquals("myViewName", view.getViewName());
	}

	public void testViewStateMarker() {
		Flow flow = new Flow("myFlow");
		ViewState state = new ViewState(flow, "viewState", null, new Transition("submit", "finish"));
		assertTrue(state.isMarker());
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelAndView view = flowExecution.start(null, request, null);
		assertEquals("viewState", flowExecution.getCurrentStateId());
		assertNull(view);
	}

	public void testSubFlowState() {
		Flow subFlow = new Flow("mySubFlow");
		ViewState subFlowState = new ViewState(subFlow, "subFlowViewState", "mySubFlowViewName", new Transition(
				"submit", "finish"));
		new EndState(subFlow, "finish");
		Flow flow = new Flow("myFlow");
		SubFlowState state = new SubFlowState(flow, "subFlowState", subFlow, new Transition("finish", "finish"));
		new EndState(flow, "finish", "myParentFlowEndingViewName");
		FlowExecution flowExecution = flow.createExecution();
		MockHttpServletRequest request = new MockHttpServletRequest();
		ModelAndView view = flowExecution.start(null, request, null);
		assertEquals("mySubFlow", flowExecution.getActiveFlowId());
		assertEquals("subFlowViewState", flowExecution.getCurrentStateId());
		assertEquals("mySubFlowViewName", view.getViewName());
		view = flowExecution.signalEvent("submit", null, request, null);
		assertEquals("myParentFlowEndingViewName", view.getViewName());
		assertTrue(!flowExecution.isActive());
	}

	public void testSubFlowStateAttributesMapping() {
		Flow subFlow = new Flow("mySubFlow");
		ViewState subFlowState = new ViewState(subFlow, "subFlowViewState", "mySubFlowViewName", new Transition(
				"submit", "finish"));
		new EndState(subFlow, "finish");
		Flow flow = new Flow("myFlow");
		SubFlowState state = new SubFlowState(flow, "subFlowState", subFlow, new InputOutputMapper(), new Transition(
				"finish", "finish"));
		new EndState(flow, "finish", "myParentFlowEndingViewName");
		FlowExecution flowExecution = flow.createExecution();
		MockHttpServletRequest request = new MockHttpServletRequest();
		Map input = new HashMap();
		input.put("parentInputAttribute", "attributeValue");
		ModelAndView view = flowExecution.start(input, request, null);
		assertEquals("mySubFlow", flowExecution.getActiveFlowId());
		assertEquals("subFlowViewState", flowExecution.getCurrentStateId());
		assertEquals("mySubFlowViewName", view.getViewName());
		assertEquals("attributeValue", flowExecution.getAttribute("childInputAttribute"));
		view = flowExecution.signalEvent("submit", null, request, null);
		assertEquals("myParentFlowEndingViewName", view.getViewName());
		assertTrue(!flowExecution.isActive());
		assertEquals("attributeValue", view.getModel().get("parentOutputAttribute"));
	}

	public static class InputOutputMapper implements FlowModelMapper {
		public Map createSubFlowInputAttributes(FlowModel parentFlowModel) {
			Map inputMap = new HashMap(1);
			inputMap.put("childInputAttribute", parentFlowModel.getAttribute("parentInputAttribute"));
			return inputMap;
		}

		public void mapSubFlowOutputAttributes(FlowModel subFlowModel,
				MutableFlowModel parentFlowModel) {
			parentFlowModel.setAttribute("parentOutputAttribute", subFlowModel.getAttribute("childInputAttribute"));
		}
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
				MutableFlowModel attributes) throws Exception {
			executionCount++;
			return result;
		}
	}
}