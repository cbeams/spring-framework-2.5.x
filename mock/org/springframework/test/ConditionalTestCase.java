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

package org.springframework.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Superclass for tests that allows conditional test execution
 * at individual test method level.
 * The isDisabledInThisEnvironment() method is invoked before the execution 
 * of each test method. Subclasses can override that method to
 * return whether or not the given test should be executed.
 * Note that the tests will still appear to have executed and
 * passed; log output will show that the test was not executed.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class ConditionalTestCase extends TestCase {
	
	/**
	 * Logger available to subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());
	
	private static int disabledTestCount;
	
	/**
	 * @return the number of tests disabled in this environment
	 */
	public static int getDisabledTestCount() {
		return disabledTestCount;
	}

	protected ConditionalTestCase() {
	}
	
	protected ConditionalTestCase(String s) {
		super(s);
	}
	
	
	/**
	 * Should this test run?
	 * @param testMethodName name of the test method
	 * @return whether the test should execute in the current envionment
	 */
	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return false;
	}
	
	public void runBare() throws Throwable {
		// getName will return the name of the method being run
		if (isDisabledInThisEnvironment(getName())) {
			++disabledTestCount;
			logger.info("**** " + getClass().getName() + "." + getName() + " disabled in this environment: " +
					"Total disabled tests=" + getDisabledTestCount());
			return;
		}
		
		// Let JUnit handle execution
		super.runBare();
	}
}
