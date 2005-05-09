/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.interceptor;

import junit.framework.TestCase;

import org.springframework.core.JdkVersion;

/**
 * @author Rob Harrop
 */
public class CustomizableTraceInterceptorTests extends TestCase {

	public void testSetEmptyEnterMessage() {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		try {
			new CustomizableTraceInterceptor().setEnterMessage("");
			fail("Should not be able to set empty enter message");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetEnterMessageWithReturnValuePlaceholder() {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		try {
			new CustomizableTraceInterceptor().setEnterMessage(CustomizableTraceInterceptor.PLACEHOLDER_RETURN_VALUE);
			fail("Should not be able to set enter message with return value placeholder");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetEnterMessageWithExceptionPlaceholder() {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		try {
			new CustomizableTraceInterceptor().setEnterMessage(CustomizableTraceInterceptor.PLACEHOLDER_EXCEPTION);
			fail("Should not be able to set enter message with exception placeholder");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetEnterMessageWithInvocationTimePlaceholder() {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		try {
			new CustomizableTraceInterceptor().setEnterMessage(CustomizableTraceInterceptor.PLACEHOLDER_INVOCATION_TIME);
			fail("Should not be able to set enter message with invocation time placeholder");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetEmptyExitMessage() {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		try {
			new CustomizableTraceInterceptor().setExitMessage("");
			fail("Should not be able to set empty exit message");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetExitMessageWithExceptionPlaceholder() {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		try {
			new CustomizableTraceInterceptor().setExitMessage(CustomizableTraceInterceptor.PLACEHOLDER_EXCEPTION);
			fail("Should not be able to set exit message with exception placeholder");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetEmptyExceptionMessage() {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		try {
			new CustomizableTraceInterceptor().setExceptionMessage("");
			fail("Should not be able to set empty exception message");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

	public void testSetExceptionMethodWithReturnValuePlaceholder() {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}
		try {
			new CustomizableTraceInterceptor().setExceptionMessage(CustomizableTraceInterceptor.PLACEHOLDER_RETURN_VALUE);
			fail("Should not be able to set exception message with return value placeholder");
		}
		catch (IllegalArgumentException ex) {
			// success
		}
	}

}
