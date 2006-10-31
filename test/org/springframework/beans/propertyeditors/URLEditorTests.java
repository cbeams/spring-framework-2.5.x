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

package org.springframework.beans.propertyeditors;

import junit.framework.TestCase;
import org.springframework.test.AssertThrows;
import org.springframework.util.ClassUtils;

import java.beans.PropertyEditor;
import java.net.URL;

/**
 * Unit tests for the {@link URLEditor} class.
 *
 * @author Rick Evans
 */
public final class URLEditorTests extends TestCase {

	public void testSunnyDay() throws Exception {
		PropertyEditor urlEditor = new URLEditor();
		urlEditor.setAsText("classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) + "/" + ClassUtils.getShortName(getClass()) + ".class");
		Object value = urlEditor.getValue();
		assertTrue(value instanceof URL);
		URL url = (URL) value;
		assertEquals(url.toExternalForm(), urlEditor.getAsText());
	}

	public void testSetAsTextWithNull() throws Exception {
		PropertyEditor urlEditor = new URLEditor();
		urlEditor.setAsText(null);
		assertNull(urlEditor.getValue());
		assertEquals("", urlEditor.getAsText());
	}

	public void testGetAsTextReturnsEmptyStringIfValueNotSet() throws Exception {
		PropertyEditor urlEditor = new URLEditor();
		assertEquals("", urlEditor.getAsText());
	}

	public void testWithNonExistentResource() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				PropertyEditor urlEditor = new URLEditor();
				urlEditor.setAsText("gonna:/freak/in/the/morning/freak/in/the.evening");
			}
		}.runTest();
	}

	public void testCtorWithNullResourceEditor() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				new URLEditor(null);
			}
		}.runTest();
	}

}
