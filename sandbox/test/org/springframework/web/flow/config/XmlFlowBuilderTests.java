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

import org.springframework.binding.AttributeAccessor;
import org.springframework.binding.AttributeSetter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.FlowExecutionContext;
import org.springframework.web.flow.LocalEvent;
import org.springframework.web.flow.NoSuchFlowDefinitionException;
import org.springframework.web.flow.ServiceLookupException;
import org.springframework.web.flow.SubFlowState;
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

	protected void setUp() throws Exception {
		XmlFlowBuilder builder = new XmlFlowBuilder(new ClassPathResource("testFlow.xml", XmlFlowBuilderTests.class));
		builder.setFlowServiceLocator(new TestFlowServiceLocator());
		flow = new FlowFactoryBean(builder).getFlow();
	}

	public void testBuildResult() {
		assertNotNull(flow);
		assertEquals("testFlow", flow.getId());
		assertEquals("actionState1", flow.getStartState().getId());
		assertEquals(7, flow.getStateIds().length);

		ActionState actionState1 = (ActionState)flow.getState("actionState1");
		assertNotNull(actionState1);
		assertEquals(2, actionState1.getActionCount());
		assertEquals(null, actionState1.getActionName(actionState1.getActions()[0]));
		assertEquals("action2Name", actionState1.getActionName(actionState1.getActions()[1]));
		assertEquals(2, actionState1.getTransitions().length);
		//assertNotNull(actionState1.getTransition("event1"));
		//assertEquals("viewState1", actionState1.getTransition("event1").getTargetStateId());
		//assertNotNull(actionState1.getTransition("action2Name.event2"));
		//assertEquals("viewState2", actionState1.getTransition("action2Name.event2").getTargetStateId());

		ViewState viewState1 = (ViewState)flow.getState("viewState1");
		assertNotNull(viewState1);
		assertFalse(viewState1.isMarker());
		assertEquals("view1", viewState1.getViewName());
		assertEquals(1, viewState1.getTransitions().length);
		//assertNotNull(viewState1.getTransition("event1"));
		//assertEquals("subFlowState1", viewState1.getTransition("event1").getTargetStateId());

		ViewState viewState2 = (ViewState)flow.getState("viewState2");
		assertNotNull(viewState2);
		assertTrue(viewState2.isMarker());
		assertNull(viewState2.getViewName());
		assertEquals(1, viewState2.getTransitions().length);
		//assertNotNull(viewState2.getTransition("event2"));
		//assertEquals("subFlowState2", viewState2.getTransition("event2").getTargetStateId());

		SubFlowState subFlowState1 = (SubFlowState)flow.getState("subFlowState1");
		assertNotNull(subFlowState1);
		assertNotNull(subFlowState1.getSubFlow());
		assertEquals("subFlow1", subFlowState1.getSubFlow().getId());
		assertNotNull(subFlowState1.getFlowAttributeMapper());
		assertEquals(1, subFlowState1.getTransitions().length);
		//assertNotNull(subFlowState1.getTransition("event1"));
		//assertEquals("endState1", subFlowState1.getTransition("event1").getTargetStateId());

		SubFlowState subFlowState2 = (SubFlowState)flow.getState("subFlowState2");
		assertNotNull(subFlowState2);
		assertNotNull(subFlowState2.getSubFlow());
		assertEquals("subFlow2", subFlowState2.getSubFlow().getId());
		assertNull(subFlowState2.getFlowAttributeMapper());
		assertEquals(1, subFlowState2.getTransitions().length);
		//assertNotNull(subFlowState2.getTransition("event2"));
		//assertEquals("endState2", subFlowState2.getTransition("event2").getTargetStateId());

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
		public Action getAction(String actionId) throws ServiceLookupException {
			if ("action1".equals(actionId) || "action2".equals(actionId)) {
				return new Action() {
					public Event execute(FlowExecutionContext context) throws Exception {
						return new LocalEvent("event1");
					}
				};
			}
			throw new NoSuchActionException(actionId);
		}

		public Flow getFlow(String flowDefinitionId) throws ServiceLookupException {
			if ("subFlow1".equals(flowDefinitionId) || "subFlow2".equals(flowDefinitionId)) {
				return new Flow(flowDefinitionId);
			}
			throw new NoSuchFlowDefinitionException(flowDefinitionId);
		}

		public FlowAttributeMapper getFlowAttributeMapper(String flowModelMapperId) throws ServiceLookupException {
			if ("modelMapper1".equals(flowModelMapperId)) {
				return new FlowAttributeMapper() {
					public Map createSubFlowInputAttributes(AttributeAccessor parentFlowModel) {
						return new HashMap();
					}

					public void mapSubFlowOutputAttributes(AttributeAccessor endingSubFlowModel,
							AttributeSetter resumingParentFlowModel) {
					}
				};
			}
			throw new NoSuchFlowAttributeMapperException(flowModelMapperId);
		}
	};

}