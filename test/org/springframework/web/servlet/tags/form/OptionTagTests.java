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

import org.springframework.beans.Colour;
import org.springframework.beans.TestBean;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.util.StringUtils;

import javax.servlet.jsp.tagext.Tag;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

/**
 * @author Rob Harrop
 */
public class OptionTagTests extends AbstractHtmlElementTagTests {

	private static final String ARRAY_SOURCE = "abc,123,def";

	private static final String[] ARRAY = StringUtils.commaDelimitedListToStringArray(ARRAY_SOURCE);

	private OptionTag tag;

	protected void onSetUp() {
		this.tag = new OptionTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setParent(new SelectTag());
		this.tag.setPageContext(getPageContext());
	}

	public void testRenderNotSelected() throws Exception {
		getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, new BindStatus(getRequestContext(), "testBean.name", false));
		this.tag.setValue("bar");
		this.tag.setLabel("Bar");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		assertOptionTagOpened(output);
		assertOptionTagClosed(output);
		assertContainsAttribute(output, "value", "bar");
		assertBlockTagContains(output, "Bar");
	}

	public void testRenderSelected() throws Exception {
		getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, new BindStatus(getRequestContext(), "testBean.name", false));
		this.tag.setValue("foo");
		this.tag.setLabel("Foo");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		assertOptionTagOpened(output);
		assertOptionTagClosed(output);
		assertContainsAttribute(output, "value", "foo");
		assertContainsAttribute(output, "selected", "selected");
		assertBlockTagContains(output, "Foo");
	}

	public void testWithNoLabel() throws Exception {
		getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, new BindStatus(getRequestContext(), "testBean.name", false));
		this.tag.setValue("bar");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		assertOptionTagOpened(output);
		assertOptionTagClosed(output);
		assertAttributeNotPresent(output, "value");
		assertBlockTagContains(output, "bar");
	}

	public void testWithoutContext() throws Exception {
		this.tag.setParent(null);
		this.tag.setValue("foo");
		this.tag.setLabel("Foo");
		try {
			int result = this.tag.doStartTag();
			fail("Shouldn't be able to use <option> tag without exposed context.");
		}
		catch (IllegalStateException e) {
			// success
		}
	}

	public void testWithEnum() throws Exception {
		getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, new BindStatus(getRequestContext(), "testBean.favouriteColour", false));

		String value = Colour.GREEN.getCode().toString();
		String label = Colour.GREEN.getLabel();

		this.tag.setValue(value);
		this.tag.setLabel(label);

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		assertOptionTagOpened(output);
		assertOptionTagClosed(output);
		assertContainsAttribute(output, "value", value);
		assertContainsAttribute(output, "selected", "selected");
		assertBlockTagContains(output, label);
	}

	public void testWithEnumNotSelected() throws Exception {
		getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, new BindStatus(getRequestContext(), "testBean.favouriteColour", false));

		String value = Colour.BLUE.getCode().toString();
		String label = Colour.BLUE.getLabel();

		this.tag.setValue(value);
		this.tag.setLabel(label);

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		assertOptionTagOpened(output);
		assertOptionTagClosed(output);
		assertContainsAttribute(output, "value", value);
		assertAttributeNotPresent(output, "selected");
		assertBlockTagContains(output, label);
	}

	public void testWithPropertyEditor() throws Exception {
		BindStatus bindStatus = new BindStatus(getRequestContext(), "testBean.stringArray", false) {
			public PropertyEditor getEditor() {
				return new StringArrayPropertyEditor();
			}
		};
		getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, bindStatus);

		this.tag.setValue(ARRAY_SOURCE);
		this.tag.setLabel("someArray");

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		assertOptionTagOpened(output);
		assertOptionTagClosed(output);
		assertContainsAttribute(output, "value", ARRAY_SOURCE);
		assertContainsAttribute(output, "selected", "selected");
		assertBlockTagContains(output, "someArray");

	}

	public void testWithPropertyEditorStringComparison() throws Exception {
		BindStatus bindStatus = new BindStatus(getRequestContext(), "testBean.spouse", false) {
			public PropertyEditor getEditor() {
				return new PropertyEditorSupport() {
					public void setAsText(String text) throws IllegalArgumentException {
						setValue(new TestBean(text, 123));
					}

					public String getAsText() {
						return ((TestBean)getValue()).getName();
					}
				};
			}
		};
		getPageContext().setAttribute(SelectTag.LIST_VALUE_PAGE_ATTRIBUTE, bindStatus);

		this.tag.setValue("Sally");

		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		assertOptionTagOpened(output);
		assertOptionTagClosed(output);
		assertAttributeNotPresent(output, "value");
		assertContainsAttribute(output, "selected", "selected");
		assertBlockTagContains(output, "Sally");
	}

	private void assertOptionTagOpened(String output) {
		assertTrue(output.startsWith("<option"));
	}

	private void assertOptionTagClosed(String output) {
		assertTrue(output.endsWith("</option>"));
	}

	protected void extendRequest(MockHttpServletRequest request) {
		TestBean bean = new TestBean();
		bean.setName("foo");
		bean.setFavouriteColour(Colour.GREEN);
		bean.setStringArray(ARRAY);
		bean.setSpouse(new TestBean("Sally"));
		request.setAttribute("testBean", bean);
	}
}
