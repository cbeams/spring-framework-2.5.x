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
import org.springframework.util.ObjectUtils;

import javax.servlet.jsp.JspException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Rob Harrop
 */
public class SelectTag extends AbstractHtmlInputElementTag {

	public static final String LIST_VALUE_PAGE_ATTRIBUTE = "org.springframework.web.servlet.tags.form.SelectTag.listValue";

	private String items;

	private String itemKey;

	private String itemValue;

	private String size;

	private TagWriter tagWriter;

	public void setItems(String items) {
		Assert.hasText(items, "'items' cannot be null or zero length.");
		this.items = items;
	}

	public void setItemKey(String itemKey) {
		Assert.hasText(itemKey, "'itemKey' cannot be null or zero length.");
		this.itemKey = itemKey;
	}

	public void setItemValue(String itemValue) {
		Assert.hasText(itemValue, "'itemValue' cannot be null or zero length.");
		this.itemValue = itemValue;
	}

	public void setSize(String size) {
		Assert.hasText(size, "'size' cannot be null or zero length.");
		this.size = size;
	}

	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("select");
		writeDefaultAttributes(tagWriter);
		tagWriter.writeOptionalAttributeValue("size", ObjectUtils.nullSafeToString(evaluate("size", this.size)));

		if (this.items != null) {
			Object itemsObject = evaluate("items", this.items);

			if (itemsObject instanceof Collection) {
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

	private void renderFromMap(Map map, TagWriter tagWriter) throws JspException {
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			renderOption(tagWriter, entry.getKey().toString(), entry.getValue().toString());
		}
	}

	private void renderFromCollection(Collection itemList, TagWriter tagWriter) throws JspException {
		if (this.itemKey == null) {
			throw new IllegalArgumentException("Attribute 'itemKey' is required when defining 'items' as a Collection.");
		}

		String keyProperty = ObjectUtils.nullSafeToString(evaluate("itemKey", this.itemKey));
		String valueProperty = (this.itemValue == null ? null : ObjectUtils.nullSafeToString(evaluate("itemValue", this.itemValue)));


		for (Iterator iterator = itemList.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			BeanWrapper wrapper = new BeanWrapperImpl(o);

			Object key = wrapper.getPropertyValue(keyProperty).toString();
			String value = (valueProperty == null ? o.toString() : wrapper.getPropertyValue(valueProperty).toString());

			renderOption(tagWriter, key, value);
		}
	}

	private void renderOption(TagWriter tagWriter, Object key, String value) throws JspException {
		tagWriter.startTag("option");
		tagWriter.writeAttribute("value", ObjectUtils.nullSafeToString(key));
		if (getValue().equals(key)) {
			tagWriter.writeAttribute("selected", "true");
		}
		tagWriter.appendValue(value);
		tagWriter.endTag();
	}

	protected String getTagName() {
		return "select";
	}

	public int doEndTag() throws JspException {
		if (this.tagWriter != null) {
			this.tagWriter.endTag();
		}
		return EVAL_PAGE;
	}

	public void doFinally() {
		super.doFinally();
		this.tagWriter = null;
	}
}
