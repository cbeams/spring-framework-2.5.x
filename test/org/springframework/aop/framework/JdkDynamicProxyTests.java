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

package org.springframework.aop.framework;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.easymock.MockControl;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13-Mar-2003
 */
public class JdkDynamicProxyTests extends AbstractAopProxyTests {

	protected Object createProxy(AdvisedSupport as) {
		assertFalse("Not forcible CGLIB", as.getProxyTargetClass());
		Object proxy = as.createAopProxy().getProxy();
		assertTrue("Should be a JDK proxy: " + proxy.getClass(), AopUtils.isJdkDynamicProxy(proxy));
		return proxy;
	}
	
	protected AopProxy createAopProxy(AdvisedSupport as) {
		return new JdkDynamicAopProxy(as);
	}
	
	public void testNullConfig() {
		try {
			JdkDynamicAopProxy aop = new JdkDynamicAopProxy(null);
			aop.getProxy();
			fail("Shouldn't allow null interceptors");
		} 
		catch (AopConfigException ex) {
			// Ok
		}
	}

	public void testProxyIsJustInterface() throws Throwable {
		TestBean raw = new TestBean();
		raw.setAge(32);
		AdvisedSupport pc = new AdvisedSupport(new Class[] {ITestBean.class});
		pc.setTarget(raw);
		JdkDynamicAopProxy aop = new JdkDynamicAopProxy(pc);

		Object proxy = aop.getProxy();
		assertTrue(proxy instanceof ITestBean);
		assertTrue(!(proxy instanceof TestBean));
	}

	public void testInterceptorIsInvokedWithNoTarget() throws Throwable {
		// Test return value
		int age = 25;
		MockControl miControl = MockControl.createControl(MethodInterceptor.class);
		MethodInterceptor mi = (MethodInterceptor) miControl.getMock();

		AdvisedSupport pc = new AdvisedSupport(new Class[] { ITestBean.class });
		pc.addAdvice(mi);
		AopProxy aop = createAopProxy(pc);

		// Really would like to permit null arg:can't get exact mi
		mi.invoke(null);
		//mi.invoke(new MethodInvocationImpl(aop, null, ITestBean.class,
		//	ITestBean.class.getMethod("getAge", null),
		//	null, l, r));
		//miControl.
		//miControl.setReturnValue(new Integer(age));
		// Have disabled strong argument checking
		miControl.setDefaultReturnValue(new Integer(age));
		miControl.replay();

		ITestBean tb = (ITestBean) aop.getProxy();
		assertTrue("correct return value", tb.getAge() == age);
		miControl.verify();
	}
	
	public void testTargetCanGetInvocationWithPrivateClass() throws Throwable {
		final ExposedInvocationTestBean expectedTarget = new ExposedInvocationTestBean() {
			protected void assertions(MethodInvocation invocation) {
				assertTrue(invocation.getThis() == this);
				assertTrue("Invocation should be on ITestBean: " + invocation.getMethod(), 
					invocation.getMethod().getDeclaringClass() == ITestBean.class);
			}
		};
	
		AdvisedSupport pc = new AdvisedSupport(new Class[] { ITestBean.class, IOther.class });
		pc.addAdvice(ExposeInvocationInterceptor.INSTANCE);
		TrapTargetInterceptor tii = new TrapTargetInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				// Assert that target matches BEFORE invocation returns
				assertEquals("Target is correct", expectedTarget, invocation.getThis());
				return super.invoke(invocation);
			}
		};
		pc.addAdvice(tii);
		pc.setTarget(expectedTarget);
		AopProxy aop = createAopProxy(pc);

		ITestBean tb = (ITestBean) aop.getProxy();
		tb.getName();
		// Not safe to trap invocation
		//assertTrue(tii.invocation == target.invocation);
	
		//assertTrue(target.invocation.getProxy() == tb);

		//	((IOther) tb).absquatulate();
		//MethodInvocation minv =  tii.invocation;
		//assertTrue("invoked on iother, not " + minv.getMethod().getDeclaringClass(), minv.getMethod().getDeclaringClass() == IOther.class);
		//assertTrue(target.invocation == tii.invocation);
		}
}
