/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.web.servlet.tags;

import java.beans.PropertyEditor;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.springframework.validation.Errors;
import org.springframework.web.util.ExpressionEvaluationUtils;

/**
 * <p>Bind tag, supporting evaluation of binding errors for a certain
 * bean or bean property. Exports a "status" variable of type BindStatus</p>
 *
 * <p>The errors object that has been bound using this tag is exposed, as well
 * as the property that this errors object applies to. Children tags can
 * use the exposed properties
 *
 * <p>Discussed in Chapter 12 of
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class BindTag extends HtmlEscapingAwareTag {

	/**
	 * Name of the exposed variable within the scope of this tag: "status".
	 */
	public static final String STATUS_VARIABLE_NAME = "status";


	private String path;

	private boolean ignoreNestedPath = false;

	private BindStatus status;


	/**
	 * Set the path that this tag should apply. Can be a bean (e.g. "person")
	 * to get global errors, or a bean property (e.g. "person.name") to get
	 * field errors (also supporting nested fields and "person.na*" mappings).
	 * "person.*" will return all errors for the specified bean, both global
	 * and field errors.
	 * @see org.springframework.validation.Errors#getGlobalErrors
	 * @see org.springframework.validation.Errors#getFieldErrors
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Return the path that this tag applies to.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Set whether to ignore a nested path, if any.
	 * Default is to not ignore.
	 */
	public void setIgnoreNestedPath(boolean ignoreNestedPath) {
	  this.ignoreNestedPath = ignoreNestedPath;
	}

	/**
	 * Return whether to ignore a nested path, if any.
	 */
	public boolean isIgnoreNestedPath() {
	  return ignoreNestedPath;
	}


	protected final int doStartTagInternal() throws Exception {
		String resolvedPath = ExpressionEvaluationUtils.evaluateString("path", getPath(), pageContext);

		if (!isIgnoreNestedPath()) {
			String nestedPath = (String) this.pageContext.getAttribute(
					NestedPathTag.NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE);
			if (nestedPath != null) {
				resolvedPath = nestedPath + resolvedPath;
			}
		}

		try {
			this.status = new BindStatus(getRequestContext(), resolvedPath, isHtmlEscape());
		}
		catch (IllegalStateException ex) {
			throw new JspTagException(ex.getMessage());
		}
		
		// create the status object
		this.pageContext.setAttribute(STATUS_VARIABLE_NAME, this.status);
		return EVAL_BODY_INCLUDE;
	}

	public int doEndTag() {
		pageContext.removeAttribute(STATUS_VARIABLE_NAME);
		return EVAL_PAGE;
	}


	/**
	 * Retrieve the property that this tag is currently bound to,
	 * or null if bound to an object rather than a specific property.
	 * Intended for cooperating nesting tags.
	 * @return the property that this tag is currently bound to,
	 * or <code>null</code> if none
	 */
	public final String getProperty() {
		return this.status.getExpression();
	}

	/**
	 * Retrieve the Errors instance that this tag is currently bound to.
	 * Intended for cooperating nesting tags.
	 * @return the current Errors instance, or null if none
	 */
	public final Errors getErrors() {
		return this.status.getErrors();
	}

	/**
	 * Retrieve the PropertyEditor for the property that this tag is
	 * currently bound to. Intended for cooperating nesting tags.
	 * @return the current PropertyEditor, or null if none
	 */
	public final PropertyEditor getEditor() {
		return this.status.getEditor();
	}

}
