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

import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit test for ParameterMethodNameResolver
 * 
 * @author Colin Sampaleanu
 */
public class ParameterMethodNameResolverTest extends TestCase {

	public void testGetHandlerMethodName() throws NoSuchRequestHandlingMethodException {
		
		ParameterMethodNameResolver resolver = new ParameterMethodNameResolver();
		resolver.setDefaultMethodName("default");
		resolver.setParamNameList(new String[] {"hello", "spring", "colin"});
		Properties logicalMappings = new Properties();
		logicalMappings.setProperty("hello", "goodbye");
		logicalMappings.setProperty("nina", "colin");
		resolver.setLogicalMappings(logicalMappings);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addParameter("nomatch", "whatever");
		try {
			resolver.getHandlerMethodName(request);
		}
		catch (NoSuchRequestHandlingMethodException e) {
			//expected
		}
		
		// verify default handler
		request = new MockHttpServletRequest();
		request.addParameter("this will not match anything", "whatever");
		assertEquals("default", resolver.getHandlerMethodName(request));
		
		// verify first resolution strategy (action=method)
		request = new MockHttpServletRequest();
		request.addParameter("action", "reset");
		assertEquals("reset", resolver.getHandlerMethodName(request));
		// this one also tests logical mapping
		request = new MockHttpServletRequest();
		request.addParameter("action", "nina");
		assertEquals("colin", resolver.getHandlerMethodName(request));

		// now validate second resolution strategy (parameter existence)
		// this also tests logical mapping
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
