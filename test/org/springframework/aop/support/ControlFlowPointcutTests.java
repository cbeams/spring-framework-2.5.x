/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ControlFlowPointcutTests.java,v 1.1 2003-12-07 10:19:10 johnsonr Exp $
 */
public class ControlFlowPointcutTests extends TestCase {
	
	public ControlFlowPointcutTests(String s) {
		super(s);
	}
	
	public void testMatches() {
		TestBean target = new TestBean();
		target.setAge(27);
		NopInterceptor nop = new NopInterceptor();
		ControlFlowPointcut cflow = new ControlFlowPointcut(One.class, "getAge");
		ProxyFactory pf = new ProxyFactory(target);
		ITestBean proxied = (ITestBean) pf.getProxy();
		pf.addAdvisor(new DefaultInterceptionAroundAdvisor(cflow, nop));
		
		// Not advised, not under One
		assertEquals(target.getAge(), proxied.getAge());
		assertEquals(0, nop.getCount());
		
		// Will be advised
		assertEquals(target.getAge(), new One().getAge(proxied));
		assertEquals(1, nop.getCount());
		
		// Won't be advised
		assertEquals(target.getAge(), new One().nomatch(proxied));
		assertEquals(1, nop.getCount());
		assertEquals(3, cflow.getEvaluations());
	}
	
	/**
	 * Check that we can use a cflow pointcut only in conjunction with
	 * a static pointcut: e.g. all setter methods that are invoked under
	 * a particular class. This greatly reduces the number of calls
	 * to the cflow pointcut, meaning that it's not so prohibitively
	 * expensive.
	 */
	public void testSelectiveApplication() {
		TestBean target = new TestBean();
		target.setAge(27);
		NopInterceptor nop = new NopInterceptor();
		ControlFlowPointcut cflow = new ControlFlowPointcut(One.class);
		Pointcut settersPc = new StaticMethodMatcherPointcut() {
			public boolean matches(Method m, Class targetClass) {
				return m.getName().startsWith("set");
			}
		};
		Pointcut settersUnderOne = Pointcuts.intersection(settersPc, cflow);
		ProxyFactory pf = new ProxyFactory(target);
		ITestBean proxied = (ITestBean) pf.getProxy();
		pf.addAdvisor(new DefaultInterceptionAroundAdvisor(settersUnderOne, nop));
	
		// Not advised, not under One
		target.setAge(16);
		assertEquals(0, nop.getCount());
	
		// Not advised; under One but not a setter
		assertEquals(16, new One().getAge(proxied));
		assertEquals(0, nop.getCount());
	
		// Won't be advised
		new One().set(proxied);
		assertEquals(1, nop.getCount());
		
		// We saved most evaluations
		assertEquals(1, cflow.getEvaluations());
	}
	
	
	public class One {
		int getAge(ITestBean proxied) {
			return proxied.getAge();
		}
		int nomatch(ITestBean proxied) {
			return proxied.getAge();
		}
		void set(ITestBean proxied) {
			proxied.setAge(5);
		}
	}

}
