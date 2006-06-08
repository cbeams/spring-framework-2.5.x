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


/**
 * Databinding-aware JSP tag for rendering an HTML '<code>textarea</code>'.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class TextareaTag extends AbstractHtmlInputElementTag {

	/**
	 * The name of the '<code>rows</code>' attribute.
	 */
	public static final String ROWS_ATTRIBUTE = "rows";

	/**
	 * The name of the '<code>cols</code>' attribute.
	 */
	public static final String COLS_ATTRIBUTE = "cols";

	/**
	 * The name of the '<code>onselect</code>' attribute.
	 */
	public static final String ONSELECT_ATTRIBUTE = "onselect";


	/**
	 * The value of the '<code>rows</code>' attribute.
	 */
	private String rows;

	/**
	 * The value of the '<code>cols</code>' attribute.
	 */
	private String cols;

	/**
	 * The value of the '<code>onselect</code>' attribute.
	 */
	private String onselect;

	/**
	 * Sets the value of the '<code>rows</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setRows(String rows) {
		this.rows = rows;
	}

	/**
	 * Gets the value of the '<code>rows</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getRows() {
		return this.rows;
	}

	/**
	 * Sets the value of the '<code>cols</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setCols(String cols) {
		this.cols = cols;
	}

	/**
	 * Gets the value of the '<code>cols</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getCols() {
		return this.cols;
	}

	/**
	 * Sets the value of the '<code>onselect</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setOnselect(String onselect) {
		this.onselect = onselect;
	}

	/**
	 * Gets the value of the '<code>onselect</code>' attribute.
	 * May be a runtime expression.
	 */
	protected String getOnselect() {
		return this.onselect;
	}


	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("textarea");
		writeDefaultAttributes(tagWriter);
		writeOptionalAttribute(tagWriter, ROWS_ATTRIBUTE, getRows());
		writeOptionalAttribute(tagWriter, COLS_ATTRIBUTE, getCols());
		writeOptionalAttribute(tagWriter, ONSELECT_ATTRIBUTE, getOnselect());
		tagWriter.appendValue(getDisplayString(getBoundValue()));
		tagWriter.endTag();
		return EVAL_PAGE;
	}

}
