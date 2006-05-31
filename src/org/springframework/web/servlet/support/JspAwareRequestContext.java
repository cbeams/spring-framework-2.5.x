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

package org.springframework.web.servlet.support;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

/**
 * JSP-aware subclass of RequestContext, allowing population of the context
 * from a JSP PageContext.
 *
 * <p>This context will also detect a JSTL locale attribute in page scope,
 * in addition to the fallback locale strategy provided by the base class.
 *
 * @author Juergen Hoeller
 * @since 1.1.4
 * @see #getFallbackLocale
 */
public class JspAwareRequestContext extends RequestContext {

	protected static final String PAGE_SCOPE_SUFFIX = ".page";

	private PageContext pageContext;


	/**
	 * Create a new JspAwareRequestContext for the given page context,
	 * using the request attributes for Errors retrieval.
	 * @param pageContext current JSP page context
	 */
	public JspAwareRequestContext(PageContext pageContext) {
		initContext(pageContext, null);
	}

	/**
	 * Create a new JspAwareRequestContext for the given page context,
	 * using the given model attributes for Errors retrieval.
	 * @param pageContext current JSP page context
	 * @param model the model attributes for the current view
	 * (can be <code>null</code>, using the request attributes for Errors retrieval)
	 */
	public JspAwareRequestContext(PageContext pageContext, Map model) {
		initContext(pageContext, model);
	}

	/**
	 * Initialize this context with the given page context,
	 * using the given model attributes for Errors retrieval.
	 * @param pageContext current JSP page context
	 * @param model the model attributes for the current view
	 * (can be <code>null</code>, using the request attributes for Errors retrieval)
	 */
	protected void initContext(PageContext pageContext, Map model) {
		if (!(pageContext.getRequest() instanceof HttpServletRequest)) {
			throw new IllegalArgumentException("RequestContext only supports HTTP requests");
		}
		this.pageContext = pageContext;
		initContext((HttpServletRequest) pageContext.getRequest(), pageContext.getServletContext(), model);
	}


	/**
	 * Return the underlying PageContext.
	 * Only intended for cooperating classes in this package.
	 */
	protected PageContext getPageContext() {
		return pageContext;
	}

	/**
	 * This implementation looks for a JSTL locale attribute in the
	 * JSP page scope, falling back to the superclass if not found.
	 * @see RequestContext#getFallbackLocale
	 */
	protected Locale getFallbackLocale() {
		Locale locale = (Locale) getPageContext().getAttribute(JSTL_LOCALE_ATTRIBUTE);
		if (locale == null) {
			locale = (Locale) getPageContext().getAttribute(JSTL_LOCALE_ATTRIBUTE + PAGE_SCOPE_SUFFIX);
			if (locale == null) {
				locale = super.getFallbackLocale();
			}
		}
		return locale;
	}

}
