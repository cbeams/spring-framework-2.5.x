/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.load;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.ResponseTimeMonitor;
import org.springframework.util.ResponseTimeMonitorImpl;
import org.springframework.util.StopWatch;

/**
 * Convenient superclass that makes it very easy to implement Tests.
 * 
 * Uses the Template Method design pattern. Concrete subclasses have
 * only to implement the abstract runPass(i) method to execute each test.
 * 
 * This class exposes bean properties controlling test execution.
 * 
 * @author  Rod Johnson
 * @since February 9, 2001
 */
public abstract class AbstractTest implements Test, BeanNameAware {

	//---------------------------------------------------------------------
	// Instance variables
	//---------------------------------------------------------------------
	/** Used to calculate random delays if a subclass wants this behavior */
	private static Random rand = new Random();
	
	protected final Log logger = LogFactory.getLog(getClass());

	private long maxPause = -1L;
	
	private int nbPasses;

	private StopWatch runningTimer;
	
	private StopWatch pauseTimer;
	
	private StopWatch elapsedTimer;

	/**
	 * List of failures encountered by this test so far
	 */
	private List testFailedExceptions;
	
	/**
	 * Whether toString should generate a verbose format.
	 * This property is inherited from the managing TestSuite unless
	 * it's overridden as a bean property.
	 */
	private boolean longReports;

	/** Number of tests completed so far */
	private int completedTests;
	
	/** Number of instances of this test. Use for weighting */
	private int instances = 1;

	/** Descriptive name of this test */	
	private String name;

	/**
	 * Helper object used to capture performance information
	 */
	private ResponseTimeMonitorImpl responseTimeMonitorImpl = new ResponseTimeMonitorImpl();

	/** Suite that manages this test */
	private AbstractTestSuite suite;	


	//---------------------------------------------------------------------
	// Constructor
	//---------------------------------------------------------------------
	/**
	 * Construct a new AbstractTest. The object must be further configured
	 * by its JavaBean properties and/or by inheritance of properties
	 * from the TestSuite it runs it. The setTestSuite() method must
	 * be invoked by the managing AbstractTestSuite.
	 */
	protected AbstractTest() {
		testFailedExceptions = new LinkedList();
		runningTimer = new StopWatch();
		runningTimer.setKeepTaskList(false);
	
		pauseTimer = new StopWatch();
		pauseTimer.setKeepTaskList(false);
	
		elapsedTimer = new StopWatch();
		elapsedTimer.setKeepTaskList(false);
	}
	
	
	/**
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name) {
		setName(name);
	}
	
	//---------------------------------------------------------------------
	// Implementation of Test
	//---------------------------------------------------------------------
	/**
	 * Subclasses can override this to save a test
	 * fixture to run application-specific tests against.
	 * This implementation doesn't know anything about fixtures, so
	 * it always throws an exception.
	 * @param context application-specific object to test
	 */
	public void setFixture(Object context) {
		throw new UnsupportedOperationException("AbstractTest.setFixture");
	}
	
	/**
	 * @return the test fixture used by this object.
	 * Subclasses that require test fixtures must override this method,
	 * which always throws UnsupportedOperationException
	 */
	public Object getFixture() {
		throw new UnsupportedOperationException("AbstractTest.getFixture");
	}
	
	/**
	 * Set the number of instances of this Test. This enables weighting
	 * of tests.
	 */
	public void setInstances(int instances) {
		this.instances = instances;
	}
	
	public int getInstances() {
		return instances;
	}

	
	/**
	 * The TestSuite object that manages this test thread invokes this
	 * method. It is used to enable properties to be inherited from the
	 * test suite unless overriden by bean properties on this class.
	 */
	public void setTestSuite(AbstractTestSuite suite) {
		this.suite = suite;
		// Set defaults if not set via this class's bean properties
		if (getPasses() == 0)
			this.setPasses(suite.getPasses());
		if (getMaxPause() < 0L)
			this.setMaxPause(suite.getMaxPause());
		this.setLongReports(suite.getLongReports()); 
	}


	public final void setName(String name) {
		this.name = name;
	}
	
	public final void setLongReports(boolean longReports) {
		this.longReports = longReports;
	}

	public final void setPasses(int nbPasses) {
		this.nbPasses = nbPasses;
	}

	public final void setMaxPause(long maxPause) {
		this.maxPause = maxPause;
	}
	
	public final long getMaxPause() {
		return this.maxPause;
	}

	public final int getPasses() {
		return nbPasses;
	}

	public final int getErrorCount() {
		return testFailedExceptions.size();
	}

	public final String getName() {
		return name;
	}

	public final int getTestsCompletedCount() {
		return completedTests;
	}

	public final boolean isComplete() {
		return getPasses() == getTestsCompletedCount();
	}
	
	/**
	 * @see org.springframework.load.TestStatus#getAverageResponseTime()
	 */
	public int getAverageResponseTime() {
		return this.responseTimeMonitorImpl.getAverageResponseTimeMillis();
	}

	/**
	 * @see org.springframework.load.TestStatus#getTestsPerSecondCount()
	 */
	public final double getTestsPerSecondCount() {
		double res = 0.0;
		double totalTime = runningTimer.getTotalTimeMillis();
		double testCompleted = getTestsCompletedCount();
		
		if (testCompleted == 0.0) 
			return 0.0;
			
		if (totalTime != 0.0)
			res = 1000.0 / (totalTime / testCompleted);
		else {
			// No time!!!!
			//return testCompleted;
			return Double.POSITIVE_INFINITY;
		}
		return res;
	}

	/**
	 * @see org.springframework.load.TestStatus#getTotalWorkingTime()
	 */
	public final long getTotalWorkingTime() {
		return runningTimer.getTotalTimeMillis();
	}

	/**
	 * @see org.springframework.load.TestStatus#getElapsedTime()
	 */
	public final long getElapsedTime() {
		return elapsedTimer.getTotalTimeMillis();
	}

	/**
	 * @see org.springframework.load.TestStatus#getTotalPauseTime()
	 */
	public final long getTotalPauseTime() {
		return pauseTimer.getTotalTimeMillis();
	}

	public final TestFailedException[] getFailureExceptions() {
		TestFailedException[] fails = new TestFailedException[testFailedExceptions.size()];
		for (int i = 0; i < fails.length; i++)
			fails[i] = (TestFailedException) testFailedExceptions.get(i);
		return fails;
	}

	/**
	 * @see org.springframework.load.Test#reset()
	 */
	public void reset() {
		elapsedTimer = new StopWatch("elapsed timer");
		completedTests = 0;
		runningTimer = new StopWatch("running timer");
		pauseTimer = new StopWatch("pause timer");
		testFailedExceptions.clear();
	}

	
	/**
	 * @return additional stats information.
	 */
	public ResponseTimeMonitor getTargetResponse() {
	    return responseTimeMonitorImpl;
	}


	/** 
	 * Run all the tests in this thread
	 * @see java.lang.Runnable#run()
	 */
	public final void run() {
		elapsedTimer.start(null);//"run");
		for (int i = 0; i < getPasses(); i++) {
			try {
				pause();
				runningTimer.start(null);//"run");
				runPass(i);
			}
			catch (AbortTestException ex) {
				// Terminate the for loop to end the work of this test thread
				System.err.println("Abortion!: " + ex);
				break;
			}
			catch (TestFailedException ex) {	
				// We don't need to wrap this			
				testFailedExceptions.add(ex);
				onTestPassFailed(ex);
			}
			catch (Exception ex) {
				// Wrap the uncaught exception in a new TestFailedException
				TestFailedException tfe = new TestFailedException("Uncaught exception: " + ex.getMessage(), ex);
				testFailedExceptions.add(tfe);
				onTestPassFailed(ex);
			}
			finally {
				++completedTests;
				runningTimer.stop();
				responseTimeMonitorImpl.recordResponseTime(runningTimer.getLastTaskTimeMillis());
			}
		} // for each test
		elapsedTimer.stop();
	} 	// run


	/**
	 * @return diagnostic information about this Test.
	 * The verbosity of the format will depend on the value of the
	 * longReports bean property.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getName() + "\t");
		sb.append(getTestsCompletedCount() + "/" + getPasses());
		sb.append("\terrs=" + getErrorCount());
		sb.append("\t" + suite.getDecimalFormat().format(getTestsPerSecondCount()) + "hps");
		sb.append("\tavg=" + responseTimeMonitorImpl.getAverageResponseTimeMillis() + "ms");
		if (longReports) {
			sb.append("\tworkt=" + getTotalWorkingTime());
			sb.append("\telt=" + getElapsedTime());
			sb.append("\best=" + responseTimeMonitorImpl.getBestResponseTimeMillis() + "ms");
			sb.append("\tworst=" + responseTimeMonitorImpl.getWorstResponseTimeMillis() + "ms");
			sb.append("\tpause=" + getTotalPauseTime());
		}
		if (isComplete() && this.longReports) {
			sb.append("\tCOMPLETED\n");
			sb.append(getErrorReport());
		}
		return sb.toString();
	}	// toString
	

	/**
	 * @return an error report string
	 */
	public final String getErrorReport() {
		String s = "";
		TestFailedException[] fails = getFailureExceptions();
		if (fails == null || fails.length == 0)
			return "No errors\n";
		else {
			for (int i = 0; i < fails.length; i++) {
				s += fails[i] + "\n";
			}
		}
		return s;
	}
	
	public String getGroup() {
		return null;
	}


	/**
	 * Subclasses must implement this method
	 * @throws TestFailedException if the test failed
	 * @throws AbortTestException if the test run should abort
	 * @throws Exception if there's any other unspecified error. 
	 * This will not cause the test run to abort.
	 * @param i index of test pass, indexed from 0
	 */
	protected abstract void runPass(int i) throws TestFailedException, AbortTestException, Exception;


	/**
	 * For handling any exceptions thrown by a Test pass. Override as desired.
	 * This implementation does nothing.
	 * This method is invoked on test failures, whether caused by an uncaught exception
	 * or a TestAssertionFailure
	 */
	protected void onTestPassFailed(Exception ex) {
		ex.printStackTrace();
	}


	//---------------------------------------------------------------------
	// Implementation methods and convenience methods for subclasses
	//---------------------------------------------------------------------
	/**
	 * Pause for up to maxPause milliseconds
	 */
	private void pause() {
		if (this.maxPause > 0L) {
			try {
				pauseTimer.start(null);//"pause");
				long p = Math.abs(rand.nextLong() % this.maxPause);
				Thread.sleep(p);
			}
			catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			finally {
				pauseTimer.stop();
			}
		}
	} 	// pause
	
	
	/**
	 * Convenience method to simulate delay for between min and max ms
	 * @param min minimum number of milliseconds to delay
	 * @param max maximum number of milliseconds to delay
	 */
	public static void simulateDelay(long min, long max) {
		if (max - min > 0L) {
			try {
				long p = Math.abs(min + rand.nextLong() % (max - min));
				//System.out.println("delay for " + p + "ms");
				Thread.sleep(p);
			}
			catch (InterruptedException ex) {
				// Ignore it	
			}
		}
	} 	// simulateDelay

	/** 
	 * Convenience method for subclasses
	 * @param sz size of array to index
	 * @return a random array of list index from 0 up to sz-1
	 */
	public static int randomIndex(int sz) {
		return Math.abs(rand.nextInt(sz));
	}
	
	/**
	 * Like JUnit assertion
	 * @param s
	 */
	protected void assertTrue(String s, boolean condition) throws TestFailedException {
		if (!condition)
			throw new TestFailedException(s);
	}
	
	protected void assertEquals(String s, Object a, Object b) throws TestFailedException {
		if (a == b)
			return;
		if (a == null) {
			if (b != null)
				throw new TestFailedException(s);
		}
		else if (b == null) {
			// a isn't null
			throw new TestFailedException(s);
		}
		if (!a.equals(b)) {
			throw new TestFailedException(s);
		}
	}
	
	protected void assertEquals(String s, int a, int b) throws TestFailedException {
		if (a != b)
			throw new TestFailedException(s + ": expected " + a + " but found " + b);
	}
	
	protected void assertEquals(String s, long a, long b) throws TestFailedException {
		if (a != b)
			throw new TestFailedException(s);
	}

} 	// class AbstractTest