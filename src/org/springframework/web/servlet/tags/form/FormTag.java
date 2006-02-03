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

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.tags.HtmlEscapingAwareTag;
import org.springframework.web.util.ExpressionEvaluationUtils;

import javax.servlet.jsp.JspException;


/**
 * @author Rob Harrop
 * @since 2.0
 */
public class FormTag extends HtmlEscapingAwareTag {

	public static final String COMMAND_NAME_VARIABLE_NAME = "commandName";

	private static final String DEFAULT_METHOD = "POST";

	public static final String ONSUBMIT_ATTRIBUTE = "onsubmit";

	public static final String ONRESET_ATTRIBUTE = "onreset";

	public static final String METHOD_ATTRIBUTE = "method";

	public static final String ACTION_ATTRIBUTE = "action";

	public static final String ENCTYPE_ATTRIBUTE = "enctype";

	private TagWriter tagWriter;

	private String commandName;

	private String action;

	private String method = DEFAULT_METHOD;

	private String enctype;

	private String onsubmit;

	private String onreset;

	public void setCommandName(String commandName) {
		Assert.notNull(commandName, "'commandName' cannot be null");
		this.commandName = commandName;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setMethod(String method) {
		Assert.hasText(method, "'method' cannot be null or zero length");
		this.method = method;
	}

	public void setEnctype(String enctype) {
		this.enctype = enctype;
	}

	public void setOnsubmit(String onsubmit) {
		this.onsubmit = onsubmit;
	}

	public void setOnreset(String onreset) {
		this.onreset = onreset;
	}

	protected int doStartTagInternal() throws Exception {

		// write the tag
		this.tagWriter = createTagWriter();
		this.tagWriter.startTag("form");
		
		this.tagWriter.writeAttribute(METHOD_ATTRIBUTE,
						ObjectUtils.nullSafeToString(evaluate(METHOD_ATTRIBUTE, this.method)));
		this.tagWriter.writeOptionalAttributeValue(ACTION_ATTRIBUTE,
						ObjectUtils.nullSafeToString(evaluate(ACTION_ATTRIBUTE, this.action)));
		this.tagWriter.writeOptionalAttributeValue(ENCTYPE_ATTRIBUTE,
						ObjectUtils.nullSafeToString(evaluate(ENCTYPE_ATTRIBUTE, this.enctype)));
		tagWriter.writeOptionalAttributeValue(ONSUBMIT_ATTRIBUTE,
						ObjectUtils.nullSafeToString(evaluate(ONSUBMIT_ATTRIBUTE, this.onsubmit)));
		tagWriter.writeOptionalAttributeValue(ONRESET_ATTRIBUTE,
						ObjectUtils.nullSafeToString(evaluate(ONRESET_ATTRIBUTE, this.onreset)));

		this.tagWriter.forceBlock();

		// expose the command name for nested tags
		this.pageContext.setAttribute(COMMAND_NAME_VARIABLE_NAME, this.commandName);
		return EVAL_BODY_INCLUDE;
	}

	protected TagWriter createTagWriter() {
		return new TagWriter(this.pageContext.getOut());
	}

	protected Object evaluate(String attributeName, String value) throws JspException {
		if (value == null) {
			return null;
		}
		return ExpressionEvaluationUtils.evaluate(attributeName, value, this.pageContext);
	}

	public int doEndTag() throws JspException {
		this.tagWriter.endTag();
		this.pageContext.removeAttribute(COMMAND_NAME_VARIABLE_NAME);
		return EVAL_PAGE;
	}

	public void doFinally() {
		super.doFinally();
		this.tagWriter = null;
	}
}
