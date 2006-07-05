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

package org.springframework.beans.factory.xml.support;

import junit.framework.TestCase;

import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.UtilNamespaceHandler;

/**
 * @author Rob Harrop
 */
public class DefaultNamespaceHandlerResolverTests extends TestCase {

	public void testResolvedMappedHandler() {
		DefaultNamespaceHandlerResolver resolver = new DefaultNamespaceHandlerResolver(getClass().getClassLoader());
		NamespaceHandler handler = resolver.resolve("http://www.springframework.org/schema/util");
		assertNotNull("Handler should not be null.", handler);
		assertEquals("Incorrect handler loaded", UtilNamespaceHandler.class, handler.getClass());
	}

	public void testNonExistentHandlerClass() throws Exception {
		String mappingPath = "org/springframework/beans/factory/xml/support/nonExistent.properties";
		try {
			new DefaultNamespaceHandlerResolver(getClass().getClassLoader(), mappingPath);
			// pass
		}
		catch (Throwable ex) {
			fail("Non-existent handler classes should be ignored: " + ex);
		}
	}

	public void testResolveInvalidHandler() throws Exception {
		String mappingPath = "org/springframework/beans/factory/xml/support/invalid.properties";
		try {
			new DefaultNamespaceHandlerResolver(getClass().getClassLoader(), mappingPath);
			fail("Should not be able to map a class that doesn't implement NamespaceHandler");
		}
		catch (Throwable ex) {
			// success
		}
	}

}
