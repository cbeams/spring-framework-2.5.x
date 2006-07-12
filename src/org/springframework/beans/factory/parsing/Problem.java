/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.parsing;

/**
 * Represents a problem with a bean definition configuration.
 * 
 * <p>May be a potential fatal problem (an error) or simply just a warning.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class Problem {

	private final String message;

	private final ParseState parseState;

	private final Throwable rootCause;

	private final Location location;


	/**
	 * Creates a new instance of the {@link Problem} class.
	 * @param message	 a message detailing the problem
	 * @param parseState the {@link ParseState} at the time of the error
	 * @param rootCause  the underlying expection that caused the error (may be <code>null</code>)
	 * @param location   the location within a bean configuration source that triggered the error
	 */
	public Problem(String message, ParseState parseState, Throwable rootCause, Location location) {
		this.message = message;
		this.parseState = parseState;
		this.rootCause = rootCause;
		this.location = location;
	}


	/**
	 * Gets the {@link ParseState} at the time of the error.
	 * @return the {@link ParseState} at the time of the error
	 */
	public ParseState getParseState() {
		return this.parseState;
	}

	/**
	 * Gets the message detailing the problem.
	 * @return the message detailing the problem
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the underlying expection that caused the error (may be <code>null</code>).
	 * @return the underlying expection that caused the error (may be <code>null</code>)
	 */
	public Throwable getRootCause() {
		return rootCause;
	}

	/**
	 * Gets the location within a bean configuration source that triggered the error
	 * @return the location within a bean configuration source that triggered the error
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Gets the description of the bean configuration source that triggered the error
	 * @return the description of the bean configuration source that triggered the error
	 */
	public String getResourceDescription() {
		return getLocation().getResource().toString();
	}


	public String toString() {
		return new StringBuffer().append('[').append(this.parseState).append("] ").
				append(this.message).append(" @ <").append(getResourceDescription()).append(">").toString();
	}

}
