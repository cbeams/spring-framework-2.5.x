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
 * Databinding-aware JSP tag for rendering an HTML '<code>input</code>'
 * element with a '<code>type</code>' of '<code>password</code>'.
 *
 * @author Rob Harrop
 * @author Rick Evans
 * @since 2.0
 */
public class PasswordInputTag extends InputTag {

	/**
	 * Returns '<code>password</code>' causing the rendered HTML '<code>input</code>'
	 * element to have a '<code>type</code>' of '<code>password</code>'.
	 */
	protected String getType() {
		return "password";
	}

	/**
	 * The {@link PasswordInputTag} never writes it's value (for security reasons).
	 */
	protected void writeValue(TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", "");
	}

}
