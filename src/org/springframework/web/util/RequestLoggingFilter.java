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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple request logging filter that writes the request URI, and optionally the querystring to a
 * Commons <code>Log</code>.
 *
 * @author Rob Harrop
 * @see #setIncludeQueryString(boolean)
 * @see #setBeforeMessagePrefix(String)
 * @see #setBeforeMessageSuffix(String)
 * @see #setAfterMessagePrefix(String)
 * @see #setAfterMessageSuffix(String)
 */
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {

	/**
	 * The <code>Log</code> to write the log messages to.
	 */
	private static final Log logger = LogFactory.getLog(RequestLoggingFilter.class);

	/**
	 * Writes a log message before the request is processed.
	 */
	protected void beforeRequest(HttpServletRequest request, String message) {
		if (logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}

	/**
	 * Writes a log message after the request is processed.
	 */
	protected void afterRequest(HttpServletRequest request, String message) {
		if (logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}
}
