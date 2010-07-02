/**
 * Generic framework code included with 
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/1861007841/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002). 
 * This code is free to use and modify. 
 * Please contact <a href="mailto:rod.johnson@interface21.com">rod.johnson@interface21.com</a>
 * for commercial support.
 */


package org.springframework.load;

import org.springframework.util.ResponseTimeMonitor;

/**
 * Interface to be implemented by Test threads.
 * <br>The run() method from Runnable interface actually runs the test.
 * @author  Rod Johnson
 * @since February 9, 2001
 */
public interface Test extends Runnable, ConfigurableTest, TestStatus {
	
	
	/**
	 * Set the descriptive name of the test
	 * @param name the human-readable name of the test, for display with test results
	 */
	void setName(String name);
	
	/**
	 * The managing TestSuite will invoke this on test startup,
	 * so it can be used for initialization.
	 * @param ts the TestSuite in which this test runs
	 */
	void setTestSuite(AbstractTestSuite ts);
	
	/**
	 * Set the number of instances of this test that the TestSuite
	 * should instantiate. Each instance is distinct.
	 * This is useful when we want several different kinds of tests,
	 * with different weightings. The default value is 1.
	 * @param count number of instances of this test to instantiate.
	 */
	void setInstances(int count);
	
	/**
	 * @return the number of instances of this distinct test thread
	 */
	int getInstances();
	
	/**
	 * Set the fixture object shared by all Test instances.
	 * (Obviously all tests will want to share the same fixture,
	 * as the aim is to bash it from multiple threads.)
	 * Not all tests will require this.
	 * @param fixture the shared test fixture
	 */
	void setFixture(Object fixture) ;
	
	/**
	 * Return the group the test belongs to. These can be
	 * displayed together for reporting purposes.
	 * @return
	 */
	String getGroup();

    
    void setLongReports(boolean flag);
    
    
    /**
     * @return an array of the failures encountered by this test.
     */
    TestFailedException[] getFailureExceptions();
    
    
    /**
     * @return a statistics object
     */
    ResponseTimeMonitor getTargetResponse();

	/**
	 * Clear all test data, so this thread can be run anew.
	 */
    void reset();
}

