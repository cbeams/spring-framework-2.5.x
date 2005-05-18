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
package org.springframework.web.flow.config;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception thrown to indicate a problem while building a flow.
 * 
 * @author Erwin Vervaet
 */
public class FlowBuilderException extends NestedRuntimeException {
	
	private FlowBuilder builder;

	/**
	 * Create a new flow builder exception.
	 * @param builder the flow builder that encountered the problem
	 * @param message descriptive message
	 */
	public FlowBuilderException(FlowBuilder builder, String message) {
		super(message);
		this.builder = builder;
	}

	/**
	 * Create a new flow builder exception.
	 * @param message descriptive message
	 * @param cause the underlying cause of this exception
	 */
	public FlowBuilderException(String message, Throwable cause) {
		this(null, message, cause);
	}

	/**
	 * Create a new flow builder exception.
	 * @param builder the flow builder that encountered the problem
	 * @param message descriptive message
	 * @param cause the underlying cause of this exception
	 */
	public FlowBuilderException(FlowBuilder builder, String message, Throwable cause) {
		super(message, cause);
		this.builder = builder;
	}

	/**
	 * Returns the flow builder that encountered a problem.
	 */
	public FlowBuilder getFlowBuilder() {
		return builder;
	}
}