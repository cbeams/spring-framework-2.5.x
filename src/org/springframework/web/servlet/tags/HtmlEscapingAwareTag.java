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

package org.springframework.web.servlet.tags;

import javax.servlet.jsp.JspException;

import org.springframework.web.util.ExpressionEvaluationUtils;

/**
 * Superclass for tags that emit content that might get HTML-escaped.
 *
 * <p>Provides a "htmlEscape" property for explicitly specifying whether to
 * apply HTML escaping. If not set, a page-level default is used (e.g. from
 * the HtmlEscapeTag), respectively an application-wide default (the
 * "defaultHtmlEscape" context-param in web.xml).
 *
 * @author Juergen Hoeller
 * @since 24.07.2004
 * @see #setHtmlEscape
 * @see HtmlEscapeTag
 * @see org.springframework.web.servlet.support.RequestContext#isDefaultHtmlEscape
 * @see org.springframework.web.util.WebUtils#isDefaultHtmlEscape
 */
public abstract class HtmlEscapingAwareTag extends RequestContextAwareTag {

	private Boolean htmlEscape;

	/**
	 * Set HTML escaping for this tag, as boolean value.
	 * Overrides the default HTML escaping setting for the current page.
	 * @see HtmlEscapeTag#setDefaultHtmlEscape
	 */
	public void setHtmlEscape(String htmlEscape) throws JspException {
		this.htmlEscape =
				new Boolean(ExpressionEvaluationUtils.evaluateBoolean("htmlEscape", htmlEscape, pageContext));
	}

	/**
	 * Return the HTML escaping setting for this tag,
	 * or the default setting if not overridden.
	 */
	protected boolean isHtmlEscape() {
		if (this.htmlEscape != null) {
			return this.htmlEscape.booleanValue();
		}
		else {
			return getRequestContext().isDefaultHtmlEscape();
		}
	}

}
