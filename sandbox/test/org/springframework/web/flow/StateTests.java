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
import org.springframework.binding.support.Mapping;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.action.EventParameterMapperAction;
import org.springframework.web.flow.config.SimpleTransitionCriteriaCreator;
import org.springframework.web.flow.config.TransitionCriteriaCreator;

/**
 * Tests that each of the Flow state types execute as expected when entered.
 * 
 * @author Keith Donald
 */
public class StateTests extends TestCase {

	private static TransitionCriteriaCreator factory = new SimpleTransitionCriteriaCreator();

	public static TransitionCriteria on(String event) {
		return factory.create(event);
	}
	public void testActionStateSingleAction() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState", new ExecutionCounterAction(), new Transition(
				on("success"), "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
		assertNull(view);
		assertEquals("success", flowExecution.getLastEventId());
		assertEquals(1, ((ExecutionCounterAction)state.getAction().getTargetAction()).getExecutionCount());
	}

	public void testActionStateActionChain() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState", new Action[] {
				new ExecutionCounterAction("not mapped result"), new ExecutionCounterAction(null),
				new ExecutionCounterAction(""), new ExecutionCounterAction("success") }, new Transition(on("success"),
				"finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
		assertNull(view);
		assertEquals("success", flowExecution.getLastEventId());
		ActionStateAction[] actions = state.getActions();
		for (int i = 0; i < actions.length; i++) {
			ActionStateAction action = actions[i];
			assertEquals(1, ((ExecutionCounterAction)(action.getTargetAction())).getExecutionCount());
		}
	}

	public void testActionStateActionChainNoMatchingTransition() {
		Flow flow = new Flow("myFlow");
		new ActionState(flow, "actionState", new Action[] { new ExecutionCounterAction("not mapped result"),
				new ExecutionCounterAction(null), new ExecutionCounterAction(""),
				new ExecutionCounterAction("yet another not mapped result") }, new Transition(on("success"), "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		try {
			flowExecution.start(new SimpleEvent(this, "start"));
			fail("Should not have matched to another state transition");
		}
		catch (NoMatchingTransitionException e) {
			// expected
		}
	}

	public void testActionStateActionChainNamedActions() {
		Flow flow = new Flow("myFlow");
		ActionStateAction[] actions = new ActionStateAction[4];
		actions[0] = new ActionStateAction(new ExecutionCounterAction("not mapped result"));
		actions[1] = new ActionStateAction(new ExecutionCounterAction(null));
		actions[2] = new ActionStateAction(new ExecutionCounterAction(""));
		actions[2].setName("action3");
		actions[3] = new ActionStateAction(new ExecutionCounterAction("success"));
		actions[3].setName("action4");
		ActionState state = new ActionState(flow, "actionState", actions, new Transition(on("action4.success"), "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
		assertNull(view);
		assertEquals("action4.success", flowExecution.getLastEventId());
		actions = state.getActions();
		for (int i = 0; i < actions.length; i++) {
			ActionStateAction action = actions[i];
			assertEquals(1, ((ExecutionCounterAction)(action.getTargetAction())).getExecutionCount());
		}
	}

	public void testViewState() {
		Flow flow = new Flow("myFlow");
		ViewState state = new ViewState(flow, "viewState", "myViewName", new Transition(on("submit"), "finish"));
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
		ViewState state = new ViewState(flow, "viewState", null, new Transition(on("submit"), "finish"));
		assertTrue(state.isMarker());
		new EndState(flow, "finish");
		FlowExecution flowExecution = flow.createExecution();
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start"));
		assertEquals("viewState", flowExecution.getCurrentStateId());
		assertNull(view);
	}

	public void testSubFlowState() {
		Flow subFlow = new Flow("mySubFlow");
		new ViewState(subFlow, "subFlowViewState", "mySubFlowViewName", new Transition(on("submit"), "finish"));
		new EndState(subFlow, "finish");
		Flow flow = new Flow("myFlow");
		new SubFlowState(flow, "subFlowState", subFlow, new Transition(on("finish"), "finish"));
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
		new ViewState(subFlow, "subFlowViewState", "mySubFlowViewName", new Transition(on("submit"), "finish"));
		new EndState(subFlow, "finish");
		Flow flow = new Flow("myFlow");
		new ActionState(flow, "mapperState", new EventParameterMapperAction(new Mapping("parentInputAttribute"), ScopeType.FLOW), new Transition(on("success"), "subFlowState"));
		new SubFlowState(flow, "subFlowState", subFlow, new InputOutputMapper(), new Transition(on("finish"), "finish"));
		new EndState(flow, "finish", "myParentFlowEndingViewName");
		FlowExecutionStack flowExecution = (FlowExecutionStack)flow.createExecution();
		Map input = new HashMap();
		input.put("parentInputAttribute", "attributeValue");
		ViewDescriptor view = flowExecution.start(new SimpleEvent(this, "start", input));
		assertEquals("mySubFlow", flowExecution.getActiveFlowId());
		assertEquals("subFlowViewState", flowExecution.getCurrentStateId());
		assertEquals("mySubFlowViewName", view.getViewName());
		assertEquals("attributeValue", flowExecution.getActiveFlowSession().getFlowScope().getAttribute(
				"childInputAttribute"));
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