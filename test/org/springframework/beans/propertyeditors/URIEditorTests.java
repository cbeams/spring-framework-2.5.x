/*
 * Copyright 2002-2007 the original author or authors.
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

import java.beans.PropertyEditor;
import java.net.URI;

import junit.framework.TestCase;

import org.springframework.core.JdkVersion;
import org.springframework.util.ClassUtils;

/**
 * @author Juergen Hoeller
 */
public class URIEditorTests extends TestCase {

	public void testStandardURI() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		PropertyEditor uriEditor = new URIEditor();
		uriEditor.setAsText("mailto:juergen.hoeller@interface21.com");
		Object value = uriEditor.getValue();
		assertTrue(value instanceof URI);
		URI uri = (URI) value;
		assertEquals(uri.toString(), uriEditor.getAsText());
	}

	public void testStandardURL() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		PropertyEditor uriEditor = new URIEditor();
		uriEditor.setAsText("http://www.springframework.org");
		Object value = uriEditor.getValue();
		assertTrue(value instanceof URI);
		URI uri = (URI) value;
		assertEquals(uri.toString(), uriEditor.getAsText());
	}

	public void testStandardURLWithWhitespace() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		PropertyEditor uriEditor = new URIEditor();
		uriEditor.setAsText("  http://www.springframework.org  ");
		Object value = uriEditor.getValue();
		assertTrue(value instanceof URI);
		URI uri = (URI) value;
		assertEquals(uri.toString(), uriEditor.getAsText());
	}

	public void testClasspathURL() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		PropertyEditor uriEditor = new URIEditor();
		uriEditor.setAsText("classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) +
				"/" + ClassUtils.getShortName(getClass()) + ".class");
		Object value = uriEditor.getValue();
		assertTrue(value instanceof URI);
		URI uri = (URI) value;
		assertEquals(uri.toString(), uriEditor.getAsText());
		assertTrue(!uri.getScheme().startsWith("classpath"));
	}

	public void testClasspathURLWithWidespace() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		PropertyEditor uriEditor = new URIEditor();
		uriEditor.setAsText("  classpath:" + ClassUtils.classPackageAsResourcePath(getClass()) +
				"/" + ClassUtils.getShortName(getClass()) + ".class  ");
		Object value = uriEditor.getValue();
		assertTrue(value instanceof URI);
		URI uri = (URI) value;
		assertEquals(uri.toString(), uriEditor.getAsText());
		assertTrue(!uri.getScheme().startsWith("classpath"));
	}

	public void testWithNonExistentResource() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		PropertyEditor uriEditor = new URIEditor();
		uriEditor.setAsText("gonna:/freak/in/the/morning/freak/in/the.evening");
		Object value = uriEditor.getValue();
		assertTrue(value instanceof URI);
		URI uri = (URI) value;
		assertEquals(uri.toString(), uriEditor.getAsText());
	}

	public void testSetAsTextWithNull() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		PropertyEditor uriEditor = new URIEditor();
		uriEditor.setAsText(null);
		assertNull(uriEditor.getValue());
		assertEquals("", uriEditor.getAsText());
	}

	public void testGetAsTextReturnsEmptyStringIfValueNotSet() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		PropertyEditor uriEditor = new URIEditor();
		assertEquals("", uriEditor.getAsText());
	}

}
