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

import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.jsp.tagext.Tag;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class FormTagTests extends AbstractHtmlElementTagTests {

	private FormTag tag;

	protected void onSetUp() {
		this.tag = new FormTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}

	public void testWriteForm() throws Exception {
		String action = "/form.html";
		String commandName = "myCommand";
		String enctype = "my/enctype";
		String method = "POST";
		String onsubmit = "onsubmit";
		String onreset = "onreset";
		String cssClass = "myClass";
		String cssStyle = "myStyle";

		this.tag.setCssClass(cssClass);
		this.tag.setCssStyle(cssStyle);
		this.tag.setAction(action);
		this.tag.setCommandName(commandName);
		this.tag.setEnctype(enctype);
		this.tag.setMethod(method);
		this.tag.setOnsubmit(onsubmit);
		this.tag.setOnreset(onreset);

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);
		assertEquals("Command name not exposed", commandName, getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		this.tag.doFinally();
		assertNull("Command name not cleared after tag ends", getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		String output = getWriter().toString();
		assertFormTagOpened(output);
		assertFormTagClosed(output);

		assertContainsAttribute(output, "class", cssClass);
		assertContainsAttribute(output, "style", cssStyle);
		assertContainsAttribute(output, "action", action);
		assertContainsAttribute(output, "enctype", enctype);
		assertContainsAttribute(output, "method", method);
		assertContainsAttribute(output, "onsubmit", onsubmit);
		assertContainsAttribute(output, "onreset", onreset);
		assertContainsAttribute(output, "name", commandName);
		assertContainsAttribute(output, "id", commandName);
	}

	public void testWriteFormWithName() throws Exception {
		String action = "/form.html";
		String commandName = "myCommand";
		String enctype = "my/enctype";
		String method = "POST";
		String onsubmit = "onsubmit";
		String onreset = "onreset";
		String cssClass = "myClass";
		String cssStyle = "myStyle";
		String name = "myName";

		this.tag.setCssClass(cssClass);
		this.tag.setCssStyle(cssStyle);
		this.tag.setAction(action);
		this.tag.setCommandName(commandName);
		this.tag.setName(name);
		this.tag.setEnctype(enctype);
		this.tag.setMethod(method);
		this.tag.setOnsubmit(onsubmit);
		this.tag.setOnreset(onreset);

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);
		assertEquals("Command name not exposed", commandName, getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		this.tag.doFinally();
		assertNull("Command name not cleared after tag ends", getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		String output = getWriter().toString();
		assertFormTagOpened(output);
		assertFormTagClosed(output);

		assertContainsAttribute(output, "class", cssClass);
		assertContainsAttribute(output, "style", cssStyle);
		assertContainsAttribute(output, "action", action);
		assertContainsAttribute(output, "enctype", enctype);
		assertContainsAttribute(output, "method", method);
		assertContainsAttribute(output, "onsubmit", onsubmit);
		assertContainsAttribute(output, "onreset", onreset);
		assertContainsAttribute(output, "name", name);
		assertContainsAttribute(output, "id", name);
	}

	public void testWriteFormWithNameAndId() throws Exception {
		String action = "/form.html";
		String commandName = "myCommand";
		String enctype = "my/enctype";
		String method = "POST";
		String onsubmit = "onsubmit";
		String onreset = "onreset";
		String cssClass = "myClass";
		String cssStyle = "myStyle";
		String name = "myName";
		String id = "myId";

		this.tag.setId(id);
		this.tag.setCssClass(cssClass);
		this.tag.setCssStyle(cssStyle);
		this.tag.setAction(action);
		this.tag.setCommandName(commandName);
		this.tag.setName(name);
		this.tag.setEnctype(enctype);
		this.tag.setMethod(method);
		this.tag.setOnsubmit(onsubmit);
		this.tag.setOnreset(onreset);

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);
		assertEquals("Command name not exposed", commandName, getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		this.tag.doFinally();
		assertNull("Command name not cleared after tag ends", getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		String output = getWriter().toString();
		assertFormTagOpened(output);
		assertFormTagClosed(output);

		assertContainsAttribute(output, "class", cssClass);
		assertContainsAttribute(output, "style", cssStyle);
		assertContainsAttribute(output, "action", action);
		assertContainsAttribute(output, "enctype", enctype);
		assertContainsAttribute(output, "method", method);
		assertContainsAttribute(output, "onsubmit", onsubmit);
		assertContainsAttribute(output, "onreset", onreset);
		assertContainsAttribute(output, "name", name);
		assertContainsAttribute(output, "id", id);
	}

	public void testWithActionFromRequest() throws Exception {
		String commandName = "myCommand";
		String enctype = "my/enctype";
		String method = "POST";
		String onsubmit = "onsubmit";
		String onreset = "onreset";

		this.tag.setCommandName(commandName);
		this.tag.setEnctype(enctype);
		this.tag.setMethod(method);
		this.tag.setOnsubmit(onsubmit);
		this.tag.setOnreset(onreset);

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_BODY_INCLUDE, result);
		assertEquals("Command name not exposed", commandName, getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		this.tag.doFinally();
		assertNull("Command name not cleared after tag ends", getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		String output = getWriter().toString();
		assertFormTagOpened(output);
		assertFormTagClosed(output);

		assertContainsAttribute(output, "action", getRequestUri() + "?" + getQueryString());
		assertContainsAttribute(output, "enctype", enctype);
		assertContainsAttribute(output, "method", method);
		assertContainsAttribute(output, "onsubmit", onsubmit);
		assertContainsAttribute(output, "onreset", onreset);
	}

	public void testWithNullResolvedCommand() throws Exception {
		this.tag.setCommandName("${null}");
		try {
			this.tag.doStartTag();
			fail("Should not be able to have a command name that resolves to null");
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}

	private void assertFormTagOpened(String output) {
		assertTrue(output.startsWith("<form "));
	}

	private void assertFormTagClosed(String output) {
		assertTrue(output.endsWith("</form>"));
	}

	protected void extendRequest(MockHttpServletRequest request) {
		request.setRequestURI(getRequestUri());
		request.setQueryString(getQueryString());
	}

	private String getRequestUri() {
		return "/my/form";
	}

	private String getQueryString() {
		return "foo=bar";
	}
}
