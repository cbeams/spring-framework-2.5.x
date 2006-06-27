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

import org.springframework.util.Assert;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.util.TagUtils;

/**
 * JSP tag for rendering an HTML '<code>option</code>' tag. Must be used nested
 * inside a {@link SelectTag}. Provides full support for databinding by marking
 * an '<code>option</code>' as 'selected' if the {@link #setValue(String) value}
 * matches the value bound to the out {@link SelectTag}.
 *
 * <p>The {@link #setValue(String) value} property is required and corresponds to
 * the '<code>value</code>' attribute of the rendered '<code>option</code>'.
 *
 * <p>An optional {@link #setLabel label} property can be specified, the value of
 * which corresponds to inner text of the rendered '<code>option</code>' tag.
 * If no {@link #setLabel label} is specified then the {@link #setValue(String) value}
 * property will be used when rendering the inner text.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class OptionTag extends AbstractFormTag {

	/**
	 * The 'value' attribute of the rendered HTML <code>&lt;option&gt;</code> tag.
	 */
	private Object value;

	/**
	 * The text body of the rendered HTML <code>&lt;option&gt;</code> tag.
	 */
	private String label;


	/**
	 * Sets the 'value' attribute of the rendered HTML <code>&lt;option&gt;</code> tag.
	 * May be a runtime expression.
	 */
	public void setValue(Object value) {
		Assert.notNull(value, "'value' cannot be null.");
		this.value = value;
	}

	/**
	 * Gets the 'value' attribute of the rendered HTML <code>&lt;option&gt;</code> tag.
	 * May be a runtime expression.
	 */
	protected Object getValue() {
		return this.value;
	}

	/**
	 * Sets the text body of the rendered HTML <code>&lt;option&gt;</code> tag.
	 * May be a runtime expression.
	 */
	public void setLabel(String label) {
		Assert.notNull(label, "'label' cannot be null.");
		this.label = label;
	}

	/**
	 * Gets the text body of the rendered HTML <code>&lt;option&gt;</code> tag.
	 * May be a runtime expression.
	 */
	protected String getLabel() {
		return this.label;
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
		assertUnderSelectTag();
		tagWriter.startTag("option");

		Object resolvedValue = evaluate("value", getValue());
		String renderedValue = getDisplayString(resolvedValue, getBindStatus().getEditor());
		String labelValue = getLabelValue(resolvedValue);

		if (!renderedValue.equals(labelValue)) {
			tagWriter.writeAttribute("value", renderedValue);
		}

		if (isSelected(resolvedValue)) {
			tagWriter.writeAttribute("selected", "selected");
		}
		tagWriter.appendValue(labelValue);

		tagWriter.endTag();

		return EVAL_PAGE;
	}

	/**
	 * Returns the value of the label for this '<code>option</code>' element.
	 * If the {@link #setLabel label} property is set then the resolved value
	 * of that property is used, otherwise the value of the <code>resolvedValue</code>
	 * argument is used.
	 */
	private String getLabelValue(Object resolvedValue) throws JspException {
		String label = getLabel();
		Object labelObj = (label == null ? resolvedValue : evaluate("label", label));
		return  getDisplayString(labelObj, getBindStatus().getEditor());
	}

	private void assertUnderSelectTag() {
		if (!TagUtils.hasAncestorOfType(this, SelectTag.class)) {
			throw new IllegalStateException("The 'option' tag can only be used inside a valid 'select' tag.");
		}
	}

	private boolean isSelected(Object resolvedValue) {
		return SelectedValueComparator.isSelected(getBindStatus(), resolvedValue);
	}


	private BindStatus getBindStatus() {
	 return  (BindStatus) this.pageContext.getAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE);
	}

}
