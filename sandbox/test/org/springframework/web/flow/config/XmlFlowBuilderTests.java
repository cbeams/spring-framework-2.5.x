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
package org.springframework.web.flow.config;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.binding.AttributeSource;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.SimpleEvent;
import org.springframework.web.flow.NoSuchFlowDefinitionException;
import org.springframework.web.flow.ServiceLookupException;
import org.springframework.web.flow.MockRequestContext;
import org.springframework.web.flow.SubFlowState;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.ViewState;

/**
 * Test case for XML flow builder.
 * 
 * @see org.springframework.web.flow.config.XmlFlowBuilder
 * 
 * @author Erwin Vervaet
 */
public class XmlFlowBuilderTests extends TestCase {

	private Flow flow;
	private MockRequestContext context;

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("testFlow.xml", XmlFlowBuilderTests.class));
		builder.setFlowServiceLocator(new TestFlowServiceLocator());
		flow = new FlowFactoryBean(builder).getFlow();
		
		context=new MockRequestContext();
	}
	
	private Event createEvent(String id) {
		return new SimpleEvent(this, id);
	}

	public void testBuildResult() {
		assertNotNull(flow);
		assertEquals("testFlow", flow.getId());
		assertEquals("actionState1", flow.getStartState().getId());
		assertEquals(7, flow.getStateIds().length);

		ActionState actionState1 = (ActionState)flow.getState("actionState1");
		assertNotNull(actionState1);
		assertEquals(5, actionState1.getActionCount());
		assertEquals(null, actionState1.getAction().getCaption());
		assertEquals("action2Name", actionState1.getActions()[1].getCaption());
		assertEquals(2, actionState1.getTransitions().length);
		context.setLastEvent(createEvent("event1"));
		assertTrue(actionState1.hasTransitionFor(context));
		Transition transition = actionState1.getTransition(context);
		assertEquals("viewState1", transition.getTargetStateId());
		context.setLastEvent(createEvent("action2Name.event2"));
		assertTrue(actionState1.hasTransitionFor(context));
		transition = actionState1.getTransition(context);
		assertEquals("viewState2", transition.getTargetStateId());

		ViewState viewState1 = (ViewState)flow.getState("viewState1");
		assertNotNull(viewState1);
		assertFalse(viewState1.isMarker());
		assertEquals("view1", viewState1.getViewName());
		assertEquals(1, viewState1.getTransitions().length);
		context.setLastEvent(createEvent("event1"));
		assertTrue(viewState1.hasTransitionFor(context));
		transition = viewState1.getTransition(context);
		assertEquals("subFlowState1", transition.getTargetStateId());

		ViewState viewState2 = (ViewState)flow.getState("viewState2");
		assertNotNull(viewState2);
		assertTrue(viewState2.isMarker());
		assertNull(viewState2.getViewName());
		assertEquals(1, viewState2.getTransitions().length);
		context.setLastEvent(createEvent("event2"));
		assertTrue(viewState2.hasTransitionFor(context));
		transition = viewState2.getTransition(context);
		assertEquals("subFlowState2", transition.getTargetStateId());

		SubFlowState subFlowState1 = (SubFlowState)flow.getState("subFlowState1");
		assertNotNull(subFlowState1);
		assertNotNull(subFlowState1.getSubFlow());
		assertEquals("subFlow1", subFlowState1.getSubFlow().getId());
		assertNotNull(subFlowState1.getFlowAttributeMapper());
		assertEquals(1, subFlowState1.getTransitions().length);
		context.setLastEvent(createEvent("event1"));
		assertTrue(subFlowState1.hasTransitionFor(context));
		transition = subFlowState1.getTransition(context);
		assertEquals("endState1", transition.getTargetStateId());

		SubFlowState subFlowState2 = (SubFlowState)flow.getState("subFlowState2");
		assertNotNull(subFlowState2);
		assertNotNull(subFlowState2.getSubFlow());
		assertEquals("subFlow2", subFlowState2.getSubFlow().getId());
		assertNull(subFlowState2.getFlowAttributeMapper());
		assertEquals(1, subFlowState2.getTransitions().length);
		context.setLastEvent(createEvent("event2"));
		assertTrue(subFlowState2.hasTransitionFor(context));
		transition = subFlowState2.getTransition(context);
		assertEquals("endState2", transition.getTargetStateId());

		EndState endState1 = (EndState)flow.getState("endState1");
		assertNotNull(endState1);
		assertFalse(endState1.isMarker());
		assertEquals("endView1", endState1.getViewName());

		EndState endState2 = (EndState)flow.getState("endState2");
		assertNotNull(endState2);
		assertTrue(endState2.isMarker());
		assertNull(endState2.getViewName());
	}

	/**
	 * Flow service locator for the services needed by the testFlow (defined in
	 * testFlow.xml)
	 * 
	 * @author Erwin Vervaet
	 */
	public static class TestFlowServiceLocator extends FlowServiceLocatorAdapter {
		public Action createAction(Class implementationClass, AutowireMode autowire) {
			return new TestAction();
		}

		public Action getAction(Class implementationClass) throws ServiceLookupException {
			return new TestAction();
		}

		public Action getAction(String actionId) throws ServiceLookupException {
			if ("action1".equals(actionId) || "action2".equals(actionId)) {
				return new Action() {
					public Event execute(RequestContext context) throws Exception {
						return new SimpleEvent(this, "event1");
					}
				};
			}
			throw new NoSuchActionException(actionId, null);
		}

		public Flow getFlow(String flowDefinitionId) throws ServiceLookupException {
			if ("subFlow1".equals(flowDefinitionId) || "subFlow2".equals(flowDefinitionId)) {
				return new Flow(flowDefinitionId);
			}
			throw new NoSuchFlowDefinitionException(flowDefinitionId, null);
		}

		public FlowAttributeMapper getFlowAttributeMapper(String flowModelMapperId) throws ServiceLookupException {
			if ("attributeMapper1".equals(flowModelMapperId)) {
				return new FlowAttributeMapper() {
					public Map createSubFlowInputAttributes(AttributeSource parentFlowModel) {
						return new HashMap();
					}

					public void mapSubFlowOutputAttributes(AttributeSource endingSubFlowModel,
							MutableAttributeSource resumingParentFlowModel) {
					}
				};
			}
			throw new NoSuchFlowAttributeMapperException(flowModelMapperId, null);
		}
	};

	public static class TestAction implements Action {
		public Event execute(RequestContext context) throws Exception {
			return new SimpleEvent(this, "success");
		}
	}
}