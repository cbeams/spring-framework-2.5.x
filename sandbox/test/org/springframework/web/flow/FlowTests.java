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

/**
 * @author Keith Donald
 * @author Rod Johnson
 * @author Colin Sampaleanu
 */
public class FlowTests extends TestCase {

	private static String PERSON_DETAILS = "personDetails";

	private static String PET_DETAILS = "petDetails";

	public void testCreateFlowWithListener() {
		testCreateFlow(true);
	}

	public void testCreateFlowWithoutListener() {
		// test for npe's implementation if no listener is set
		testCreateFlow(false);
	}

	private void testCreateFlow(boolean listen) {
		final String flowId = "testFlow";

		String getStateId = "personDetails.get";
		String viewStateId = "personDetails.view";
		String submitStateId = "personDetails.bindAndValidate";

		String submitEventId = "submit";
		String finishEventId = "finish";

		HttpServletRequest req1 = new MockHttpServletRequest();
		HttpServletRequest req2 = new MockHttpServletRequest();

		TestFlow flow = new TestFlow(flowId);
		MockControl flowDaoMc = MockControl.createControl(FlowDao.class);
		FlowDao dao = (FlowDao)flowDaoMc.getMock();
		dao.getActionBean("personDetails.get");
		flowDaoMc.setReturnValue(new NoOpActionBean("success"));
		dao.getActionBean("personDetails.bindAndValidate");
		flowDaoMc.setReturnValue(new NoOpActionBean("success"));
		flowDaoMc.replay();
		flow.setFlowDao(dao);

		FlowSessionExecutionStack fes = new FlowSessionExecutionStack();

		MockControl flowListenerMc = MockControl.createControl(FlowSessionExecutionListener.class);
		FlowSessionExecutionListener mockListener = (FlowSessionExecutionListener)flowListenerMc.getMock();

		mockListener.started(fes);
		flowListenerMc.setVoidCallable();

		mockListener.stateTransitioned(fes, null, flow.getRequiredState(getStateId));
		flowListenerMc.setVoidCallable();

		mockListener.stateTransitioned(fes, flow.getRequiredState(getStateId), flow.getRequiredState(viewStateId));
		flowListenerMc.setVoidCallable();

		mockListener.stateTransitioned(fes, flow.getRequiredState(viewStateId), flow.getRequiredState(submitStateId));
		flowListenerMc.setVoidCallable();

		flowListenerMc.replay();

		if (listen) {
			flow.setFlowSessionExecutionListener(mockListener);
		}

		assertEquals(1, flow.getViewStateCount());

		FlowSessionExecutionStartResult result = flow.start(req1, null, null);
		ViewDescriptor vdesc = result.getStartingView();
		fes = (FlowSessionExecutionStack)result.getFlowSessionExecutionInfo();

		assertEquals(viewStateId, vdesc.getViewName());
		assertEquals(0, vdesc.getModel().size());

		// ignoring now b/c this is failing on the above flowEnded() call that
		// is currently untestable (that is indeed working correctly)
		vdesc = flow.execute(submitEventId, viewStateId, fes, req2, null);
		assertEquals(viewStateId, vdesc.getViewName());
		assertEquals(0, vdesc.getModel().size());

		flowDaoMc.verify();

		if (listen) {
			flowListenerMc.verify();
		}
	}

	private class TestFlow extends Flow {

		public TestFlow(String id) {
			super(id);
			initFlow();

		}

		protected void initFlow() {
			add(createGetState(PERSON_DETAILS));
			add(createViewState(PERSON_DETAILS));
			add(createBindAndValidateState(PERSON_DETAILS));
			add(createFinishEndState("viewPersonDetails"));
		}
	};

	/**
	 * Does nothing, just returns "success"
	 */
	private final class NoOpActionBean implements ActionBean {

		private String retVal;

		public NoOpActionBean(String retVal) {
			this.retVal = retVal;
		}

		public ActionBeanEvent execute(HttpServletRequest request, HttpServletResponse response,
				MutableAttributesAccessor attributes) throws RuntimeException {
			return new ActionBeanEvent(this, retVal);
		}
	}

}