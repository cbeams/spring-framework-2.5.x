package org.springframework.load;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception thrown when a test or test suite cannot be initialized
 * because of misconfiguration. This typically means that the 
 * configuration data file is invalid.
 * 
 * @author Rod Johnson
 */
public class TestConfigurationException extends NestedRuntimeException {

	/**
	 * Constructor for TestConfigurationException.
	 * @param s
	 */
	public TestConfigurationException(String s) {
		super(s);
	}

	/**
	 * Constructor for TestConfigurationException.
	 * @param s
	 * @param ex
	 */
	public TestConfigurationException(String s, Throwable ex) {
		super(s, ex);
	}

}
