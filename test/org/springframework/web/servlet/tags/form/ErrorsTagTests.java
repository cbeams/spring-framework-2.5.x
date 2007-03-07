/*
 * Copyright 2002-2007 the original author or authors.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import org.springframework.beans.TestBean;
import org.springframework.mock.web.MockPageContext;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.tags.RequestContextAwareTag;

/**
 * @author Rob Harrop
 * @author Rick Evans
 * @author Juergen Hoeller
 */
public class ErrorsTagTests extends AbstractHtmlElementTagTests {

	private static final String COMMAND_NAME = "testBean";

	private ErrorsTag tag;


	protected void onSetUp() {
		this.tag = new ErrorsTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPath("name");
		this.tag.setPageContext(getPageContext());
		this.tag.setParent(new FormTag());
	}


	public void testWithExplicitNonWhitespaceBodyContent() throws Exception {
		String mockContent = "This is some explicit body content";
		this.tag.setBodyContent(new MockBodyContent(mockContent, getWriter()));

		// construct an errors instance of the tag
		TestBean target = new TestBean();
		target.setName("Rob Harrop");
		Errors errors = new BindException(target, COMMAND_NAME);
		errors.rejectValue("name", "some.code", "Default Message");

		exposeBindingResult(errors);

		int result = this.tag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);
		assertEquals(mockContent, getWriter().toString());
	}

	public void testWithExplicitWhitespaceBodyContent() throws Exception {
		this.tag.setBodyContent(new MockBodyContent("\t\n   ", getWriter()));

		// construct an errors instance of the tag
		TestBean target = new TestBean();
		target.setName("Rob Harrop");
		Errors errors = new BindException(target, COMMAND_NAME);
		errors.rejectValue("name", "some.code", "Default Message");

		exposeBindingResult(errors);

		int result = this.tag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertElementTagOpened(output);
		assertElementTagClosed(output);

		assertContainsAttribute(output, "id", "name.errors");
		assertBlockTagContains(output, "Default Message");
	}

	public void testWithExplicitEmptyWhitespaceBodyContent() throws Exception {
		this.tag.setBodyContent(new MockBodyContent("", getWriter()));

		// construct an errors instance of the tag
		TestBean target = new TestBean();
		target.setName("Rob Harrop");
		Errors errors = new BindException(target, COMMAND_NAME);
		errors.rejectValue("name", "some.code", "Default Message");

		exposeBindingResult(errors);

		int result = this.tag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertElementTagOpened(output);
		assertElementTagClosed(output);

		assertContainsAttribute(output, "id", "name.errors");
		assertBlockTagContains(output, "Default Message");
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
		assertElementTagOpened(output);
		assertElementTagClosed(output);

		assertContainsAttribute(output, "id", "name.errors");
		assertBlockTagContains(output, "<br/>");
		assertBlockTagContains(output, "Default Message");
		assertBlockTagContains(output, "Too Short");
	}

	public void testWithEscapedErrors() throws Exception {
		this.tag.setHtmlEscape("true");

		// construct an errors instance of the tag
		TestBean target = new TestBean();
		target.setName("Rob Harrop");
		Errors errors = new BindException(target, COMMAND_NAME);
		errors.rejectValue("name", "some.code", "Default <> Message");
		errors.rejectValue("name", "too.short", "Too & Short");

		exposeBindingResult(errors);

		int result = this.tag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertElementTagOpened(output);
		assertElementTagClosed(output);

		assertContainsAttribute(output, "id", "name.errors");
		assertBlockTagContains(output, "<br/>");
		assertBlockTagContains(output, "Default &lt;&gt; Message");
		assertBlockTagContains(output, "Too &amp; Short");
	}

	public void testWithErrorsAndCustomElement() throws Exception {
		// construct an errors instance of the tag
		TestBean target = new TestBean();
		target.setName("Rob Harrop");
		Errors errors = new BindException(target, COMMAND_NAME);
		errors.rejectValue("name", "some.code", "Default Message");
		errors.rejectValue("name", "too.short", "Too Short");

		exposeBindingResult(errors);

		this.tag.setElement("div");
		int result = this.tag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertElementTagOpened(output);
		assertElementTagClosed(output);

		assertContainsAttribute(output, "id", "name.errors");
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

	public void testWithoutErrorsInstance() throws Exception {
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
		assertNotNull(getPageContext().getAttribute(ErrorsTag.MESSAGES_ATTRIBUTE));
		String bodyContent = "Foo";
		this.tag.setBodyContent(new MockBodyContent(bodyContent, getWriter()));
		this.tag.doEndTag();
		this.tag.doFinally();
		assertEquals(bodyContent, getWriter().toString());
		assertNull(getPageContext().getAttribute(ErrorsTag.MESSAGES_ATTRIBUTE));
	}

	public void testAsBodyTagWithExistingMessagesAttribute() throws Exception {
		String existingAttribute = "something";
		getPageContext().setAttribute(ErrorsTag.MESSAGES_ATTRIBUTE, existingAttribute);
		Errors errors = new BindException(new TestBean(), "COMMAND_NAME");
		errors.rejectValue("name", "some.code", "Default Message");
		errors.rejectValue("name", "too.short", "Too Short");
		exposeBindingResult(errors);
		int result = this.tag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);
		assertNotNull(getPageContext().getAttribute(ErrorsTag.MESSAGES_ATTRIBUTE));
		assertTrue(getPageContext().getAttribute(ErrorsTag.MESSAGES_ATTRIBUTE) instanceof List);
		String bodyContent = "Foo";
		this.tag.setBodyContent(new MockBodyContent(bodyContent, getWriter()));
		this.tag.doEndTag();
		this.tag.doFinally();
		assertEquals(bodyContent, getWriter().toString());
		assertEquals(existingAttribute, getPageContext().getAttribute(ErrorsTag.MESSAGES_ATTRIBUTE));
	}

	/**
	 * http://opensource.atlassian.com/projects/spring/browse/SPR-2788
	 */
	public void testAsBodyTagWithErrorsAndExistingMessagesAttributeInNonPageScopeAreNotClobbered() throws Exception {
		String existingAttribute = "something";
		getPageContext().setAttribute(ErrorsTag.MESSAGES_ATTRIBUTE, existingAttribute, PageContext.APPLICATION_SCOPE);
		Errors errors = new BindException(new TestBean(), "COMMAND_NAME");
		errors.rejectValue("name", "some.code", "Default Message");
		errors.rejectValue("name", "too.short", "Too Short");
		exposeBindingResult(errors);
		int result = this.tag.doStartTag();
		assertEquals(BodyTag.EVAL_BODY_BUFFERED, result);
		assertNotNull(getPageContext().getAttribute(ErrorsTag.MESSAGES_ATTRIBUTE));
		assertTrue(getPageContext().getAttribute(ErrorsTag.MESSAGES_ATTRIBUTE) instanceof List);
		String bodyContent = "Foo";
		this.tag.setBodyContent(new MockBodyContent(bodyContent, getWriter()));
		this.tag.doEndTag();
		this.tag.doFinally();
		assertEquals(bodyContent, getWriter().toString());
		assertEquals(existingAttribute,
				getPageContext().getAttribute(ErrorsTag.MESSAGES_ATTRIBUTE, PageContext.APPLICATION_SCOPE));
	}

	/**
	 * http://opensource.atlassian.com/projects/spring/browse/SPR-2788
	 */
	public void testAsBodyTagWithNoErrorsAndExistingMessagesAttributeInApplicationScopeAreNotClobbered() throws Exception {
		assertWhenNoErrorsExistingMessagesInScopeAreNotClobbered(PageContext.APPLICATION_SCOPE);
	}

	/**
	 * http://opensource.atlassian.com/projects/spring/browse/SPR-2788
	 */
	public void testAsBodyTagWithNoErrorsAndExistingMessagesAttributeInSessionScopeAreNotClobbered() throws Exception {
		assertWhenNoErrorsExistingMessagesInScopeAreNotClobbered(PageContext.SESSION_SCOPE);
	}

	/**
	 * http://opensource.atlassian.com/projects/spring/browse/SPR-2788
	 */
	public void testAsBodyTagWithNoErrorsAndExistingMessagesAttributeInPageScopeAreNotClobbered() throws Exception {
		assertWhenNoErrorsExistingMessagesInScopeAreNotClobbered(PageContext.PAGE_SCOPE);
	}

	/**
	 * http://opensource.atlassian.com/projects/spring/browse/SPR-2788
	 */
	public void testAsBodyTagWithNoErrorsAndExistingMessagesAttributeInRequestScopeAreNotClobbered() throws Exception {
		assertWhenNoErrorsExistingMessagesInScopeAreNotClobbered(PageContext.REQUEST_SCOPE);
	}

	private void assertWhenNoErrorsExistingMessagesInScopeAreNotClobbered(int scope) throws JspException {
		String existingAttribute = "something";
		getPageContext().setAttribute(ErrorsTag.MESSAGES_ATTRIBUTE, existingAttribute, scope);

		Errors errors = new BindException(new TestBean(), "COMMAND_NAME");
		exposeBindingResult(errors);
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertEquals(0, output.length());

		assertEquals(existingAttribute, getPageContext().getAttribute(ErrorsTag.MESSAGES_ATTRIBUTE, scope));
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

	private void assertElementTagOpened(String output) {
		assertTrue(output.startsWith("<" + this.tag.getElement() + " "));
	}

	private void assertElementTagClosed(String output) {
		assertTrue(output.endsWith("</" + this.tag.getElement() + ">"));
	}

}
