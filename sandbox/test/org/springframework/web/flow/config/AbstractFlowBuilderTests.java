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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.web.flow.Action;
import org.springframework.web.flow.ActionResult;
import org.springframework.web.flow.ActionState;
import org.springframework.web.flow.EndState;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.MutableAttributesAccessor;
import org.springframework.web.flow.SubFlowState;
import org.springframework.web.flow.ViewState;
import org.springframework.web.flow.action.AbstractAction;

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
		TestMasterFlowDependencyLookup master = new TestMasterFlowDependencyLookup();
		master.setFlowServiceLocator(new FlowServiceLocatorAdapter() {
			public Action getAction(String actionId) throws FlowServiceLookupException {
				return new NoOpAction();
			}

			public Flow getFlow(String flowDefinitionId) throws FlowServiceLookupException {
				if (flowDefinitionId.equals(PERSON_DETAILS)) {
					BaseFlowBuilder builder = new TestFlowDependencyLookup();
					builder.setFlowServiceLocator(this);
					return new FlowFactoryBean(builder).getFlow();
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
		TestMasterFlowDependencyLookup master = new TestMasterFlowDependencyLookup();
		try {
			new FlowFactoryBean(master).getFlow();
			fail("Should have failed, no bean factory set for default bean factory flow service locator");
		}
		catch (IllegalStateException e) {
			// expected
		}
	}

	private class TestMasterFlowDependencyLookup extends AbstractFlowBuilder {
		protected String flowId() {
			return PERSONS_LIST;
		}

		public void buildStates() {
			addGetState(PERSONS_LIST);
			addViewState(PERSONS_LIST, onSubmit(PERSON_DETAILS));
			addSubFlowState(PERSON_DETAILS, PERSON_DETAILS, null, get(PERSONS_LIST));
			addFinishEndState();
		}
	}

	private class TestFlowDependencyLookup extends AbstractFlowBuilder {
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

	private class TestFlowTypeSafeDependencyLookup extends AbstractFlowBuilder {
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

	private class TestFlowTypeSafeDependencyInjection extends AbstractFlowBuilder {

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
	private final class NoOpAction extends AbstractAction {
		public ActionResult doExecuteAction(HttpServletRequest request, HttpServletResponse response,
				MutableAttributesAccessor attributes) throws Exception {
			return success();
		}
	}
}