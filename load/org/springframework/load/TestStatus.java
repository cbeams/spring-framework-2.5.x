package org.springframework.load;

/**
 * Interface to be implemented by classes that can expose
 * test status information for reporting. Both Test and TestSuite objects
 * implement this interface to allow consistent treatment.
 * <br>The information available includes performance statistics
 * and whether this test has completed all passes. TestStatus is
 * available during test running as well as after completion.
 * 
 * @author Rod Johnson
 */
public interface TestStatus {
	
	/**
	 * @return the number of scheduled passes for this test.
	 * The test is complete if the getTestsCompletedCount() equals
	 * this value.
	 */	
	int getPasses();

	/**
	 * @return the number of passes completed so far by this test
	 */	
	int getTestsCompletedCount();
	
	/**
	 * @return the descriptive name of this test
	 */
	String getName();
	
	/**
	 * @return the number of errors encountered in this test
	 */
	int getErrorCount();
	
	/**
	 * @return whether this test has completed all scheduled passes
	 */
	boolean isComplete();

	/**
	 * @return the number of hits per second achieved
	 * by this test
	 */
	double getTestsPerSecondCount();

	/**
	 * @return the total time for which this thread has
	 * been running. This is the sum of pause time and
	 * working time.
	 */
	long getElapsedTime();
	
	/**
	 * @return the total working time in this thread
	 */
	long getTotalWorkingTime();

	/**
	 * @return the total pause time in this thread
	 */
	long getTotalPauseTime();
	
	/**
	 * @return the average response time in milliseconds
	 * achieved by this test
	 */
	int getAverageResponseTime();
	
}
