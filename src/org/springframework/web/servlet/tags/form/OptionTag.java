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
public class OptionTag extends AbstractFormElementTag {

	/**
	 * The 'value' attribute of the rendered HTML <code>&lt;option&gt;</code> tag.
	 */
	private String value;

	/**
	 * The text body of the rendered HTML <code>&lt;option&gt;</code> tag.
	 */
	private String label;

	/**
	 * Sets the 'value' attribute of the rendered HTML <code>&lt;option&gt;</code> tag.
	 * May be a runtime expression.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Sets the text body of the rendered HTML <code>&lt;option&gt;</code> tag.
	 * May be a runtime expression.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns <code>option</code>.
	 */
	protected String getTagName() {
		return "option";
	}

	/**
	 * Renders an HTML <code>&lt;option&gt;</code> using configured
	 * {@link #setValue value} and {@link #setLabel label} values.
	 * <p/>
	 * If the resolved value is equal to the value exposed by the containing
	 * {@link SelectTag} then the '<code>selected</code>' attribute is written
	 * with the value of <code>true</code>.
	 */
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("option");

		Object selectedValue = getSelectedValue();

		Object resolvedValue = evaluate("value", this.value);
		String resolvedLabel = ObjectUtils.nullSafeToString(evaluate("label", this.label));

		tagWriter.writeAttribute("value", ObjectUtils.nullSafeToString(resolvedValue));
		if (selectedValue.equals(resolvedValue)) {
			tagWriter.writeAttribute("selected", "true");
		}
		tagWriter.appendValue(resolvedLabel);

		tagWriter.endTag();

		return EVAL_PAGE;
	}

	private Object getSelectedValue() {
		Object selectedValue = this.pageContext.getAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE);
		if (selectedValue == null) {
			throw new IllegalStateException("The 'option' tag can only be used inside a valid 'select' tag.");
		}
		return selectedValue;
	}
}
