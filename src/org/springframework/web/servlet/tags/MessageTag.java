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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.util.ExpressionEvaluationUtils;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.TagUtils;

/**
 * Custom tag to look up a message in the scope of this page.
 * Messages are looked up using the ApplicationContext, and thus should
 * support internationalization.
 *
 * <p>Regards a HTML escaping setting, either on this tag instance,
 * the page level, or the web.xml level.
 *
 * <p>If "code" isn't set or cannot be resolved, "text" will be used as default
 * message. Thus, this tag can also be used for HTML escaping of any texts.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setCode
 * @see #setText
 * @see #setHtmlEscape
 * @see HtmlEscapeTag#setDefaultHtmlEscape
 * @see HtmlEscapeTag#HTML_ESCAPE_CONTEXT_PARAM
 */
public class MessageTag extends RequestContextAwareTag {

	private String code = null;

	private String text = null;
	
	private String var = null;
	
	private String scope = TagUtils.SCOPE_PAGE;

	/**
	 * Set the message code for this tag.
	 */
	public final void setCode(String code) throws JspException {
		this.code = code;
	}

	/**
	 * Set the message text for this tag.
	 */
	public final void setText(String text) throws JspException {
		this.text = text;
	}
	
	/**
	 * Set othe var String under which to bind the variable.
	 */
	public final void setVar(String var)  throws JspException {
		this.var = var;
	}
	
	/**
	 * Set the scope to export the var to.
	 */
	public final void setScope(String scope) throws JspException {
		this.scope = scope;
	}

	protected final int doStartTagInternal() throws Exception {
		MessageSource messageSource = getMessageSource();
		if (messageSource == null) {
			throw new JspTagException("No corresponding MessageSource found");
		}
		String resolvedCode = ExpressionEvaluationUtils.evaluateString("code", this.code, pageContext);
		String resolvedText = ExpressionEvaluationUtils.evaluateString("text", this.text, pageContext);
		String resolvedVar = ExpressionEvaluationUtils.evaluateString("var", this.var, pageContext);
		try {
			String msg = null;
			if (resolvedCode != null) {
				if (resolvedText != null) {
					msg = messageSource.getMessage(resolvedCode, null, resolvedText,
					                               getRequestContext().getLocale());
				}
				else {
					msg = messageSource.getMessage(resolvedCode, null,
					                               getRequestContext().getLocale());
				}
			}
			else {
				msg = resolvedText;
			}
			msg = isHtmlEscape() ? HtmlUtils.htmlEscape(msg) : msg;
			if (resolvedVar != null) {
				String resolvedScope = ExpressionEvaluationUtils.evaluateString("scope", this.scope, pageContext);
				pageContext.setAttribute(resolvedVar, msg, TagUtils.getScope(resolvedScope));
			}
			else {
				writeMessage(msg);
			}
		}
		catch (NoSuchMessageException ex) {
			throw new JspTagException(getNoSuchMessageExceptionDescription(ex));
		}
		return EVAL_BODY_INCLUDE;
	}

	protected void writeMessage(String msg) throws IOException {
		pageContext.getOut().write(msg);
	}

	/**
	 * Use the application context itself for default message resolution.
	 */
	protected MessageSource getMessageSource() {
		return getRequestContext().getWebApplicationContext();
	}

	/**
	 * Return default exception message.
	 */
	protected String getNoSuchMessageExceptionDescription(NoSuchMessageException ex) {
		return ex.getMessage();
	}
	
	public void release() {
		super.release();
		this.code = null;
		this.text = null;
		this.var = null;
		this.scope = TagUtils.SCOPE_PAGE;
	}

}
