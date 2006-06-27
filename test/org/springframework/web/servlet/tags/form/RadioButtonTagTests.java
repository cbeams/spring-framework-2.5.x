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

import javax.servlet.jsp.tagext.Tag;

/**
 * @author Rob Harrop
 */
public class RadioButtonTagTests extends AbstractFormTagTests {

	private RadioButtonTag tag;

	protected void onSetUp() {
		this.tag = new RadioButtonTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}

	public void testWithCheckedValue() throws Exception {
		this.tag.setPath("sex");
		this.tag.setValue("M");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "sex");
		assertContainsAttribute(output, "type", "radio");
		assertContainsAttribute(output, "value", "M");
		assertContainsAttribute(output, "checked", "checked");
	}

	public void testWithCheckedObjectValue() throws Exception {
		this.tag.setPath("myFloat");
		this.tag.setValue(getFloat());
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "myFloat");
		assertContainsAttribute(output, "type", "radio");
		assertContainsAttribute(output, "value", getFloat().toString());
		assertContainsAttribute(output, "checked", "checked");
	}

	public void testWithUncheckedObjectValue() throws Exception {
		Float value = new Float("99.45");
		this.tag.setPath("myFloat");
		this.tag.setValue(value);
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "myFloat");
		assertContainsAttribute(output, "type", "radio");
		assertContainsAttribute(output, "value", value.toString());
		assertAttributeNotPresent(output, "checked");
	}

	public void testWithUncheckedValue() throws Exception {
		this.tag.setPath("sex");
		this.tag.setValue("F");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();
		assertTagOpened(output);
		assertTagClosed(output);
		assertContainsAttribute(output, "name", "sex");
		assertContainsAttribute(output, "type", "radio");
		assertContainsAttribute(output, "value", "F");
		assertAttributeNotPresent(output, "checked");
	}

	private void assertTagOpened(String output) {
		assertTrue(output.indexOf("<input ") > -1);
	}

	private void assertTagClosed(String output) {
		assertTrue(output.indexOf("/>") > -1);
	}

	private Float getFloat() {
		return new Float("12.99");
	}

	protected TestBean createTestBean() {
		TestBean bean = new TestBean();
		bean.setSex("M");
		bean.setMyFloat(getFloat());
		return bean;
	}


}
