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
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.Mapping;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.action.AttributeMapperAction;
import org.springframework.web.flow.config.TextToTransitionCriteria;
import org.springframework.web.flow.config.TextToViewDescriptorCreator;
import org.springframework.web.flow.execution.FlowExecution;
import org.springframework.web.flow.execution.impl.FlowExecutionImpl;

/**
 * Tests that each of the Flow state types execute as expected when entered.
 * 
 * @author Keith Donald
 */
public class StateTests extends TestCase {

	public void testActionStateSingleAction() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState", new ExecutionCounterAction(), new Transition(
				on("success"), "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ViewDescriptor view = flowExecution.start(new Event(this, "start"));
		assertNull(view);
		assertEquals("success", flowExecution.getLastEventId());
		assertEquals(1, ((ExecutionCounterAction)state.getAction().getTargetAction()).getExecutionCount());
	}

	public void testActionAttributesChain() {
		Flow flow = new Flow("myFlow");
		ActionState state = new ActionState(flow, "actionState", new Action[] {
				new ExecutionCounterAction("not mapped result"), new ExecutionCounterAction(null),
				new ExecutionCounterAction(""), new ExecutionCounterAction("success") }, new Transition(on("success"),
				"finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ViewDescriptor view = flowExecution.start(new Event(this, "start"));
		assertNull(view);
		assertEquals("success", flowExecution.getLastEventId());
		AnnotatedAction[] actions = state.getActions();
		for (int i = 0; i < actions.length; i++) {
			AnnotatedAction action = actions[i];
			assertEquals(1, ((ExecutionCounterAction)(action.getTargetAction())).getExecutionCount());
		}
	}

	public void testActionAttributesChainNoMatchingTransition() {
		Flow flow = new Flow("myFlow");
		new ActionState(flow, "actionState", new Action[] { new ExecutionCounterAction("not mapped result"),
				new ExecutionCounterAction(null), new ExecutionCounterAction(""),
				new ExecutionCounterAction("yet another not mapped result") }, new Transition(on("success"), "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		try {
			flowExecution.start(new Event(this, "start"));
			fail("Should not have matched to another state transition");
		}
		catch (NoMatchingTransitionException e) {
			// expected
		}
	}

	public void testActionAttributesChainNamedActions() {
		Flow flow = new Flow("myFlow");
		AnnotatedAction[] actions = new AnnotatedAction[4];
		actions[0] = new AnnotatedAction(new ExecutionCounterAction("not mapped result"));
		actions[1] = new AnnotatedAction(new ExecutionCounterAction(null));
		Properties properties = new Properties();
		properties.put("name", "action3");
		actions[2] = new AnnotatedAction(new ExecutionCounterAction(""), properties);
		properties = new Properties();
		properties.put("name", "action4");
		actions[3] = new AnnotatedAction(new ExecutionCounterAction("success"), properties);
		ActionState state = new ActionState(flow, "actionState", actions, new Transition(on("action4.success"), "finish"));
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ViewDescriptor view = flowExecution.start(new Event(this, "start"));
		assertNull(view);
		assertEquals("action4.success", flowExecution.getLastEventId());
		actions = state.getActions();
		for (int i = 0; i < actions.length; i++) {
			AnnotatedAction action = actions[i];
			assertEquals(1, ((ExecutionCounterAction)(action.getTargetAction())).getExecutionCount());
		}
	}

	public void testViewState() {
		Flow flow = new Flow("myFlow");
		ViewState state = new ViewState(flow, "viewState", view("myViewName"), new Transition(on("submit"), "finish"));
		assertTrue(state.isTransitionable());
		assertTrue(!state.isMarker());
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ViewDescriptor view = flowExecution.start(new Event(this, "start"));
		assertEquals("viewState", flowExecution.getActiveSession().getCurrentState().getId());
		assertNotNull(view);
		assertEquals("myViewName", view.getViewName());
	}

	public void testViewStateMarker() {
		Flow flow = new Flow("myFlow");
		ViewState state = new ViewState(flow, "viewState", new Transition(on("submit"), "finish"));
		assertTrue(state.isMarker());
		new EndState(flow, "finish");
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ViewDescriptor view = flowExecution.start(new Event(this, "start"));
		assertEquals("viewState", flowExecution.getActiveSession().getCurrentState().getId());
		assertNull(view);
	}

	public void testSubFlowState() {
		Flow subFlow = new Flow("mySubFlow");
		new ViewState(subFlow, "subFlowViewState", view("mySubFlowViewName"), new Transition(on("submit"), "finish"));
		new EndState(subFlow, "finish");
		Flow flow = new Flow("myFlow");
		new SubflowState(flow, "subFlowState", subFlow, new Transition(on("finish"), "finish"));
		new EndState(flow, "finish", view("myParentFlowEndingViewName"));
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		ViewDescriptor view = flowExecution.start(new Event(this, "start"));
		assertEquals("mySubFlow", flowExecution.getActiveSession().getFlow().getId());
		assertEquals("subFlowViewState", flowExecution.getActiveSession().getCurrentState().getId());
		assertEquals("mySubFlowViewName", view.getViewName());
		view = flowExecution.signalEvent(new Event(this, "submit"));
		assertEquals("myParentFlowEndingViewName", view.getViewName());
		assertTrue(!flowExecution.isActive());
	}

	public void testSubFlowStateModelMapping() {
		Flow subFlow = new Flow("mySubFlow");
		new ViewState(subFlow, "subFlowViewState", view("mySubFlowViewName"), new Transition(on("submit"), "finish"));
		new EndState(subFlow, "finish");
		Flow flow = new Flow("myFlow");
		new ActionState(flow, "mapperState", new AttributeMapperAction(new Mapping("sourceEvent.parameters.parentInputAttribute", "flowScope.parentInputAttribute")), new Transition(on("success"), "subFlowState"));
		new SubflowState(flow, "subFlowState", subFlow, new InputOutputMapper(), new Transition(on("finish"), "finish"));
		new EndState(flow, "finish", view("myParentFlowEndingViewName"));
		FlowExecution flowExecution = new FlowExecutionImpl(flow);
		Map input = new HashMap();
		input.put("parentInputAttribute", "attributeValue");
		ViewDescriptor view = flowExecution.start(new Event(this, "start", input));
		assertEquals("mySubFlow", flowExecution.getActiveSession().getFlow().getId());
		assertEquals("subFlowViewState", flowExecution.getActiveSession().getCurrentState().getId());
		assertEquals("mySubFlowViewName", view.getViewName());
		assertEquals("attributeValue", flowExecution.getActiveSession().getScope().getAttribute(
				"childInputAttribute"));
		view = flowExecution.signalEvent(new Event(this, "submit"));
		assertEquals("myParentFlowEndingViewName", view.getViewName());
		assertTrue(!flowExecution.isActive());
		assertEquals("attributeValue", view.getModel().get("parentOutputAttribute"));
	}

	public static TransitionCriteria on(String event) {
		return (TransitionCriteria)new TextToTransitionCriteria().convert(event);
	}
	
	public static ViewDescriptorCreator view(String viewName) {
		return (ViewDescriptorCreator)new TextToViewDescriptorCreator().convert(viewName);
	}
	
	public static class InputOutputMapper implements FlowAttributeMapper {
		public Map createSubflowInput(RequestContext context) {
			Map inputMap = new HashMap(1);
			inputMap.put("childInputAttribute", context.getFlowScope().getAttribute("parentInputAttribute"));
			return inputMap;
		}

		public void mapSubflowOutput(RequestContext context) {
			MutableAttributeSource parentAttributes = (MutableAttributeSource)context.getFlowContext().getActiveSession().getParent().getScope();
			parentAttributes.setAttribute("parentOutputAttribute", context.getFlowScope().getAttribute("childInputAttribute"));
		}
	}

	public static class ExecutionCounterAction implements Action {
		private Event result = new Event(this, "success");

		private int executionCount;

		public ExecutionCounterAction() {

		}

		public ExecutionCounterAction(String result) {
			if (StringUtils.hasText(result)) {
				this.result = new Event(this, result);
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