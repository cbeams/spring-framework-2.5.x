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

import junit.framework.TestCase;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Rod Johnson
 */
public class MulticastFlowLifecycleListenerTests extends TestCase {

	public void testNoListeners() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		bf.registerBeanDefinition("multicast", new RootBeanDefinition(MulticastFlowLifecycleListener.class, null));

		MulticastFlowLifecycleListener mfll = (MulticastFlowLifecycleListener)bf.getBean("multicast");
		assertTrue("No listeners should have been found", mfll.getListeners().isEmpty());
		mfll.flowEnded(new Flow("testFlow"), null, null, null);
	}

	public void test2Listeners() {
		DefaultListableBeanFactory bf = createFactoryWith2Listeners();
		bf.registerBeanDefinition("multicast", new RootBeanDefinition(MulticastFlowLifecycleListener.class, null));

		MulticastFlowLifecycleListener mfll = (MulticastFlowLifecycleListener)bf.getBean("multicast");
		assertEquals(2, mfll.getListeners().size());

		CountingListener listener1 = (CountingListener)bf.getBean("listener1");
		CountingListener listener2 = (CountingListener)bf.getBean("listener2");
		mfll.flowStarted(new Flow("testFlow"), null, null);
		assertEquals(1, listener1.flowStartedCount);
		assertEquals(1, listener2.flowStartedCount);

		assertEquals(0, listener1.flowEndedCount);
		assertEquals(0, listener2.flowEndedCount);
		mfll.flowEnded(new Flow("testFlow"), null, null, null);
		assertEquals(1, listener1.flowEndedCount);
		assertEquals(1, listener2.flowEndedCount);
	}

	private DefaultListableBeanFactory createFactoryWith2Listeners() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		bf.registerSingleton("listener1", new CountingListener());
		bf.registerSingleton("listener2", new CountingListener());
		return bf;
	}


	private static class CountingListener implements FlowLifecycleListener {

		public int flowEndedCount;

		public int flowEventProcessedCount;

		public int flowEventSignaledCount;

		public int flowStartedCount;

		public int flowStateTransitionedCount;

		public void flowEnded(
				Flow source, FlowSession endedFlowSession, FlowSessionExecution sessionExecutionInfo,
				HttpServletRequest request) {
			++flowEndedCount;
		}

		public void flowEventProcessed(
				Flow source, String eventId, AbstractState state, FlowSessionExecution sessionExecutionInfo,
				HttpServletRequest request) {
			++flowEventProcessedCount;
		}

		public void flowEventSignaled(
				Flow source, String eventId, AbstractState state, FlowSessionExecution sessionExecutionInfo,
				HttpServletRequest request) {
			++flowEventSignaledCount;
		}

		public void flowStarted(Flow source, FlowSessionExecution sessionExecutionInfo, HttpServletRequest request) {
			++flowStartedCount;
		}

		public void flowStateTransitioned(
				Flow source, AbstractState oldState, AbstractState newState, FlowSessionExecution sessionExecutionInfo,
				HttpServletRequest request) {
			++flowStateTransitionedCount;
		}
	}

}
