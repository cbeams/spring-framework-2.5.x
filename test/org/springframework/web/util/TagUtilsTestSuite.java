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

package org.springframework.web.util;

import javax.servlet.jsp.PageContext;

import junit.framework.TestCase;

/**
 * @author Alef Arendsen
 */
public class TagUtilsTestSuite extends TestCase {
	
	public void testTagUtils() {
		// it's simple, test all scope, plus a non-existing one
		// (which should evaluate to SCOPE_PAGE)
		
		assertEquals(TagUtils.SCOPE_PAGE, "page");
		assertEquals(TagUtils.SCOPE_APPLICATION, "application");
		assertEquals(TagUtils.SCOPE_SESSION, "session");
		assertEquals(TagUtils.SCOPE_REQUEST, "request");
		
		assertEquals(TagUtils.getScope("page"), PageContext.PAGE_SCOPE);
		assertEquals(TagUtils.getScope("request"), PageContext.REQUEST_SCOPE);
		assertEquals(TagUtils.getScope("session"), PageContext.SESSION_SCOPE);
		assertEquals(TagUtils.getScope("application"), PageContext.APPLICATION_SCOPE);
		assertEquals(TagUtils.getScope("bla"), PageContext.PAGE_SCOPE);
		
		try {
			TagUtils.getScope(null);
			fail("Null scope, no excpetion thrown!");			
		}
		catch (IllegalArgumentException e) {
			// ok
		}
	}

}
