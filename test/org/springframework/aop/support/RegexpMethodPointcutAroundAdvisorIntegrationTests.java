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
 * @version $Id: RegexpMethodPointcutAroundAdvisorIntegrationTests.java,v 1.3 2004-03-18 03:01:17 trisberg Exp $
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
