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

package org.springframework.context;

import junit.framework.TestCase;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.context.event.EventPublicationInterceptor;
import org.springframework.context.support.StaticApplicationContext;

/** 
 * @author Dmitriy Kopylenko
 * @version $Id: EventPublicationInterceptorTests.java,v 1.4 2004-05-29 21:27:12 jhoeller Exp $
 */
public class EventPublicationInterceptorTests extends TestCase {

	public void testWithIncorrectRequiredProperties() throws Exception {
		EventPublicationInterceptor interceptor = new EventPublicationInterceptor();
		ApplicationContext ctx = new StaticApplicationContext();
		interceptor.setApplicationContext(ctx);

		try {
			interceptor.setApplicationEventClass(null);
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}

		try {
			interceptor.setApplicationEventClass(getClass());
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testExpectedBehavior() throws Exception {
		TestBean target = new TestBean();
		final TestListener listener = new TestListener();

		class TestContext extends StaticApplicationContext {
			protected void onRefresh() throws BeansException {
				addListener(listener);
			}
		}

		StaticApplicationContext ctx = new TestContext();
		ctx.refresh();

		EventPublicationInterceptor interceptor = new EventPublicationInterceptor();
		interceptor.setApplicationContext(ctx);
		interceptor.setApplicationEventClass(TestEvent.class);

		ProxyFactory factory = new ProxyFactory(target);
		factory.addInterceptor(0, interceptor);

		ITestBean testBean = (ITestBean) factory.getProxy();

		// invoke any method on the advised proxy to see if the interceptor has been invoked
		testBean.getAge();
		// two events: ContextRefreshedEvent and TestEvent
		assertTrue("Interceptor published 1 event", listener.getEventCount() == 2);
	}


	public static class TestEvent extends ApplicationEvent {

		public TestEvent(Object source) {
			super(source);
		}
	}

}
