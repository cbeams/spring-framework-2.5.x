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
package org.springframework.web.flow.execution;

import org.springframework.core.NestedRuntimeException;

/**
 * Exception signaling a fatal, technical problem while accessing
 * a flow execution storage.
 * 
 * @author Erwin Vervaet
 */
public class FlowExecutionStorageException extends NestedRuntimeException {
	
	private FlowExecutionStorage flowExecutionStorage;
	
	/**
	 * Create a new flow execution storage exception.
	 * @param msg a descriptive message
	 * @param ex the underlying cause of this exception
	 */
	public FlowExecutionStorageException(String msg, Throwable ex) {
		super(msg, ex);
	}

	/**
	 * Create a new flow execution storage exception.
	 * @param flowExecutionStorage the flow execution storage encountering the problem
	 * @param msg a descriptive message
	 * @param ex the underlying cause of this exception
	 */
	public FlowExecutionStorageException(FlowExecutionStorage flowExecutionStorage, String msg, Throwable ex) {
		super(msg, ex);
		this.flowExecutionStorage = flowExecutionStorage;
	}
	
	/**
	 * Returns the flow execution storage that encountered the problem.
	 * Could be <code>null</code>.
	 */
	public FlowExecutionStorage getFlowExecutionStorage() {
		return flowExecutionStorage;
	}
}