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

/**
 * Utility class to transform Strings to scopes:<br>
 * <code>page</code> will be transformed to
 * {@link javax.servlet.jsp.PageContext#PAGE_SCOPE PageContext.PAGE_SCOPE}
 * <code>request</code> will be transformed to
 * {@link javax.servlet.jsp.PageContext#REQUEST_SCOPE PageContext.REQUEST_SCOPE}<br>
 * <code>session</code> will be transformed to
 * {@link javax.servlet.jsp.PageContext#SESSION_SCOPE PageContext.SESSION_SCOPE}<br>
 * <code>application</code> will be transformed to
 * {@link javax.servlet.jsp.PageContext#APPLICATION_SCOPE PageContext.APPLICATION_SCOPE}<br>
 *
 * @author Alef Arendsen
 */
public abstract class TagUtils {

	/** constant identifying the page scope String */
	public static final String SCOPE_PAGE = "page";

	/** constant identifying the request scope String */
	public static final String SCOPE_REQUEST = "request";

	/** constant identifying the session scope String */
	public static final String SCOPE_SESSION = "session";

	/** constant identifying the application scope String */
	public static final String SCOPE_APPLICATION = "application";

	/**
	 * Determines the scope for a given input String. If the string does not match
	 * 'request', 'session', 'page' or 'application', the method will return
	 * PageContext.PAGE_SCOPE.
	 * @param scope the string to inspect
	 * @return the scope found, or PageContext.PAGE_SCOPE if no scope matched.
	 * @throws java.lang.IllegalArgumentException if the scope is null
	 */
	public static int getScope(String scope) {
		if (scope == null) {
			throw new IllegalArgumentException(
					"Scope to search for cannot be null");
		}
		else if (scope.equals(SCOPE_REQUEST)) {
			return PageContext.REQUEST_SCOPE;
		}
		else if (scope.equals(SCOPE_SESSION)) {
			return PageContext.SESSION_SCOPE;
		}
		else if (scope.equals(SCOPE_APPLICATION)) {
			return PageContext.APPLICATION_SCOPE;
		}
		else {
			return PageContext.PAGE_SCOPE;
		}
	}

}
