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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.util.ExpressionEvaluationUtils;

/**
 * Superclass for all tags that require a RequestContext.
 * The RequestContext instance provides easy access to current
 * state like WebApplicationContext, Locale, Theme, etc.
 *
 * <p>Supports an HTML escaping setting per tag instance,
 * overriding any default setting at the page or web.xml level.
 *
 * <p>Note: Only intended for DispatcherServlet requests!
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.support.RequestContext
 * @see HtmlEscapeTag#setDefaultHtmlEscape
 * @see HtmlEscapeTag#HTML_ESCAPE_CONTEXT_PARAM
 */
public abstract class RequestContextAwareTag extends TagSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	private String htmlEscape;

	private RequestContext requestContext;

	/**
	 * Sets HTML escaping for this tag, overriding the default
	 * HTML escaping setting for the current page.
	 * @see HtmlEscapeTag#setDefaultHtmlEscape
	 */
	public final void setHtmlEscape(String htmlEscape) throws JspException {
		this.htmlEscape =	htmlEscape;
	}

	/**
	 * Returns the HTML escaping setting for this tag,
	 * or the default setting if not overridden.
	 */
	protected final boolean isHtmlEscape() throws JspException {
		if (this.htmlEscape != null) {
			return ExpressionEvaluationUtils.evaluateBoolean("htmlEscape", this.htmlEscape, pageContext);
		}
		else {
			return HtmlEscapeTag.isDefaultHtmlEscape(this.pageContext);
		}
	}

	/**
	 * Return the current RequestContext.
	 */
	protected final RequestContext getRequestContext() {
		return requestContext;
	}

	/**
	 * Create and set the current RequestContext.
	 * Delegates to doStartTagInternal for actual work.
	 */
	public final int doStartTag() throws JspException {
		try {
			this.requestContext =	new RequestContext((HttpServletRequest) this.pageContext.getRequest());
			return doStartTagInternal();
		}
		catch (JspException ex) {
			throw ex;
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			pageContext.getServletContext().log("Exception in custom tag", ex);
			throw new JspTagException(ex.getMessage());
		}
	}

	/**
	 * Called by doStartTag to perform the actual work.
	 * @return same as TagSupport.doStartTag
	 * @throws Exception any exception, any checked one other than
	 * a JspException gets wrapped in a JspException by doStartTag
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag
	 */
	protected int doStartTagInternal() throws Exception {
		return super.doStartTag();
	}

}
