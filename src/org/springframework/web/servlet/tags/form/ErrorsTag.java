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

import org.springframework.util.ObjectUtils;

import javax.servlet.jsp.JspException;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class ErrorsTag extends AbstractHtmlElementTag {

	/**
	 * The HTML '<code>span</code>' tag.
	 */
	public static final String SPAN_TAG = "span";

	private String delimiter = "<br/>";

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	protected boolean shouldRender() throws JspException {
		return getBindStatus().isError();
	}

	protected String getName() throws JspException {
		return getPath() + ".errors";
	}

	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		if (shouldRender()) {
			tagWriter.startTag(SPAN_TAG);
			writeDefaultAttributes(tagWriter);
			String delimiter = ObjectUtils.nullSafeToString(evaluate("delimiter", this.delimiter));
			String[] errorMessages = getBindStatus().getErrorMessages();
			for (int i = 0; i < errorMessages.length; i++) {
				String errorMessage = errorMessages[i];
				if (i > 0) {
					tagWriter.appendValue(delimiter);
				}
				tagWriter.appendValue(errorMessage);
			}
			tagWriter.endTag();
		}
		return EVAL_PAGE;
	}
}
