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
import org.springframework.web.servlet.support.JspAwareRequestContext;
import org.springframework.web.servlet.support.RequestContext;
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
public abstract class AbstractHtmlElementTagTests extends AbstractTagTests {

	private StringWriter writer;

	private MockPageContext pageContext;

	public static final String COMMAND_NAME = "testBean";

	protected StringWriter getWriter() {
		return this.writer;
	}

	protected MockPageContext getPageContext() {
		return this.pageContext;
	}

	protected final void assertContainsAttribute(String output, String attributeName, String attributeValue) {
		String attributeString = attributeName + "=\"" + attributeValue + "\"";
		assertTrue("Expected to find attribute '" + attributeName +
						"' with value '" + attributeValue +
						"' in output + '" + output + "'",
						output.indexOf(attributeString) > -1);
	}

	protected final void assertAttributeNotPresent(String output, String attributeName) {
		assertTrue("Unexpected attribute '" + attributeName + "' in output '" + output + "'.",
						output.indexOf(attributeName + "=\"") < 0);
	}

	protected final void assertBlockTagContains(String output, String desiredContents) {
		String contents = output.substring(output.indexOf(">") + 1, output.lastIndexOf("<"));
		assertTrue("Expected to find '" + desiredContents + "' in the contents of block tag '" + output + "'",
						contents.indexOf(desiredContents) > -1);
	}

	protected final RequestContext getRequestContext() {
		return (RequestContext) getPageContext().getAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE);
	}

	protected MockPageContext createAndPopulatePageContext() {
		MockPageContext pageContext = createPageContext();
		MockHttpServletRequest request = (MockHttpServletRequest) pageContext.getRequest();
		RequestContext requestContext = new JspAwareRequestContext(pageContext);
	  pageContext.setAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE, requestContext);
		extendRequest(request);
		extendPageContext(pageContext);
		return pageContext;
	}

	protected void extendPageContext(MockPageContext pageContext) {
	}

	protected void extendRequest(MockHttpServletRequest request) {
	}

	protected final void setUp() throws Exception {
		// set up a writer for the tag content to be written to
		this.writer = new StringWriter();

		// configure the page context
		this.pageContext = createAndPopulatePageContext();

		onSetUp();
	}

	protected void onSetUp(){}

	protected void exposeBindingResult(Errors errors) {
		// wrap errors in a Model
		Map model = new HashMap();
		model.put(BindingResult.MODEL_KEY_PREFIX + COMMAND_NAME, errors);

		// replace the request context with one containing the errors
		MockPageContext pageContext = getPageContext();
		RequestContext context = new RequestContext((HttpServletRequest) pageContext.getRequest(), model);
		pageContext.setAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE, context);
	}
}
