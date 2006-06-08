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

import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.tags.HtmlEscapingAwareTag;
import org.springframework.web.util.ExpressionEvaluationUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * Base class for all JSP form tags. Provides utility methods for
 * null-safe EL evaluation and for accessing and working with a {@link TagWriter}.
 *
 * <p>Subclasses should implement the {@link #writeTagContent(TagWriter)} to perform
 * actual tag rendering.
 *
 * <p>Subclasses (or test classes) can override the {@link #createTagWriter()} method to
 * redirect output to a {@link java.io.Writer} other than the {@link javax.servlet.jsp.JspWriter}
 * associated with the current {@link javax.servlet.jsp.PageContext}.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AbstractFormTag extends HtmlEscapingAwareTag {

	/**
	 * Evaluates the supplied value for the supplied attribute name. If the supplied value
	 * is <code>null</code> then <code>null</code> is returned, otherwise evaluation is
	 * handled using {@link ExpressionEvaluationUtils#evaluate(String, String, javax.servlet.jsp.PageContext)}.
	 */
	protected Object evaluate(String attributeName, String value) throws JspException {
		if (value == null) {
			return null;
		}
		return ExpressionEvaluationUtils.evaluate(attributeName, value, this.pageContext);
	}

	/**
	 * Optionally writes the supplied value under the supplied attribute name into the supplied
	 * {@link TagWriter}. In this case, the supplied value is {@link #evaluate evaluated} first
	 * and then the {@link ObjectUtils#getDisplayString String representation} is written as the
	 * attribute value. If the resultant <code>String</code> representation is <code>null</code>
	 * or empty, no attribute is written.
	 * @see TagWriter#writeOptionalAttributeValue(String, String)
	 */
	protected final void writeOptionalAttribute(TagWriter tagWriter, String attributeName, String value) throws JspException {
		tagWriter.writeOptionalAttributeValue(attributeName, getDisplayString(evaluate(attributeName, value)));
	}

	/**
	 * Creates the {@link TagWriter} which all output will be written to. By default,
	 * the {@link TagWriter} writes its output to the {@link javax.servlet.jsp.JspWriter}
	 * for the current {@link javax.servlet.jsp.PageContext}. Subclasses may choose to
	 * change the {@link java.io.Writer} to which output is actually written.
	 */
	protected TagWriter createTagWriter() {
		return new TagWriter(this.pageContext.getOut());
	}

	/**
	 * Provides a simple template method that calls {@link #createTagWriter()} and passes
	 * the created {@link TagWriter} to the {@link #writeTagContent(TagWriter)} method.
	 * @return the value returned by {@link #writeTagContent(TagWriter)}
	 */
	protected final int doStartTagInternal() throws Exception {
		return writeTagContent(createTagWriter());
	}

	/**
	 * Gets the display value of the supplied <code>Object</code>, HTML escaped
	 * as required.
	 */
	protected String getDisplayString(Object value) {
		String displayValue = ObjectUtils.getDisplayString(value);
		return (isHtmlEscape() ? HtmlUtils.htmlEscape(displayValue) : displayValue);
	}

	/**
	 * Subclasses should implement this method to perform tag content rendering.
	 * @return valid tag render instruction as per {@link javax.servlet.jsp.tagext.Tag#doStartTag()}.
	 */
	protected abstract int writeTagContent(TagWriter tagWriter) throws JspException;

}
