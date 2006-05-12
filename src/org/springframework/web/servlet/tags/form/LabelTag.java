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

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class LabelTag extends AbstractHtmlElementTag {

	/**
	 * The HTML '<code>label</code>' tag.
	 */
	private static final String LABEL_TAG = "label";

	/**
	 * The name of the '<code>for</code>' attribute.
	 */
	private static final String FOR_ATTRIBUTE = "for";


	/**
	 * Tha {@link TagWriter} instance being used. Stored so we can close the tag on
	 * {@link #doEndTag()}.
	 */
	private TagWriter tagWriter;

	/**
	 * Writes the opening '<code>label</code>' tag and forces a block tag so
	 * that body content is written correctly.
	 */
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag(LABEL_TAG);
		tagWriter.writeAttribute(FOR_ATTRIBUTE, getPath());
		writeDefaultAttributes(tagWriter);
		tagWriter.forceBlock();
		this.tagWriter = tagWriter;
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Overrides {@link #getName()} and appends '<code>.label</code>' to the end
	 * of the default value.
	 */
	protected String getName() throws JspException {
		return super.getName() + ".label";
	}

	/**
	 * Close the '<code>label</code>' tag.
	 */
	public int doEndTag() throws JspException {
		this.tagWriter.endTag();
		return EVAL_PAGE;
	}

	/**
	 * Clean the {@link TagWriter} instance.
	 */
	public void doFinally() {
		this.tagWriter = null;
	}
}
