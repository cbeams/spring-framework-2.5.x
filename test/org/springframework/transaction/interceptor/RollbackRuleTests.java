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

package org.springframework.transaction.interceptor;

import javax.ejb.EJBException;
import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.aop.framework.AopConfigException;
import org.springframework.beans.FatalBeanException;

/**
 * 
 * @author Rod Johnson
 * @since 09-Apr-2003
 */
public class RollbackRuleTests extends TestCase {

	/**
	 * Constructor for RollbackRuleTests.
	 * @param arg0
	 */
	public RollbackRuleTests(String arg0) {
		super(arg0);
	}

	public void testFoundImmediatelyWithString() {
		RollbackRuleAttribute rr = new RollbackRuleAttribute("java.lang.Exception");
		assertTrue(rr.getDepth(new Exception()) == 0);
	}
	
	public void testFoundImmediatelyWithClass() {
		RollbackRuleAttribute rr = new RollbackRuleAttribute(Exception.class);
		assertTrue(rr.getDepth(new Exception()) == 0);
	}
	
	public void testNotFound() {
		RollbackRuleAttribute rr = new RollbackRuleAttribute("javax.servlet.ServletException");
		assertTrue(rr.getDepth(new EJBException()) == -1);
	}
	
	public void testAncestry() {
		RollbackRuleAttribute rr = new RollbackRuleAttribute("java.lang.Exception");
		// Exception -> Runtime -> EJBException
		assertTrue(rr.getDepth(new EJBException()) == 2);
	}
	
	
	public void testAlwaysTrue() {
		RollbackRuleAttribute rr = new RollbackRuleAttribute("java.lang.Throwable");
		// Exception -> Runtime -> EJBException
		assertTrue(rr.getDepth(new EJBException()) > 0);
		assertTrue(rr.getDepth(new ServletException()) > 0);
		assertTrue(rr.getDepth(new FatalBeanException(null,null)) > 0);
		assertTrue(rr.getDepth(new RuntimeException()) > 0);
	}
	
	public void testConstructorArgMustBeAThrowableClass() {
		try {
			new RollbackRuleAttribute(StringBuffer.class);
			fail("Can't construct a RollbackRuleAttribute without a throwable");
		}
		catch (AopConfigException ex) {
			// Ok
		}
	}

}
