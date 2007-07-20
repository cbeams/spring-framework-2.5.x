/*
 * Copyright 2002-2007 the original author or authors.
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

import java.util.Collection;

import javax.servlet.jsp.JspException;

import org.springframework.util.Assert;
import org.springframework.web.bind.WebDataBinder;

/**
 * Databinding-aware JSP tag for rendering an HTML '<code>input</code>'
 * element with a '<code>type</code>' of '<code>checkbox</code>'.
 *
 * <p>May be used in one of three different approaches depending on the
 * type of the {@link #getValue bound value}.
 *
 * <h3>Approach One</h3>
 * When the bound value is of type {@link Boolean} then the '<code>input(checkbox)</code>'
 * is marked as 'checked' if the bound value is <code>true</code>. The '<code>value</code>'
 * attribute corresponds to the resolved value of the {@link #setValue(Object) value} property.
 * <h3>Approach Two</h3>
 * When the bound value is of type {@link Collection} then the '<code>input(checkbox)</code>'
 * is marked as 'checked' if the configured {@link #setValue(Object) value} is present in
 * the bound {@link Collection}.
 * <h3>Approach Three</h3>
 * For any other bound value type, the '<code>input(checkbox)</code>' is marked as 'checked'
 * if the the configured {@link #setValue(Object) value} is equal to the bound value.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class CheckboxTag extends AbstractHtmlInputElementTag {

	private Object value;


	/**
	 * Set the value of the '<code>value</code>' attribute.
	 * May be a runtime expression.
	 */
	public void setValue(Object value) {
		Assert.notNull(value, "'value' must not be null");
		this.value = value;
	}

	/**
	 * Get the value of the '<code>value</code>' attribute.
	 * May be a runtime expression.                                              
	 */
	protected Object getValue() {
		return this.value;
	}


	/**
	 * Writes the '<code>input(checkbox)</code>' to the supplied {@link TagWriter},
	 * marking it as 'checked' if appropriate.
	 */
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("input");
		writeDefaultAttributes(tagWriter);
		tagWriter.writeAttribute("type", "checkbox");

		Object boundValue = getBoundValue();
		Class valueType = getBindStatus().getValueType();

		if (Boolean.class.equals(valueType) || boolean.class.equals(valueType)) {
			// the concrete type may not be a Boolean - can be String
			if (boundValue instanceof String) {
				boundValue = Boolean.valueOf((String) boundValue);
			}
			Boolean booleanValue = (boundValue != null ? (Boolean) boundValue : Boolean.FALSE);
			renderFromBoolean(booleanValue, tagWriter);
		}

		else {
			Object value = getValue();
			if (value == null) {
				throw new IllegalArgumentException("Attribute 'value' is required when binding to non-boolean values");
			}
			Object resolvedValue = (value instanceof String ? evaluate("value", (String) value) : value);
			if (boundValue != null && boundValue.getClass().isArray()) {
				renderFromCollection(resolvedValue, tagWriter);
			}
			else if (boundValue instanceof Collection) {
				renderFromCollection(resolvedValue, tagWriter);
			}
			else {
				renderSingleValue(resolvedValue, tagWriter);
			}
		}

		tagWriter.endTag();

		if (!isDisabled()) {
			// Write out the 'field was present' marker.
			tagWriter.startTag("input");
			tagWriter.writeAttribute("type", "hidden");
			tagWriter.writeAttribute("name", WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + getName());
			tagWriter.writeAttribute("value", "on");
			tagWriter.endTag();
		}

		return EVAL_PAGE;
	}

	/**
	 * Render the '<code>input(checkbox)</code>' with the supplied value, marking the
	 * '<code>input</code>' element as 'checked' if the supplied value matches the
	 * bound value.
	 */
	private void renderSingleValue(Object resolvedValue, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", getDisplayString(resolvedValue, getPropertyEditor()));
		if (SelectedValueComparator.isSelected(getBindStatus(), resolvedValue)) {
			tagWriter.writeAttribute("checked", "checked");
		}
	}

	/**
	 * Render the '<code>input(checkbox)</code>' with the supplied value, marking
	 * the '<code>input</code>' element as 'checked' if the supplied value is
	 * present in the bound Collection value.
	 */
	private void renderFromCollection(Object resolvedValue, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", getDisplayString(resolvedValue, getPropertyEditor()));
		if (SelectedValueComparator.isSelected(getBindStatus(), resolvedValue)) {
			tagWriter.writeAttribute("checked", "checked");
		} 
	}

	/**
	 * Render the '<code>input(checkbox)</code>' with the supplied value, marking
	 * the '<code>input</code>' element as 'checked' if the supplied Boolean is
	 * <code>true</code>.
	 */
	private void renderFromBoolean(Boolean boundValue, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", "true");
		if (boundValue.booleanValue()) {
			tagWriter.writeAttribute("checked", "checked");
		}
	}

	/**
	 * Return a unique ID for the bound name within the current PageContext.
	 */
	protected String autogenerateId() throws JspException {
		return TagIdGenerator.nextId(getName(), this.pageContext);
	}

}
