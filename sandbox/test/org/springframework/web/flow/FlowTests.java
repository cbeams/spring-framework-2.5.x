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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.flow.action.AbstractActionBean;

/**
 * @author Keith Donald
 * @author Rod Johnson
 * @author Colin Sampaleanu
 */
public class FlowTests extends TestCase {

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

		String getStateId = flow.get(PERSON_DETAILS);
		String viewStateId = flow.view(PERSON_DETAILS);
		String bindAndValidateStateId = flow.bindAndValidate(PERSON_DETAILS);

		MockControl flowDaoMc = MockControl.createControl(FlowServiceLocator.class);
		FlowServiceLocator dao = (FlowServiceLocator)flowDaoMc.getMock();
		dao.getActionBean(getStateId);
		flowDaoMc.setReturnValue(new NoOpActionBean());
		dao.getActionBean(bindAndValidateStateId);
		flowDaoMc.setReturnValue(new NoOpActionBean());
		flowDaoMc.replay();
		flow.setFlowDao(dao);
	}

	private class TestMasterFlowDependencyLookup extends Flow {
		private String PERSONS_LIST = "persons";

		public TestMasterFlowDependencyLookup() {
			super("test.masterFlow");
		}

		protected void initFlow() {
			add(createGetState(PERSONS_LIST));
			add(createViewState(PERSONS_LIST, onSubmit(edit(PERSON_DETAILS))));
			addSubFlowState(edit(PERSON_DETAILS), edit(PERSON_DETAILS), null, get(PERSONS_LIST));
			add(createFinishEndState());
		}
	}

	private class TestFlowDependencyLookup extends Flow {

		public TestFlowDependencyLookup() {
			final String flowId = "testFlow";
			initFlow();
		}

		protected void initFlow() {
			add(createGetState(PERSON_DETAILS));
			add(createViewState(PERSON_DETAILS));
			add(createBindAndValidateState(PERSON_DETAILS));
			add(createFinishEndState());
		}
	};

	private class TestFlowTypeSafeDependencyLookup extends Flow {

		public TestFlowTypeSafeDependencyLookup() {
			super("test.detailFlow");
			initFlow();
		}

		protected void initFlow() {
			add(createGetState(PERSON_DETAILS, useActionBean(NoOpActionBean.class)));
			add(createViewState(PERSON_DETAILS));
			add(createBindAndValidateState(PERSON_DETAILS, useActionBean(NoOpActionBean.class)));
			add(createFinishEndState());
		}
	};

	private class TestFlowTypeSafeDependencyInjection extends Flow {

		private NoOpActionBean noOpAction;

		public void setNoOpAction(NoOpActionBean noOpAction) {
			this.noOpAction = noOpAction;
		}

		public TestFlowTypeSafeDependencyInjection() {
			super("test.detailFlow");
			initFlow();
		}

		protected void initFlow() {
			add(createGetState(PERSON_DETAILS, noOpAction));
			add(createViewState(PERSON_DETAILS));
			add(createBindAndValidateState(PERSON_DETAILS, noOpAction));
			add(createFinishEndState());
		}
	};

	/**
	 * Action bean stub thatr does nothing, just returns "success"
	 */
	private final class NoOpActionBean extends AbstractActionBean {
		public ActionBeanEvent doExecuteAction(HttpServletRequest request, HttpServletResponse response,
				MutableAttributesAccessor attributes) throws RuntimeException {
			return success();
		}
	}

}