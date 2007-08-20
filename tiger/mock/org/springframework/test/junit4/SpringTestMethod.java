/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test.junit4;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.internal.runners.TestClass;

/**
 * SpringTestMethod is a custom implementation of JUnit 4.4's
 * {@link org.junit.internal.runners.TestMethod}. Due to class and method
 * visibility constraints, it is necessary to duplicate the code of TestMethod
 * in a local Spring package instead of extending TestMethod.
 *
 * @author Sam Brannen
 * @version $Revision: 1.5 $
 * @since 2.1
 */
class SpringTestMethod {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final Method	method;

	private final TestClass	testClass;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a test method for the supplied {@link Method method} and
	 * {@link TestClass test class}.
	 *
	 * @param method The test method.
	 * @param testClass The test class.
	 */
	public SpringTestMethod(final Method method, final TestClass testClass) {

		this.method = method;
		this.testClass = testClass;
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Determines if this test method is {@link Test#expected() expected} to
	 * throw an exception.
	 *
	 * @return <code>true</code> if this test method should throw an
	 *         exception.
	 */
	public boolean expectsException() {

		return getExpectedException() != null;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the {@link After @After} methods for this test method.
	 *
	 * @return The <em>after</em> methods.
	 */
	public List<Method> getAfters() {

		return getTestClass().getAnnotatedMethods(After.class);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the {@link Before @Before} methods for this test method.
	 *
	 * @return The <em>before</em> methods.
	 */
	public List<Method> getBefores() {

		return getTestClass().getAnnotatedMethods(Before.class);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the {@link Test#expected() exception} that this test method is
	 * expected to throw.
	 *
	 * @return The expected exception, or <code>null</code> if none was
	 *         specified.
	 */
	protected Class<? extends Throwable> getExpectedException() {

		// XXX Optional: add support for Spring's @ExpectedException

		final Test annotation = getMethod().getAnnotation(Test.class);
		if (annotation == null || annotation.expected() == None.class) {
			return null;
		}
		else {
			return annotation.expected();
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the actual {@link Method method} referenced by this test method.
	 *
	 * @return The test method.
	 */
	public final Method getMethod() {

		return this.method;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the {@link TestClass test class} for this test method.
	 *
	 * @return The test class.
	 */
	public final TestClass getTestClass() {

		return this.testClass;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the configured {@link Test#timeout() timeout} for this test method.
	 *
	 * @return The timeout, or <code>0</code> if none was specified.
	 */
	public long getTimeout() {

		// XXX Optional: add support for Spring's @Timed

		final Test annotation = getMethod().getAnnotation(Test.class);
		if (annotation == null) {
			return 0;
		}
		final long timeout = annotation.timeout();
		return timeout;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Convenience method for {@link Method#invoke(Object, Object...) invoking}
	 * the method associated with this test method. Throws exceptions consistent
	 * with {@link Method#invoke(Object, Object...) Method.invoke()}.
	 *
	 * @param testInstance The test instance upon which to invoke the method.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void invoke(final Object testInstance) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		getMethod().invoke(testInstance);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Determines if this test method should be ignored.
	 *
	 * @return <code>true</code> if this test method should be ignored.
	 */
	public boolean isIgnored() {

		return getMethod().getAnnotation(Ignore.class) != null;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Determines if this test method {@link Test#expected() expects} exceptions
	 * of the type of the supplied <code>exception</code> to be thrown.
	 *
	 * @param exception The thrown exception.
	 * @return <code>true</code> if the supplied exception was of an expected
	 *         type.
	 */
	public boolean isUnexpected(final Throwable exception) {

		return !getExpectedException().isAssignableFrom(exception.getClass());
	}

	// ------------------------------------------------------------------------|

}
