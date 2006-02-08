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
import org.springframework.util.Assert;

import javax.servlet.jsp.JspException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class CheckboxTag extends AbstractHtmlInputElementTag {

	private String value;

	/**
	 * Sets the value that is sent to the server when the render checkbox is selected.
	 * Can be a runtime expression.
	 */
	public void setValue(String value) {
		Assert.notNull(value, "'value' cannot be null.");
		this.value = value;
	}

	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("input");
		writeDefaultAttributes(tagWriter);
		tagWriter.writeAttribute("type", "checkbox");

		Object boundValue = getValue();

		if (boundValue instanceof Boolean) {
			renderFromBoolean((Boolean) boundValue, tagWriter);
		}
		else {

			if (this.value == null) {
				throw new IllegalArgumentException("Attribute 'value' is required when binding to non-Boolean values.");
			}

			Object resolvedValue = evaluate("value", this.value);

			if (boundValue != null && boundValue.getClass().isArray()) {
				renderFromCollection(resolvedValue, toList(boundValue), tagWriter);
			}
			else if (boundValue instanceof Collection) {
				renderFromCollection(resolvedValue, (Collection) boundValue, tagWriter);
			}
			else {
				renderSingleValue(resolvedValue, boundValue, tagWriter);
			}
		}

		tagWriter.endTag();

		// write out the marker field
		tagWriter.startTag("input");
		tagWriter.writeAttribute("type", "hidden");
		tagWriter.writeAttribute("boundValue", "1");
		tagWriter.writeAttribute("name", "_" + getPath());
		tagWriter.endTag();

		return EVAL_PAGE;
	}

	private void renderSingleValue(Object resolvedValue, Object boundValue, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", ObjectUtils.nullSafeToString(resolvedValue));

		if (boundValue != null && resolvedValue.equals(boundValue)) {
			tagWriter.writeAttribute("checked", "true");
		}
	}

	private void renderFromCollection(Object resolvedValue, Collection boundValue, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", ObjectUtils.nullSafeToString(resolvedValue));

		if (boundValue.contains(resolvedValue)) {
			tagWriter.writeAttribute("checked", "true");
		}
	}

	private void renderFromBoolean(Boolean b, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", "true");
		if (b.booleanValue()) {
			tagWriter.writeAttribute("checked", "true");
		}
	}

	private List toList(Object boundValue) {
		List values = new ArrayList();
		boolean primitive = boundValue.getClass().getComponentType().isPrimitive();
		Object[] array = (primitive ? ObjectUtils.toObjectArray(boundValue) : (Object[]) boundValue);
		values.addAll(Arrays.asList(array));
		return values;
	}
}
