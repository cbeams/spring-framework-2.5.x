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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.springframework.beans.TestBean;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;

/**
 * @author Seth Ladd
 * @author Juergen Hoeller
 * @since 28.07.2004
 */
public class NestedPathTestSuite extends AbstractTagTests {

	public void testDoEndTag() throws JspException {
		PageContext pc = createPageContext();
		NestedPathTag tag = new NestedPathTag();
		tag.setPath("foo");
		tag.setPageContext(pc);
		tag.doStartTag();
		int returnValue = tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, returnValue);
		assertEquals("", pc.getAttribute(NestedPathTag.NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE));
	}

	public void testDoEndTagWithNesting() throws JspException {
		PageContext pc = createPageContext();
		NestedPathTag tag = new NestedPathTag();
		tag.setPath("foo");
		tag.setPageContext(pc);
		tag.doStartTag();

		NestedPathTag anotherTag = new NestedPathTag();
		anotherTag.setPageContext(pc);
		anotherTag.setPath("bar");
		anotherTag.doStartTag();
		anotherTag.doEndTag();

		assertEquals("foo.", pc.getAttribute(NestedPathTag.NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE));

		tag.doEndTag();
		assertEquals("", pc.getAttribute(NestedPathTag.NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE));
	}

	public void testDoStartTagInternal() throws JspException {
		PageContext pc = createPageContext();
		NestedPathTag tag = new NestedPathTag();
		tag.setPath("foo");
		tag.setPageContext(pc);
		int returnValue = tag.doStartTag();

		assertEquals(Tag.EVAL_BODY_INCLUDE, returnValue);
		assertEquals("foo.", pc.getAttribute(NestedPathTag.NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE));
	}

	public void testDoStartTagInternalWithNesting() throws JspException {
		PageContext pc = createPageContext();
		NestedPathTag tag = new NestedPathTag();
		tag.setPath("foo");
		tag.setPageContext(pc);
		tag.doStartTag();
		assertEquals("foo.", pc.getAttribute(NestedPathTag.NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE));

		NestedPathTag anotherTag = new NestedPathTag();
		anotherTag.setPageContext(pc);
		anotherTag.setPath("bar");
		anotherTag.doStartTag();

		assertEquals("foo.bar.", pc.getAttribute(NestedPathTag.NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE));

		NestedPathTag yetAnotherTag = new NestedPathTag();
		yetAnotherTag.setPageContext(pc);
		yetAnotherTag.setPath("boo");
		yetAnotherTag.doStartTag();

		assertEquals("foo.bar.boo.", pc.getAttribute(NestedPathTag.NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE));

		yetAnotherTag.doEndTag();

		NestedPathTag andAnotherTag = new NestedPathTag();
		andAnotherTag.setPageContext(pc);
		andAnotherTag.setPath("boo2");
		andAnotherTag.doStartTag();

		assertEquals("foo.bar.boo2.", pc.getAttribute(NestedPathTag.NESTED_PATH_VARIABLE_NAME, PageContext.REQUEST_SCOPE));
	}

	public void testWithBindTag() throws JspException {
		PageContext pc = createPageContext();
		BindException errors = new ServletRequestDataBinder(new TestBean(), "tb").getErrors();
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb", errors);

		NestedPathTag tag = new NestedPathTag();
		tag.setPath("tb");
		tag.setPageContext(pc);
		tag.doStartTag();

		BindTag bindTag = new BindTag();
		bindTag.setPageContext(pc);
		bindTag.setPath("name");

		assertTrue("Correct doStartTag return value", bindTag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertEquals("tb.name", status.getPath());
	}

	public void testWithBindTagWithIgnoreNestedPath() throws JspException {
		PageContext pc = createPageContext();
		BindException errors = new ServletRequestDataBinder(new TestBean(), "tb2").getErrors();
		pc.getRequest().setAttribute(BindException.ERROR_KEY_PREFIX + "tb2", errors);

		NestedPathTag tag = new NestedPathTag();
		tag.setPath("tb");
		tag.setPageContext(pc);
		tag.doStartTag();

		BindTag bindTag = new BindTag();
		bindTag.setPageContext(pc);
		bindTag.setIgnoreNestedPath(true);
		bindTag.setPath("tb2.name");

		assertTrue("Correct doStartTag return value", bindTag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		BindStatus status = (BindStatus) pc.getAttribute(BindTag.STATUS_VARIABLE_NAME);
		assertTrue("Has status variable", status != null);
		assertEquals("tb2.name", status.getPath());
	}

}
