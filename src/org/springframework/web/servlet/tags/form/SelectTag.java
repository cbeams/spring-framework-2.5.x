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

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.jsp.JspException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Databinding-aware JSP tag that renders an HTML '<code>select</code>' element.
 * Inner '<code>option</code>' tags can be rendered using one of three approaches.
 * <h3>Approach One</h3>
 * In the first approach you specify an array or {@link Collection} of
 * {@link #setItems objects} which are used to render the inner '<code>option</code>'
 * tags. When using this approach you specify the name of the property on the
 * objects which corresponds to the {@link #setItemValue value} of the rendered
 * '<code>option</code>' and, optionally, the name of the property that
 * corresponds to the {@link #setItemLabel label}. When the {@link #setItemLabel label}
 * property name is ommitted the name of the {@link #setItemValue value}
 * property is used.
 * <h3>Approach Two</h3>
 * In the second approach, '<code>option</code>' tags are rendered from a source
 * {@link Map}. The key and value of the entries in the {@link Map} correspond
 * to the value and label of the rendered '<code>option</code>'.
 * <h3>Approach Three</h3>
 * In the third approach, '<code>option</code>' tags are defined using nested
 * {@link OptionTag OptionTags}. This tag simply
 * {@link #LIST_VALUE_PAGE_ATTRIBUTE exposes the bound value} so that the nested
 * {@link OptionTag OptionTags} can mark themselves as 'selected' as appropriate.
 * When {@link #doEndTag() closing the tag} the block '<code>select</code>' tag
 * is closed. This approach is only used if the {@link #setItems items} property
 * is ommitted.
 * <p/>
 * When using any of these approaches, an '<code>option</code>' is marked
 * as 'selected' if its key {@link #isActiveValue matches} the value that
 * is bound to the tag instance.
 * 
 * @author Rob Harrop
 * @see OptionTag
 * @since 2.0
 */
public class SelectTag extends AbstractHtmlInputElementTag {

	/**
	 * The {@link javax.servlet.jsp.PageContext} attribute under which
	 * the bound value is exposed to inner {@link OptionTag OptionTags}.
	 */
	public static final String LIST_VALUE_PAGE_ATTRIBUTE = "org.springframework.web.servlet.tags.form.SelectTag.listValue";

	/**
	 * The {@link Collection} of objects used to generate the inner
	 * '<code>option</code>' tags.
	 */
	private String items;

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
	 * The {@link TagWriter} instance that the output is being written.
	 * Only used in conjunction with nested {@link OptionTag OptionTags}.
	 */
	private TagWriter tagWriter;

	/**
	 * Sets the {@link Collection} of objects used to generate the inner
	 * '<code>option</code>' tags. Required when wishing to render
	 * '<code>option</code>' tags from an array, {@link Collection} or
	 * {@link Map}.
	 * Typically a runtime expression.
	 */
	public void setItems(String items) {
		Assert.hasText(items, "'items' cannot be null or zero length.");
		this.items = items;
	}

	/**
	 * Sets the name of the property mapped to the '<code>value</code>' attribute
	 * of the '<code>option</code>' tag. Required when wishing to render
	 * '<code>option</code>' tags from an array or {@link Collection}.
	 * May be a runtime expression.
	 */
	public void setItemValue(String itemValue) {
		Assert.hasText(itemValue, "'itemValue' cannot be null or zero length.");
		this.itemValue = itemValue;
	}

	/**
	 * Sets the name of the property mapped to the label (inner text) of the
	 * '<code>option</code>' tag. May be a runtime expression.
	 */
	public void setItemLabel(String itemLabel) {
		Assert.hasText(itemLabel, "'itemLabel' cannot be null or zero length.");
		this.itemLabel = itemLabel;
	}

	/**
	 * Sets the value of the HTML '<code>size</code>' attribute rendered
	 * on the final '<code>select</code>' element. May be a runtime
	 * expression.
	 */
	public void setSize(String size) {
		Assert.hasText(size, "'size' cannot be null or zero length.");
		this.size = size;
	}

	/**
	 * Renders the HTML '<code>select</code>' tag to supplied {@link TagWriter}.
	 * Renders nested '<code>option</code>' tags if the {@link #setItems items}
	 * properties are set, otherwise exposes the bound value for the
	 * nested {@link OptionTag OptionTags}.
	 */
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("select");
		writeDefaultAttributes(tagWriter);
		tagWriter.writeOptionalAttributeValue("size", ObjectUtils.nullSafeToString(evaluate("size", this.size)));

		if (this.items != null) {
			Object itemsObject = evaluate("items", this.items);

			if (itemsObject.getClass().isArray()) {
				renderFromCollection(CollectionUtils.toList(itemsObject), tagWriter);
			}
			else if (itemsObject instanceof Collection) {
				renderFromCollection((Collection) itemsObject, tagWriter);
			}
			else if (itemsObject instanceof Map) {
				renderFromMap((Map) itemsObject, tagWriter);
			}
			else {
				throw new JspException("Property 'items' must be of type '" + Collection.class.getName() + "'.");
			}

			tagWriter.endTag();
			return EVAL_PAGE;

		}
		else {
			// using nested <form:option/> tags so just expose the value in the PageContext
			tagWriter.forceBlock();
			this.tagWriter = tagWriter;
			this.pageContext.setAttribute(LIST_VALUE_PAGE_ATTRIBUTE, getValue());
			return EVAL_BODY_INCLUDE;
		}
	}

	/**
	 * Renders the inner '<code>option</code>' tags using the supplied {@link Map} as
	 * the source.
	 * @see #renderOption
	 */
	private void renderFromMap(Map map, TagWriter tagWriter) throws JspException {
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			renderOption(tagWriter, entry, entry.getKey().toString(), entry.getValue().toString());
		}
	}

	/**
	 * Renders the inner '<code>option</code>' tags using the supplied {@link Collection} of
	 * objects as the source. The value of the {@link #setItemValue itemValue} property is used
	 * when rendering the '<code>value</code>' of the '<code>option</code>' and the value of the
	 * {@link #setItemLabel itemLabel} property is used when rendering the label.
	 */
	private void renderFromCollection(Collection itemList, TagWriter tagWriter) throws JspException {

		String valueProperty = (this.itemValue == null ? null :ObjectUtils.nullSafeToString(evaluate("itemValue", this.itemValue)));
		String labelProperty = (this.itemLabel == null ? null : ObjectUtils.nullSafeToString(evaluate("itemLabel", this.itemLabel)));


		for (Iterator iterator = itemList.iterator(); iterator.hasNext();) {
			Object item = iterator.next();
			BeanWrapper wrapper = new BeanWrapperImpl(item);

			Object value = (valueProperty == null ? item.toString(): wrapper.getPropertyValue(valueProperty).toString());
			String label = (labelProperty == null ? item.toString() : ObjectUtils.nullSafeToString(wrapper.getPropertyValue(labelProperty)));

			renderOption(tagWriter, item, value, label);
		}
	}

	/**
	 * Renders an HTML '<code>option</code>' with the supplied value and label. Marks the
	 * value as 'selected' if either the item itself or its {@link #setItemValue value}
	 * match the bound value.
	 * @see #isActiveValue
	 */
	private void renderOption(TagWriter tagWriter, Object item, Object value, String label) throws JspException {
		tagWriter.startTag("option");
		tagWriter.writeAttribute("value", ObjectUtils.nullSafeToString(value));
		if (isActiveValue(value) || isActiveValue(item)) {
			tagWriter.writeAttribute("selected", "true");
		}
		tagWriter.appendValue(label);
		tagWriter.endTag();
	}

	/**
	 * Closes any block tag that might have been opened when using
	 * nested {@link OptionTag options}.
	 */
	public int doEndTag() throws JspException {
		if (this.tagWriter != null) {
			this.tagWriter.endTag();
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
	}
}
