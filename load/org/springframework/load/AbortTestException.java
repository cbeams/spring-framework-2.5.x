package org.springframework.load;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception that will cause test to abort when thrown from the
 * runPass() method of an AbstractTest.
 * @author Rod Johnson
 */
public class AbortTestException extends NestedRuntimeException {

	/**
	 * Constructor for AbortTestException.
	 * @param s
	 */
	public AbortTestException(String s) {
		super(s);
	}

	/**
	 * Constructor for AbortTestException.
	 * @param s
	 * @param ex
	 */
	public AbortTestException(String s, Throwable ex) {
		super(s, ex);
	}

}
