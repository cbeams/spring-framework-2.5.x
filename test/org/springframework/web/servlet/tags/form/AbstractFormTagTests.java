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

import org.springframework.web.servlet.tags.AbstractTagTests;
import org.springframework.web.servlet.tags.RequestContextAwareTag;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.beans.TestBean;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.Errors;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AbstractFormTagTests extends AbstractHtmlElementTagTests {

	public static final String COMMAND_NAME = "testBean";

	protected void extendPageContext(MockPageContext pageContext) {
		pageContext.setAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME, COMMAND_NAME);
	}

	protected void extendRequest(MockHttpServletRequest request) {
		request.setAttribute(AbstractFormTagTests.COMMAND_NAME, createTestBean());
	}

	protected abstract TestBean createTestBean();

	protected void exposeErrors(Errors errors) {
		// wrap errors in a Model
		Map model = new HashMap();
		model.put(BindingResult.MODEL_KEY_PREFIX + COMMAND_NAME, errors);

		// replace the request context with one containing the errors
		MockPageContext pageContext = getPageContext();
		RequestContext context = new RequestContext((HttpServletRequest) pageContext.getRequest(), model);
		pageContext.setAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE, context);
	}
}
