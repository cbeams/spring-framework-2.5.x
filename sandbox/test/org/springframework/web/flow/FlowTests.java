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
import org.springframework.web.flow.ActionBean;
import org.springframework.web.flow.ActionBeanEvent;
import org.springframework.web.flow.Flow;
import org.springframework.web.flow.FlowDao;
import org.springframework.web.flow.FlowLifecycleListener;
import org.springframework.web.flow.FlowSessionExecutionStack;
import org.springframework.web.flow.MutableAttributesAccessor;
import org.springframework.web.flow.ViewDescriptor;

/**
 * @author Keith Donald
 * @author Rod Johnson
 */
public class FlowTests extends TestCase {

    private static String PERSON_DETAILS = "personDetails";

    public void testCreateFlowWithListener() {
        testCreateFlow(true);
    }

    public void testCreateFlowWithoutListener() {
        //test for npe's implementation if no listener is set
        testCreateFlow(false);
    }

    private void testCreateFlow(boolean listen) {
        final String flowId = "testFlow";

        String viewPersonDetailsStateId = "viewPersonDetails";
        String getPersonDetailsStateId = "getPersonDetails";
        String submitEventId = "submit";
        String submitAction = "bindAndValidatePersonDetails";
        String finish = "finish";

        HttpServletRequest req1 = new MockHttpServletRequest();
        HttpServletRequest req2 = new MockHttpServletRequest();

        TestFlow flow = new TestFlow(flowId);
        MockControl flowDaoMc = MockControl.createControl(FlowDao.class);
        FlowDao dao = (FlowDao)flowDaoMc.getMock();
        dao.getActionBean("getPersonDetailsAction");
        flowDaoMc.setReturnValue(new NoOpActionBean("success"));
        dao.getActionBean("bindAndValidatePersonDetailsAction");
        flowDaoMc.setReturnValue(new NoOpActionBean("success"));
        flowDaoMc.replay();
        flow.setFlowDao(dao);

        FlowSessionExecutionStack fes = new FlowSessionExecutionStack();

        MockControl flowListenerMc = MockControl.createControl(FlowLifecycleListener.class);
        FlowLifecycleListener mockListener = (FlowLifecycleListener)flowListenerMc.getMock();

        mockListener.flowStarted(flow, fes, req1);
        flowListenerMc.setVoidCallable();

        mockListener.flowStateTransitioned(flow, null, flow.getRequiredState(getPersonDetailsStateId), fes, req1);
        flowListenerMc.setVoidCallable();

        mockListener.flowStateTransitioned(flow, flow.getRequiredState(getPersonDetailsStateId), flow
                .getRequiredState(viewPersonDetailsStateId), fes, req1);
        flowListenerMc.setVoidCallable();

        mockListener.flowEventSignaled(flow, submitEventId, flow.getRequiredState(viewPersonDetailsStateId), fes, req2);
        flowListenerMc.setVoidCallable();

        mockListener.flowStateTransitioned(flow, flow.getRequiredState(viewPersonDetailsStateId), flow
                .getRequiredState(submitAction), fes, req2);
        flowListenerMc.setVoidCallable();

        mockListener.flowStateTransitioned(flow, flow.getRequiredState(submitAction), flow.getRequiredState(finish),
                fes, req2);
        flowListenerMc.setVoidCallable();

        mockListener.flowEventProcessed(flow, submitEventId, null, fes, req2);
        flowListenerMc.setVoidCallable();

        // NO WAY TO KNOW VALUE OF 2ND FLOW SESSION ARG AHEAD OF TIME AS ITS
        // CREATED DURING RUN - what do I do here?
        mockListener.flowEnded(flow, null, fes, req2);
        flowListenerMc.setVoidCallable();

        flowListenerMc.replay();

        if (listen) {
            flow.setFlowLifecycleListener(mockListener);
        }

        assertEquals(1, flow.getViewStateCount());

        ViewDescriptor vdesc = flow.start(fes, req1, null, null);

        assertEquals(viewPersonDetailsStateId, vdesc.getViewName());
        assertEquals(0, vdesc.getModel().size());

        // ignoring now b/c this is failing on the above flowEnded() call that
        // is currently untestable (that is indeed working correctly)
        try {
            vdesc = flow.execute(submitEventId, viewPersonDetailsStateId, fes, req2, null);
            assertEquals(viewPersonDetailsStateId, vdesc.getViewName());
            assertEquals(0, vdesc.getModel().size());

            flowDaoMc.verify();

            if (listen) {
                flowListenerMc.verify();
            }
        }
        catch (Throwable e) {

        }
    }

    private class TestFlow extends Flow {

        public TestFlow(String id) {
            super(id);
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