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

import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.NoSuchFlowExecutionException;

/**
 * Storage strategy for flow executions. A flow execution manager uses
 * this interface to load and save flow executions.
 * 
 * @see org.springframework.web.flow.execution.FlowExecutionManager
 * 
 * @author Erwin Vervaet
 */
public interface FlowExecutionStorage {

	/**
	 * Load an existing flow execution, identified by given unique id, from
	 * the storage.
	 * @param id the unique id of the flow execution, as returned by the
	 *        {@link #save(String, FlowExecution, Event) save} method
	 * @param requestingEvent the event requesting the load of the flow execution
	 * @return the loaded flow execution
	 * @throws NoSuchFlowExecutionException when there is no flow execution
	 *         with specified id in the storage
	 * @throws FlowExecutionStorageException when there is a technical problem
	 *         accessing the flow execution storage
	 */
	public FlowExecution load(String id, Event requestingEvent) throws NoSuchFlowExecutionException,
			FlowExecutionStorageException;

	/**
	 * Save given flow execution in the storage.
	 * @param id the unique id of the flow execution, or <code>null</code>
	 *        if the flow execution does not yet have an id (e.g. was not
	 *        previously saved)
	 * @param requestingEvent the event requesting the save of the flow execution
	 * @param flowExecution the flow execution to save
	 * @return the unique id that actually identifies the saved flow execution,
	 *         this could be different from the id passed into the method
	 * @throws FlowExecutionStorageException when there is a technical problem
	 *         accessing the flow execution storage
	 */
	public String save(String id, FlowExecution flowExecution, Event requestingEvent)
			throws FlowExecutionStorageException;

	/**
	 * Remove the identified flow execution from the storage.
	 * @param id the unique id of the flow execution, as returned by the
	 *        {@link #save(String, FlowExecution, Event) save} method
	 * @param requestingEvent the event requesting the remove of the flow execution
	 * @throws FlowExecutionStorageException when there is a technical problem
	 *         accessing the flow execution storage
	 */
	public void remove(String id, Event requestingEvent) throws FlowExecutionStorageException;

}