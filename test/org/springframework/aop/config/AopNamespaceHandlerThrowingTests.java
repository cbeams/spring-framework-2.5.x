/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.config;

import junit.framework.TestCase;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.CountingBeforeAdvice;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.ITestBean;
import org.springframework.beans.PropertyAccessExceptionsException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rob Harrop
 */
public class AopNamespaceHandlerThrowingTests extends TestCase {

	private ApplicationContext context;

	protected String getOKConfigLocation() {
		return "org/springframework/aop/config/aopNamespaceHandlerThrowingOKTests.xml";
	}

	protected String getErrorConfigLocation() {
		return "org/springframework/aop/config/aopNamespaceHandlerThrowingErrorTests.xml";
	}

	public void testThrowingOnThrowingAdvice() {
		this.context = new ClassPathXmlApplicationContext(getOKConfigLocation());
	}
	
	public void testParseThrowingOnOtherAdviceType() {
		try {
			this.context = new ClassPathXmlApplicationContext(getErrorConfigLocation());
			fail("Expected BeanCreationException");
		} catch (BeanCreationException beanEx) {
			Throwable cause = beanEx.getCause();
			assertTrue("Expected PropertyAccessExceptionsException, got: " + cause.getClass(),cause instanceof PropertyAccessExceptionsException);
			PropertyAccessExceptionsException ex = (PropertyAccessExceptionsException) cause;
			cause = ex.getPropertyAccessException("throwingName");
			// we get back a MethodInvocationException, which nests what we really want to test...
			cause = cause.getCause();
			assertTrue("Expected UnsupportedOperationException, got: " + cause.getClass(),cause instanceof UnsupportedOperationException);
			assertEquals("Only afterThrowing advice can be used to bind a thrown exception",cause.getMessage());
		}
	}

	protected ITestBean getTestBean() {
		return (ITestBean) this.context.getBean("testBean");
	}

}
