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
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.util.ExpressionEvaluationUtils;

import javax.servlet.jsp.JspException;

/**
 * @author Rob Harrop
 */
public abstract class AbstractDataBoundFormElementTag extends AbstractFormElementTag {

	public static final String ID_ATTRIBUTE = "id";

	private BindStatus bindStatus;

	private String id;

	private String path;

	public void setPath(String path) {
		Assert.hasText(path, "'path' cannot be null or zero length.");
		this.path = path;
	}

	public void setId(String id) {
		Assert.notNull(id, "'id' cannot be null.");
		this.id = id;
	}

	protected String getPath() throws JspException {
		return (String) ExpressionEvaluationUtils.evaluate("path", this.path, this.pageContext);
	}

	protected void writeDefaultAttributes(TagWriter tagWriter) throws JspException {
		writeOptionalAttribute(tagWriter, ID_ATTRIBUTE, this.id);
		tagWriter.writeAttribute("name", getName());
	}

	protected String getName() throws JspException {
		return getPath();
	}

	protected final Object getValue() throws JspException {
		return getBindStatus().getValue();
	}

	protected BindStatus getBindStatus() throws JspException {
		if (this.bindStatus == null) {
			String resolvedPropertyPath = getPath();
			String bindPath = getBindPath(resolvedPropertyPath);
			this.bindStatus = new BindStatus(getRequestContext(), bindPath, this.isHtmlEscape());
		}
		return this.bindStatus;
	}

	private String getBindPath(String resolvedSubPath) {
		return getCommandName() + "." + resolvedSubPath;
	}

	private String getCommandName() {
		return (String) this.pageContext.getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME);
	}

	public void doFinally() {
		super.doFinally();
		this.bindStatus = null;
	}

}
