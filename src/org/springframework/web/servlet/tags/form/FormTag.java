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

package org.springframework.web.servlet.tags.form;

import javax.servlet.jsp.JspException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Data-binding aware JSP tag for rendering an HTML '<code>form</code>' whose
 * inner elements are bound to properties on a {@link #setCommandName command object}.
 *
 * <p>Users should place the command object into the {@link org.springframework.web.servlet.ModelAndView}
 * when populating the data for their view. The name of this command object can
 * be configured using the {@link #setCommandName commandName} property.
 *
 * <p>The default value for the {@link #setCommandName commandName} property is
 * '<code>command</code>' which corresponds to the default name when using the
 * {@link org.springframework.web.servlet.mvc.SimpleFormController}.
 *
 * <p>Inner tags can access the name of the command object via the
 * {@link javax.servlet.jsp.PageContext}. The attribute name is defined in
 * {@link #COMMAND_NAME_VARIABLE_NAME}.
 *
 * @author Rob Harrop
 * @since 2.0
 * @see org.springframework.web.servlet.mvc.SimpleFormController
 */
public class FormTag extends AbstractHtmlElementTag {

	/**
	 * The name of the {@link javax.servlet.jsp.PageContext} attribute under which the
	 * command object name is exposed.
	 */
	public static final String COMMAND_NAME_VARIABLE_NAME = "org.springframework.web.servlet.tags.form.FormTag.commandName";

	/**
	 * The default HTTP method using which form values are sent to the server: "post".
	 */
	private static final String DEFAULT_METHOD = "post";

	/**
	 * The default command object name: "command".
	 */
	public static final String DEFAULT_COMMAND_NAME = "command";

	/**
	 * The name of the '<code>commandName</code>' attribute.
	 */
	public static final String COMMAND_NAME_ATTRIBUTE = "commandName";

	/**
	 * The name of the '<code>name</code>' attribute.
	 */
	public static final String NAME_ATTRIBUTE = "name";

	/**
	 * The name of the '<code>onsubmit</code>' attribute.
	 */
	public static final String ONSUBMIT_ATTRIBUTE = "onsubmit";

	/**
	 * The name of the '<code>onreset</code>' attribute.
	 */
	public static final String ONRESET_ATTRIBUTE = "onreset";

	/**
	 * The name of the '<code>method</code>' attribute.
	 */
	public static final String METHOD_ATTRIBUTE = "method";

	/**
	 * The name of the '<code>action</code>' attribute.
	 */
	public static final String ACTION_ATTRIBUTE = "action";

	/**
	 * The name of the '<code>enctype</code>' attribute.
	 */
	public static final String ENCTYPE_ATTRIBUTE = "enctype";


	/**
	 * The {@link TagWriter} instance used by this tag.
	 */
	private TagWriter tagWriter;

	/**
	 * The name of the command object.
	 */
	private String commandName = DEFAULT_COMMAND_NAME;

	/**
	 * The value of the '<code>name</code>' attribute.
	 */
	private String name;

	/**
	 * The value of the '<code>action</code>' attribute.
	 */
	private String action;

	/**
	 * The value of the '<code>method</code>' attribute.
	 */
	private String method = DEFAULT_METHOD;

	/**
	 * The value of the '<code>enctype</code>' attribute.
	 */
	private String enctype;

	/**
	 * The value of the '<code>onsubmit</code>' attribute.
	 */
	private String onsubmit;

	/**
	 * The value of the '<code>onreset</code>' attribute.
	 */
	private String onreset;


	/**
	 * Sets the name of the command object.
	 * May be a runtime expression.
	 */
	public void setCommandName(String commandName) {
		Assert.notNull(commandName, "'commandName' cannot be null");
		this.commandName = commandName;
	}

	/**
	 * Gets the value of the '<code>commandName</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getCommandName() {
		return this.commandName;
	}

	/**
	 * Sets the value of the '<code>name</code>' attribute.
	 * Defaults to the value of {@link #resolveCommandName} if
	 * not specified.
	 * May be a runtime expression.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the value of the '<code>name</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getName() {
		return this.name;
	}

	/**
	 * Sets the value of the '<code>action</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setAction(String action) {
		this.action = (action != null ? action : "");
	}

	/**
	 * Gets the value of the '<code>action</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getAction() {
		return this.action;
	}

	/**
	 * Sets the value of the '<code>method</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setMethod(String method) {
		Assert.hasText(method, "'method' cannot be null or zero length");
		this.method = method;
	}

	/**
	 * Gets the value of the '<code>method</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getMethod() {
		return this.method;
	}

	/**
	 * Sets the value of the '<code>enctype</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setEnctype(String enctype) {
		Assert.hasText(enctype, "'enctype' cannot be null or zero length");
		this.enctype = enctype;
	}

	/**
	 * Gets the value of the '<code>enctype</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getEnctype() {
		return this.enctype;
	}

	/**
	 * Sets the value of the '<code>onsubmit</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setOnsubmit(String onsubmit) {
		Assert.hasText(onsubmit, "'onsubmit' cannot be null or zero length");
		this.onsubmit = onsubmit;
	}

	/**
	 * Gets the value of the '<code>onsubmit</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getOnsubmit() {
		return this.onsubmit;
	}

	/**
	 * Sets the value of the '<code>onreset</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setOnreset(String onreset) {
		Assert.hasText(onreset, "'onreset' cannot be null or zero length");
		this.onreset = onreset;
	}

	/**
	 * Gets the value of the '<code>onreset</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getOnreset() {
		return this.onreset;
	}


	/**
	 * Writes the opening part of the block	'<code>form</code>' tag and exposes
	 * the command name in the {@link javax.servlet.jsp.PageContext}.
	 */
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		this.tagWriter = tagWriter;
		this.tagWriter.startTag("form");
		writeDefaultAttributes(tagWriter);
		this.tagWriter.writeAttribute(METHOD_ATTRIBUTE, getDisplayString(evaluate(METHOD_ATTRIBUTE, this.method)));
		writeOptionalAttribute(tagWriter, NAME_ATTRIBUTE, resolveName());
		this.tagWriter.writeAttribute(ACTION_ATTRIBUTE, resolveAction());
		writeOptionalAttribute(tagWriter, ENCTYPE_ATTRIBUTE, this.enctype);
		writeOptionalAttribute(tagWriter, ONSUBMIT_ATTRIBUTE, this.onsubmit);
		writeOptionalAttribute(tagWriter, ONRESET_ATTRIBUTE, this.onreset);

		this.tagWriter.forceBlock();

		// expose the command name for nested tags
		this.pageContext.setAttribute(COMMAND_NAME_VARIABLE_NAME, resolveCommandName());
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Resolves the value of the '<code>name</code>' attribute. If the user specifies a value for
	 * '<code>name</code>' then this is used. Otherwise the value returned by {@link #resolveCommandName()}
	 * is used.
	 */
	private String resolveName() throws JspException {
		String name = getName();
		if(StringUtils.hasText(name)) {
			return ObjectUtils.getDisplayString(evaluate(NAME_ATTRIBUTE, name));
		} else {
			return resolveCommandName();
		}
	}


	/**
	 * Resolve the value of the '<code>action</code>' attribute. If the user configured
	 * an '<code>action</code>' value then the result of evaluating this value is used.
	 * Otherwise, the {@link org.springframework.web.servlet.support.RequestContext#getRequestUri() originating URI}
	 * is used.
	 */
	private String resolveAction() throws JspException {
		if (StringUtils.hasText(this.action)) {
			return ObjectUtils.getDisplayString(evaluate(ACTION_ATTRIBUTE, this.action));
		}
		else {
			String requestUri = getRequestContext().getRequestUri();
			ServletResponse response = this.pageContext.getResponse();
			if (response instanceof HttpServletResponse) {
				requestUri = ((HttpServletResponse) response).encodeURL(requestUri);
			}
			if (StringUtils.hasText(requestUri)) {
				return requestUri;
			}
			else {
				throw new IllegalArgumentException("Attribute 'action' is required. Attempted to resolve " +
								"against current request URI but request URI was null.");
			}
		}
	}

	/**
	 * {@link #evaluate Resolves} and returns the name of the command object.
	 * @throws IllegalArgumentException if the command object resolves to null.
	 */
	protected String resolveCommandName() throws JspException {
		Object resolvedCommmandName = evaluate(COMMAND_NAME_ATTRIBUTE, this.commandName);
		if (resolvedCommmandName == null) {
			throw new IllegalArgumentException("'commandName' cannot be null");
		}
		return (String) resolvedCommmandName;
	}

	/**
	 * Closes the '<code>form</code>' block tag and removes the command name
	 * from the {@link javax.servlet.jsp.PageContext}.
	 */
	public int doEndTag() throws JspException {
		this.tagWriter.endTag();
		this.pageContext.removeAttribute(COMMAND_NAME_VARIABLE_NAME);
		return EVAL_PAGE;
	}


	/**
	 * Override resolve CSS class since error class is not supported.
	 */
	protected String resolveCssClass() throws JspException {
		return ObjectUtils.getDisplayString(evaluate("class", getCssClass()));
	}

	/**
	 * Clears the stored {@link TagWriter}.
	 */
	public void doFinally() {
		super.doFinally();
		this.tagWriter = null;
	}

	/**
	 * Unsupported for forms.
	 */
	public void setPath(String path) {
		throw new UnsupportedOperationException("The 'path' attribute is not supported for forms.");
	}

	/**
	 * Unsupported for forms.
	 */
	public void setCssErrorClass(String cssErrorClass) {
		throw new UnsupportedOperationException("The 'cssErrorClass' attribute is not supported for forms.");
	}

}
