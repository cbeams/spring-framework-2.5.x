/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.autobuilds.ejbtest.hibernate.tx.ejb;

/**
 * Used as an Ejb Application exception to indicate that a test failed
 * 
 * @author colin sampaleanu
 */
public class TestFailureException extends Exception {

	public TestFailureException() {
		super();
	}

	public TestFailureException(String message) {
		super(message);
	}

	public TestFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public TestFailureException(Throwable cause) {
		super(cause);
	}
}