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
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.context.event.EventPublicationInterceptor;
import org.springframework.context.support.StaticApplicationContext;

/** 
 * @author Dmitriy Kopylenko
 * @version $Id: EventPublicationInterceptorTests.java,v 1.3 2004-03-18 03:01:20 trisberg Exp $
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
		catch (IllegalArgumentException e) {
			//Expected
		}

		try {
			interceptor.setApplicationEventClass(getClass());
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException e) {
			//Expected	
		}
	}

	public void testExpectedBehavior() throws Exception {
		TestBean target = new TestBean();
		TestListener listener = new TestListener();

		class StaticContext extends StaticApplicationContext {
			public void addListener(ApplicationListener l) {
				super.addListener(l);
			}
		}

		ApplicationContext ctx = new StaticContext();
		((StaticContext)ctx).addListener(listener);

		EventPublicationInterceptor interceptor = new EventPublicationInterceptor();
		interceptor.setApplicationContext(ctx);
		interceptor.setApplicationEventClass(TestApplicationEvent.class);

		ProxyFactory factory = new ProxyFactory(target);
		factory.addInterceptor(0, interceptor);

		ITestBean testBean = (ITestBean)factory.getProxy();

		//Invoke any method on the advised proxy to see if the interceptor has been invoked
		testBean.getAge();
		assertTrue("Interceptor published 1 event", listener.getEventCount() == 1);
	}


	public static class TestApplicationEvent extends ApplicationEvent {

		public TestApplicationEvent(Object source) {
			super(source);
		}
	}

}
