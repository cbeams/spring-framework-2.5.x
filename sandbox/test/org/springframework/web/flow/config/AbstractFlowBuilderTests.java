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

import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowAttributeMapper;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.SubflowState;
import org.springframework.web.flow.Transition;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.ViewState;
import org.springframework.web.flow.execution.ServiceLookupException;

/**
 * Test Java based flow builder logic (subclasses of AbstractFlowBuilder).
 * 
 * @see org.springframework.web.flow.config.AbstractFlowBuilder
 * 
 * @author Keith Donald
 * @author Rod Johnson
 * @author Colin Sampaleanu
 */
public class AbstractFlowBuilderTests extends TestCase {

	private String PERSONS_LIST = "person.List";

	private static String PERSON_DETAILS = "person.Detail";

	public void testDependencyLookup() {
		TestMasterFlowBuilderLookupById master = new TestMasterFlowBuilderLookupById();
		master.setFlowServiceLocator(new FlowServiceLocatorAdapter() {
			public Action getAction(String actionId) throws ServiceLookupException {
				return new NoOpAction();
			}
			
			public Flow createFlow(AutowireMode autowireMode) throws ServiceLookupException {
				return new Flow();
			}

			public Flow getFlow(String flowDefinitionId, Class flowBuilderImplementation) throws ServiceLookupException {
				if (flowDefinitionId.equals(PERSON_DETAILS)) {
					BaseFlowBuilder builder = new TestDetailFlowBuilderLookupById();
					builder.setFlowServiceLocator(this);
					return new FlowFactoryBean(builder).getFlow();
				}
				else {
					throw new ServiceLookupException(Flow.class, flowDefinitionId, null);
				}
			}

			public FlowAttributeMapper getFlowAttributeMapper(String id) throws ServiceLookupException {
				if (id.equals("id.attributeMapper")) {
					return new PersonIdMapper();
				}
				else {
					throw new ServiceLookupException(FlowAttributeMapper.class, id, null);
				}
			}
			
			public TransitionCriteria createTransitionCriteria(String encodedCriteria, AutowireMode autowireMode)
					throws ServiceLookupException {
				return new SimpleTransitionCriteriaCreator().create(encodedCriteria);
			}
		});
		Flow flow = new FlowFactoryBean(master).getFlow();
		assertEquals("person.List", flow.getId());
		assertTrue(flow.getStateCount() == 4);
		assertTrue(flow.containsState("getPersonList"));
		assertTrue(flow.getState("getPersonList") instanceof ActionState);
		assertTrue(flow.containsState("viewPersonList"));
		assertTrue(flow.getState("viewPersonList") instanceof ViewState);
		assertTrue(flow.containsState("person.Detail"));
		assertTrue(flow.getState("person.Detail") instanceof SubflowState);
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
			addActionState("getPersonList", action("noOptAction"), on(success(), "viewPersonList"));
			addViewState("viewPersonList", "person.list.view", on(submit(), "person.Detail"));
			addSubFlowState(PERSON_DETAILS, flow("person.Detail", TestDetailFlowBuilderLookupByType.class),
					attributeMapper("id.attributeMapper"), onAnyEvent("getPersonList"));
			addEndState("finish");
		}
	}

	public class TestMasterFlowBuilderLookupByType extends AbstractFlowBuilder {
		protected String flowId() {
			return PERSONS_LIST;
		}

		public void buildStates() {
			addActionState("getPersonList", actionRef(NoOpAction.class), on(success(), "viewPersonList"));
			addViewState("viewPersonList", "person.list.view", on(submit(), "person.Detail"));
			addSubFlowState(PERSON_DETAILS, flow("person.Detail", TestDetailFlowBuilderLookupByType.class),
					attributeMapperRef(PersonIdMapper.class), onAnyEvent("getPersonList"));
			addEndState("finish");
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
			addActionState("getPersonList", noOpAction, on(success(), "viewPersonList"));
			addViewState("viewPersonList", "person.list.view", on(submit(), "person.Detail"));
			addSubFlowState(PERSON_DETAILS, subFlow, personIdMapper, onAnyEvent("getPersonList"));
			addEndState("finish");
		}
	}

	public static class PersonIdMapper implements FlowAttributeMapper {
		public Map createSubflowInput(RequestContext context) {
			Map inputMap = new HashMap(1);
			inputMap.put("personId", context.getFlowScope().getAttribute("personId"));
			return inputMap;
		}

		public void mapSubflowOutput(RequestContext context) {
		}
	}

	public class TestDetailFlowBuilderLookupById extends AbstractFlowBuilder {
		protected String flowId() {
			return PERSON_DETAILS;
		}

		public void buildStates() {
			addActionState("getDetails", action("noOpAction"), on(success(), "viewDetails"));
			addViewState("viewDetails", "person.Detail.view", on(submit(), "bindAndValidateDetails"));
			addActionState("bindAndValidateDetails", action("noOpAction"), new Transition[] {
					on(error(), "viewDetails"), on(success(), "finish") });
			addEndState("finish");
		}
	}

	public class TestDetailFlowBuilderLookupByType extends AbstractFlowBuilder {
		protected String flowId() {
			return PERSON_DETAILS;
		}

		public void buildStates() {
			addActionState("getDetails", action(NoOpAction.class), on(success(), "viewDetails"));
			addViewState("viewDetails", "person.Detail.view", on(submit(), "bindAndValidateDetails"));
			addActionState("bindAndValidateDetails", actionRef(NoOpAction.class), new Transition[] {
					on(error(), "viewDetails"), on(success(), "finish") });
			addEndState("finish");
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
			addActionState("getDetails", noOpAction, on(success(), "viewDetails"));
			addViewState("viewDetails", "person.Detail.view", on(submit(), "bindAndValidateDetails"));
			addActionState("bindAndValidateDetails", noOpAction, new Transition[] { on(error(), "viewDetails"),
					on(success(), "finish") });
			addEndState("finish");
		}
	};

	/**
	 * Action bean stub that does nothing, just returns a "success" result.
	 */
	public static final class NoOpAction implements Action {
		public Event execute(RequestContext context) throws Exception {
			return new Event(this, "success");
		}
	}
}