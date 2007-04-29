/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.web.servlet.tags.form;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * Databinding-aware JSP tag for rendering an HTML '<code>form</code>' whose
 * inner elements are bound to properties on a {@link #setCommandName command object}.
 * 
 * <p>Users should place the command object into the
 * {@link org.springframework.web.servlet.ModelAndView} when populating the
 * data for their view. The name of this command object can be configured
 * using the {@link #setCommandName commandName} property.
 * 
 * <p>The default value for the {@link #setCommandName commandName} property
 * is '<code>command</code>' which corresponds to the default name when using
 * the {@link org.springframework.web.servlet.mvc.SimpleFormController}.
 * 
 * <p>Inner tags can access the name of the command object via the
 * {@link javax.servlet.jsp.PageContext}. The attribute name is defined in
 * {@link #COMMAND_NAME_VARIABLE_NAME}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.web.servlet.mvc.SimpleFormController
 */
public class FormTag extends AbstractHtmlElementTag {

	/** The default HTTP method using which form values are sent to the server: "post" */
	private static final String DEFAULT_METHOD = "post";

	/** The default command object name: "command" */
	public static final String DEFAULT_COMMAND_NAME = "command";

	public static final String NAME_ATTRIBUTE = "name";

	public static final String ONSUBMIT_ATTRIBUTE = "onsubmit";

	public static final String ONRESET_ATTRIBUTE = "onreset";

	public static final String METHOD_ATTRIBUTE = "method";

	public static final String ACTION_ATTRIBUTE = "action";

	public static final String ENCTYPE_ATTRIBUTE = "enctype";


	private TagWriter tagWriter;

	private String commandName = DEFAULT_COMMAND_NAME;

	private String name;

	private String action;

	private String method = DEFAULT_METHOD;

	private String enctype;

	private String onsubmit;

	private String onreset;


	/**
	 * Set the name of the command object.
	 * <p>May be a runtime expression.
	 */
	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	/**
	 * Get the value of the '<code>commandName</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getCommandName() {
		return this.commandName;
	}

	/**
	 * Set the value of the '<code>name</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the value of the '<code>action</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setAction(String action) {
		this.action = (action != null ? action : "");
	}

	/**
	 * Get the value of the '<code>action</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getAction() {
		return this.action;
	}

	/**
	 * Set the value of the '<code>method</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Get the value of the '<code>method</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getMethod() {
		return this.method;
	}

	/**
	 * Set the value of the '<code>enctype</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setEnctype(String enctype) {
		this.enctype = enctype;
	}

	/**
	 * Get the value of the '<code>enctype</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getEnctype() {
		return this.enctype;
	}

	/**
	 * Set the value of the '<code>onsubmit</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setOnsubmit(String onsubmit) {
		this.onsubmit = onsubmit;
	}

	/**
	 * Get the value of the '<code>onsubmit</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getOnsubmit() {
		return this.onsubmit;
	}

	/**
	 * Set the value of the '<code>onreset</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setOnreset(String onreset) {
		this.onreset = onreset;
	}

	/**
	 * Get the value of the '<code>onreset</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getOnreset() {
		return this.onreset;
	}


	/**
	 * Writes the opening part of the block	'<code>form</code>' tag and exposes
	 * the command name in the {@link javax.servlet.jsp.PageContext}.
	 * @param tagWriter the {@link TagWriter} to which the form content is to be written
	 * @return {@link javax.servlet.jsp.tagext.Tag#EVAL_BODY_INCLUDE}
	 */
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		this.tagWriter = tagWriter;
		this.tagWriter.startTag("form");
		writeDefaultAttributes(tagWriter);
		this.tagWriter.writeAttribute(METHOD_ATTRIBUTE, getDisplayString(evaluate(METHOD_ATTRIBUTE, getMethod())));
		this.tagWriter.writeAttribute(ACTION_ATTRIBUTE, resolveAction());
		writeOptionalAttribute(tagWriter, ENCTYPE_ATTRIBUTE, getEnctype());
		writeOptionalAttribute(tagWriter, ONSUBMIT_ATTRIBUTE, getOnsubmit());
		writeOptionalAttribute(tagWriter, ONRESET_ATTRIBUTE, getOnreset());

		this.tagWriter.forceBlock();

		// expose the command name for nested tags
		this.pageContext.setAttribute(COMMAND_NAME_VARIABLE_NAME, resolveCommandName(), PageContext.REQUEST_SCOPE);
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Name is not a valid attribute for form on XHTML 1.0. However,
	 * it is sometimes needed for backward compatibility.
	 */
	protected String getName() throws JspException {
		return this.name;
	}

	/**
	 * Autogenerated IDs correspond to the command name.
	 */
	protected String autogenerateId() throws JspException {
		return resolveCommandName();
	}

	/**
	 * {@link #evaluate Resolves} and returns the name of the command object.
	 * @throws IllegalArgumentException if the command object resolves to <code>null</code>
	 */
	protected String resolveCommandName() throws JspException {
		Object resolvedCommmandName = evaluate(COMMAND_NAME_ATTRIBUTE, getCommandName());
		if (resolvedCommmandName == null) {
			throw new IllegalArgumentException("'commandName' must not be null");
		}
		return (String) resolvedCommmandName;
	}

	/**
	 * Resolve the value of the '<code>action</code>' attribute.
	 * <p>If the user configured an '<code>action</code>' value then
	 * the result of evaluating this value is used. Otherwise, the
	 * {@link org.springframework.web.servlet.support.RequestContext#getRequestUri() originating URI}
	 * is used.
	 * @return the value that is to be used for the '<code>action</code>' attribute
	 */
	protected String resolveAction() throws JspException {
		String action = getAction();
		if (StringUtils.hasText(action)) {
			return getDisplayString(evaluate(ACTION_ATTRIBUTE, action));
		}
		else {
			String requestUri = getRequestContext().getRequestUri();
			ServletResponse response = this.pageContext.getResponse();
			if (response instanceof HttpServletResponse) {
				requestUri = ((HttpServletResponse) response).encodeURL(requestUri);
				String queryString = getRequestContext().getQueryString();
				if (StringUtils.hasText(queryString)) {
					requestUri += "?" + HtmlUtils.htmlEscape(queryString);
				}
			}
			if (StringUtils.hasText(requestUri)) {
				return requestUri;
			}
			else {
				throw new IllegalArgumentException("Attribute 'action' is required. Attempted to resolve " +
						"against current request URI but request URI was null");
			}
		}
	}


	/**
	 * Closes the '<code>form</code>' block tag and removes the command name
	 * from the {@link javax.servlet.jsp.PageContext}.
	 */
	public int doEndTag() throws JspException {
		this.tagWriter.endTag();
		this.pageContext.getRequest().removeAttribute(COMMAND_NAME_VARIABLE_NAME);
		return EVAL_PAGE;
	}

	/**
	 * Clears the stored {@link TagWriter}.
	 */
	public void doFinally() {
		super.doFinally();
		this.tagWriter = null;
	}


	/**
	 * Override resolve CSS class since error class is not supported.
	 */
	protected String resolveCssClass() throws JspException {
		return ObjectUtils.getDisplayString(evaluate("class", getCssClass()));
	}

	/**
	 * Unsupported for forms.
	 * @throws UnsupportedOperationException always
	 */
	public void setPath(String path) {
		throw new UnsupportedOperationException("The 'path' attribute is not supported for forms");
	}

	/**
	 * Unsupported for forms.
	 * @throws UnsupportedOperationException always
	 */
	public void setCssErrorClass(String cssErrorClass) {
		throw new UnsupportedOperationException("The 'cssErrorClass' attribute is not supported for forms");
	}

}
