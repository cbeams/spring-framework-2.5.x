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
public abstract class AbstractHtmlElementTag extends AbstractDataBoundFormElementTag {

	public static final String CLASS_ATTRIBUTE = "class";

	public static final String STYLE_ATTRIBUTE = "style";

	public static final String LANG_ATTRIBUTE = "lang";

	public static final String TITLE_ATTRIBUTE = "title";

	public static final String DIR_ATTRIBUTE = "dir";

	public static final String TABINDEX_ATTRIBUTE = "tabindex";

	public static final String ONCLICK_ATTRIBUTE = "onclick";

	public static final String ONDBLCLICK_ATTRIBUTE = "ondblclick";

	public static final String ONMOUSEDOWN_ATTRIBUTE = "onmousedown";

	public static final String ONMOUSEUP_ATTRIBUTE = "onmouseup";

	public static final String ONMOUSEOVER_ATTRIBUTE = "onmouseover";

	public static final String ONMOUSEMOVE_ATTRIBUTE = "onmousemove";

	public static final String ONMOUSEOUT_ATTRIBUTE = "onmouseout";

	public static final String ONKEYPRESS_ATTRIBUTE = "onkeypress";

	public static final String ONKEYUP_ATTRIBUTE = "onkeyup";

	public static final String ONKEYDOWN_ATTRIBUTE = "onkeydown";

	private String cssClass;

	private String cssStyle;

	private String lang;

	private String title;

	private String dir;

	private String tabindex;

	private String onclick;

	private String ondblclick;

	private String onmousedown;

	private String onmouseup;

	private String onmouseover;

	private String onmousemove;

	private String onmouseout;

	private String onkeypress;

	private String onkeyup;

	private String onkeydown;

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public void setCssStyle(String cssStyle) {
		this.cssStyle = cssStyle;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public void setTabindex(String tabindex) {
		this.tabindex = tabindex;
	}

	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	public void setOndblclick(String ondblclick) {
		this.ondblclick = ondblclick;
	}

	public void setOnmousedown(String onmousedown) {
		this.onmousedown = onmousedown;
	}

	public void setOnmouseup(String onmouseup) {
		this.onmouseup = onmouseup;
	}

	public void setOnmouseover(String onmouseover) {
		this.onmouseover = onmouseover;
	}

	public void setOnmousemove(String onmousemove) {
		this.onmousemove = onmousemove;
	}

	public void setOnmouseout(String onmouseout) {
		this.onmouseout = onmouseout;
	}

	public void setOnkeypress(String onkeypress) {
		this.onkeypress = onkeypress;
	}

	public void setOnkeyup(String onkeyup) {
		this.onkeyup = onkeyup;
	}

	public void setOnkeydown(String onkeydown) {
		this.onkeydown = onkeydown;
	}

	/**
	 * Writes the default attributes configured via this base class to the supplied {@link TagWriter}.
	 * Subclasses should call this when they want the base attribute set to be written to the output.
	 */
	protected void writeDefaultAttributes(TagWriter tagWriter) throws JspException {
		super.writeDefaultAttributes(tagWriter);
		tagWriter.writeOptionalAttributeValue(CLASS_ATTRIBUTE, ObjectUtils.nullSafeToString(evaluate("cssClass", this.cssClass)));
		tagWriter.writeOptionalAttributeValue(STYLE_ATTRIBUTE, ObjectUtils.nullSafeToString(evaluate("cssStyle", this.cssStyle)));
		writeOptionalAttribute(tagWriter, LANG_ATTRIBUTE, this.lang);
		writeOptionalAttribute(tagWriter, TITLE_ATTRIBUTE, this.title);
		writeOptionalAttribute(tagWriter, DIR_ATTRIBUTE, this.dir);
		writeOptionalAttribute(tagWriter, TABINDEX_ATTRIBUTE, this.tabindex);
		writeOptionalAttribute(tagWriter, ONCLICK_ATTRIBUTE, this.onclick);
		writeOptionalAttribute(tagWriter, ONDBLCLICK_ATTRIBUTE, this.ondblclick);
		writeOptionalAttribute(tagWriter, ONMOUSEDOWN_ATTRIBUTE, this.onmousedown);
		writeOptionalAttribute(tagWriter, ONMOUSEUP_ATTRIBUTE, this.onmouseup);
		writeOptionalAttribute(tagWriter, ONMOUSEOVER_ATTRIBUTE, this.onmouseover);
		writeOptionalAttribute(tagWriter, ONMOUSEMOVE_ATTRIBUTE, this.onmousemove);
		writeOptionalAttribute(tagWriter, ONMOUSEOUT_ATTRIBUTE, this.onmouseout);
		writeOptionalAttribute(tagWriter, ONKEYPRESS_ATTRIBUTE, this.onkeypress);
		writeOptionalAttribute(tagWriter, ONKEYUP_ATTRIBUTE, this.onkeyup);
		writeOptionalAttribute(tagWriter, ONKEYDOWN_ATTRIBUTE, this.onkeydown);
	}


}
