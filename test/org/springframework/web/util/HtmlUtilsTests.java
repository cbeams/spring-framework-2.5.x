/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.util;

import junit.framework.TestCase;

/**
 * @author Alef Arendsen
 */
public class HtmlUtilsTests extends TestCase {

	public void testHtmlEscape() {
		String unescaped = "\"This is a quote";
		String escaped = HtmlUtils.htmlEscape(unescaped);
		if (escaped.startsWith("&#")) {
			assertEquals("&#34;This is a quote",escaped);
		}
		else {
			assertEquals("&quot;This is a quote",escaped);
		}
	}

	public void testHtmlUnescape() {
		String escaped = "&quot;This is a quote";		
		String unescaped = HtmlUtils.htmlUnescape(escaped);
		assertEquals(unescaped, "\"This is a quote");
	}

}
