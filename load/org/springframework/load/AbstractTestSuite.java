/**
 * Generic framework code included with 
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002). 
 * This code is free to use and modify. 
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */

package org.springframework.load;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Superclass for test suites.
 * Test suites manage 1 or more worker threads (instances of
 * Test).
 * @author  Rod Johnson
 * @since February 9, 2001
 */
public abstract class AbstractTestSuite implements ConfigurableTest, TestStatus {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/** Time this test suite started execution */
	private long startTime;

	/** Maximum pause between passes. Inherited by threads,
	 * unless they override it */
	private long maxPause;

	/** Interval between reports to the console */
	private long reportInterval;

	/** The Test thread objects managed by this class.
	 * The execution of this object runs all these Tests
	 */
	private Test[] tests;

	/**
	 */
	private int numTests;

	/**
	 * The number of worker threads
	 */
	private int numWorkerThreads;

	/** The descriptive name for this test suite */
	private String name;


	private boolean longReports;

	private DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();

	/**
	 * Test fixture shared by all test threads.
	 * May not always be necessary: ignored if it's null.
	 */
	private Object fixture;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** 
	 * Creates a new AbstractThreadedTest */
	public AbstractTestSuite() {
		df.applyPattern("###.##");
	}

	//---------------------------------------------------------------------
	// JavaBean properties
	//---------------------------------------------------------------------
	/**
	 * Gets the name.
	 * @return Returns a String
	 */
	public String getName() {
		return name;
	}

	public final DecimalFormat getDecimalFormat() {
		return df;
	}

	public final void setDoubleFormat(String pattern) {
		df.applyPattern(pattern);
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set whether toString methods in test threads should use
	 * more verbose format
	 */
	public final void setLongReports(boolean longReports) {
		this.longReports = longReports;
	}

	/**
	 * @return get whether toString methods in test threads should
	 * use more verbose format (unless they override this via
	 * their own bean property)
	 */
	public final boolean getLongReports() {
		return longReports;
	}

	/**
	 * Set the fixture that should be available to all tests.
	 * This is an object that will be set on all tests.
	 */
	public void setFixture(Object context) {
		this.fixture = context;
	}
	
	
	/**
	 * @return the test fixture, which will be made available to all test 
	 * threads. This enables all test threads to bash the same object.
	 * This object may be null, as not all tests require a fixture
	 * (for example, they may obtain a singleton).
	 */
	public Object getFixture() {
		return this.fixture;
	}


	/**
	 * @return the number of test passes executed so far
	 */
	public int getTotalPassCount() {
		int passCount = 0;
		Test[] tests = getTests();
		if (tests != null) {
			for (int i = 0; i < tests.length; i++) {
				passCount += tests[i].getPasses();
			}
		}
		return passCount;
	}

	//---------------------------------------------------------------------
	// Public methods
	//---------------------------------------------------------------------
	/**
	 * Run all the tests
	 * @param blockTillComplete should we block until the tests are complete?
	 */
	public void runAllTests(boolean blockTillComplete) {

		Test[] tests = getTests();
		Thread[] runners = new Thread[tests.length];

		int nbTests = tests.length;

		// Instantiate the Threads the run them - we do not want our timing to
		// be augmented by the instantantion overhead
		for (int i = 0; i < nbTests; i++) {
			tests[i].reset();
			runners[i] = new Thread(tests[i]);
		}

		for (int i = 0; i < nbTests; i++) {
			//System.out.println("Starting thread " + i);
			runners[i].start();
		}

		// Use a Java 1.3 Timer to do periodic reporting
		if (this.reportInterval > 0L) {
			Timer timer = new Timer();
			timer.schedule(new ReportTimerTask(), getReportInterval(), getReportInterval());
			System.out.println("Reporting every " + getReportInterval() + "ms");
		}

		if (blockTillComplete) {
			for (int i = 0; i < nbTests; i++) {
				try {
					runners[i].join();
				}
				catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		// Always do a final report
		report();
	} 	// runAllTests


	/**
	 * Return the tests managed by this suite.
	 * Asks concrete subclass to create tests if this object
	 * has not yet fully initialized.
	 */
	public Test[] getTests() {
		if (this.tests == null) {
			this.tests = createTests();

			if (tests == null)
				throw new TestConfigurationException("Must define some tests in " + getClass().getName() + ".createTests");

			numWorkerThreads = tests.length;
			for (int i = 0; i < tests.length; i++) {
				String classname = tests[i].getClass().getName();
				classname = classname.substring(classname.lastIndexOf(".") + 1);
				if (tests[i].getName() == null)
					tests[i].setName(classname + "-" + i);
				tests[i].setTestSuite(this);
				if (this.fixture != null) {
					// If there's a fixture, all test threads must understand fixtures
					tests[i].setFixture(this.fixture);
				}
			}
		}
		return tests;
	} 	// getTests

	/**
	 * @return the number of threads running tests
	 */
	public int getThreads() {
		if (tests == null)
			return numWorkerThreads;
		return tests.length;
	}

	/**
	 * Clear tests results
	 */
	public void clearResults() {
		Test[] allTests = getTests();
		for (int i = 0; i < allTests.length; i++)
			allTests[i].reset();
	}

	//---------------------------------------------------------------------
	// Additional bean properties subclasses may want to use
	//---------------------------------------------------------------------
	public long getMaxPause() {
		return maxPause;
	}

	public void setMaxPause(long maxPause) {
		this.maxPause = maxPause;
	}

	public long getReportInterval() {
		return reportInterval;
	}

	public void setReportInterval(long reportInterval) {
		this.reportInterval = reportInterval;
	}

	public void setReportIntervalSeconds(int reportIntervalSecs) {
		this.reportInterval = reportIntervalSecs * 1000L;
	}

	/** Only use if desired */
	public final void setThreads(int numWorkerThreads) {
		this.numWorkerThreads = numWorkerThreads;
	}

	/** Subclasses can use this as a convenience if all their test threads
	 * have the same number of tests */
	public final void setPasses(int numTests) {
		this.numTests = numTests;
	}

	public final int getPasses() {
		return numTests;
	}

	public String toString() {
		return "Test suite name='"
			+ name
			+ "': numTests="
			+ numTests
			+ "; numberOfWorkerThreads="
			+ numWorkerThreads
			+ "; maxPause="
			+ maxPause;
	}

	//---------------------------------------------------------------------
	// Abstract methods to be implemented by subclasses
	//---------------------------------------------------------------------
	/**
	 * Subclasses must implement this method to return 
	 * all the tests they manage.
	 * Must not return null.
	 */
	protected abstract Test[] createTests();

	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * Generate default report to console
	 */
	public void report() {
		report(new PrintWriter(System.out));
	}

	/**
	 * Generate a report to this PrintWriter
	 */
	public void report(PrintWriter pw) {
		StringBuffer sb = new StringBuffer();
		report(sb);
		pw.println(sb.toString());		
		pw.flush();
	}

	/**
	 * Write a descriptive report to this StringBuffer
	 */
	public void report(StringBuffer sb) {
		sb.append("-----------------------------------\n");
		Test[] theirTests = getTests();
		// Take our own copy so we can sort it
		Test[] myTests = new Test[theirTests.length];
		System.arraycopy(theirTests, 0, myTests, 0, theirTests.length);
		Arrays.sort(myTests, new TestPerformanceComparator());
		for (int i = 0; i < myTests.length; i++) {
			sb.append(myTests[i]).append("\n");
		}
		
		// Now do by group, ignoring default group
		
		// Key is group name, key is a list
		HashMap groupsToTests = new HashMap();
		for (int i = 0; i < myTests.length; i++) {
			if (myTests[i].getGroup() != null) {
				List l = (List) groupsToTests.get(myTests[i].getGroup());
				if (l == null) {
					l = new LinkedList();
					groupsToTests.put(myTests[i].getGroup(), l);
				}
				l.add(myTests[i]);
			}
		}
		
		for (Iterator itr = groupsToTests.keySet().iterator(); itr.hasNext(); ) {
			String name = (String) itr.next();
			List l = (List) groupsToTests.get(name);
			if (l.size() > 1) {
				sb.append(new Stats("Group [" + name + "]", (Test[]) l.toArray(new Test[l.size()])));
			}
		}
		
		sb.append(new Stats());
		sb.append("Free memory=" + Runtime.getRuntime().freeMemory() /8L / 1024L + " Kb");
	}


	//---------------------------------------------------------------------
	// Implementation of TestStatus
	//---------------------------------------------------------------------
	/**
	 * @see org.springframework.load.TestStatus#getElapsedTime()
	 */
	public long getElapsedTime() {
		return new Stats().elapsedTime;
	}

	/**
	 * @see org.springframework.load.TestStatus#getErrorCount()
	 */
	public int getErrorCount() {
		return new Stats().errors;
	}

	/**
	 * @see org.springframework.load.TestStatus#getTestsCompletedCount()
	 */
	public int getTestsCompletedCount() {
		return new Stats().totalHits;
	}

	/**
	 * @see org.springframework.load.TestStatus#getTestsPerSecondCount()
	 */
	public double getTestsPerSecondCount() {
		return new Stats().hitsPerSecond;

	}

	/**
	 * @see org.springframework.load.TestStatus#getTotalPauseTime()
	 */
	public long getTotalPauseTime() {
		return new Stats().totalPauseTime;
	}

	/**
	 * @see org.springframework.load.TestStatus#getTotalWorkingTime()
	 */
	public long getTotalWorkingTime() {
		return new Stats().workingTime;
	}
	
	/**
	 * @see org.springframework.load.TestStatus#getAverageResponseTime()
	 */
	public int getAverageResponseTime() {
		return new Stats().avgResponseTime;
	}
	
	/**
	 * @return whether this test suite is complete (have all tests
	 * executed?)
	 */
	public final boolean isComplete() {
		for (int i = 0; i < getTests().length; i++) {
			if (!getTests()[i].isComplete())
				return false;
		}
		return true;
	}

	//---------------------------------------------------------------------
	// Inner classes
	//---------------------------------------------------------------------
	/**
	 * Inner class used in reporting.
	 * Used to run reports regularly
	 */
	private class ReportTimerTask extends TimerTask {
		/**
		 * @see Runnable#run()
		 */
		public void run() {
			AbstractTestSuite.this.report();
			if (AbstractTestSuite.this.isComplete()) {
				cancel();
				System.exit(0);
			}
		}
	}
	
	
	/**
	 * Collects information about this class.
	 * Could make this implement TestStatus. Or composite test status?
	 */
	private class Stats {
		public final int totalHits;
		public final long totalPauseTime;
		public final double hitsPerSecond;
		public final int avgResponseTime; 
		public final int errors;
		public final long elapsedTime;
		public final long workingTime;
		public String description;
		
		public Stats() {
			this("All tests", getTests());
		}
		
		public Stats(String description, Test[] allTests) {
			int totalResponseTimeAvg = 0;
			int totalHits = 0;
			int errors = 0;
			long elapsedTime = 0L;
			long workingTime = 0L;
			long totalPause = 0L;
			double hps = 0.0;
			for (int i = 0; i < allTests.length; i++) {
				totalResponseTimeAvg += allTests[i].getTargetResponse().getAverageResponseTimeMillis();
				hps += allTests[i].getTestsPerSecondCount();
				totalHits += allTests[i].getTestsCompletedCount();
				totalPause += allTests[i].getTotalPauseTime();
				errors += allTests[i].getErrorCount();
				elapsedTime += allTests[i].getElapsedTime();
				workingTime += allTests[i].getTotalWorkingTime();
			}

			this.totalHits = totalHits;
			this.avgResponseTime = totalResponseTimeAvg / allTests.length;
			this.hitsPerSecond = hps;
			this.totalPauseTime = totalPause;
			this.errors = errors;
			this.elapsedTime = elapsedTime;
			this.workingTime = workingTime;
			this.description = description;
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("[" + description + "]");
			sb.append("Total hits=" + totalHits);
			sb.append("; HPS=" + df.format(hitsPerSecond));
			sb.append("; Average response=" + avgResponseTime + "\n");
			return sb.toString();
		}
	}	// class Stats
	
	
	/**
	 * Sort in ascending order by hits per second
	 */
	private class TestPerformanceComparator implements Comparator {
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			if (! ((o1 instanceof Test) && (o2 instanceof Test)))
				return 0;
			Test t1 = (Test) o1;
			Test t2 = (Test) o2;
			int result = (int) (100.0 * (t2.getTestsPerSecondCount() - t1.getTestsPerSecondCount()));
			//System.out.println("t1.hps=" + t1.getTestsPerSecondCount() + "t2.hps=" + t2.getTestsPerSecondCount() + "; result is " + result);
			return result;
		}
	}

} 	// class AbstractTestSuite