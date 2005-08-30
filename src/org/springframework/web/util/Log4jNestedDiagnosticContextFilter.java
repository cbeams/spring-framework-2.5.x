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

import org.apache.log4j.NDC;

/**
 * Simple request logging filter that adds the request log message to the log4j nested
 * diagnostic context before the request is processed and removes it after the request
 * is processed.
 *
 * @author Rob Harrop
 * @see #setIncludeQueryString(boolean)
 * @see #setBeforeMessagePrefix(String)
 * @see #setBeforeMessageSuffix(String)
 * @see #setAfterMessagePrefix(String)
 * @see #setAfterMessageSuffix(String)
 * @see NDC#push(String)
 * @see NDC#pop()
 */
public class Log4jNestedDiagnosticContextFilter extends AbstractRequestLoggingFilter {

	/**
	 * Adds a log message the <code>NDC</code> before the request is processed.
	 */
	protected void beforeRequest(HttpServletRequest request, String message) {
		NDC.push(message);
	}

	/**
	 * Removes the log message from the <code>NDC</code> before the request is processed.
	 */
	protected void afterRequest(HttpServletRequest request, String message) {
		NDC.pop();
	}
}
