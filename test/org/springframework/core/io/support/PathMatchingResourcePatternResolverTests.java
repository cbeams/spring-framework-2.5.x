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
package org.springframework.core.io.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.core.io.Resource;

/**
 * @author Oliver Hutchison
 * @since 17.11.2004
 */
public class PathMatchingResourcePatternResolverTests extends TestCase {

	private static final String[] CLASSES_IN_CORE_IO_SUPPORT =
			new String[] {"PathMatchingResourcePatternResolver.class",
					"ResourceArrayPropertyEditor.class", "ResourcePatternResolver.class"};

	private static final String[] TEST_CLASSES_IN_CORE_IO_SUPPORT =
			new String[] {"PathMatchingResourcePatternResolverTests.class"};

	private static final String[] CLASSES_IN_AOPALLIANCE =
			new String[] {"Advice.class", "AspectException.class", "ConstructorInterceptor.class",
					"ConstructorInvocation.class", "Interceptor.class", "Invocation.class",
					"Joinpoint.class", "MethodInterceptor.class", "MethodInvocation.class"};

	private PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	public void testSingleResourceOnFileSystem() throws IOException {
		Resource[] resources =
				resolver.getResources("org/springframework/core/io/support/PathMatchingResourcePatternResolverTests.class");
		assertEquals(1, resources.length);
		assertProtocolAndFilename(resources[0], "file", "PathMatchingResourcePatternResolverTests.class");
	}

	public void testSingleResourceInJar() throws IOException {
		Resource[] resources = resolver.getResources("java/net/URL.class");
		assertEquals(1, resources.length);
		assertProtocolAndFilename(resources[0], "jar", "URL.class");
	}

	public void testClasspathStarWithPatternOnFileSystem() throws IOException {
		Resource[] resources = resolver.getResources("classpath*:org/springframework/core/io/sup*/*.class");
		assertProtocolAndFilenames(resources, "file", CLASSES_IN_CORE_IO_SUPPORT, TEST_CLASSES_IN_CORE_IO_SUPPORT);
	}

	public void testClasspathWithPatternInJar() throws IOException {
		Resource[] resources = resolver.getResources("classpath:org/aopalliance/**/*.class");
		assertProtocolAndFilenames(resources, "jar", CLASSES_IN_AOPALLIANCE);
	}

	public void testClasspathStartWithPatternInJar() throws IOException {
		Resource[] resources = resolver.getResources("classpath*:org/aopalliance/**/*.class");
		assertProtocolAndFilenames(resources, "jar", CLASSES_IN_AOPALLIANCE);
	}

	private void assertProtocolAndFilename(Resource resource, String urlProtocol, String fileName) throws IOException {
		assertProtocolAndFilenames(new Resource[]{resource}, urlProtocol, new String[] {fileName});
	}

	private void assertProtocolAndFilenames(
			Resource[] resources, String urlProtocol, String[] fileNames1, String[] fileNames2) throws IOException {
		List fileNames = new ArrayList(Arrays.asList(fileNames1));
		fileNames.addAll(Arrays.asList(fileNames2));
		assertProtocolAndFilenames(resources, urlProtocol, (String[]) fileNames.toArray(new String[fileNames.size()]));
	}

	private void assertProtocolAndFilenames(Resource[] resources, String urlProtocol, String[] fileNames)
			throws IOException {
		assertEquals("Correct number of files found", fileNames.length, resources.length);
		for (int i = 0; i < resources.length; i++) {
			Resource resource = resources[i];
			assertEquals(urlProtocol, resource.getURL().getProtocol());
			assertFileNameIn(resource, fileNames);
		}
	}

	private void assertFileNameIn(Resource resource, String[] fileNames) {
		for (int i = 0; i < fileNames.length; i++) {
			if (resource.getFilename().endsWith(fileNames[i])) {
				return;
			}
		}
		fail("resource [" + resource + "] does not have a filename that matches and of the names in 'fileNames'");
	}

}
