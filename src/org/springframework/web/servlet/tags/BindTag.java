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
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.springframework.context.NoSuchMessageException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.web.util.ExpressionEvaluationUtils;
import org.springframework.web.util.HtmlUtils;

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
public class BindTag extends RequestContextAwareTag {

	public static final String STATUS_VARIABLE_NAME = "status";

	private String path;

	private String property;

	private Errors errors;

	private PropertyEditor editor;

	/**
	 * Set the path that this tag should apply.
	 * Can be a bean (e.g. "person"), or a bean property
	 * (e.g. "person.name"), also supporting nested beans.
	 */
	public void setPath(String path) throws JspException {
		this.path = path;
	}

	/**
	 * Retrieve the path that this tag should apply to
	 * @return the path that this tag should apply to or <code>null</code>
	 * if it is not set
	 * @see #setPath(String)
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Retrieve the Errors instance that this tag is currently bound to.
	 * Intended for cooperating nesting tags.
	 * @return an instance of Errors
	 */
	public Errors getErrors() {
		return errors;
	}

	/**
	 * Retrieve the property that this tag is currently bound to,
	 * or null if bound to an object rather than a specific property.
	 * Intended for cooperating nesting tags.
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * Retrieve the property editor for the property that this tag is
	 * currently bound to. Intended for cooperating nesting tags.
	 * @return the property editor, or null if none applicable
	 */
	public PropertyEditor getEditor() {
		return editor;
	}

	protected int doStartTagInternal() throws Exception {
		String resolvedPath = ExpressionEvaluationUtils.evaluateString("path", this.path, pageContext);

		// determine name of the object and property
		String name = null;
		this.property = null;

		int dotPos = resolvedPath.indexOf('.');
		if (dotPos == -1) {
			// property not set, only the object itself
			name = resolvedPath;
		}
		else {
			name = resolvedPath.substring(0, dotPos);
			this.property = resolvedPath.substring(dotPos + 1);
		}

		// retrieve Errors object
		this.errors = getRequestContext().getErrors(name, false);
		if (this.errors == null) {
			throw new JspTagException("Could not find Errors instance for bean '" + name + "' in request: " +
			                          "add the Errors model to your ModelAndView via errors.getModel()");
		}

		List fes = null;
		Object value = null;

		if (this.property != null) {
			if ("*".equals(this.property)) {
				fes = this.errors.getAllErrors();
			}
			else {
				fes = this.errors.getFieldErrors(this.property);
				value = this.errors.getFieldValue(this.property);
				if (this.errors instanceof BindException) {
					this.editor = ((BindException) this.errors).getCustomEditor(this.property);
				}
				else {
					logger.warn("Cannot not expose custom property editor because Errors instance [" + this.errors +
											"] is not of type BindException");
				}
				if (isHtmlEscape() && value instanceof String) {
					value = HtmlUtils.htmlEscape((String)value);
				}
			}
		}
		else {
			fes = this.errors.getGlobalErrors();
		}

		// instantiate the status object
		BindStatus status = new BindStatus(this.property, value, getErrorCodes(fes), getErrorMessages(fes));
		this.pageContext.setAttribute(STATUS_VARIABLE_NAME, status);
		return EVAL_BODY_INCLUDE;
	}

	/**
	 * Extract the error codes from the given ObjectError list.
	 */
	private String[] getErrorCodes(List fes) {
		String[] codes = new String[fes.size()];
		for (int i = 0; i < fes.size(); i++) {
			ObjectError error = (ObjectError) fes.get(i);
			codes[i] = error.getCode();
		}
		return codes;
	}

	/**
	 * Extract the error messages from the given ObjectError list.
	 */
	private String[] getErrorMessages(List fes) throws NoSuchMessageException, JspException {
		String[] messages = new String[fes.size()];
		for (int i = 0; i < fes.size(); i++) {
			ObjectError error = (ObjectError) fes.get(i);
			messages[i] = getRequestContext().getMessage(error, isHtmlEscape());
		}
		return messages;
	}

	public void release() {
		super.release();
		this.path = null;
		this.errors = null;
		this.property = null;
	}

}
