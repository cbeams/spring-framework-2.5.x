/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import junit.framework.TestCase;

import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.beans.ITestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author Rod Johnson
 * @version $Id: RegexpMethodPointcutAroundAdvisorIntegrationTests.java,v 1.1 2004-01-13 18:48:30 johnsonr Exp $
 */
public class RegexpMethodPointcutAroundAdvisorIntegrationTests extends TestCase {

	/**
	 * Constructor for RegexpMethodPointcutAroundAdvisorTests.
	 * @param arg0
	 */
	public RegexpMethodPointcutAroundAdvisorIntegrationTests(String arg0) {
		super(arg0);
	}
	
	public void testRegExp() throws Throwable {
		BeanFactory bf = new ClassPathXmlApplicationContext("org/springframework/aop/support/regexpSetterTests.xml"); 
		ITestBean advised = (ITestBean) bf.getBean("advised");
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

}
