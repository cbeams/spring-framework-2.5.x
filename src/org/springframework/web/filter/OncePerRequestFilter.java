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

package org.springframework.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter base class that guarantees to be just executed once per request,
 * on any servlet container. It provides a <code>doFilterInternal</code>
 * method with HttpServletRequest and HttpServletResponse arguments.
 *
 * <p>The <code>getAlreadyFilteredAttributeName</code> method determines how
 * to identify that a request is already filtered. The default implementation
 * is based on the configured name of the concrete filter instance.
 *
 * @author Juergen Hoeller
 * @since 06.12.2003
 * @see #doFilterInternal
 * @see #getAlreadyFilteredAttributeName
 */
public abstract class OncePerRequestFilter extends GenericFilterBean {

	/**
	 * Suffix that gets appended to the filter name for the
	 * "already filtered" request attribute.
	 * @see #getAlreadyFilteredAttributeName
	 */
	public static final String ALREADY_FILTERED_SUFFIX = ".FILTERED";

	/**
	 * This doFilter implementation stores a request attribute for
	 * "already filtered", proceeding without filtering again if the
	 * attribute is already there.
	 * @see #getAlreadyFilteredAttributeName
	 * @see #shouldNotFilter
	 * @see #doFilterInternal
	 */
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String alreadyFilteredAttributeName = getAlreadyFilteredAttributeName();
		if (request.getAttribute(alreadyFilteredAttributeName) != null || shouldNotFilter(httpRequest)) {
			// proceed without invoking this filter
			filterChain.doFilter(request, response);
		}
		else {
			// invoke this filter
			request.setAttribute(alreadyFilteredAttributeName, Boolean.TRUE);
			doFilterInternal(httpRequest, httpResponse, filterChain);
		}
	}

	/**
	 * Return the name of the request attribute that identifies that a request
	 * is already filtered. Default implementation takes the configured name
	 * of the concrete filter instance and appends ".FILTERED".
	 * @see #getFilterName
	 * @see #ALREADY_FILTERED_SUFFIX
	 */
	protected String getAlreadyFilteredAttributeName() {
		return getFilterName() + ALREADY_FILTERED_SUFFIX;
	}

	/**
	 * Can return true to avoid filtering of the given request.
	 * The default implementation always returns false.
	 * Can be overridden in subclasses for custom filtering control.
	 * @param request current HTTP request
	 * @return whether the given request should <i>not</i> be filtered
	 * @throws ServletException in case of errors
	 */
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return false;
	}

	/**
	 * Same contract as for doFilter, but guaranteed to be just invoked once per
	 * request. Provides HttpServletRequest and HttpServletResponse arguments
	 * instead of the default ServletRequest and ServletResponse ones.
	 */
	protected abstract void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException;

}
