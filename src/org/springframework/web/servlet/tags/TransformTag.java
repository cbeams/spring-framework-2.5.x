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
import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.web.util.ExpressionEvaluationUtils;
import org.springframework.web.util.TagUtils;

/**
 * Tag useful for transforming reference data values from form controllers and
 * other objects inside a <code>spring:bind</code> tag.
 *
 * <p>The bind tag has a PropertyEditor that it used to transform the property
 * of bean to a String, useable in HTML forms. This tag uses that PropertyEditor
 * to transform objects passed into this tag.
 *
 * @author Alef Arendsen
 * @since 20.09.2003
 */
public class TransformTag extends RequestContextAwareTag {

	/** the value to transform using the appropriate property editor */
	private String value;

	/** the variable to put the result in */
	private String var;

	/** the scope of the variable the result will be put in */
	private String scope = TagUtils.SCOPE_PAGE;

	/**
	 * Set the value to finally transform using the appropriate property editor
	 * that has to be found in the BindTag.
	 * @param value the value
	 * @throws JspException if expression evaluation fails
	 */
	public void setValue(String value) throws JspException {
		this.value = value;
	}

	/**
	 * Set the name of the variable to which to export the result of the transformation.
	 * @param var the name of the variable
	 */
	public void setVar(String var) {
		this.var = var;
	}

	/**
	 * Set the scope to which to export the variable indicated.
	 * If the scope isn't one of the allowed scope, it'll be the default (SCOPE_PAGE).
	 * @param scope the scope (SCOPE_PAGE, SCOPE_REQUEST, SCOPE_APPLICATION or SCOPE_SESSION)
	 */
	public void setScope(String scope) throws JspException {
		this.scope = scope;
	}

	public int doStartTagInternal() throws JspException {
		Object resolvedValue = ExpressionEvaluationUtils.evaluate("value", this.value, Object.class, pageContext);
		if (resolvedValue != null) {
			// find the bingtag (if applicable)
			BindTag tag = (BindTag) TagSupport.findAncestorWithClass(this, org.springframework.web.servlet.tags.BindTag.class);
			if (tag == null) {
				// the tag can only be used inside a bind tag
				throw new JspException("TransformTag can only be used within BindTag");
			}
			// ok, get the property editor
			PropertyEditor editor = tag.getEditor();
			String result = null;
			if (editor != null) {
				// if an editor was found, edit the value
				editor.setValue(resolvedValue);
				result = editor.getAsText();
			}
			else {
				// else, just do a toString
				result = resolvedValue.toString();
			}
			String resolvedVar = ExpressionEvaluationUtils.evaluateString("var", this.var, pageContext);
			if (resolvedVar != null) {
				String resolvedScope = ExpressionEvaluationUtils.evaluateString("scope", this.scope, pageContext);
				pageContext.setAttribute(resolvedVar, result, TagUtils.getScope(resolvedScope));
			}
			else {
				try {
					// else, just print it out
					pageContext.getOut().print(result);
				}
				catch (IOException ex) {
					throw new JspException(ex);
				}
			}
		}
		return SKIP_BODY;
	}

	/**
	 * Releasing of resources.
	 */
	public void release() {
		this.scope = TagUtils.SCOPE_PAGE;
		this.var = null;
		this.value = null;
	}

}
