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

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * Unit tests for the {@link FormTag} class.
 *
 * @author Rob Harrop
 * @author Rick Evans
 * @since 2.0
 */
public final class FormTagTests extends AbstractHtmlElementTagTests {
	
	private static final String REQUEST_URI = "/my/form";

	private static final String QUERY_STRING = "foo=bar";


	private FormTag tag;


	protected void onSetUp() {
		this.tag = new FormTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}


	protected void extendRequest(MockHttpServletRequest request) {
		request.setRequestURI(REQUEST_URI);
		request.setQueryString(QUERY_STRING);
	}


	public void testWriteForm() throws Exception {
		String action = "/form.html";
		String commandName = "myCommand";
		String name = "formName";
		String enctype = "my/enctype";
		String method = "POST";
		String onsubmit = "onsubmit";
		String onreset = "onreset";
		String cssClass = "myClass";
		String cssStyle = "myStyle";

		this.tag.setName(name);
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
		assertEquals("Command name not exposed", commandName, getPageContext().getRequest().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		this.tag.doFinally();
		assertNull("Command name not cleared after tag ends", getPageContext().getRequest().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME));

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
		assertContainsAttribute(output, "id", commandName);
		assertContainsAttribute(output, "name", name);
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
		assertEquals("Command name not exposed", commandName, getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME, PageContext.REQUEST_SCOPE));

		result = this.tag.doEndTag();
		assertEquals(Tag.EVAL_PAGE, result);

		this.tag.doFinally();
		assertNull("Command name not cleared after tag ends", getPageContext().getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME, PageContext.REQUEST_SCOPE));

		String output = getWriter().toString();
		assertFormTagOpened(output);
		assertFormTagClosed(output);

		assertContainsAttribute(output, "action", REQUEST_URI + "?" + QUERY_STRING);
		assertContainsAttribute(output, "enctype", enctype);
		assertContainsAttribute(output, "method", method);
		assertContainsAttribute(output, "onsubmit", onsubmit);
		assertContainsAttribute(output, "onreset", onreset);
		assertAttributeNotPresent(output, "name");
	}

	public void testWithNullResolvedCommand() throws Exception {
		this.tag.setCommandName("${null}");
		try {
			this.tag.doStartTag();
			fail("Must not be able to have a command name that resolves to null.");
		}
		catch (IllegalArgumentException expected) {
		}
	}


	private static void assertFormTagOpened(String output) {
		assertTrue(output.startsWith("<form "));
	}

	private static void assertFormTagClosed(String output) {
		assertTrue(output.endsWith("</form>"));
	}

}
