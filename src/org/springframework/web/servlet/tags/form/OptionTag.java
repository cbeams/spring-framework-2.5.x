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
import javax.servlet.jsp.tagext.BodyContent;

import org.springframework.util.Assert;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.util.TagUtils;

/**
 * JSP tag for rendering an HTML '<code>option</code>' tag. Must be used nested
 * inside a {@link SelectTag}. Provides full support for databinding by marking
 * an '<code>option</code>' as 'selected' if the {@link #setValue value}
 * matches the value bound to the out {@link SelectTag}.
 *
 * <p>The {@link #setValue value} property is required and corresponds to
 * the '<code>value</code>' attribute of the rendered '<code>option</code>'.
 *
 * <p>An optional {@link #setLabel label} property can be specified, the value of
 * which corresponds to inner text of the rendered '<code>option</code>' tag.
 * If no {@link #setLabel label} is specified then the {@link #setValue value}
 * property will be used when rendering the inner text.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class OptionTag extends AbstractHtmlElementBodyTag {

	/**
	 * The name of the JSP variable used to expose the value for this tag.
	 */
	public static final String VALUE_VARIABLE_NAME = "value";

	/**
	 * The name of the JSP variable used to expose the display value for this tag.
	 */
	public static final String DISPLAY_VALUE_VARIABLE_NAME = "displayValue";

	/**
	 * The name of the '<code>selected</code>' attribute.
	 */
	private static final String SELECTED_ATTRIBUTE = "selected";

	/**
	 * The name of the '<code>value</code>' attribute.
	 */
	private static final String VALUE_ATTRIBUTE = VALUE_VARIABLE_NAME;


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

	protected void renderDefaultContent(TagWriter tagWriter) throws JspException {
		Object value = this.pageContext.getAttribute(VALUE_VARIABLE_NAME);
		String label = getLabelValue(value);
		renderOption(value,  label, tagWriter);
	}


	protected void renderFromBodyContent(BodyContent bodyContent, TagWriter tagWriter) throws JspException {
		Object value = this.pageContext.getAttribute(VALUE_VARIABLE_NAME);
		String label = bodyContent.getString();
		renderOption(value, label, tagWriter);
	}

	/**
	 * Returns the supplied value to attach to this tag. Evaluates EL expressions
	 * as required.
	 */
	private Object resolveValue() throws JspException {
		return evaluate(VALUE_VARIABLE_NAME, getValue());
	}

	/**
	 * Make sure we are under a '<code>select</code>' tag before proceeding.
	 */
	protected void onWriteTagContent() {
		assertUnderSelectTag();
	}

	protected void exposeAttributes() throws JspException {
		Object value = resolveValue();
		this.pageContext.setAttribute(VALUE_VARIABLE_NAME, value);
		this.pageContext.setAttribute(DISPLAY_VALUE_VARIABLE_NAME, getDisplayString(value, getBindStatus().getEditor()));
	}


	protected void removeAttributes() {
		this.pageContext.removeAttribute(VALUE_VARIABLE_NAME);
		this.pageContext.removeAttribute(DISPLAY_VALUE_VARIABLE_NAME);
	}

	private void renderOption(Object value, String label, TagWriter tagWriter) throws JspException {
		tagWriter.startTag("option");

		String renderedValue = getDisplayString(value, getBindStatus().getEditor());

		if (!renderedValue.equals(label)) {
			tagWriter.writeAttribute(VALUE_ATTRIBUTE, renderedValue);
		}

		if (isSelected(value)) {
			tagWriter.writeAttribute(SELECTED_ATTRIBUTE, SELECTED_ATTRIBUTE);
		}
		tagWriter.appendValue(label);

		tagWriter.endTag();
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

	protected BindStatus getBindStatus() {
		return (BindStatus) this.pageContext.getAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE);
	}

}
