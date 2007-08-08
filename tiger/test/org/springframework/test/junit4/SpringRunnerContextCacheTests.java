/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.junit4;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.runners.InitializationError;
import org.junit.runner.RunWith;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.ContextConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextCache;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.TestContextManager;

/**
 * JUnit 4 based unit test which verifies correct
 * {@link ContextCache application context caching} in conjunction with the
 * {@link SpringJUnit4ClassRunner} and the {@link DirtiesContext} annotation.
 *
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.1
 */
@RunWith(SpringRunnerContextCacheTests.TestableSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "SpringJUnit4ClassRunnerAppCtxTests-context.xml" })
public class SpringRunnerContextCacheTests {

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Asserts the statistics of the supplied context cache.
	 *
	 * @param contextCache the cache against which to assert the statistics.
	 * @param usageScenario the scenario in which the statistics are used.
	 * @param expectedSize the expected number of contexts in the cache.
	 * @param expectedHitCount the expected hit count.
	 * @param expectedMissCount the expected miss count.
	 */
	public static final void assertContextCacheStatistics(final ContextCache<?, ?> contextCache,
			final String usageScenario, final int expectedSize, final int expectedHitCount, final int expectedMissCount) {

		assertEquals("Verifying number of contexts in cache (" + usageScenario + ").", expectedSize,
				contextCache.size());
		assertEquals("Verifying number of cache hits (" + usageScenario + ").", expectedHitCount,
				contextCache.getHitCount());
		assertEquals("Verifying number of cache misses (" + usageScenario + ").", expectedMissCount,
				contextCache.getMissCount());
	}

	// ------------------------------------------------------------------------|

	@BeforeClass
	public static void verifyInitialCacheState() {

		final ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> contextCache = TestableSpringJUnit4ClassRunner.testableTestContextManager.getVisibleContextCache();
		contextCache.clear();
		contextCache.clearStatistics();
		assertContextCacheStatistics(contextCache, "BeforeClass", 0, 0, 0);
	}

	// ------------------------------------------------------------------------|

	@AfterClass
	public static void verifyFinalCacheState() {

		final ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> contextCache = TestableSpringJUnit4ClassRunner.testableTestContextManager.getVisibleContextCache();
		assertContextCacheStatistics(contextCache, "AfterClass", 1, 1, 1);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	@Test
	@DirtiesContext
	public void alwaysPassesButDirtiesContext() {

		final ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> contextCache = TestableSpringJUnit4ClassRunner.testableTestContextManager.getVisibleContextCache();
		assertContextCacheStatistics(contextCache, "alwaysPassesButDirtiesContext()", 1, 0, 1);
	}

	// ------------------------------------------------------------------------|

	@Test
	public void verifyDirtiesContext() {

		final ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> contextCache = TestableSpringJUnit4ClassRunner.testableTestContextManager.getVisibleContextCache();

		// TODO verify @DirtiesContext functionality.
		// In other words, change the following assertion accordingly...
		assertContextCacheStatistics(contextCache, "verifyDirtiesContext()", 1, 1, 1);
	}

	// ------------------------------------------------------------------------|
	// --- STATIC CLASSES -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	public static class TestableSpringJUnit4ClassRunner<T> extends SpringJUnit4ClassRunner<T> {

		static TestableTestContextManager<?> testableTestContextManager;

		public TestableSpringJUnit4ClassRunner(final Class<T> clazz) throws InitializationError {

			super(clazz);
		}

		@Override
		protected TestContextManager<T> createTestContextManager(final Class<T> clazz) throws Exception {

			final TestableTestContextManager<T> testableTestContextManager = new TestableTestContextManager<T>(clazz);
			TestableSpringJUnit4ClassRunner.testableTestContextManager = testableTestContextManager;
			return testableTestContextManager;
		}

		TestableTestContextManager<T> getTestableTestContextManager() {

			return (TestableTestContextManager<T>) super.getTestContextManager();
		}
	}

	// ------------------------------------------------------------------------|

	private static class TestableTestContextManager<T> extends TestContextManager<T> {

		public TestableTestContextManager(final Class<T> testClass) throws Exception {

			super(testClass);
		}

		ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> getVisibleContextCache() {

			return super.getContextCache();
		}
	}

}
