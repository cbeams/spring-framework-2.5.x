package org.springframework.orm.jpa.spi;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Not used.
 * Based on commons logging.
 * @author rodj
 *
 */
public class ClassLoaderSubstitutingTestSuite extends TestSuite {

	/**
	 * The classloader that should be set as the context classloader before each
	 * test in the suite is run.
	 */
	private ClassLoader contextLoader;

	/**
	 * Constructor.
	 * 
	 * @param testClass
	 *            is the TestCase that is to be run, as loaded by the
	 *            appropriate ClassLoader.
	 * 
	 * @param contextClassLoader
	 *            is the loader that should be returned by calls to
	 *            Thread.currentThread.getContextClassLoader from test methods
	 *            (or any method called by test methods).
	 */
	public ClassLoaderSubstitutingTestSuite(Class testClass, ClassLoader contextClassLoader) {
		super(testClass);
		contextLoader = contextClassLoader;
	}

	/**
	 * This method is invoked once for each Test in the current TestSuite. Note
	 * that a Test may itself be a TestSuite object (ie a collection of tests).
	 * <p>
	 * The context classloader and system properties are saved before each test,
	 * and restored after the test completes to better isolate tests.
	 */
	public void runTest(Test test, TestResult result) {
		ClassLoader origContext = Thread.currentThread().getContextClassLoader();
		Properties oldSysProps = (Properties) System.getProperties().clone();
		try {
			System.out.println("Suite: Set loader to " + contextLoader);
			Thread.currentThread().setContextClassLoader(contextLoader);
			test.run(result);
		}
		finally {
			System.setProperties(oldSysProps);
			Thread.currentThread().setContextClassLoader(origContext);
		}
	}
}
