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
package org.springframework.test.context.junit4;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.internal.runners.TestClass;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.annotation.Timed;

/**
 * <p>
 * SpringTestMethod is a custom implementation of JUnit 4.4's
 * {@link org.junit.internal.runners.TestMethod TestMethod}. Due to class and
 * method visibility constraints, it is necessary to duplicate the code of
 * TestMethod in a local Spring package instead of extending TestMethod.
 * </p>
 * <p>
 * SpringTestMethod also provides support for
 * {@link ExpectedException @ExpectedException} and {@link Timed @Timed}. See
 * {@link #getExpectedException()} and {@link #getTimeout()} for further
 * details.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.1
 */
class SpringTestMethod {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log	LOG	= LogFactory.getLog(SpringTestMethod.class);

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final Method		method;

	private final TestClass		testClass;

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
	 * <p>
	 * Gets the <code>exception</code> that this test method is expected to
	 * throw.
	 * </p>
	 * <p>
	 * Supports both Spring's {@link ExpectedException @ExpectedException(...)}
	 * and JUnit's {@link Test#expected() @Test(expected=...)} annotations, but
	 * not both simultaneously.
	 * </p>
	 *
	 * @return The expected exception, or <code>null</code> if none was
	 *         specified.
	 * @throws IllegalStateException if both types of configuration are used
	 *         simultaneously.
	 */
	public Class<? extends Throwable> getExpectedException() throws IllegalStateException {

		final ExpectedException expectedExceptionAnnotation = getMethod().getAnnotation(ExpectedException.class);
		final Test testAnnotation = getMethod().getAnnotation(Test.class);

		Class<? extends Throwable> expectedException = null;
		final Class<? extends Throwable> springExpectedException = ((expectedExceptionAnnotation != null) && (expectedExceptionAnnotation.value() != null)) ? expectedExceptionAnnotation.value()
				: null;
		final Class<? extends Throwable> junitExpectedException = ((testAnnotation != null) && (testAnnotation.expected() != None.class)) ? testAnnotation.expected()
				: null;

		if ((springExpectedException != null) && (junitExpectedException != null)) {
			final String msg = "Test method ["
					+ getMethod()
					+ "] has been configured with Spring's @ExpectedException("
					+ springExpectedException.getName()
					+ ".class) and JUnit's @Test(expected="
					+ junitExpectedException.getName()
					+ ".class) annotations. Only one declaration of an 'expected exception' is permitted per test method.";
			LOG.error(msg);
			throw new IllegalStateException(msg);
		}
		else if (springExpectedException != null) {
			expectedException = springExpectedException;
		}
		else if (junitExpectedException != null) {
			expectedException = junitExpectedException;
		}

		return expectedException;
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
	 * <p>
	 * Gets the configured <code>timeout</code> for this test method.
	 * </p>
	 * <p>
	 * Supports both Spring's {@link Timed @Timed(millis=...)} and JUnit's
	 * {@link Test#timeout() @Test(timeout=...)} annotations, but not both
	 * simultaneously.
	 * </p>
	 *
	 * @return The timeout, or <code>0</code> if none was specified.
	 * @throws IllegalStateException if both types of configuration are used
	 *         simultaneously.
	 */
	public long getTimeout() throws IllegalStateException {

		final Timed timedAnnotation = getMethod().getAnnotation(Timed.class);
		final Test testAnnotation = getMethod().getAnnotation(Test.class);

		long timeout = 0;
		final long springTimeout = ((timedAnnotation != null) && (timedAnnotation.millis() > 0)) ? timedAnnotation.millis()
				: 0;
		final long junitTimeout = ((testAnnotation != null) && (testAnnotation.timeout() > 0)) ? testAnnotation.timeout()
				: 0;

		if ((springTimeout > 0) && (junitTimeout > 0)) {
			final String msg = "Test method [" + getMethod() + "] has been configured with Spring's @Timed(millis="
					+ springTimeout + ") and JUnit's @Test(timeout=" + junitTimeout
					+ ") annotations. Only one declaration of a 'timeout' is permitted per test method.";
			LOG.error(msg);
			throw new IllegalStateException(msg);
		}
		else if (springTimeout > 0) {
			timeout = springTimeout;
		}
		else if (junitTimeout > 0) {
			timeout = junitTimeout;
		}

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
