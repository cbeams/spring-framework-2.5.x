/*
 * Copyright 2004-2005 the original author or authors.
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
package org.springframework.web.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.aop.target.scope.ScopeMap;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

public class HttpRequestAndSessionScopeMapTests extends TestCase {

	public void testGetTargetFromRequest() {
		HttpServletRequest request = new MockHttpServletRequest();
		ScopeMap scopeMap = new HttpRequestScopeMap();
		request.setAttribute("test", "test1");
		assertEquals("test1", scopeMap.get(request, "test"));
	}
	
	public void testSetTargetOnRequest() {
		HttpServletRequest request = new MockHttpServletRequest();
		ScopeMap scopeMap = new HttpRequestScopeMap();
		scopeMap.put(request, "test", "test2");
		assertEquals("test2", request.getAttribute("test"));
	}
	
	public void testRemoveTargetFromRequest() {
		HttpServletRequest request = new MockHttpServletRequest();
		request.setAttribute("test", "test3");
		ScopeMap scopeMap = new HttpRequestScopeMap();
		scopeMap.remove(request, "test");
		assertNull(request.getAttribute("test"));
	}
	
	public void testHttpRequestScopeMapIsPersistentFalse() {
		assertFalse(new HttpRequestScopeMap().isPersistent(null));
	}
	
	public void testGetTargetFromSession() {
		HttpSession session = new MockHttpServletRequest().getSession();
		ScopeMap scopeMap = new HttpSessionScopeMap();
		session.setAttribute("test", "test4");
		assertEquals("test4", scopeMap.get(session, "test"));
	}
	
	public void testSetTargetOnSession() {
		HttpSession session = new MockHttpServletRequest().getSession();
		ScopeMap scopeMap = new HttpSessionScopeMap();
		scopeMap.put(session, "test", "test5");
		assertEquals("test5", scopeMap.get(session, "test"));
	}
	
	public void testRemoveTargetFromSession() {
		HttpSession session = new MockHttpServletRequest().getSession();
		session.setAttribute("test", "test6");
		ScopeMap scopeMap = new HttpSessionScopeMap();
		scopeMap.remove(session, "test");
		assertNull(session.getAttribute("test"));
	}
	
	public void testHttpSessionScopeMapisPersistentFalse() {
		assertFalse(new HttpSessionScopeMap().isPersistent(null));
	}
		
}
