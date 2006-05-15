/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.servlet.mvc;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.util.PathMatcher;
import org.springframework.util.AntPathMatcher;

/**
 * @author Juergen Hoeller
 * @since 14.09.2005
 */
public class UrlFilenameViewControllerTests extends TestCase {

	private PathMatcher pathMatcher = new AntPathMatcher();

	public void testWithPlainFilename() throws Exception {
		UrlFilenameViewController ctrl = new UrlFilenameViewController();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = ctrl.handleRequest(request, response);
		assertEquals("index", mv.getViewName());
		assertTrue(mv.getModel().isEmpty());
	}

	public void testWithFilenamePlusExtension() throws Exception {
		UrlFilenameViewController ctrl = new UrlFilenameViewController();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = ctrl.handleRequest(request, response);
		assertEquals("index", mv.getViewName());
		assertTrue(mv.getModel().isEmpty());
	}

	public void testWithPrefixAndSuffix() throws Exception {
		UrlFilenameViewController ctrl = new UrlFilenameViewController();
		ctrl.setPrefix("mypre_");
		ctrl.setSuffix("_mysuf");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = ctrl.handleRequest(request, response);
		assertEquals("mypre_index_mysuf", mv.getViewName());
		assertTrue(mv.getModel().isEmpty());
	}

	public void testWithPrefix() throws Exception {
		UrlFilenameViewController ctrl = new UrlFilenameViewController();
		ctrl.setPrefix("mypre_");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = ctrl.handleRequest(request, response);
		assertEquals("mypre_index", mv.getViewName());
		assertTrue(mv.getModel().isEmpty());
	}

	public void testWithSuffix() throws Exception {
		UrlFilenameViewController ctrl = new UrlFilenameViewController();
		ctrl.setSuffix("_mysuf");
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = ctrl.handleRequest(request, response);
		assertEquals("index_mysuf", mv.getViewName());
		assertTrue(mv.getModel().isEmpty());
	}

	public void testMultiLevel() throws Exception {
		UrlFilenameViewController ctrl = new UrlFilenameViewController();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/docs/cvs/commit.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = ctrl.handleRequest(request, response);
		assertEquals("docs/cvs/commit", mv.getViewName());
		assertTrue(mv.getModel().isEmpty());
	}

	public void testMultiLevelWithMapping() throws Exception {
		UrlFilenameViewController ctrl = new UrlFilenameViewController();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/docs/cvs/commit.html");
		exposePathInMapping(request, "/docs/**");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = ctrl.handleRequest(request, response);
		assertEquals("cvs/commit", mv.getViewName());
		assertTrue(mv.getModel().isEmpty());
	}

	public void testMultiLevelMappingWithFallback() throws Exception {
	  UrlFilenameViewController ctrl = new UrlFilenameViewController();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/docs/cvs/commit.html");
		exposePathInMapping(request, "/docs/cvs/commit.html");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = ctrl.handleRequest(request, response);
		assertEquals("docs/cvs/commit", mv.getViewName());
		assertTrue(mv.getModel().isEmpty());
	}

	public void testWithContextMapping() throws Exception {
		UrlFilenameViewController ctrl = new UrlFilenameViewController();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/myapp/docs/cvs/commit.html");
		request.setContextPath("/myapp");
		MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView mv = ctrl.handleRequest(request, response);
		assertEquals("docs/cvs/commit", mv.getViewName());
		assertTrue(mv.getModel().isEmpty());
	}
	private void exposePathInMapping(MockHttpServletRequest request, String mapping) {
		String pathInMapping = this.pathMatcher.extractPathWithinPattern(mapping, request.getRequestURI());
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, pathInMapping);
	}
}
