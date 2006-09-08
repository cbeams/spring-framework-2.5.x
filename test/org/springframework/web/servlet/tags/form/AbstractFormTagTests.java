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

import org.springframework.beans.TestBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPageContext;

import javax.servlet.jsp.PageContext;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AbstractFormTagTests extends AbstractHtmlElementTagTests {

	protected void extendPageContext(MockPageContext pageContext) {
		pageContext.setAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME, COMMAND_NAME, PageContext.REQUEST_SCOPE);
	}

	protected void extendRequest(MockHttpServletRequest request) {
		request.setAttribute(AbstractFormTagTests.COMMAND_NAME, createTestBean());
	}

	protected abstract TestBean createTestBean();

}
