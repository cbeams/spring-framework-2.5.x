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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.AttributeSource;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Tests that each of the Flow state types execute as expected when entered.
 * 
 * @author Keith Donald
 */
public class StateTests extends TestCase {

	public void testActionStateSingleAction() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState", new ExecutionCounterAction(), new Transition(
				"success", "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
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
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
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
		try {
			ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
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
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
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
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
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
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
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
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
		assertEquals("mySubFlow", flowExecution.getActiveFlowId());
		assertEquals("subFlowViewState", flowExecution.getCurrentStateId());
		assertEquals("mySubFlowViewName", view.getViewName());
		view = flowExecution.signalEvent(new SimpleEvent(this, "submit"));
		assertEquals("myParentFlowEndingViewName", view.getViewName());
		assertTrue(!flowExecution.isActive());
	}

	public void testSubFlowStateModelMapping() {
		Flow subFlow = new Flow("mySubFlow");
		ViewState subFlowState = new ViewState(subFlow, "subFlowViewState", "mySubFlowViewName", new Transition(
				"submit", "finish"));
		new EndState(subFlow, "finish");
		Flow flow = new Flow("myFlow");
		SubFlowState state = new SubFlowState(flow, "subFlowState", subFlow, new InputOutputMapper(), new Transition(
				"finish", "finish"));
		new EndState(flow, "finish", "myParentFlowEndingViewName");
		FlowExecutionStack flowExecution = (FlowExecutionStack)flow.createExecution();
		Map input = new HashMap();
		input.put("parentInputAttribute", "attributeValue");
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start", input));
		assertEquals("mySubFlow", flowExecution.getActiveFlowId());
		assertEquals("subFlowViewState", flowExecution.getCurrentStateId());
		assertEquals("mySubFlowViewName", view.getViewName());
		assertEquals("attributeValue", flowExecution.getActiveFlowSession().getFlowScope().getAttribute("childInputAttribute"));
		view = flowExecution.signalEvent(new SimpleEvent(this, "submit"));
		assertEquals("myParentFlowEndingViewName", view.getViewName());
		assertTrue(!flowExecution.isActive());
		assertEquals("attributeValue", view.getModel().get("parentOutputAttribute"));
	}

	public static class InputOutputMapper implements FlowAttributeMapper {
		public Map createSubFlowInputAttributes(AttributeSource parentFlowModel) {
			Map inputMap = new HashMap(1);
			inputMap.put("childInputAttribute", parentFlowModel.getAttribute("parentInputAttribute"));
			return inputMap;
		}

		public void mapSubFlowOutputAttributes(AttributeSource subFlowModel, MutableAttributeSource parentFlowModel) {
			parentFlowModel.setAttribute("parentOutputAttribute", subFlowModel.getAttribute("childInputAttribute"));
		}
	}

	public static class ExecutionCounterAction implements Action {
		private Event result = new SimpleEvent(this, "success");

		private int executionCount;

		public ExecutionCounterAction() {

		}

		public ExecutionCounterAction(String result) {
			if (StringUtils.hasText(result)) {
				this.result = new SimpleEvent(this, result);
			}
			else {
				this.result = null;
			}
		}

		public int getExecutionCount() {
			return executionCount;
		}

		public Event execute(RequestContext context) throws Exception {
			executionCount++;
			return result;
		}
	}
}