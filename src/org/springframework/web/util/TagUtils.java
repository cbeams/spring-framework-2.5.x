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

package org.springframework.web.util;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.springframework.util.Assert;

/**
 * Utility class to translate {@link String Strings} to web scopes.
 *
 * <p>
 * <ul>
 * <li><code>page</code> will be transformed to
 * {@link javax.servlet.jsp.PageContext#PAGE_SCOPE PageContext.PAGE_SCOPE}
 * <li><code>request</code> will be transformed to
 * {@link javax.servlet.jsp.PageContext#REQUEST_SCOPE PageContext.REQUEST_SCOPE}
 * <li><code>session</code> will be transformed to
 * {@link javax.servlet.jsp.PageContext#SESSION_SCOPE PageContext.SESSION_SCOPE}
 * <li><code>application</code> will be transformed to
 * {@link javax.servlet.jsp.PageContext#APPLICATION_SCOPE PageContext.APPLICATION_SCOPE}
 * </ul>
 *
 * @author Alef Arendsen
 * @author Rob Harrop
 */
public abstract class TagUtils {

	/** Constant identifying the page scope */
	public static final String SCOPE_PAGE = "page";

	/** Constant identifying the request scope */
	public static final String SCOPE_REQUEST = "request";

	/** Constant identifying the session scope */
	public static final String SCOPE_SESSION = "session";

	/** Constant identifying the application scope */
	public static final String SCOPE_APPLICATION = "application";


	/**
	 * Determines the scope for a given input <code>String</code>.
	 * <p>If the <code>String</code> does not match 'request', 'session',
	 * 'page' or 'application', the method will return {@link PageContext#PAGE_SCOPE}.
	 * @param scope the <code>String</code> to inspect
	 * @return the scope found, or {@link PageContext#PAGE_SCOPE} if no scope matched
	 */
	public static int getScope(String scope) {
		Assert.notNull(scope, "Scope to search for cannot be null");
		if (scope.equals(SCOPE_REQUEST)) {
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

	/**
	 * Determine whether the supplied {@link Tag} has any ancestor tag
	 * of the supplied type.
	 * @return <code>true</code> if the supplied {@link Tag} has any ancestor tag
	 * of the supplied type
	 */
	public static boolean hasAncestorOfType(Tag tag, Class parentTagClass) {
		Assert.notNull(tag, "Tag cannot be null");
		Assert.notNull(parentTagClass, "Parent tag class cannot be null");
		if (!Tag.class.isAssignableFrom(parentTagClass)) {
			throw new IllegalArgumentException(
					"Class '" + parentTagClass.getName() + "' is not a valid Tag type");
		}
		Tag ancestor = tag.getParent();
		while (ancestor != null) {
			if (parentTagClass.equals(ancestor.getClass())) {
				return true;
			}
			ancestor = ancestor.getParent();
		}
		return false;
	}

}
