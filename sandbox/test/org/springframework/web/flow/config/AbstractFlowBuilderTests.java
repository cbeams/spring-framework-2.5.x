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
import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.FlowExecutionContext;
import org.springframework.web.flow.LocalEvent;
import org.springframework.web.flow.ServiceLookupException;
import org.springframework.web.flow.SubFlowState;
import org.springframework.web.flow.ViewState;

/**
 * Test java based flow builder logic.
 * @author Keith Donald
 * @author Rod Johnson
 * @author Colin Sampaleanu
 */
public class AbstractFlowBuilderTests extends TestCase {

	private String PERSONS_LIST = "persons";

	private static String PERSON_DETAILS = "personDetails";

	public void testDependencyLookup() {
		TestMasterFlowBuilderLookupById master = new TestMasterFlowBuilderLookupById();
		master.setFlowServiceLocator(new FlowServiceLocatorAdapter() {
			public Action getAction(String actionId) throws ServiceLookupException {
				return new NoOpAction();
			}

			public Flow getFlow(String flowDefinitionId) throws ServiceLookupException {
				if (flowDefinitionId.equals(PERSON_DETAILS)) {
					BaseFlowBuilder builder = new TestDetailFlowBuilderLookupById();
					builder.setFlowServiceLocator(this);
					return new FlowFactoryBean(builder).getFlow();
				}
				else {
					throw new UnsupportedOperationException();
				}
			}

			public FlowAttributeMapper getFlowAttributeMapper(String id) throws ServiceLookupException {
				if (id.equals("personId.modelMapper")) {
					return new PersonIdMapper();
				}
				else {
					throw new UnsupportedOperationException();
				}
			}
		});
		Flow flow = new FlowFactoryBean(master).getFlow();
		assertEquals("persons", flow.getId());
		assertTrue(flow.getStateCount() == 4);
		assertTrue(flow.containsState("persons.get"));
		assertTrue(flow.getState("persons.get") instanceof ActionState);
		assertTrue(flow.containsState("persons.view"));
		assertTrue(flow.getState("persons.view") instanceof ViewState);
		assertTrue(flow.containsState("personDetails"));
		assertTrue(flow.getState("personDetails") instanceof SubFlowState);
		assertTrue(flow.containsState("finish"));
		assertTrue(flow.getState("finish") instanceof EndState);
	}

	public void testNoBeanFactorySet() {
		TestMasterFlowBuilderLookupById master = new TestMasterFlowBuilderLookupById();
		try {
			new FlowFactoryBean(master).getFlow();
			fail("Should have failed, no bean factory set for default bean factory flow service locator");
		}
		catch (IllegalStateException e) {
			// expected
		}
	}

	public class TestMasterFlowBuilderLookupById extends AbstractFlowBuilder {
		protected String flowId() {
			return PERSONS_LIST;
		}

		public void buildStates() {
			addGetState(PERSONS_LIST);
			addViewState(PERSONS_LIST, onSubmit(PERSON_DETAILS));
			addSubFlowState(PERSON_DETAILS, useAttributeMapper("personId"), get(PERSONS_LIST));
			addFinishEndState();
		}
	}

	public class TestMasterFlowBuilderLookupByType extends AbstractFlowBuilder {
		protected String flowId() {
			return PERSONS_LIST;
		}

		public void buildStates() {
			addGetState(PERSONS_LIST, executeAction(NoOpAction.class));
			addViewState(PERSONS_LIST, onSubmit(PERSON_DETAILS));
			addSubFlowState(PERSON_DETAILS, TestDetailFlowBuilderLookupByType.class,
					useAttributeMapper(PersonIdMapper.class), get(PERSONS_LIST));
			addFinishEndState();
		}
	}

	public class TestMasterFlowBuilderDependencyInjection extends AbstractFlowBuilder {
		private NoOpAction noOpAction;

		private Flow subFlow;

		private PersonIdMapper personIdMapper;

		public void setNoOpAction(NoOpAction noOpAction) {
			this.noOpAction = noOpAction;
		}

		public void setPersonIdMapper(PersonIdMapper personIdMapper) {
			this.personIdMapper = personIdMapper;
		}

		public void setSubFlow(Flow subFlow) {
			this.subFlow = subFlow;
		}

		protected String flowId() {
			return PERSONS_LIST;
		}

		public void buildStates() {
			addGetState(PERSONS_LIST, noOpAction);
			addViewState(PERSONS_LIST, onSubmit(PERSON_DETAILS));
			addSubFlowState(PERSON_DETAILS, subFlow, personIdMapper, get(PERSONS_LIST));
			addFinishEndState();
		}
	}

	public static class PersonIdMapper implements FlowAttributeMapper {
		public Map createSubFlowInputAttributes(AttributeAccessor parentFlowModel) {
			Map inputMap = new HashMap(1);
			inputMap.put("personId", parentFlowModel.getAttribute("personId"));
			return inputMap;
		}

		public void mapSubFlowOutputAttributes(AttributeAccessor subFlowModel, AttributeSetter parentFlowModel) {
		}
	}

	private class TestDetailFlowBuilderLookupById extends AbstractFlowBuilder {
		protected String flowId() {
			return PERSON_DETAILS;
		}

		public void buildStates() {
			addGetState(PERSON_DETAILS);
			addViewState(PERSON_DETAILS);
			addBindAndValidateState(PERSON_DETAILS);
			addFinishEndState();
		}
	}

	public class TestDetailFlowBuilderLookupByType extends AbstractFlowBuilder {
		protected String flowId() {
			return PERSON_DETAILS;
		}

		public void buildStates() {
			addGetState(PERSON_DETAILS, executeAction(NoOpAction.class));
			addViewState(PERSON_DETAILS);
			addBindAndValidateState(PERSON_DETAILS, executeAction(NoOpAction.class));
			addFinishEndState();
		}
	};

	public static class TestDetailFlowBuilderDependencyInjection extends AbstractFlowBuilder {

		private NoOpAction noOpAction;

		public void setNoOpAction(NoOpAction noOpAction) {
			this.noOpAction = noOpAction;
		}

		protected String flowId() {
			return PERSON_DETAILS;
		}

		public void buildStates() {
			addGetState(PERSON_DETAILS, noOpAction);
			addViewState(PERSON_DETAILS);
			addBindAndValidateState(PERSON_DETAILS, noOpAction);
			addFinishEndState();
		}
	};

	/**
	 * Action bean stub that does nothing, just returns a "success" result
	 */
	public static final class NoOpAction implements Action {
		public Event execute(FlowExecutionContext context) throws Exception {
			return new LocalEvent("success");
		}
	}
}