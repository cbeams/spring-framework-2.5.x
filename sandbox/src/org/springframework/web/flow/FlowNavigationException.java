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
package org.springframework.web.flow;

/**
 * Abstract superclass of all navigation exceptions.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class FlowNavigationException extends RuntimeException {

	private Flow flow;

	/**
	 * Create a new navigation exception.
	 * @param flow The flow in which the navigation exception occured
	 */
	public FlowNavigationException(Flow flow) {
		super();
		this.flow = flow;
	}

	/**
	 * Create a new navigation exception.
	 * @param flow The flow in which the navigation exception occured
	 * @param message A descriptive message
	 */
	public FlowNavigationException(Flow flow, String message) {
		super(message);
		this.flow = flow;
	}

	/**
	 * Create a new navigation exception.
	 * @param flow The flow in which the navigation exception occured
	 * @param message A descriptive message
	 * @param cause The underlying cause of the exception
	 */
	public FlowNavigationException(Flow flow, String message, Throwable cause) {
		super(message, cause);
		this.flow = flow;
	}

	/**
	 * Create a new navigation exception.
	 * @param flow The flow in which the navigation exception occured
	 * @param cause The underlying cause of the exception
	 */
	public FlowNavigationException(Flow flow, Throwable cause) {
		super(cause);
		this.flow = flow;
	}

	/**
	 * @return The flow in which the navigation exception occured.
	 */
	protected Flow getFlow() {
		return flow;
	}
}