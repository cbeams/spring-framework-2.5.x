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
import java.util.Iterator;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.support.BindStatus;

/**
 * Provides supporting functionality to render a list of '<code>option</code>'
 * tags based on some source object. This object can be either an array, a
 * {@link Collection} or a {@link Map}.
 *
 * <h3>Using an array or a {@link Collection}</h3>
 * In the first approach you specify an array or {@link Collection} source object
 * which are used to render the inner '<code>option</code>' tags. When using
 * this approach you specify the name of the property on the objects which
 * corresponds to the value of the rendered '<code>option</code>' and the name
 * of the property that corresponds to the label. These properties are then used
 * when rendering each element of the array/{@link Collection} as an '<code>option</code>'.
 * When the either property name is ommitted the name of the {@link Object#toString()} of
 * the array/{@link Collection} element is used.
 * Property names are specified as arguments to the constructor.
 * <h3>Using a {@link Map}</h3>
 * In the second approach, '<code>option</code>' tags are rendered from a source
 * {@link Map}. The key and value of the entries in the {@link Map} correspond
 * to the value and label of the rendered '<code>option</code>'.
 *
 * <p>When using any of these approaches, an '<code>option</code>' is marked
 * as 'selected' if its key {@link #isSelected matches} the value that
 * is bound to the tag instance.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
final class OptionWriter {

	private final Object optionSource;

	private final BindStatus bindStatus;

	private final String valueProperty;

	private final String labelProperty;

	private final boolean htmlEscape;


	/**
	 * Creates a new <code>OptionWriter</code> for the supplied <code>objectSource</code>.
	 * @param optionSource the source of the <code>options</code> (never <code>null</code>)
	 * @param bindStatus the {@link BindStatus} for the bound value (never <code>null</code>)
	 * @param valueProperty the name of the property used to render <code>option</code> values
	 * (optional)
	 * @param labelProperty the name of the property used to render <code>option</code> labels
	 * (optional)
	 */
	public OptionWriter(
			Object optionSource, BindStatus bindStatus, String valueProperty, String labelProperty, boolean htmlEscape) {

		Assert.notNull(optionSource, "'optionSource' must not be null");
		Assert.notNull(bindStatus, "'bindStatus' must not be null");
		this.optionSource = optionSource;
		this.bindStatus = bindStatus;
		this.valueProperty = valueProperty;
		this.labelProperty = labelProperty;
		this.htmlEscape = htmlEscape;
	}

	/**
	 * Write the '<code>option</code>' tags for the configured {@link #optionSource} to
	 * the supplied {@link TagWriter}.
	 */
	public void writeOptions(TagWriter tagWriter) throws JspException {
		if (this.optionSource.getClass().isArray()) {
			renderFromArray(tagWriter);
		}
		else if (this.optionSource instanceof Collection) {
			renderFromCollection(tagWriter);
		}
		else if (this.optionSource instanceof Map) {
			renderFromMap(tagWriter);
		}
		else {
			throw new JspException(
					"Type [" + this.optionSource.getClass().getName() + "] is not valid for option items");
		}
	}

	/**
	 * Renders the inner '<code>option</code>' tags using the {@link #optionSource}.
	 * @see #doRenderFromCollection(java.util.Collection, TagWriter)
	 */
	private void renderFromArray(TagWriter tagWriter) throws JspException {
		doRenderFromCollection(CollectionUtils.arrayToList(this.optionSource), tagWriter);
	}

	/**
	 * Renders the inner '<code>option</code>' tags using the supplied {@link Map} as
	 * the source.
	 * @see #renderOption
	 */
	private void renderFromMap(TagWriter tagWriter) throws JspException {
		Map optionMap = (Map) this.optionSource;
		for (Iterator iterator = optionMap.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			renderOption(tagWriter, entry, entry.getKey().toString(), entry.getValue().toString());
		}
	}

	/**
	 * Renders the inner '<code>option</code>' tags using the {@link #optionSource}.
	 * @see #doRenderFromCollection(java.util.Collection, TagWriter)
	 */
	private void renderFromCollection(TagWriter tagWriter) throws JspException {
		doRenderFromCollection((Collection) this.optionSource, tagWriter);
	}

	/**
	 * Renders the inner '<code>option</code>' tags using the supplied {@link Collection} of
	 * objects as the source. The value of the {@link #valueProperty} field is used
	 * when rendering the '<code>value</code>' of the '<code>option</code>' and the value of the
	 * {@link #labelProperty} property is used when rendering the label.
	 */
	private void doRenderFromCollection(Collection optionCollection, TagWriter tagWriter) throws JspException {
		for (Iterator it = optionCollection.iterator(); it.hasNext();) {
			Object item = it.next();
			BeanWrapper wrapper = new BeanWrapperImpl(item);
			Object value = (this.valueProperty != null ? wrapper.getPropertyValue(this.valueProperty) : item);
			Object label = (this.labelProperty != null ? wrapper.getPropertyValue(this.labelProperty) : item);
			renderOption(tagWriter, item, value, label);
		}
	}

	/**
	 * Renders an HTML '<code>option</code>' with the supplied value and label. Marks the
	 * value as 'selected' if either the item itself or its value match the bound value.
	 */
	private void renderOption(TagWriter tagWriter, Object item, Object value, Object label) throws JspException {
		tagWriter.startTag("option");

		String valueDisplayString = getDisplayString(value);
		String labelDisplayString = getDisplayString(label);

		// allows render values to handle some strange browser compat issues.
		tagWriter.writeAttribute("value", valueDisplayString);

		if (isSelected(value) || isSelected(item)) {
			tagWriter.writeAttribute("selected", "selected");
		}
		tagWriter.appendValue(labelDisplayString);
		tagWriter.endTag();
	}

	/**
	 * Determine whether the supplied values matched the selected value.
	 * Delegates to {@link SelectedValueComparator#isSelected}.
	 */
	private boolean isSelected(Object resolvedValue) {
		return SelectedValueComparator.isSelected(this.bindStatus, resolvedValue);
	}

	/**
	 * Determines the display value of the supplied <code>Object</code>,
	 * HTML-escaped as required.
	 */
	private String getDisplayString(Object value) {
		return ValueFormatter.getDisplayString(value, this.bindStatus.getEditor(), this.htmlEscape);
	}

}
