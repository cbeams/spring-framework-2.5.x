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

import javax.servlet.ServletException;

import junit.framework.TestCase;

/**
 * 
 * @author Rod Johnson
 * @since 23-Jul-2003
 * @version $Id: RegexpMethodPointcutTests.java,v 1.4 2004-03-18 03:01:17 trisberg Exp $
 */
public class RegexpMethodPointcutTests extends TestCase {

	/**
	 * Constructor for RegexpMethodPointcutTests.
	 * @param arg0
	 */
	public RegexpMethodPointcutTests(String arg0) {
		super(arg0);
	}
	
	/**
	 * Matches nothing
	 * @throws Exception
	 */
	public void testNoPatternSupplied() throws Exception {
		RegexpMethodPointcut rpc = new RegexpMethodPointcut();
		assertFalse(rpc.matches(Object.class.getMethod("hashCode", null), String.class));
		assertFalse(rpc.matches(Object.class.getMethod("wait", null), Object.class));
		assertEquals(0, rpc.getPatterns().length);
	}
	
	public void testExactMatch() throws Exception {
		RegexpMethodPointcut rpc = new RegexpMethodPointcut();
		rpc.setPattern("java.lang.Object.hashCode");
		assertTrue(rpc.matches(Object.class.getMethod("hashCode", null), String.class));
	}
	
	public void testWildcard() throws Exception {
		RegexpMethodPointcut rpc = new RegexpMethodPointcut();
		rpc.setPattern(".*Object.hashCode");
		assertTrue(rpc.matches(Object.class.getMethod("hashCode", null), Object.class));
		assertFalse(rpc.matches(Object.class.getMethod("wait", null), Object.class));
	}
	
	public void testWildcardForOneClass() throws Exception {
		RegexpMethodPointcut rpc = new RegexpMethodPointcut();
		rpc.setPattern("java.lang.Object.*");
		assertTrue(rpc.matches(Object.class.getMethod("hashCode", null), String.class));
		assertTrue(rpc.matches(Object.class.getMethod("wait", null), String.class));
	}
	
	public void testMatchesObjectClass() throws Exception {
		RegexpMethodPointcut rpc = new RegexpMethodPointcut();
		rpc.setPattern("java.lang.Object.*");
		assertTrue(rpc.matches(Exception.class.getMethod("hashCode", null), ServletException.class));
		// Doesn't match a method from Throwable
		assertFalse(rpc.matches(Exception.class.getMethod("getMessage", null), Exception.class));
	}

}
