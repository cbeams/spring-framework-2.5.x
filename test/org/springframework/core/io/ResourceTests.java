/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.core.io;

import java.io.IOException;

import junit.framework.TestCase;

import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.ServletContextResource;

/**
 * @author Juergen Hoeller
 * @since 09.09.2004
 */
public class ResourceTests extends TestCase {

	public void doTestResource() throws IOException {
		Resource resource = new ClassPathResource("org/springframework/core/io/Resource.class");
		doTestResource(resource);
	}

	public void testClassPathResourceWithClassLoader() throws IOException {
		Resource resource =
				new ClassPathResource("org/springframework/core/io/Resource.class", getClass().getClassLoader());
		doTestResource(resource);
	}

	public void testClassPathResourceWithClass() throws IOException {
		Resource resource = new ClassPathResource("Resource.class", getClass());
		doTestResource(resource);
	}

	public void testFileSystemResource() throws IOException {
		Resource resource = new FileSystemResource(getClass().getResource("Resource.class").getFile());
		doTestResource(resource);
	}

	public void testUrlResource() throws IOException {
		Resource resource = new UrlResource(getClass().getResource("Resource.class"));
		doTestResource(resource);
	}

	public void testServletContextResource() throws IOException {
		MockServletContext sc = new MockServletContext();
		Resource resource = new ServletContextResource(sc, "org/springframework/core/io/Resource.class");
		doTestResource(resource);
	}

	private void doTestResource(Resource resource) throws IOException {
		assertEquals("Resource.class", resource.getFilename());
		assertTrue(resource.getURL().getFile().endsWith("Resource.class"));

		Resource relative1 = resource.createRelative("ClassPathResource.class");
		assertEquals("ClassPathResource.class", relative1.getFilename());
		assertTrue(relative1.getURL().getFile().endsWith("ClassPathResource.class"));
		assertTrue(relative1.exists());

		Resource relative2 = resource.createRelative("support/ResourcePatternResolver.class");
		assertEquals("ResourcePatternResolver.class", relative2.getFilename());
		assertTrue(relative2.getURL().getFile().endsWith("ResourcePatternResolver.class"));
		assertTrue(relative2.exists());

		Resource relative3 = resource.createRelative("../SpringVersion.class");
		assertEquals("SpringVersion.class", relative3.getFilename());
		assertTrue(relative3.getURL().getFile().endsWith("SpringVersion.class"));
		assertTrue(relative3.exists());
	}

}
