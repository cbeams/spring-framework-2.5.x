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
import org.springframework.mock.web.MockPageContext;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.BodyTag;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class ErrorsTagTests extends AbstractHtmlElementTagTests {

	public static final String COMMAND_NAME = "testBean";

	private ErrorsTag tag;

	protected void onSetUp() {
		this.tag = new ErrorsTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPath("name");
		this.tag.setPageContext(getPageContext());
	}

	public void testWithErrors() throws Exception {
		// construct an errors instance of the tag
		TestBean target = new TestBean();
		target.setName("Rob Harrop");
		Errors errors = new BindException(target, COMMAND_NAME);
		errors.rejectValue("name", "some.code", "Default Message");
		errors.rejectValue("name", "too.short", "Too Short");

		exposeBindingResult(errors);

		int result = this.tag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertSpanTagOpened(output);
		assertSpanTagClosed(output);

		assertContainsAttribute(output, "name", "name.errors");
		assertBlockTagContains(output, "<br/>");
		assertBlockTagContains(output, "Default Message");
		assertBlockTagContains(output, "Too Short");
	}

	public void testWithoutErrors() throws Exception {
		Errors errors = new BindException(new TestBean(), "COMMAND_NAME");
		exposeBindingResult(errors);
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertEquals(0, output.length());
	}

	public void testAsBodyTag() throws Exception {
		Errors errors = new BindException(new TestBean(), "COMMAND_NAME");
		errors.rejectValue("name", "some.code", "Default Message");
		errors.rejectValue("name", "too.short", "Too Short");
		exposeBindingResult(errors);
		int result = this.tag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);
		String bodyContent = "Foo";
		this.tag.setBodyContent(new MockBodyContent(bodyContent, getWriter()));
		this.tag.doEndTag();
		assertEquals(bodyContent, getWriter().toString());

	}

	private void assertSpanTagOpened(String output) {
		assertTrue(output.startsWith("<span "));
	}

	private void assertSpanTagClosed(String output) {
		assertTrue(output.endsWith("</span>"));
	}

	protected void exposeBindingResult(Errors errors) {
		// wrap errors in a Model
		Map model = new HashMap();
		model.put(BindException.ERROR_KEY_PREFIX + COMMAND_NAME, errors);

		// replace the request context with one containing the errors
		MockPageContext pageContext = getPageContext();
		RequestContext context = new RequestContext((HttpServletRequest) pageContext.getRequest(), model);
		pageContext.setAttribute(RequestContextAwareTag.REQUEST_CONTEXT_PAGE_ATTRIBUTE, context);
	}

	protected void extendPageContext(MockPageContext pageContext) {
		pageContext.getRequest().setAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME, COMMAND_NAME);
	}

}
