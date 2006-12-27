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

import java.util.Collection;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.support.BindStatus;

/**
 * Databinding-aware JSP tag that renders an HTML '<code>select</code>'
 * element.
 * 
 * <p>Inner '<code>option</code>' tags can be rendered using one of the
 * approaches supported by the {@link OptionWriter} class.
 *
 * <p>Also supports the use of nested {@link OptionTag OptionTags} or
 * (typically one) nested {@link OptionsTag}.
 *
 * @author Rob Harrop
 * @since 2.0
 * @see OptionTag
 * @see OptionWriter
 */
public class SelectTag extends AbstractHtmlInputElementTag {

	/**
	 * The {@link javax.servlet.jsp.PageContext} attribute under
	 * which the bound value is exposed to inner {@link OptionTag OptionTags}.
	 */
	public static final String LIST_VALUE_PAGE_ATTRIBUTE =
			"org.springframework.web.servlet.tags.form.SelectTag.listValue";


	/**
	 * The {@link Collection}, {@link Map} or array of objects used to generate the inner
	 * '<code>option</code>' tags.
	 */
	private Object items;

	/**
	 * The name of the property mapped to the '<code>value</code>' attribute
	 * of the '<code>option</code>' tag.
	 */
	private String itemValue;

	/**
	 * The name of the property mapped to the inner text of the
	 * '<code>option</code>' tag.
	 */
	private String itemLabel;

	/**
	 * The value of the HTML '<code>size</code>' attribute rendered
	 * on the final '<code>select</code>' element.
	 */
	private String size;

	/**
	 * Indicates whether or not the '<code>select</code>' tag allows
	 * multiple-selections.
	 */
	private Object multiple = Boolean.FALSE;

	/**
	 * The {@link TagWriter} instance that the output is being written.
	 * <p>Only used in conjunction with nested {@link OptionTag OptionTags}.
	 */
	private TagWriter tagWriter;


	/**
	 * Sets the {@link Collection}, {@link Map} or array of objects used to
	 * generate the inner '<code>option</code>' tags.
	 * <p>Required when wishing to render '<code>option</code>' tags from
	 * an array, {@link Collection} or {@link Map}.
	 * <p>Typically a runtime expression.
	 * @param items the items that comprise the options of this selection
	 * @throws IllegalArgumentException if the supplied <code>items</code> instance is <code>null</code> 
	 */
	public void setItems(Object items) {
		Assert.notNull(items, "'items' cannot be null.");
		this.items = items;
	}

	/**
	 * Gets the value of the '<code>items</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	protected Object getItems() {
		return items;
	}

	/**
	 * Sets the name of the property mapped to the '<code>value</code>'
	 * attribute of the '<code>option</code>' tag.
	 * <p>Required when wishing to render '<code>option</code>' tags from
	 * an array or {@link Collection}.
	 * <p>May be a runtime expression.
	 */
	public void setItemValue(String itemValue) {
		Assert.hasText(itemValue, "'itemValue' must not be empty");
		this.itemValue = itemValue;
	}

	/**
	 * Gets the value of the '<code>itemValue</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	protected String getItemValue() {
		return itemValue;
	}

	/**
	 * Sets the name of the property mapped to the label (inner text) of the
	 * '<code>option</code>' tag.
	 * <p>May be a runtime expression.
	 * @param itemLabel said label
	 * @throws IllegalArgumentException if the supplied <code>itemLabel</code> is
	 * <code>null</code> or consists wholly of whitespace
	 */
	public void setItemLabel(String itemLabel) {
		Assert.hasText(itemLabel, "'itemLabel' must not be empty");
		this.itemLabel = itemLabel;
	}

	/**
	 * Gets the value of the '<code>itemLabel</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	protected String getItemLabel() {
		return itemLabel;
	}

	/**
	 * Sets the value of the HTML '<code>size</code>' attribute rendered
	 * on the final '<code>select</code>' element.
	 * <p>May be a runtime expression.
	 * @param size the desired value of the '<code>size</code>' attribute
	 * @throws IllegalArgumentException if the supplied <code>size</code> is
	 * <code>null</code> or consists wholly of whitespace
	 */
	public void setSize(String size) {
		Assert.hasText(size, "'size' must not be empty");
		this.size = size;
	}

	/**
	 * Gets the value of the '<code>size</code>' attribute.
	 * <p>May be a runtime expression.
	 */
	protected String getSize() {
		return size;
	}

	/**
	 * Sets the value of the HTML '<code>multiple</code>' attribute rendered
	 * on the final '<code>select</code>' element.
	 * <p>May be a runtime expression.
	 */
	public void setMultiple(Object multiple) {
		this.multiple = multiple;
	}

	/**
	 * Gets the value of the HTML '<code>multiple</code>' attribute rendered
	 * on the final '<code>select</code>' element.
	 * <p>May be a runtime expression.
	 */
	protected Object getMultiple() {
		return multiple;
	}


	/**
	 * Renders the HTML '<code>select</code>' tag to the supplied
	 * {@link TagWriter}.
	 * <p>Renders nested '<code>option</code>' tags if the
	 * {@link #setItems items} property is set, otherwise exposes the
	 * bound value for the nested {@link OptionTag OptionTags}.
	 */
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("select");
		writeDefaultAttributes(tagWriter);

		if (isMultiple()) {
			tagWriter.writeAttribute("multiple", "multiple");
		}

		tagWriter.writeOptionalAttributeValue("size", getDisplayString(evaluate("size", getSize())));

		Object items = getItems();
		if (items != null) {
			Object itemsObject = (items instanceof String ? evaluate("items", (String) items) : items);

			String valueProperty = (getItemValue() == null ? null :
							ObjectUtils.getDisplayString(evaluate("itemValue", getItemValue())));
			String labelProperty = (getItemLabel() == null ? null :
							ObjectUtils.getDisplayString(evaluate("itemLabel", getItemLabel())));

			OptionWriter optionWriter = new OptionWriter(itemsObject, getBindStatus(), valueProperty, labelProperty, isHtmlEscape());
			optionWriter.writeOptions(tagWriter);

			tagWriter.endTag();
			writeHiddenTagIfNecessary(tagWriter);
			return EVAL_PAGE;
		}
		else {
			// using nested <form:option/> tags so just expose the value in the PageContext
			tagWriter.forceBlock();
			this.tagWriter = tagWriter;
			this.pageContext.setAttribute(LIST_VALUE_PAGE_ATTRIBUTE, getBindStatus());
			return EVAL_BODY_INCLUDE;
		}
	}

	/**
	 * If using a multi select, a hidden element is needed to make sure all
	 * items are correctly unselected on the server-side in response to a
	 * <code>null</code>  post.
	 */
	private void writeHiddenTagIfNecessary(TagWriter tagWriter) throws JspException {
		if (isMultiple()) {
			tagWriter.startTag("input");
			tagWriter.writeAttribute("type", "hidden");
			tagWriter.writeAttribute("name", "_" + getName());
			tagWriter.writeAttribute("value", "1");
			tagWriter.endTag();
		}
	}

	private boolean isMultiple() throws JspException {
		Object multiple = getMultiple();
		if (Boolean.TRUE.equals(multiple)
				|| "true".equals(multiple)
				|| "multiple".equals(multiple)) {
			return true;
		}
		else if (this.multiple instanceof String) {
			Object evaluatedValue = evaluate("multiple", multiple);
			return Boolean.TRUE.equals(evaluatedValue);
		}
		return forceMultiple();
	}

	/**
	 * Returns '<code>true</code>' if the bound value requires the
	 * resultant '<code>select</code>' tag to be multi-select.
	 */
	private boolean forceMultiple() throws JspException {
		BindStatus bindStatus = getBindStatus();
		Class valueType = bindStatus.getValueType();
		if (valueType != null && typeRequiresMultiple(valueType)) {
			return true;
		}
		else if (bindStatus.getEditor() != null) {
			Object editorValue = bindStatus.getEditor().getValue();
			if (editorValue != null && typeRequiresMultiple(editorValue.getClass())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns '<code>true</code>' for arrays, {@link Collection Collections}
	 * and {@link Map Maps}.
	 */
	private static boolean typeRequiresMultiple(Class type) {
		return (type.isArray()
				|| Collection.class.isAssignableFrom(type)
				|| Map.class.isAssignableFrom(type));
	}

	/**
	 * Closes any block tag that might have been opened when using
	 * nested {@link OptionTag options}.
	 */
	public int doEndTag() throws JspException {
		if (this.tagWriter != null) {
			this.tagWriter.endTag();
			writeHiddenTagIfNecessary(tagWriter);
		}
		return EVAL_PAGE;
	}

	/**
	 * Clears the {@link TagWriter} that might have been left over when using
	 * nested {@link OptionTag options}.
	 */
	public void doFinally() {
		super.doFinally();
		this.tagWriter = null;
		this.pageContext.removeAttribute(LIST_VALUE_PAGE_ATTRIBUTE);
	}

}
