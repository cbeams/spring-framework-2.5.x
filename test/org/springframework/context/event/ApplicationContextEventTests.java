/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.context.event;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInvocation;
import org.easymock.MockControl;
import org.easymock.internal.AlwaysMatcher;
import org.easymock.internal.EqualsMatcher;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.AbstractApplicationContextTests.MyEvent;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author Alef Arendsen
 */
public class ApplicationContextEventTests extends TestCase {
	
	private AbstractApplicationEventMulticaster getMulticaster() {
		AbstractApplicationEventMulticaster mc = 
			new AbstractApplicationEventMulticaster() {
			public void multicastEvent(ApplicationEvent event) {				
			}
		};
		return mc;
	}
	
	public void testMulticasterNewCollectionClass() {
		AbstractApplicationEventMulticaster mc = getMulticaster();
		
		mc.addApplicationListener(new ConsoleListener());
		
		mc.setCollectionClass(ArrayList.class);
		
		assertEquals(1, mc.getApplicationListeners().size());
		assertEquals(ArrayList.class, mc.getApplicationListeners().getClass());		
	}
	
	public void testMulticasterInvalidCollectionClass() {
		AbstractApplicationEventMulticaster mc = getMulticaster();
		try {
			mc.setCollectionClass(ApplicationContextEventTests.class);
			fail("IllegalArgumentException should have been thrown");
		} catch (IllegalArgumentException e) {
			// this is okay!
		}
		
		try {
			mc.setCollectionClass(List.class);
			fail("FatalBeansException should have been thrown");
		} catch (FatalBeanException e) {
			// this is okay!
		}
	}
	
	public void testMulticasterRemoveAll() {
		AbstractApplicationEventMulticaster mc = getMulticaster();
		mc.addApplicationListener(new ConsoleListener());		
		mc.removeAllListeners();
		
		assertEquals(0, mc.getApplicationListeners().size());
		
	}
	
	public void testMulticasterRemoveOne() {
		AbstractApplicationEventMulticaster mc = getMulticaster();
		ConsoleListener one = new ConsoleListener();
		ConsoleListener two = new ConsoleListener();
		mc.addApplicationListener(one);
		mc.addApplicationListener(two);
		
		mc.removeApplicationListener(one);
		
		assertEquals(1, mc.getApplicationListeners().size());
		assertTrue("Remaining listener present", mc.getApplicationListeners().contains(two));		
	}
	
	public void testSimpleApplicationEventMulticaster() {
		
		MockControl ctrl = MockControl.createControl(ApplicationListener.class);
		ApplicationListener listener = (ApplicationListener)ctrl.getMock();
		
		ApplicationEvent evt = new ContextClosedEvent(new StaticApplicationContext());
		
		listener.onApplicationEvent(evt);
		ctrl.setMatcher(new EqualsMatcher());		
		
		SimpleApplicationEventMulticaster smc = new SimpleApplicationEventMulticaster();
		smc.addApplicationListener(listener);
		
		ctrl.replay();
		
		smc.multicastEvent(evt);
		
		ctrl.verify();
	}
	
	public void testEvenPublicationInterceptor() 
	throws Throwable {
		
		MockControl invCtrl = MockControl.createControl(MethodInvocation.class);
		MethodInvocation invocation = (MethodInvocation)invCtrl.getMock();
		
		MockControl ctxCtrl = MockControl.createControl(ApplicationContext.class);
		ApplicationContext ctx = (ApplicationContext)ctxCtrl.getMock();
						
		EventPublicationInterceptor interceptor = 
			new EventPublicationInterceptor();
		interceptor.setApplicationEventClass(MyEvent.class);
		interceptor.setApplicationEventPublisher(ctx);
		interceptor.afterPropertiesSet();
		
		invocation.proceed();
		invCtrl.setReturnValue(new Object());

		invocation.getThis();
		invCtrl.setReturnValue(new Object());
		ctx.publishEvent(new MyEvent(new Object()));
		ctxCtrl.setDefaultMatcher(new AlwaysMatcher());
		
		ctxCtrl.replay();
		invCtrl.replay();
		
		interceptor.invoke(invocation);
		
		ctxCtrl.verify();
		invCtrl.verify();
	}
	
	public void testEventPublicationInterceptorIllegalState() {
		EventPublicationInterceptor interceptor = 
			new EventPublicationInterceptor();
		
		try {
			interceptor.afterPropertiesSet();
			fail("IllegalArgumentException should have been thrown");
		} catch (IllegalArgumentException e) {
			// this is okay!
		}
		
		try {
			interceptor.setApplicationEventClass(String.class);
			fail("IllegalArgumentException should have been thrown");
		} catch (IllegalArgumentException e) {
			// this is okay!
		}		
	}
}
