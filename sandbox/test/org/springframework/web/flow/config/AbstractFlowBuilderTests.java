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

import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.flow.ActionResult;
import org.springframework.web.flow.MutableAttributesAccessor;
import org.springframework.web.flow.action.AbstractAction;

/**
 * @author Keith Donald
 * @author Rod Johnson
 * @author Colin Sampaleanu
 */
public class AbstractFlowBuilderTests extends TestCase {

	private static String PERSON_DETAILS = "personDetails";

	public void testCreateFlowWithListener() {
		testCreateFlow(true);
	}

	public void testCreateFlowWithoutListener() {
		// test for npe's implementation if no listener is set
		testCreateFlow(false);
	}

	private void testCreateFlow(boolean listen) {
		HttpServletRequest request = new MockHttpServletRequest();
		HttpServletResponse response = new MockHttpServletResponse();

		HttpServletRequest request2 = new MockHttpServletRequest();

		TestFlowDependencyLookup flow = new TestFlowDependencyLookup();

		String getStateId = PERSON_DETAILS + ".get";
		String viewStateId = PERSON_DETAILS + ".view";
		String bindAndValidateStateId = PERSON_DETAILS + ".bindAndValidate";

		MockControl flowDaoMc = MockControl.createControl(FlowServiceLocator.class);
		FlowServiceLocator dao = (FlowServiceLocator)flowDaoMc.getMock();
		dao.getAction(getStateId);
		flowDaoMc.setReturnValue(new NoOpActionBean());
		dao.getAction(bindAndValidateStateId);
		flowDaoMc.setReturnValue(new NoOpActionBean());
		flowDaoMc.replay();
	}

	private class TestMasterFlowDependencyLookup extends AbstractFlowBuilder {
		private String PERSONS_LIST = "persons";

		protected String flowId() {
			return "test.masterFlow";
		}

		public void buildStates() {
			addGetState(PERSONS_LIST);
			addViewState(PERSONS_LIST, onSubmit(edit(PERSON_DETAILS)));
			addSubFlowState(edit(PERSON_DETAILS), edit(PERSON_DETAILS), null, get(PERSONS_LIST));
			addFinishEndState();
		}
	}

	private class TestFlowDependencyLookup extends AbstractFlowBuilder {

		protected String flowId() {
			return "test.detailFlow";
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
			return "test.detailFlow";
		}

		public void buildStates() {
			addGetState(PERSON_DETAILS, executeAction(NoOpActionBean.class));
			addViewState(PERSON_DETAILS);
			addBindAndValidateState(PERSON_DETAILS, executeAction(NoOpActionBean.class));
			addFinishEndState();
		}
	};

	private class TestFlowTypeSafeDependencyInjection extends AbstractFlowBuilder {

		private NoOpActionBean noOpAction;

		public void setNoOpAction(NoOpActionBean noOpAction) {
			this.noOpAction = noOpAction;
		}

		protected String flowId() {
			return "test.detailFlow";
		}

		public void buildStates() {
			addGetState(PERSON_DETAILS, noOpAction);
			addViewState(PERSON_DETAILS);
			addBindAndValidateState(PERSON_DETAILS, noOpAction);
			addFinishEndState();
		}
	};

	/**
	 * Action bean stub thatr does nothing, just returns "success"
	 */
	private final class NoOpActionBean extends AbstractAction {
		public ActionResult doExecuteAction(HttpServletRequest request, HttpServletResponse response,
				MutableAttributesAccessor attributes) throws Exception {
			return success();
		}
	}

}