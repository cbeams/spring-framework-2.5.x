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
import org.springframework.util.StringUtils;
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

	private String code;

	private String arguments;

	private String text;
	
	private String var;
	
	private String scope = TagUtils.SCOPE_PAGE;

	/**
	 * Set the message code for this tag.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Set optional message arguments for this tag,
	 * as comma-delimited list of Strings.
	 */
	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	/**
	 * Set the message text for this tag.
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * Set PageContext attribute name under which to expose
	 * a variable that contains the resolved message.
	 * @see #setScope
	 * @see javax.servlet.jsp.PageContext#setAttribute
	 */
	public void setVar(String var) {
		this.var = var;
	}
	
	/**
	 * Set the scope to export the variable to.
	 * Default is SCOPE_PAGE ("page").
	 * @see #setVar
	 * @see org.springframework.web.util.TagUtils#SCOPE_PAGE
	 * @see javax.servlet.jsp.PageContext#setAttribute
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	protected final int doStartTagInternal() throws JspException, IOException {
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
				String resolvedArguments = ExpressionEvaluationUtils.evaluateString("arguments", this.arguments, pageContext);
				String[] argumentsArray = StringUtils.commaDelimitedListToStringArray(resolvedArguments);
				if (resolvedText != null) {
					msg = messageSource.getMessage(resolvedCode, argumentsArray, resolvedText,
					                               getRequestContext().getLocale());
				}
				else {
					msg = messageSource.getMessage(resolvedCode, argumentsArray,
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
