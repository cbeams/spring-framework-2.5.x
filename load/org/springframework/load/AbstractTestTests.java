package org.springframework.load;

import junit.framework.TestCase;

/**
 * 
 * @author Rod Johnson
 * @since 04-Dec-02
 */
public class AbstractTestTests extends TestCase {

	/**
	 * Constructor for AbstractTestTests.
	 */
	public AbstractTestTests() {
		super();
	}

	/**
	 * Constructor for AbstractTestTests.
	 * @param arg0
	 */
	public AbstractTestTests(String arg0) {
		super(arg0);
	}
	
	public void testNoTests() throws Exception {
		AbstractTestSuite ats = new AbstractTestSuite() {
			protected Test[] createTests() {
				return null;
			}
		};
	
		try {
			ats.runAllTests(false);
			fail("Shouldn't accept no tests");
		}
		catch (TestConfigurationException ex) {
			// OK
		}
	}
	
	
	public void testAbortion() throws Exception {
		final DemoTest one = new DemoTest();
		final Test abort = new AbstractTest() {
			protected void runPass(int i) throws TestFailedException, AbortTestException, Exception {
				if (i >= 2)
					throw new AbortTestException("Abort!");
			}
		};
		AbstractTestSuite ats = new AbstractTestSuite() {
			protected Test[] createTests() {
				return new Test[] { one, abort };
			}
		};
		
		ats.setPasses(10);
		ats.runAllTests(true);
		
		// Should run 13 tests: 10 + 2 in abort
		// Abortion counts as a test
		assertTrue("Ran 13 tests, not " + ats.getTestsCompletedCount(), ats.getTestsCompletedCount() == 13);
		assertTrue("Isn't complete", !ats.isComplete());
		assertTrue("One is complete", one.isComplete());
	}
	
	
	public void testFailureCount() throws Exception {
		final DemoTest one = new DemoTest();
		final TestFailedException failure = new TestFailedException("Failure!");
		class Fails extends AbstractTest {
			int callbacks;
			protected void runPass(int i) throws TestFailedException, AbortTestException, Exception {
				if (i % 2 == 0)
					throw failure;
			}
			
			/** Override this method to check callback operation */
			protected void onTestPassFailed(Exception ex) {
				//assertTrue("Failure callback had correct argument", ex == failure);
				++callbacks;
			}
		}
		final Fails fails = new Fails();
		AbstractTestSuite ats = new AbstractTestSuite() {
			protected Test[] createTests() {
				return new Test[] { one, fails };
			}
		};
		
		ats.setPasses(10);
		ats.runAllTests(true);
		
		// Should run 13 tests: 10 + 2 in abort
		// Abortion counts as a test
		assertTrue("Ran 20 tests, not " + ats.getTestsCompletedCount(), ats.getTestsCompletedCount() == 20);
		assertTrue("Suite is complete", ats.isComplete());
		// Works so long as even number of tests in total
		assertTrue("Failure count=5", ats.getErrorCount() == 5);
		assertTrue("No tests failed in 1", one.getErrorCount() == 0);
		assertTrue("5 tests failed in 2", fails.getErrorCount() == 5);
		// Check all failures were same as input
		for (int i = 0; i < fails.getFailureExceptions().length; i++) {
			assertTrue("Failure was same as input", fails.getFailureExceptions()[i] == failure);
		}
		assertTrue("Correct number of callbacks", fails.callbacks == fails.getErrorCount());
	}
	
	public void testStatsAddUp() throws Exception {
		testStatsAddUp(97, 378, 20, 55, 3L);
		testStatsAddUp(5, 40, 50, 12, 3L);
	}
	
	private void testStatsAddUp(int delay1, int delay2, int passes, int maxPause, long window) throws Exception {

		System.out.println("Testing addup: delay1=" + delay1 + "; delay2=" + delay2 + "; passes=" + passes + "; maxPause=" + maxPause);
		
		final DemoTest one = new DemoTest();
		one.setMethodExecutionTime(delay1);
		one.setUseRandom(false);
		final DemoTest two = new DemoTest();
		two.setMethodExecutionTime(delay2);
		two.setUseRandom(false);
		
		AbstractTestSuite ats = new AbstractTestSuite() {
			protected Test[] createTests() {
				return new Test[] { one, two };
			}
		};
		ats.setMaxPause(maxPause);
		ats.setPasses(passes);
		ats.runAllTests(true);
		assertTrue("Should have run " + 2 * passes + " tests", ats.getTestsCompletedCount() == 2*passes);
		assertTrue("Should have 0 errors", ats.getErrorCount() == 0);
		assertTrue("One art should be " + delay1 + "+-2, not " + one.getAverageResponseTime(), Math.abs(one.getAverageResponseTime() - delay1) <= window);
		assertTrue("Two art should be " + delay2 + "+-2, not " + two.getAverageResponseTime(), Math.abs(two.getAverageResponseTime() - delay2) <= window);
		double avg = (delay1 + delay2 ) / 2;
		assertTrue("Overall art should be " + avg + "+-2, not " + ats.getAverageResponseTime(), Math.abs(ats.getAverageResponseTime() - avg) <= window);
	}
	
	
	/**
	 * Test properties copied from parent
	 */
	public void testPropertyCopy() throws Exception {
		final Test maxPauseTest = new DemoTest();
		maxPauseTest.setMaxPause(11L);
		final Test noMaxPauseTest = new DemoTest();
		final Test zeroPauseTest = new DemoTest();
		zeroPauseTest.setMaxPause(0L);
		
		AbstractTestSuite ats = new AbstractTestSuite() {
			protected Test[] createTests() {
				return new Test[] { maxPauseTest, noMaxPauseTest, zeroPauseTest };
			}
		};
	
		ats.setMaxPause(666L);
		
		ats.runAllTests(true);
		assertTrue("No max pause inherited maxPause from suite", noMaxPauseTest.getMaxPause() == 666L);
		assertTrue("max pause kept own maxPause value", maxPauseTest.getMaxPause() == 11L);
		assertTrue("zero pause kept own maxPause value", zeroPauseTest.getMaxPause() == 0L);
	}
	
	
	public void testFixtureCopied() throws Exception {
		class FixtureTest extends DemoTest {
			private Object fixture;
			public void setFixture(Object o) { this.fixture = o; }
			public Object getFixture() { return this.fixture; }
		}
		final Test maxPauseTest = new FixtureTest();
		maxPauseTest.setMaxPause(11L);
		final Test noMaxPauseTest = new FixtureTest();
		
		AbstractTestSuite ats = new AbstractTestSuite() {
			protected Test[] createTests() {
				return new Test[] { maxPauseTest, noMaxPauseTest };
			}
		};
	
		Object fixture = new Object();
		ats.setFixture(fixture);
		
		ats.runAllTests(true);
		for (int i = 0; i < ats.getTests().length; i++) {
			assertTrue("Test fixture is suite fixture, not " + ats.getTests()[i].getFixture(), ats.getTests()[i].getFixture() == fixture);
		}
	}
	
	/* MULTI INSTANCE CREATION IS CURRENTLY IN SUBCLASSES
	public void testMultipleInstances() throws Exception {
		final Test maxPauseTest = new DemoTest();
		maxPauseTest.setMaxPause(11L);
		class MultiInstanceTest extends DemoTest {
		};
		final Test noMaxPauseTest = new MultiInstanceTest();
		int instancesOfTwo = 16;
		noMaxPauseTest.setInstances(instancesOfTwo);
		
		AbstractTestSuite ats = new AbstractTestSuite() {
			protected Test[] createTests() {
				return new Test[] { maxPauseTest, noMaxPauseTest };
			}
		};
	
		ats.setMaxPause(666L);
		
		ats.runAllTests(true);
		assertTrue("expected " + instancesOfTwo + " threads, not " + ats.getThreads(), ats.getThreads() == instancesOfTwo + 1);
		int instancesOf2Counted = 0;
		for (int i = 0; i < ats.getTests().length; i++) {
			if (ats.getTests()[i] instanceof MultiInstanceTest)
				++instancesOf2Counted;
		}
		assertTrue("counted " + instancesOfTwo + " instances of two", instancesOf2Counted == instancesOfTwo);
	}
	*/
	
	
//	public void testUncaughtExceptions() throws Exception {
//		AbstractTestSuite ats = new AbstractTestSuite() {
//			protected Test[] createTests() {
//				return new Test[] { new FailingTestWithUncaughtException() };
//			}
//		};
//	
//		try {
//			ats.runAllTests(false);
//			//fail("Shouldn't accept no tests");
//		}
//		catch (TestConfigurationException ex) {
//			// OK
//		}
//	}
	
	
	private class FailingTestWithUncaughtException extends AbstractTest {
		/**
		 * @see org.springframework.load.AbstractTest#runPass(int)
		 */
		protected void runPass(int i) throws Exception {
			throw new Exception("Unexpected failure");
		}

	}
	
	/**
	 * Controlled failure
	 */
	private class FailingTestWithTestFailure extends AbstractTest {
		/**
		 * @see org.springframework.load.AbstractTest#runPass(int)
		 */
		protected void runPass(int i) throws Exception {
			throw new TestFailedException("test failure");
		}

	}

}
