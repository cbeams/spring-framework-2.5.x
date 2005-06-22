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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Base class for <code>Filter</code>s that perform logging operations before and after a
 * request is processed.
 * <p/>
 * Sub-classes should override the <code>beforeRequest(HttpServletRequest, String)</code> and
 * <code>afterRequest(HttpServletRequest, String)</code> methods to perform the actual logging around
 * the request.
 * <p/>
 * Sub-classes are passed the message to write to the log in the <code>beforeRequest</code>
 * and <code>afterRequest</code> methods. By default, only the URI of the request is logged.
 * However, setting the <code>includeQueryString</code> property to <code>true</code> will
 * cause the query string of the request to be included also.
 * <p/>
 * Prefixes and suffixes for the before and after messages can be configured using the
 * <code>beforeMessagePrefix</code>, <code>afterMessagePrefix</code>, <code>beforeMessageSuffix</code> and
 * <code>afterMessageSuffix</code> properties,
 * <p/>
 *
 * @author Rob Harrop
 * @see #setIncludeQueryString(boolean)
 * @see #setBeforeMessagePrefix(String)
 * @see #setBeforeMessageSuffix(String)
 * @see #setAfterMessagePrefix(String)
 * @see #setAfterMessageSuffix(String)
 * @see #beforeRequest(javax.servlet.http.HttpServletRequest, String)
 * @see #afterRequest(javax.servlet.http.HttpServletRequest, String)
 */
public abstract class AbstractRequestLoggingFilter extends OncePerRequestFilter {

	/**
	 * Indicates whether or not the query string for the request should be included in the
	 * log message.
	 *
	 * @see #setIncludeQueryString(boolean)
	 */
	private boolean includeQueryString;

	/**
	 * The value that is prepended to the log message when writing the <strong>before</code>
	 * request message.
	 */
	protected String beforeMessagePrefix = "Before Request[ ";

	/**
	 * The value that is appended to the log message when writing the <strong>before</code>
	 * request message.
	 */
	protected String beforeMessageSuffix = "].";

	/**
	 * The value that is prepended to the log message when writing the <strong>after</code>
	 * request message.
	 */
	protected String afterMessagePrefix = "After Request[ ";

	/**
	 * The value that is appended to the log message when writing the <strong>after</code>
	 * request message.
	 */
	protected String afterMessageSuffix = "].";

	/**
	 * Sets the value of <code>includeQueryString</code> flag indicating whether or not
	 * the query string should be included in the log message.
	 * <p/>
	 * <p/>
	 * Should be configured using an <code>&lt;init-param&gt;</code> in the filter definition
	 * in web.xml
	 */
	public void setIncludeQueryString(boolean includeQueryString) {
		this.includeQueryString = includeQueryString;
	}

	/**
	 * Sets the value that should be prepended to the log message written <strong>before</code> a
	 * request is processed.
	 */
	public void setBeforeMessagePrefix(String beforeMessagePrefix) {
		this.beforeMessagePrefix = beforeMessagePrefix;
	}

	/**
	 * Sets the value that should be apppended to the log message written <strong>before</code> a
	 * request is processed.
	 */
	public void setBeforeMessageSuffix(String beforeMessageSuffix) {
		this.beforeMessageSuffix = beforeMessageSuffix;
	}

	/**
	 * Sets the value that should be prepended to the log message written <strong>after</code> a
	 * request is processed.
	 */
	public void setAfterMessagePrefix(String afterMessagePrefix) {
		this.afterMessagePrefix = afterMessagePrefix;
	}

	/**
	 * Sets the value that should be appended to the log message written <strong>after</code> a
	 * request is processed.
	 */
	public void setAfterMessageSuffix(String afterMessageSuffix) {
		this.afterMessageSuffix = afterMessageSuffix;
	}

	/**
	 * Forwards the request to the next filter in the chain and delegates down to the subclasses
	 * to perform the actual request logging both before and after the request is processed.
	 *
	 * @see #beforeRequest(javax.servlet.http.HttpServletRequest, String)
	 * @see #afterRequest(javax.servlet.http.HttpServletRequest, String)
	 */
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		beforeRequest(request, getBeforeMessage(request));
		filterChain.doFilter(request, response);
		afterRequest(request, getAfterMessage(request));
	}

	/**
	 * Gets the message to write to the log before the request.
	 *
	 * @see #createMessage(javax.servlet.http.HttpServletRequest, String, String)
	 */
	private String getBeforeMessage(HttpServletRequest request) {
		return createMessage(request, this.beforeMessagePrefix, this.beforeMessageSuffix);
	}

	/**
	 * Gets the message to write to the log after the request.
	 *
	 * @see #createMessage(javax.servlet.http.HttpServletRequest, String, String)
	 */
	private String getAfterMessage(HttpServletRequest request) {
		return createMessage(request, this.afterMessagePrefix, this.afterMessageSuffix);
	}

	/**
	 * Creates a log message for the given request, prefix and suffix.
	 * <p/>
	 * If <code>includeQueryString</code> is <code>true</code> then the inner part of the log message
	 * will take the form <code>request_uri?query_string</code> otherwise the message will simply be of
	 * the form <code>request_uri</code>.
	 * <p/>
	 * The final message is composed of the inner part as described and the supplied prefix and suffix.
	 */
	private String createMessage(HttpServletRequest request, String prefix, String suffix) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(prefix);
		buffer.append(request.getRequestURI());
		if (this.includeQueryString) {
			buffer.append('?');
			buffer.append(request.getQueryString());
		}
		buffer.append(suffix);
		return buffer.toString();
	}


	/**
	 * Concrete sub-classes should implement this method to write a log message <strong>before</code> the
	 * request is processed.
	 */
	protected abstract void beforeRequest(HttpServletRequest request, String message);

	/**
	 * Concrete sub-classes should implement this method to write a log message <strong>after</code> the
	 * request is processed.
	 */
	protected abstract void afterRequest(HttpServletRequest request, String message);


}
