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
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.TagUtils;

/**
 * Tag for transforming reference data values from form controllers and
 * other objects inside a <code>spring:bind</code> tag.
 *
 * <p>The BindTag has a PropertyEditor that it uses to transform properties of
 * a bean to a String, useable in HTML forms. This tag uses that PropertyEditor
 * to transform objects passed into this tag.
 *
 * @author Alef Arendsen
 * @since 20.09.2003
 * @see BindTag
 */
public class TransformTag extends HtmlEscapingAwareTag {

	/** the value to transform using the appropriate property editor */
	private String value;

	/** the variable to put the result in */
	private String var;

	/** the scope of the variable the result will be put in */
	private String scope = TagUtils.SCOPE_PAGE;


	/**
	 * Set the value to finally transform using the appropriate
	 * PropertyEditor from the BindTag.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Set PageContext attribute name under which to expose
	 * a variable that contains the result of the transformation.
	 * @see #setScope
	 * @see javax.servlet.jsp.PageContext#setAttribute
	 */
	public void setVar(String var) {
		this.var = var;
	}

	/**
	 * Set the scope to export the variable to.
	 * Default is SCOPE_PAGE ("page").
	 * @see #setVar
	 * @see org.springframework.web.util.TagUtils#SCOPE_PAGE
	 * @see javax.servlet.jsp.PageContext#setAttribute
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}


	protected final int doStartTagInternal() throws JspException {
		Object resolvedValue = ExpressionEvaluationUtils.evaluate("value", this.value, Object.class, pageContext);
		if (resolvedValue != null) {
			// find the BingTag (if applicable)
			BindTag tag = (BindTag) TagSupport.findAncestorWithClass(this, BindTag.class);
			if (tag == null) {
				// the tag can only be used within a BindTag
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
			result = isHtmlEscape() ? HtmlUtils.htmlEscape(result) : result;
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

}
