/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
 * @version $Id: EventPublicationInterceptorTests.java,v 1.1 2003-10-04 15:58:29 jhoeller Exp $
 */
public class EventPublicationInterceptorTests extends TestCase {

	public void testWithIncorrectRequiredProperties() throws Exception {
		EventPublicationInterceptor interceptor = new EventPublicationInterceptor();
		ApplicationContext ctx = new StaticApplicationContext();
		interceptor.setApplicationContext(ctx);
		interceptor.setApplicationEventClass(null);
		try {
			interceptor.afterPropertiesSet();
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalArgumentException e) {
			//Expected
		}

		interceptor.setApplicationEventClass(getClass());
		try {
			interceptor.afterPropertiesSet();
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
		interceptor.setApplicationEventClass(org.springframework.context.TestApplicationEvent.class);
		interceptor.afterPropertiesSet();

		ProxyFactory factory = new ProxyFactory(target);
		factory.addInterceptor(0, interceptor);

		ITestBean testBean = (ITestBean)factory.getProxy();

		//Invoke any method on the advised proxy to see if the interceptor has been invoked
		testBean.getAge();
		assertTrue("Interceptor published 1 event", listener.getEventCount() == 1);
	}

}
