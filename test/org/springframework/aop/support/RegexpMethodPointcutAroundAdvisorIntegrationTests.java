/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import junit.framework.TestCase;

import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author Rod Johnson
 * @version $Id: RegexpMethodPointcutAroundAdvisorIntegrationTests.java,v 1.2 2004-01-13 22:33:49 johnsonr Exp $
 */
public class RegexpMethodPointcutAroundAdvisorIntegrationTests extends TestCase {

	/**
	 * Constructor for RegexpMethodPointcutAroundAdvisorTests.
	 * @param arg0
	 */
	public RegexpMethodPointcutAroundAdvisorIntegrationTests(String arg0) {
		super(arg0);
	}
	
	public void testSinglePattern() throws Throwable {
		BeanFactory bf = new ClassPathXmlApplicationContext("org/springframework/aop/support/regexpSetterTests.xml"); 
		ITestBean advised = (ITestBean) bf.getBean("settersAdvised");
		// Interceptor behind regexp advisor
		NopInterceptor nop = (NopInterceptor) bf.getBean("nopInterceptor");
		assertEquals(0, nop.getCount());
		
		int newAge = 12;
		// Not advised
		advised.exceptional(null);
		assertEquals(0, nop.getCount());
		advised.setAge(newAge);
		assertEquals(newAge, advised.getAge());
		// Only setter fired
		assertEquals(1, nop.getCount());
	}
	
	public void testMultiplePatterns() throws Throwable {
		BeanFactory bf = new ClassPathXmlApplicationContext("org/springframework/aop/support/regexpSetterTests.xml"); 
		// This is a CGLIB proxy, so we can proxy it to the target class
		TestBean advised = (TestBean) bf.getBean("settersAndAbsquatulateAdvised");
		// Interceptor behind regexp advisor
		NopInterceptor nop = (NopInterceptor) bf.getBean("nopInterceptor");
		assertEquals(0, nop.getCount());
	
		int newAge = 12;
		// Not advised
		advised.exceptional(null);
		assertEquals(0, nop.getCount());
		
		// This is proxied
		advised.absquatulate();
		assertEquals(1, nop.getCount());
		advised.setAge(newAge);
		assertEquals(newAge, advised.getAge());
		// Only setter fired
		assertEquals(2, nop.getCount());
	}

}
