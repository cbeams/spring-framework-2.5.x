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

import org.springframework.util.ObjectUtils;

/**
 * Form tag for displaying errors for a particular field or object.
 * <p/>
 * This tag supports three main usage patterns:
 * <ol>
 *  <li>Field only - set '<code>path</code>' to the field name (or path)</li>
 *  <li>Object errors only - omit '<code>path</code>'</li>
 *  <li>All errors - set '<code>path</code>' to '<code>*</code>'</li>
 * </ol>
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class ErrorsTag extends AbstractHtmlElementTag {

	/** The HTML '<code>span</code>' tag */
	public static final String SPAN_TAG = "span";


	private String delimiter = "<br/>";

	/**
	 * What delimiter should be used between error messages. Default to an HTML
	 * '<code>&lt;br/&gt;</code>' tag.
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Only renders output when there are errors for the configured {@link #setPath path}.
	 */
	protected boolean shouldRender() throws JspException {
		return getBindStatus().isError();
	}

	/**
	 * Appends '<code>.errors</code>' to the value returned by {@link #getPath()}.
	 */
	protected String getName() throws JspException {
		return getPath() + ".errors";
	}

	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		if (shouldRender()) {
			tagWriter.startTag(SPAN_TAG);
			writeDefaultAttributes(tagWriter);
			String delimiter = ObjectUtils.getDisplayString(evaluate("delimiter", this.delimiter));
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
