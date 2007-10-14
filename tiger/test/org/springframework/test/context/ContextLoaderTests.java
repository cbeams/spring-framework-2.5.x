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

package org.springframework.test.context;

import static org.junit.Assert.assertArrayEquals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.support.GenericXmlContextLoader;
import org.springframework.util.ObjectUtils;

/**
 * JUnit 4 based unit test which verifies proper
 * {@link ContextLoader#processLocations(Class,String...) processing} of
 * <code>resource locations</code> by a {@link ContextLoader} configured via
 * {@link ContextConfiguration @ContextConfiguration}. Specifically, this test
 * addresses the issues raised in <a
 * href="http://opensource.atlassian.com/projects/spring/browse/SPR-3949"
 * target="_blank">SPR-3949</a>:
 * <em>ContextConfiguration annotation should accept not only classpath resources</em>.
 *
 * @author Sam Brannen
 * @since 2.5
 */
public class ContextLoaderTests {

	private static final Log logger = LogFactory.getLog(ContextLoaderTests.class);


	@SuppressWarnings("unchecked")
	private void assertContextConfigurationLocations(final Class<?> testClass, final String[] expectedLocations)
			throws Exception {

		final ContextConfiguration contextConfig = testClass.getAnnotation(ContextConfiguration.class);
		final ContextLoader contextLoader = new GenericXmlContextLoader();
		final String[] configuredLocations = (String[]) AnnotationUtils.getValue(contextConfig, "locations");
		final String[] processedLocations = contextLoader.processLocations(testClass, configuredLocations);

		if (logger.isDebugEnabled()) {
			logger.debug("----------------------------------------------------------------------");
			logger.debug("Configured locations: " + ObjectUtils.nullSafeToString(configuredLocations));
			logger.debug("Expected   locations: " + ObjectUtils.nullSafeToString(expectedLocations));
			logger.debug("Processed  locations: " + ObjectUtils.nullSafeToString(processedLocations));
		}

		assertArrayEquals("Verifying locations for test [" + testClass + "].", expectedLocations, processedLocations);
	}

	@Test
	public void testContextConfigurationLocationProcessing() throws Exception {
		assertContextConfigurationLocations(
				ClasspathDefaultLocationsTest.class,
				new String[] { "classpath:/org/springframework/test/context/ContextLoaderTests$ClasspathDefaultLocationsTest-context.xml" });

		assertContextConfigurationLocations(ImplicitClasspathLocationsTest.class, new String[] {
			"classpath:/org/springframework/test/context/context1.xml",
			"classpath:/org/springframework/test/context/context2.xml" });

		assertContextConfigurationLocations(ExplicitClasspathLocationsTest.class,
				new String[] { "classpath:context.xml" });

		assertContextConfigurationLocations(ExplicitFileLocationsTest.class,
				new String[] { "file:/testing/directory/context.xml" });

		assertContextConfigurationLocations(ExplicitUrlLocationsTest.class,
				new String[] { "http://example.com/context.xml" });

		assertContextConfigurationLocations(ExplicitMixedPathTypesLocationsTest.class, new String[] {
			"classpath:/org/springframework/test/context/context1.xml", "classpath:context2.xml",
			"classpath:/context3.xml", "file:/testing/directory/context.xml", "http://example.com/context.xml" });
	}


	@ContextConfiguration
	private static class ClasspathDefaultLocationsTest {
	}

	@ContextConfiguration(locations = { "context1.xml", "context2.xml" })
	private static class ImplicitClasspathLocationsTest {
	}

	@ContextConfiguration(locations = { "classpath:context.xml" })
	private static class ExplicitClasspathLocationsTest {
	}

	@ContextConfiguration(locations = { "file:/testing/directory/context.xml" })
	private static class ExplicitFileLocationsTest {
	}

	@ContextConfiguration(locations = { "http://example.com/context.xml" })
	private static class ExplicitUrlLocationsTest {
	}

	@ContextConfiguration(locations = { "context1.xml", "classpath:context2.xml", "/context3.xml",
		"file:/testing/directory/context.xml", "http://example.com/context.xml" })
	private static class ExplicitMixedPathTypesLocationsTest {
	}

}
