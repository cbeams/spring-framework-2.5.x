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

import javax.servlet.jsp.tagext.Tag;
import java.io.StringWriter;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class PasswordInputTagTests extends InputTagTests {

	/**
	 * http://opensource.atlassian.com/projects/spring/browse/SPR-2866
	 */
	public void testPasswordValueIsNeverRendered() throws Exception {
		this.getTag().setPath("name");

		assertEquals(Tag.EVAL_PAGE, this.getTag().doStartTag());

		String output = getWriter().toString();

		assertTagOpened(output);
		assertTagClosed(output);

		assertContainsAttribute(output, "type", getType());
		assertContainsAttribute(output, "value", "");
	}

	protected String getType() {
		return "password";
	}

	protected InputTag createTag(final StringWriter writer) {
		return new PasswordInputTag() {
			protected TagWriter createTagWriter() {
				return new TagWriter(writer);
			}
		};
	}
}
