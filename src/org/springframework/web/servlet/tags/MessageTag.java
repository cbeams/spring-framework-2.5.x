/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.util.ExpressionEvaluationUtils;
import org.springframework.web.util.HtmlUtils;

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
	
	private int scope = PageContext.PAGE_SCOPE;

	/**
	 * Set the message code for this tag.
	 */
	public final void setCode(String code) throws JspException {
		this.code = ExpressionEvaluationUtils.evaluateString("code", code, pageContext);
	}

	/**
	 * Set the message text for this tag.
	 */
	public final void setText(String text) throws JspException {
		this.text = ExpressionEvaluationUtils.evaluateString("text", text, pageContext);
	}
	
	/**
	 * Set othe var String under which to bind the variable.
	 */
	public final void setVar(String var)  throws JspException {
		this.var = ExpressionEvaluationUtils.evaluateString("var", var, pageContext);
	}
	
	/**
	 * Set the scope to export the var to.
	 */
	public final void setScope(String scope) throws JspException {
		String tmpScope = ExpressionEvaluationUtils.evaluateString("scope", this.var, pageContext);
		this.scope = TagUtils.getScope(tmpScope);
	}

	protected final int doStartTagInternal() throws Exception {
		MessageSource messageSource = getMessageSource();
		if (messageSource == null) {
			throw new JspTagException("No corresponding MessageSource found");
		}
		try {
			String msg = null;
			if (this.code != null) {
				if (this.text != null) {
					msg = messageSource.getMessage(this.code, null, this.text,
					                               getRequestContext().getLocale());
				}
				else {
					msg = messageSource.getMessage(this.code, null,
					                               getRequestContext().getLocale());
				}
			}
			else {
				msg = this.text;
			}
			msg = isHtmlEscape() ? HtmlUtils.htmlEscape(msg) : msg;
			if (this.var == null) {
				writeMessage(msg);
			}
			else {
				pageContext.setAttribute(var, msg, scope);				
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
		this.var = null;
		this.scope = PageContext.PAGE_SCOPE;
		this.code = null;
		this.text = null;
	}

}
