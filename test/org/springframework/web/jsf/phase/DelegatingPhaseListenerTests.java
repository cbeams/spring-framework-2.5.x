/*
 * Copyright 2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.web.jsf.phase;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.MockFacesContext;

/**
 * Tests for DelegatingPhaseListener
 * 
 * @author Colin Sampaleanu
 */
public class DelegatingPhaseListenerTests extends TestCase {

	private MockFacesContext		facesContext;

	private WebApplicationContext   appContext;

	private MockControl			 acControl;

	private PhaseListener		   testListener;

	private MockControl			 listenerControl;

	private DelegatingPhaseListener delPhaseListener;

	protected void setUp() throws Exception {
		facesContext = new MockFacesContext();
		acControl = MockControl.createControl(WebApplicationContext.class);
		appContext = (WebApplicationContext) acControl.getMock();
		listenerControl = MockControl.createControl(PhaseListener.class);
		testListener = (PhaseListener) listenerControl.getMock();

		delPhaseListener = new DelegatingPhaseListener() {
			protected WebApplicationContext getApplicationContext(
					FacesContext facesContext) throws BeansException {
				return appContext;
			}
		};
	}

	public void testGetSingleDelegate() {

		TestListener target = new TestListener();
		appContext.getBean(DelegatingPhaseListener.PHASE_LISTENER_BEAN);
		acControl.setReturnValue(target);
		acControl.replay();

		assertEquals(delPhaseListener.getPhaseId(), PhaseId.ANY_PHASE);

		List delegates = delPhaseListener.getDelegates(facesContext);
		assertTrue("must be one delegate", delegates.size() == 1);

		delegates.add(new TestListener());
		acControl.reset();

		List delegates2 = delPhaseListener.getDelegates(facesContext);
		assertTrue("must cache previous get operation", delegates == delegates2);
	}

	public void testGetDelegateList() {
		List delegates = new ArrayList();
		delegates.add(new TestListener());
		delegates.add(new TestListener());

		appContext.getBean(DelegatingPhaseListener.PHASE_LISTENER_BEAN);
		acControl.setReturnValue(delegates);
		acControl.replay();

		List delegates2 = delPhaseListener.getDelegates(facesContext);
		assertTrue(delegates == delegates2);
	}

	public void testBeforeAndAfterPhase() {

		TestListener target = new TestListener();
		appContext.getBean(DelegatingPhaseListener.PHASE_LISTENER_BEAN);
		acControl.setReturnValue(target);
		acControl.replay();

		assertEquals(delPhaseListener.getPhaseId(), PhaseId.ANY_PHASE);

		MockControl control = MockClassControl.createControl(PhaseEvent.class);
		PhaseEvent event = (PhaseEvent) control.getMock();
	    control.expectAndDefaultReturn(event.getFacesContext(), facesContext);
	    control.replay();

		delPhaseListener.beforePhase(event);

		assertEquals("once we have delegate getPhaseId from it should be used",
				delPhaseListener.getPhaseId(), PhaseId.APPLY_REQUEST_VALUES);

		assertTrue(target.beforeCalled);
		
		delPhaseListener.afterPhase(event);
		
		assertTrue(target.afterCalled);
	}

	public static class TestListener implements PhaseListener {
		boolean beforeCalled = false;

		boolean afterCalled  = false;

		public void afterPhase(PhaseEvent arg0) {
			afterCalled = true;
		}

		public void beforePhase(PhaseEvent arg0) {
			beforeCalled = true;
		}

		public PhaseId getPhaseId() {
			return PhaseId.APPLY_REQUEST_VALUES;
		}
	}

}
