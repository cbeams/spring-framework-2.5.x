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

import junit.framework.TestCase;

import org.aopalliance.intercept.Interceptor;
import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.StringUtils;

/**
 * Also tests AdvisedSupport superclass.
 * @author Rod Johnson
 * @since 14-Mar-2003
 */
public class ProxyFactoryTests extends TestCase {

	/**
	 * Constructor for ProxyFactoryTests.
	 * @param arg0
	 */
	public ProxyFactoryTests(String arg0) {
		super(arg0);
	}

	public void testNullTarget() {

		try {
			// Use the constructor taking Object
			new ProxyFactory((Object) null);
			fail("Should't allow proxy with null target");
		} 
		catch (AopConfigException ex) {
		}
	}
	
	public void testIndexOfMethods() {
		TestBean target = new TestBean();
		ProxyFactory pf = new ProxyFactory(target);
		NopInterceptor nop = new NopInterceptor();
		Advisor advisor = new DefaultPointcutAdvisor(new CountingBeforeAdvice());
		Advised advised = (Advised) pf.getProxy();
		// Can use advised and ProxyFactory interchangeably
		advised.addAdvice(nop);
		pf.addAdvisor(advisor);
		assertEquals(-1, pf.indexOf((Advice) null));
		assertEquals(-1, pf.indexOf(new NopInterceptor()));
		assertEquals(0, pf.indexOf(nop));
		assertEquals(-1, advised.indexOf((Advisor) null));
		assertEquals(1, pf.indexOf(advisor));
		assertEquals(-1, advised.indexOf(new DefaultPointcutAdvisor(null)));
	}
	
	public void testRemoveAdvisorByReference() {
		TestBean target = new TestBean();
		ProxyFactory pf = new ProxyFactory(target);
		NopInterceptor nop = new NopInterceptor();
		CountingBeforeAdvice cba = new CountingBeforeAdvice();
		Advisor advisor = new DefaultPointcutAdvisor(cba);
		pf.addAdvice(nop);
		pf.addAdvisor(advisor);
		ITestBean proxied = (ITestBean) pf.getProxy();
		proxied.setAge(5);
		assertEquals(1, cba.getCalls());
		assertEquals(1, nop.getCount());
		assertFalse(pf.removeAdvisor(null));
		assertTrue(pf.removeAdvisor(advisor));
		assertEquals(5, proxied.getAge());
		assertEquals(1, cba.getCalls());
		assertEquals(2, nop.getCount());
		assertFalse(pf.removeAdvisor(new DefaultPointcutAdvisor(null)));
	}
	
	
	public void testRemoveAdvisorByIndex() {
		TestBean target = new TestBean();
		ProxyFactory pf = new ProxyFactory(target);
		NopInterceptor nop = new NopInterceptor();
		CountingBeforeAdvice cba = new CountingBeforeAdvice();
		Advisor advisor = new DefaultPointcutAdvisor(cba);
		pf.addAdvice(nop);
		pf.addAdvisor(advisor);
		NopInterceptor nop2 = new NopInterceptor();
		pf.addAdvice(nop2);
		ITestBean proxied = (ITestBean) pf.getProxy();
		proxied.setAge(5);
		assertEquals(1, cba.getCalls());
		assertEquals(1, nop.getCount());
		assertEquals(1, nop2.getCount());
		// Removes counting before advisor
		pf.removeAdvisor(1);
		assertEquals(5, proxied.getAge());
		assertEquals(1, cba.getCalls());
		assertEquals(2, nop.getCount());
		assertEquals(2, nop2.getCount());
		// Removes Nop1
		pf.removeAdvisor(0);
		assertEquals(5, proxied.getAge());
		assertEquals(1, cba.getCalls());
		assertEquals(2, nop.getCount());
		assertEquals(3, nop2.getCount());
		
		// Check out of bounds
		try {
			pf.removeAdvisor(-1);
		}
		catch (AopConfigException ex) {
			// Ok
		}
		
		try {
			pf.removeAdvisor(2);
		}
		catch (AopConfigException ex) {
			// Ok
		}
		
		assertEquals(5, proxied.getAge());
		assertEquals(4, nop2.getCount());
	}
	
	
	public void testReplaceAdvisor() {
		TestBean target = new TestBean();
		ProxyFactory pf = new ProxyFactory(target);
		NopInterceptor nop = new NopInterceptor();
		CountingBeforeAdvice cba1 = new CountingBeforeAdvice();
		CountingBeforeAdvice cba2 = new CountingBeforeAdvice();
		Advisor advisor1 = new DefaultPointcutAdvisor(cba1);
		Advisor advisor2 = new DefaultPointcutAdvisor(cba2);
		pf.addAdvisor(advisor1);
		pf.addAdvice(nop);
		ITestBean proxied = (ITestBean) pf.getProxy();
		// Use the type cast feature
		// Replace etc methods on advised should be same as on ProxyFactory
		Advised advised = (Advised) proxied;
		proxied.setAge(5);
		assertEquals(1, cba1.getCalls());
		assertEquals(0, cba2.getCalls());
		assertEquals(1, nop.getCount());
		assertFalse(advised.replaceAdvisor(null, null));
		assertFalse(advised.replaceAdvisor(null, advisor2));
		assertFalse(advised.replaceAdvisor(advisor1, null));
		assertTrue(advised.replaceAdvisor(advisor1, advisor2));
		assertEquals(advisor2, pf.getAdvisors()[0]);
		assertEquals(5, proxied.getAge());
		assertEquals(1, cba1.getCalls());
		assertEquals(2, nop.getCount());
		assertEquals(1, cba2.getCalls());
		assertFalse(pf.replaceAdvisor(new DefaultPointcutAdvisor(null), advisor1));
	}

	public static class Concrete {
		public void foo() {
		}
	}

	public void testAddRepeatedInterface() {
		TimeStamped tst = new TimeStamped() {
			public long getTimeStamp() {
				throw new UnsupportedOperationException("getTimeStamp");
			}
		};
		ProxyFactory pf = new ProxyFactory(tst);
		// We've already implicitly added this interface.
		// This call should be ignored without error
		pf.addInterface(TimeStamped.class);
		// All cool
		TimeStamped ts = (TimeStamped) pf.getProxy();
	}

	public void testGetsAllInterfaces() throws Exception {
		// Extend to get new interface
		class TestBeanSubclass extends TestBean implements Comparable {
			public int compareTo(Object arg0) {
				throw new UnsupportedOperationException("compareTo");
			}
		}
		TestBeanSubclass raw = new TestBeanSubclass();
		ProxyFactory factory = new ProxyFactory(raw);
		assertEquals("Found correct number of interfaces", 4, factory.getProxiedInterfaces().length);
		//System.out.println("Proxied interfaces are " + StringUtils.arrayToDelimitedString(factory.getProxiedInterfaces(), ","));
		ITestBean tb = (ITestBean) factory.getProxy();
		assertTrue("Picked up secondary interface", tb instanceof IOther);
				
		raw.setAge(25);
		assertTrue(tb.getAge() == raw.getAge());

		long t = 555555L;
		TimestampIntroductionInterceptor ti = new TimestampIntroductionInterceptor(t);
		
		System.out.println(StringUtils.arrayToDelimitedString(factory.getProxiedInterfaces(), "/"));
		
		factory.addAdvisor(0, new DefaultIntroductionAdvisor(ti, TimeStamped.class));
		
		System.out.println(StringUtils.arrayToDelimitedString(factory.getProxiedInterfaces(), "/"));
		

		TimeStamped ts = (TimeStamped) factory.getProxy();
		assertTrue(ts.getTimeStamp() == t);
		// Shouldn't fail;
		 ((IOther) ts).absquatulate();
	}
	
	public void testCanOnlyAddMethodInterceptors() {
		ProxyFactory factory = new ProxyFactory(new TestBean());
		factory.addAdvice(0, new NopInterceptor());
		try {
			factory.addAdvice(0, new Interceptor() {
			});
			fail("Should only be able to add MethodInterceptors");
		}
		catch (AopConfigException ex) {
		}
		
		// Check we can still use it
		IOther other = (IOther) factory.getProxy();
		other.absquatulate();
	}
	
	public void testInterceptorInclusionMethods() {
		NopInterceptor di = new NopInterceptor();
		NopInterceptor diUnused = new NopInterceptor();
		ProxyFactory factory = new ProxyFactory(new TestBean());
		factory.addAdvice(0, di);
		ITestBean tb = (ITestBean) factory.getProxy();
		assertTrue(factory.interceptorIncluded(di));
		assertTrue(!factory.interceptorIncluded(diUnused));
		assertTrue(factory.countInterceptorsOfType(NopInterceptor.class) == 1);
		assertTrue(factory.countInterceptorsOfType(TransactionInterceptor.class) == 0);
	
		factory.addAdvice(0, diUnused);
		assertTrue(factory.interceptorIncluded(diUnused));
		assertTrue(factory.countInterceptorsOfType(NopInterceptor.class) == 2);
	}

}
