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
public abstract class AbstractHtmlInputElementTag extends AbstractHtmlElementTag {

	protected String onfocus;

	protected String onblur;

	protected String onchange;

	protected String accesskey;

	public void setOnfocus(String onfocus) {
		this.onfocus = onfocus;
	}

	public void setOnblur(String onblur) {
		this.onblur = onblur;
	}

	public void setOnchange(String onchange) {
		this.onchange = onchange;
	}

	public void setAccesskey(String accesskey) {
		this.accesskey = accesskey;
	}

	protected void writeDefaultAttributes(TagWriter tagWriter) throws JspException {
		super.writeDefaultAttributes(tagWriter);
		tagWriter.writeOptionalAttributeValue("onfocus", ObjectUtils.nullSafeToString(evaluate("onfocus", this.onfocus)));
		tagWriter.writeOptionalAttributeValue("onblur", ObjectUtils.nullSafeToString(evaluate("onblur", this.onblur)));
		tagWriter.writeOptionalAttributeValue("onchange", ObjectUtils.nullSafeToString(evaluate("onchange", this.onchange)));
		tagWriter.writeOptionalAttributeValue("accesskey", ObjectUtils.nullSafeToString(evaluate("accesskey", this.accesskey)));
	}
}
