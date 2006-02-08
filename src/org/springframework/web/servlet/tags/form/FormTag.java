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

import javax.servlet.jsp.JspException;


/**
 * @author Rob Harrop
 * @since 2.0
 */
public class FormTag extends AbstractFormTag {

	public static final String COMMAND_NAME_VARIABLE_NAME = "org.springframework.web.servlet.tags.form.FormTag.commandName";

	private static final String DEFAULT_METHOD = "POST";

	public static final String DEFAULT_COMMAND_NAME = "command";

	public static final String ONSUBMIT_ATTRIBUTE = "onsubmit";

	public static final String ONRESET_ATTRIBUTE = "onreset";

	public static final String METHOD_ATTRIBUTE = "method";

	public static final String ACTION_ATTRIBUTE = "action";

	public static final String ENCTYPE_ATTRIBUTE = "enctype";

	public static final String COMMAND_NAME_ATTRIBUTE = "commandName";

	private TagWriter tagWriter;

	private String commandName = DEFAULT_COMMAND_NAME;

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
		Assert.hasText(action, "'action' cannot be null or zero length.");
		this.action = action;
	}

	public void setMethod(String method) {
		Assert.hasText(method, "'method' cannot be null or zero length");
		this.method = method;
	}

	public void setEnctype(String enctype) {
		Assert.hasText(enctype, "'enctype' cannot be null or zero length.");
		this.enctype = enctype;
	}

	public void setOnsubmit(String onsubmit) {
		Assert.hasText(onsubmit, "'onsubmit' cannot be null or zero length.");
		this.onsubmit = onsubmit;
	}

	public void setOnreset(String onreset) {
		Assert.hasText(onreset, "'onreset' cannot be null or zero length.");
		this.onreset = onreset;
	}

	protected int doStartTagInternal() throws Exception {

		// write the tag
		this.tagWriter = createTagWriter();
		this.tagWriter.startTag("form");

		this.tagWriter.writeAttribute(METHOD_ATTRIBUTE,
						ObjectUtils.nullSafeToString(evaluate(METHOD_ATTRIBUTE, this.method)));
		writeOptionalAttribute(tagWriter, ACTION_ATTRIBUTE, this.action);
		writeOptionalAttribute(tagWriter, ENCTYPE_ATTRIBUTE, this.enctype);
		writeOptionalAttribute(tagWriter, ONSUBMIT_ATTRIBUTE, this.onsubmit);
		writeOptionalAttribute(tagWriter, ONRESET_ATTRIBUTE, this.onreset);

		this.tagWriter.forceBlock();

		// expose the command name for nested tags
		this.pageContext.setAttribute(COMMAND_NAME_VARIABLE_NAME, resolveCommandName());
		return EVAL_BODY_INCLUDE;
	}

	private String resolveCommandName() throws JspException {
		Object resolvedCommmandName = evaluate(COMMAND_NAME_ATTRIBUTE, this.commandName);
		if (resolvedCommmandName == null) {
			throw new IllegalArgumentException("'commandName' cannot be null.");
		}
		return (String) resolvedCommmandName;
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
