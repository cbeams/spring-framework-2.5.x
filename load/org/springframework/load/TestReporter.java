package org.springframework.load;

/**
 * Interface to be implemented by objects that can report on
 * the status of a test suite. Reporting can occur during or
 * after a test run. 
 * 
 * @author Rod Johnson
 */
public interface TestReporter {
	
	/** 
	 * Depending on the configuration of the test reporter,
	 * report on the test suite.
	 * @param testSuite test suite to report on
	 * @throws Exception if any error is encountered generating
	 * test output. It's simpler to let reporters throw any exceptions
	 * they like, and let the calling process deal with them.
	 */
	void generateReport(AbstractTestSuite testSuite) throws Exception;

}
