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
public class InputTag extends AbstractHtmlInputElementTag {

	private String maxlength;

	private String alt;

	private String onselect;

	public static final String MAXLENGTH_ATTRIBUTE = "maxlength";

	public static final String ALT_ATTRIBUTE = "alt";

	public static final String ONSELECT_ATTRIBUTE = "onselect";

	public void setMaxlength(String maxlength) {
		this.maxlength = maxlength;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public void setOnselect(String onselect) {
		this.onselect = onselect;
	}

	protected String getType() {
		return "text";
	}

	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("input");
		writeDefaultAttributes(tagWriter);
		tagWriter.writeAttribute("type", getType());
		tagWriter.writeAttribute("value", ObjectUtils.nullSafeToString(getValue()));

		// custom optional attributes
		writeOptionalAttribute(tagWriter, MAXLENGTH_ATTRIBUTE, this.maxlength);
		writeOptionalAttribute(tagWriter, ALT_ATTRIBUTE, this.maxlength);
		writeOptionalAttribute(tagWriter, ONSELECT_ATTRIBUTE, this.maxlength);
		tagWriter.endTag();
		return EVAL_PAGE;
	}
}
