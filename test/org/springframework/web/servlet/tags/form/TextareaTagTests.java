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
 */
public class TextareaTagTests extends AbstractFormTagTests {

	private TextareaTag tag;

	protected void onSetUp() {
		this.tag = new TextareaTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(getWriter());
			}
		};
		this.tag.setPageContext(getPageContext());
	}

	public void testSimpleBind() throws Exception {
		this.tag.setPath("name");
		assertEquals(Tag.EVAL_PAGE, this.tag.doStartTag());
		String output = getWriter().toString();
		assertContainsAttribute(output, "name", "name");
		assertBlockTagContains(output, "Rob");

	}

	public void testCustomBind() throws Exception {
		BeanPropertyBindingResult result = new BeanPropertyBindingResult(createTestBean(), "testBean");
		result.getPropertyAccessor().registerCustomEditor(Float.class, new SimpleFloatEditor());
		exposeBindingResult(result);
		this.tag.setPath("myFloat");
		assertEquals(Tag.EVAL_PAGE, this.tag.doStartTag());
		String output = getWriter().toString();
		assertContainsAttribute(output, "name", "myFloat");
		assertBlockTagContains(output, "12.34f");

	}

	protected TestBean createTestBean() {
		// set up test data
		TestBean rob = new TestBean();
		rob.setName("Rob");
		rob.setMyFloat(new Float(12.34));

		TestBean sally = new TestBean();
		sally.setName("Sally");
		rob.setSpouse(sally);

		return rob;
	}
}
