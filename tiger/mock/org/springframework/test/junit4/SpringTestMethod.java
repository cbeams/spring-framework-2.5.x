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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.internal.runners.TestClass;

/**
 * TODO Add comments for SpringTestMethod.
 *
 * @author Sam Brannen
 * @version $Revision: 1.1 $
 * @since 2.1
 */
public class SpringTestMethod {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	protected static final Log LOG = LogFactory.getLog(SpringTestMethod.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final Method method;

	private final TestClass testClass;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

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

	public boolean expectsException() {

		return getExpectedException() != null;
	}

	public List<Method> getAfters() {

		return getTestClass().getAnnotatedMethods(After.class);
	}

	public List<Method> getBefores() {

		return getTestClass().getAnnotatedMethods(Before.class);
	}

	protected Class<? extends Throwable> getExpectedException() {

		final Test annotation = getMethod().getAnnotation(Test.class);
		if (annotation == null || annotation.expected() == None.class) {
			return null;
		}
		else {
			return annotation.expected();
		}
	}

	/**
	 * @return the test method.
	 */
	public final Method getMethod() {

		return this.method;
	}

	/**
	 * @return the test class.
	 */
	public final TestClass getTestClass() {

		return this.testClass;
	}

	public long getTimeout() {

		final Test annotation = getMethod().getAnnotation(Test.class);
		if (annotation == null) {
			return 0;
		}
		final long timeout = annotation.timeout();
		return timeout;
	}

	public void invoke(final Object testInstance) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		getMethod().invoke(testInstance);
	}

	public boolean isIgnored() {

		return getMethod().getAnnotation(Ignore.class) != null;
	}

	public boolean isUnexpected(final Throwable exception) {

		return !getExpectedException().isAssignableFrom(exception.getClass());
	}

}
