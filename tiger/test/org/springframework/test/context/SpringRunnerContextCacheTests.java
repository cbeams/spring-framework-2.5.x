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
package org.springframework.test.context;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.runners.InitializationError;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.ContextConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.junit4.SpringJUnit4ClassRunner;

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
@ContextConfiguration(locations = { "/org/springframework/test/junit4/SpringJUnit4ClassRunnerAppCtxTests-context.xml" })
public class SpringRunnerContextCacheTests {

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// XXX Remove suite() once we've migrated to Ant 1.7 with JUnit 4 support.
	public static junit.framework.Test suite() {

		return new JUnit4TestAdapter(SpringRunnerContextCacheTests.class);
	}

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

		final ContextCache<ContextConfigurationAttributes, ApplicationContext> contextCache = TestableSpringJUnit4ClassRunner.testableTestContextManager.getVisibleContextCache();
		contextCache.clear();
		contextCache.clearStatistics();
		assertContextCacheStatistics(contextCache, "BeforeClass", 0, 0, 0);
	}

	// ------------------------------------------------------------------------|

	@AfterClass
	public static void verifyFinalCacheState() {

		final ContextCache<ContextConfigurationAttributes, ApplicationContext> contextCache = TestableSpringJUnit4ClassRunner.testableTestContextManager.getVisibleContextCache();
		assertContextCacheStatistics(contextCache, "AfterClass", 1, 0, 2);
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	@Test
	@DirtiesContext
	public void dirtyContext() {

		final ContextCache<ContextConfigurationAttributes, ApplicationContext> contextCache = TestableSpringJUnit4ClassRunner.testableTestContextManager.getVisibleContextCache();
		assertContextCacheStatistics(contextCache, "dirtyContext()", 1, 0, 1);
	}

	// ------------------------------------------------------------------------|

	@Test
	public void verifyDirtiesContext() {

		final ContextCache<ContextConfigurationAttributes, ApplicationContext> contextCache = TestableSpringJUnit4ClassRunner.testableTestContextManager.getVisibleContextCache();
		assertContextCacheStatistics(contextCache, "verifyDirtiesContext()", 1, 0, 2);
	}

	// ------------------------------------------------------------------------|
	// --- STATIC CLASSES -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	public static class TestableSpringJUnit4ClassRunner<T> extends SpringJUnit4ClassRunner<T> {

		static TestableTestContextManager<?>	testableTestContextManager;

		public TestableSpringJUnit4ClassRunner(final Class<T> clazz) throws InitializationError {

			super(clazz);
		}

		@Override
		protected TestContextManager<T> createTestContextManager(final Class<T> clazz) throws Exception {

			final TestableTestContextManager<T> testableTestContextManager = new TestableTestContextManager<T>(clazz);
			TestableSpringJUnit4ClassRunner.testableTestContextManager = testableTestContextManager;
			return testableTestContextManager;
		}
	}

	// ------------------------------------------------------------------------|

	private static class TestableTestContextManager<T> extends TestContextManager<T> {

		public TestableTestContextManager(final Class<T> testClass) throws Exception {

			super(testClass);
		}

		ContextCache<ContextConfigurationAttributes, ApplicationContext> getVisibleContextCache() {

			return super.getContextCache();
		}
	}

}
