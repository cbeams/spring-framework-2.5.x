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

import org.springframework.util.StringUtils;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * @author Rob Harrop
 * @since 2.0
 */
class TagWriter {

	private SafeWriter writer;

	private Stack tagState = new Stack();

	public TagWriter(Writer writer) {
		this.writer = new SafeWriter(writer);
	}

	public void startTag(String tagName) throws JspException {
		if (inTag()) {
			closeTagAndMarkAsBlock();
		}

		push(tagName);

		this.writer.append("<").append(tagName);
	}

	public void appendValue(String value) throws JspException {
		if (!inTag()) {
			throw new IllegalStateException("Cannot write tag value. No open tag available.");
		}
		closeTagAndMarkAsBlock();
		this.writer.append(value);
	}


	public void writeAttribute(String attributeName, String attributeValue) throws JspException {
		if (currentState().isBlockTag()) {
			throw new IllegalStateException("Cannot write attributes after opening tag is closed.");
		}
		this.writer.append(" ").append(attributeName).append("=\"").append(attributeValue).append("\"");
	}

	public void writeOptionalAttributeValue(String attributeName, String attributeValue) throws JspException {
		if (StringUtils.hasText(attributeValue)) {
			writeAttribute(attributeName, attributeValue);
		}
	}

	/**
	 * Indicates that the currently open tag should be closed and marked as a block level element.
	 * Useful when you plan to write additional content in the body outside the context of the current
	 * <code>TagWriter</code>.
	 */
	public void forceBlock() throws JspException {
		if (currentState().isBlockTag()) {
			return; // just ignore since we are already in the block
		}
		closeTagAndMarkAsBlock();
	}

	public void endTag() throws JspException {
		if (!inTag()) {
			throw new IllegalStateException("Cannot write end of tag. No open tag available.");
		}

		if (currentState().isBlockTag()) {
			// writing the end of the block - the opening tag was closed earlier.
			this.writer.append("</").append(currentState().getTagName()).append(">");
		}
		else {
			this.writer.append("/>");
		}

		// remove the current tag from the state stack
		this.tagState.pop();
	}

	private void push(String tagName) {
		// create an entry for this tag on the stack
		TagStateEntry entry = new TagStateEntry();
		entry.setTagName(tagName);
		this.tagState.push(entry);
	}

	private void closeTagAndMarkAsBlock() throws JspException {
		if (!currentState().isBlockTag()) {
			currentState().setBlockTag(true);
			this.writer.append(">");
		}
	}

	private boolean inTag() {
		return (this.tagState.size() > 0);
	}

	private TagStateEntry currentState() {
		return (TagStateEntry) this.tagState.peek();
	}

	private static class TagStateEntry {

		private String tagName;

		private boolean blockTag;

		public String getTagName() {
			return tagName;
		}

		public void setTagName(String tagName) {
			this.tagName = tagName;
		}

		public boolean isBlockTag() {
			return blockTag;
		}

		public void setBlockTag(boolean blockTag) {
			this.blockTag = blockTag;
		}
	}

	private static class SafeWriter {

		private Writer writer;

		public SafeWriter(Writer writer) {
			this.writer = writer;
		}

		public SafeWriter append(String value) throws JspException {
			try {
				this.writer.append(value);
				return this;
			}
			catch (IOException e) {
				throw new JspException("Unable to write to JspWriter.", e);
			}
		}
	}
}
