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

import org.springframework.core.NestedRuntimeException;

/**
 * Abstract superclass of all flow navigation exceptions. A flow
 * navigation exception signals a problem while navigating inside
 * a web flow, e.g. when the target state of a transition cannot
 * be found.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class FlowNavigationException extends NestedRuntimeException {

	/**
	 * The definition of the flow where the exception was thrown.
	 */
	private Flow flow;

	/**
	 * Create a new navigation exception.
	 * @param flow the flow in which the navigation exception occured
	 * @param message a descriptive message
	 */
	public FlowNavigationException(Flow flow, String message) {
		super(message);
		this.flow = flow;
	}

	/**
	 * Create a new navigation exception.
	 * @param flow the flow in which the navigation exception occured
	 * @param message a descriptive message
	 * @param cause the underlying cause of the exception
	 */
	public FlowNavigationException(Flow flow, String message, Throwable cause) {
		super(message, cause);
		this.flow = flow;
	}

	/**
	 * Returns the flow in which the navigation exception occured.
	 */
	protected Flow getFlow() {
		return flow;
	}
}