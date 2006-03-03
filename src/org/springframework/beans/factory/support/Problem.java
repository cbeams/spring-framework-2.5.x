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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.ParseState;

/**
 * Represents a problem with a bean definition configuration. May be a
 * potential fatal problem (an error) or simply just a warning.
 * @author Rob Harrop
 * @since 2.0
 */
public class Problem {

	/**
	 * The message
	 */
	private String message;

	private ParseState parseState;

	private Throwable rootCause;

	private Location location;

	public Problem(String message, ParseState parseState, Throwable rootCause, Location location) {
		this.message = message;
		this.parseState = parseState;
		this.rootCause = rootCause;
		this.location = location;
	}

	public ParseState getParseState() {
		return this.parseState;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Throwable getRootCause() {
		return rootCause;
	}

	public void setRootCause(Throwable rootCause) {
		this.rootCause = rootCause;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getResourceDescription() {
		return getLocation().getResource().toString();
	}

	public String toString() {
		return new StringBuffer()
						.append('[')
						.append(this.parseState)
						.append("] ")
						.append(this.message)
						.append(" @ <")
						.append(getResourceDescription())
						.append(">.").toString();
	}
}
