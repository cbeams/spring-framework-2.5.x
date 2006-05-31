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
 * @since 2.0
 */
public class LabelTagTests extends AbstractFormTagTests {

	private LabelTag tag;

	protected void onSetUp() {
		this.tag = new LabelTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPath("name");
		this.tag.setPageContext(getPageContext());
	}


	public void testSimpleRender() throws Exception {
		int startResult = this.tag.doStartTag();
		int endResult = this.tag.doEndTag();

		assertEquals(Tag.EVAL_BODY_INCLUDE, startResult);
		assertEquals(Tag.EVAL_PAGE, endResult);

		String output = getWriter().toString();
		assertContainsAttribute(output, "for", "name");
		assertContainsAttribute(output, "name", "name.label");
		assertTrue(output.startsWith("<label "));
		assertTrue(output.endsWith("</label>"));
	}

	public void testOverrideFor() throws Exception {
		this.tag.setFor("myElement");
		int startResult = this.tag.doStartTag();
		int endResult = this.tag.doEndTag();

		assertEquals(Tag.EVAL_BODY_INCLUDE, startResult);
		assertEquals(Tag.EVAL_PAGE, endResult);

		String output = getWriter().toString();
		assertContainsAttribute(output, "for", "myElement");
		assertContainsAttribute(output, "name", "myElement.label");
		assertTrue(output.startsWith("<label "));
		assertTrue(output.endsWith("</label>"));
	}

	protected TestBean createTestBean() {
		return new TestBean();
	}
}
