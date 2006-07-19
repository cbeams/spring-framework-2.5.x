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
import org.springframework.validation.BeanPropertyBindingResult;

import javax.servlet.jsp.tagext.Tag;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class HiddenInputTagTests extends AbstractFormTagTests {

	private HiddenInputTag tag;

	private TestBean bean;

	protected void onSetUp() {
		this.tag = new HiddenInputTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}

	public void testRender() throws Exception {
		this.tag.setPath("name");
		int result = this.tag.doStartTag();
		assertEquals(Tag.EVAL_PAGE, result);

		String output = getWriter().toString();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", "hidden");
		assertContainsAttribute(output, "value", "Sally Greenwood");
	}

	public void testWithCustomBinder() throws Exception {
		this.tag.setPath("myFloat");

		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(this.bean, COMMAND_NAME);
		errors.getPropertyAccessor().registerCustomEditor(Float.class, new SimpleFloatEditor());
		exposeBindingResult(errors);

		assertEquals(Tag.EVAL_PAGE, this.tag.doStartTag());

		String output = getWriter().toString();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", "hidden");
		assertContainsAttribute(output, "value", "12.34f");
	}

	private void assertTagClosed(String output) {
		assertTrue(output.endsWith("/>"));
	}

	private void assertTagOpened(String output) {
		assertTrue(output.startsWith("<input "));
	}

	protected TestBean createTestBean() {
		this.bean = new TestBean();
		bean.setName("Sally Greenwood");
		bean.setMyFloat(new Float("12.34"));
		return bean;
	}
}
