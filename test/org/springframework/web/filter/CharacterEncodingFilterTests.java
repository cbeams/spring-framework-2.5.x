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

package org.springframework.web.filter;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import java.util.Vector;

/**
 * Unit tests for the CharacterEncodingFilter class.
 *
 * @author Rick Evans
 */
public final class CharacterEncodingFilterTests extends TestCase {

	public static final String FILTER_NAME = "boot";
	private static final String ENCODING = "UTF-8";


	public void testForceAlwaysSetsEncoding() throws Exception {
		MockControl mockRequest = MockControl.createControl(HttpServletRequest.class);
		HttpServletRequest request = (HttpServletRequest) mockRequest.getMock();
		request.setCharacterEncoding(ENCODING);
		request.getAttribute(FILTER_NAME + OncePerRequestFilter.ALREADY_FILTERED_SUFFIX);
		mockRequest.setReturnValue(null);
		request.setAttribute(FILTER_NAME + OncePerRequestFilter.ALREADY_FILTERED_SUFFIX, Boolean.TRUE);
		mockRequest.replay();

		MockHttpServletResponse response = new MockHttpServletResponse();

		MockControl mockFilter = MockControl.createControl(FilterChain.class);
		FilterChain filterChain = (FilterChain) mockFilter.getMock();
		filterChain.doFilter(request, response);
		mockFilter.replay();

		MockControl mockFilterConfig = MockControl.createControl(FilterConfig.class);
		FilterConfig filterConfig = (FilterConfig) mockFilterConfig.getMock();
		filterConfig.getFilterName();
		mockFilterConfig.setReturnValue(FILTER_NAME);
		filterConfig.getInitParameterNames();
		mockFilterConfig.setReturnValue(new Vector().elements());
		filterConfig.getServletContext();
		mockFilterConfig.setReturnValue(new MockServletContext());
		filterConfig.getFilterName();
		mockFilterConfig.setReturnValue(FILTER_NAME);
		filterConfig.getFilterName();
		mockFilterConfig.setReturnValue(FILTER_NAME);
		mockFilterConfig.replay();

		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setForceEncoding(true);
		filter.setEncoding(ENCODING);
		filter.init(filterConfig);
		filter.doFilter(request, response, filterChain);

		mockRequest.verify();
		mockFilter.verify();
		mockFilterConfig.verify();
	}

	public void testEncodingIfEmptyAndNotForced() throws Exception {
		MockControl mockRequest = MockControl.createControl(HttpServletRequest.class);
		HttpServletRequest request = (HttpServletRequest) mockRequest.getMock();
		request.getCharacterEncoding();
		mockRequest.setReturnValue(null);
		request.setCharacterEncoding(ENCODING);
		request.getAttribute(FILTER_NAME + OncePerRequestFilter.ALREADY_FILTERED_SUFFIX);
		mockRequest.setReturnValue(null);
		request.setAttribute(FILTER_NAME + OncePerRequestFilter.ALREADY_FILTERED_SUFFIX, Boolean.TRUE);
		mockRequest.replay();

		MockHttpServletResponse response = new MockHttpServletResponse();

		MockControl mockFilter = MockControl.createControl(FilterChain.class);
		FilterChain filterChain = (FilterChain) mockFilter.getMock();
		filterChain.doFilter(request, response);
		mockFilter.replay();

		MockControl mockFilterConfig = MockControl.createControl(FilterConfig.class);
		FilterConfig filterConfig = (FilterConfig) mockFilterConfig.getMock();
		filterConfig.getFilterName();
		mockFilterConfig.setReturnValue(FILTER_NAME);
		filterConfig.getInitParameterNames();
		mockFilterConfig.setReturnValue(new Vector().elements());
		filterConfig.getServletContext();
		mockFilterConfig.setReturnValue(new MockServletContext());
		filterConfig.getFilterName();
		mockFilterConfig.setReturnValue(FILTER_NAME);
		filterConfig.getFilterName();
		mockFilterConfig.setReturnValue(FILTER_NAME);
		mockFilterConfig.replay();

		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setForceEncoding(false);
		filter.setEncoding(ENCODING);
		filter.init(filterConfig);
		filter.doFilter(request, response, filterChain);

		mockRequest.verify();
		mockFilter.verify();
		mockFilterConfig.verify();
	}

	public void testDoesNowtIfEncodingIsNotEmptyAndNotForced() throws Exception {
		MockControl mockRequest = MockControl.createControl(HttpServletRequest.class);
		HttpServletRequest request = (HttpServletRequest) mockRequest.getMock();
		request.getCharacterEncoding();
		mockRequest.setReturnValue(ENCODING);
		request.getAttribute(FILTER_NAME + OncePerRequestFilter.ALREADY_FILTERED_SUFFIX);
		mockRequest.setReturnValue(null);
		request.setAttribute(FILTER_NAME + OncePerRequestFilter.ALREADY_FILTERED_SUFFIX, Boolean.TRUE);
		mockRequest.replay();

		MockHttpServletResponse response = new MockHttpServletResponse();

		MockControl mockFilter = MockControl.createControl(FilterChain.class);
		FilterChain filterChain = (FilterChain) mockFilter.getMock();
		filterChain.doFilter(request, response);
		mockFilter.replay();

		MockControl mockFilterConfig = MockControl.createControl(FilterConfig.class);
		FilterConfig filterConfig = (FilterConfig) mockFilterConfig.getMock();
		filterConfig.getFilterName();
		mockFilterConfig.setReturnValue(FILTER_NAME);
		filterConfig.getInitParameterNames();
		mockFilterConfig.setReturnValue(new Vector().elements());
		filterConfig.getServletContext();
		mockFilterConfig.setReturnValue(new MockServletContext());
		filterConfig.getFilterName();
		mockFilterConfig.setReturnValue(FILTER_NAME);
		filterConfig.getFilterName();
		mockFilterConfig.setReturnValue(FILTER_NAME);
		mockFilterConfig.replay();

		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding(ENCODING);
		filter.init(filterConfig);
		filter.doFilter(request, response, filterChain);

		mockRequest.verify();
		mockFilter.verify();
		mockFilterConfig.verify();
	}

}
