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
public class TextareaTag extends AbstractHtmlInputElementTag {

	public static final String ROWS_ATTRIBUTE = "rows";

	public static final String COLS_ATTRIBUTE = "cols";

	public static final String ONSELECT_ATTRIBUTE = "onselect";

	private String rows;

	private String cols;

	private String onselect;

	public void setRows(String rows) {
		this.rows = rows;
	}

	public void setCols(String cols) {
		this.cols = cols;
	}

	public void setOnselect(String onselect) {
		this.onselect = onselect;
	}

	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("textarea");
		writeDefaultAttributes(tagWriter);
		writeOptionalAttribute(tagWriter, ROWS_ATTRIBUTE, this.rows);
		writeOptionalAttribute(tagWriter, COLS_ATTRIBUTE, this.cols);
		writeOptionalAttribute(tagWriter, ONSELECT_ATTRIBUTE, this.onselect);
		tagWriter.appendValue(ObjectUtils.nullSafeToString(getValue()));
		tagWriter.endTag();
		return EVAL_PAGE;
	}

}
