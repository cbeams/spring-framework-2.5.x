/*
 * Copyright 2005 the original author or authors.
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

package org.springframework.web.servlet.mvc.multiaction;

import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * Unit test for ParameterNameMethodNameResolver
 * 
 * @author Colin Sampaleanu
 */
public class ParameterNameMethodNameResolverTest extends TestCase {

	public void testInit() throws NoSuchRequestHandlingMethodException {
		
		ParameterNameMethodNameResolver resolver = new ParameterNameMethodNameResolver();
		MockHttpServletRequest request = new MockHttpServletRequest();
		
		try {
			resolver.afterPropertiesSet();
			resolver.getHandlerMethodName(request);
		}
		catch (IllegalArgumentException e) {
			//expected
		}
	}
	
	public void testGetHandlerMethodName() throws NoSuchRequestHandlingMethodException {
		
		ParameterNameMethodNameResolver resolver = new ParameterNameMethodNameResolver();
		resolver.setDefaultMethodName("default");
		resolver.setMappings(new String[] {"hello:goodbye", "spring", "colin:nina"});

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("nomatch", "whatever");
		try {
			resolver.getHandlerMethodName(request);
		}
		catch (NoSuchRequestHandlingMethodException e) {
			//expected
		}

		request = new MockHttpServletRequest();
		request.addParameter("hello", "whatever");
		assertEquals("goodbye", resolver.getHandlerMethodName(request));
		
		request = new MockHttpServletRequest();
		request.addParameter("spring", "whatever");
		assertEquals("spring", resolver.getHandlerMethodName(request));
		
		request = new MockHttpServletRequest();
		request.addParameter("hello", "whatever");
		request.addParameter("spring", "whatever");
		assertEquals("goodbye", resolver.getHandlerMethodName(request));
		
		request = new MockHttpServletRequest();
		request.addParameter("colin", "whatever");
		request.addParameter("spring", "whatever");
		assertEquals("spring", resolver.getHandlerMethodName(request));
	}
}
