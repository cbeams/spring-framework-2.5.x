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

package org.springframework.util;

import java.io.IOException;

import javax.servlet.ServletException;

import org.springframework.beans.BeansException;

import junit.framework.TestCase;

/**
 * @author Rod Johnson
 */
public class ObjectUtilsTests extends TestCase {
	
	public void testIsCheckedException() {
		assertTrue(ObjectUtils.isCheckedException(new Exception()));
		assertTrue(ObjectUtils.isCheckedException(new ServletException()));
		assertFalse(ObjectUtils.isCheckedException(new RuntimeException()));
		assertFalse(ObjectUtils.isCheckedException(new BeansException("", null) {}));
		assertFalse(ObjectUtils.isCheckedException(new Throwable()));
	}

	public void testIsCompatibleWithThrowsClause() {
		Class[] empty = new Class[0];
		Class[] exception = new Class[] { Exception.class };
		Class[] servletAndIO = new Class[] { ServletException.class, IOException.class };
		Class[] throwable = new Class[] { Throwable.class } ;
		
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new RuntimeException(), null));
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new RuntimeException(), empty));
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new RuntimeException(), exception));
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new RuntimeException(), servletAndIO));
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new RuntimeException(), throwable));
		
		assertFalse(ObjectUtils.isCompatibleWithThrowsClause(new Exception(), null));
		assertFalse(ObjectUtils.isCompatibleWithThrowsClause(new Exception(), empty));
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new Exception(), exception));
		assertFalse(ObjectUtils.isCompatibleWithThrowsClause(new Exception(), servletAndIO));
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new Exception(), throwable));
		
		assertFalse(ObjectUtils.isCompatibleWithThrowsClause(new ServletException(), null));
		assertFalse(ObjectUtils.isCompatibleWithThrowsClause(new ServletException(), empty));
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new ServletException(), exception));
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new ServletException(), servletAndIO));
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new ServletException(), throwable));
		
		assertFalse(ObjectUtils.isCompatibleWithThrowsClause(new Throwable(), null));
		assertFalse(ObjectUtils.isCompatibleWithThrowsClause(new Throwable(), empty));
		assertFalse(ObjectUtils.isCompatibleWithThrowsClause(new Throwable(), exception));
		assertFalse(ObjectUtils.isCompatibleWithThrowsClause(new Throwable(), servletAndIO));
		assertTrue(ObjectUtils.isCompatibleWithThrowsClause(new Throwable(), throwable));
	}

}
