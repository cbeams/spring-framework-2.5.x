
package org.springframework.aop.interceptor;

import junit.framework.TestCase;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * @author robh
 */
public class CustomizableTraceInterceptorTests extends TestCase {

	public void setUp() {
		System.setProperty("org.apache.commons.logging.Log", SimpleLog.class.getName());
	}

	public void testSetEmptyEnterMessage() {
		try {
			new CustomizableTraceInterceptor().setEnterMessage("");
			fail("Should not be able to set empty enter message");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetEnterMessageWithReturnValuePlaceholder() {
		try {
			new CustomizableTraceInterceptor().setEnterMessage(CustomizableTraceInterceptor.PLACEHOLDER_RETURN_VALUE);
			fail("Should not be able to set enter message with return value placeholder");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetEnterMessageWithExceptionPlaceholder() {
		try {
			new CustomizableTraceInterceptor().setEnterMessage(CustomizableTraceInterceptor.PLACEHOLDER_EXCEPTION);
			fail("Should not be able to set enter message with exception placeholder");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetEnterMessageWithInvocationTimePlaceholder() {
		try {
			new CustomizableTraceInterceptor().setEnterMessage(CustomizableTraceInterceptor.PLACEHOLDER_INVOCATION_TIME);
			fail("Should not be able to set enter message with invocation time placeholder");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetEmptyExitMessage() {
		try {
			new CustomizableTraceInterceptor().setExitMessage("");
			fail("Should not be able to set empty exit message");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetExitMessageWithExceptionPlaceholder() {
		try {
			new CustomizableTraceInterceptor().setExitMessage(CustomizableTraceInterceptor.PLACEHOLDER_EXCEPTION);
			fail("Should not be able to set exit message with exception placeholder");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetEmptyExceptionMessage() {
		try {
			new CustomizableTraceInterceptor().setExceptionMessage("");
			fail("Should not be able to set empty exception message");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetExceptionMethodWithReturnValuePlaceholder() {
		try {
			new CustomizableTraceInterceptor().setExceptionMessage(CustomizableTraceInterceptor.PLACEHOLDER_RETURN_VALUE);
			fail("Should not be able to set exception message with return value placeholder");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}
}
