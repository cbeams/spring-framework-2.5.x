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
package org.springframework.web.flow;

import java.util.Map;

/**
 * A context for a currently executing flow.
 * @author Keith Donald
 */
public interface FlowExecutionContext {

	/**
	 * Returns this flow execution's root flow definition.
	 * @return The root flow definition.
	 */
	public Flow getRootFlow();

	/**
	 * Returns this flow execution's active flow definition.
	 * @return The active flow definition
	 * @throws IllegalStateException the flow execution is not active
	 */
	public Flow getActiveFlow() throws IllegalStateException;

	/**
	 * Returns a mutable list of listeners attached to this flow execution.
	 * @return The flow execution listener list
	 */
	public FlowExecutionListenerList getFlowExecutionListenerList();

	/**
	 * Is the flow execution managed in this context active?
	 * @return true if yes, false otherwise
	 */
	public boolean isFlowExecutionActive();

	/**
	 * Returns the current state of the flow execution.
	 * @return The current state
	 * @throws IllegalStateException the flow execution is not active
	 */
	public State getCurrentState() throws IllegalStateException;

	/**
	 * Returns the last event signaled within this flow execution context. The
	 * event may or may not have caused a state transition to happen.
	 * @return The last signaled event
	 */
	public Event getEvent();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * request scope.
	 * @return the attribute setter
	 */
	public Scope requestScope();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * flow scope.
	 * @return the attribute setter
	 */
	public Scope flowScope();

	/**
	 * Returns a synchronizer for demaracting application transactions within
	 * the flow execution associated with this context.
	 * @return The transaction synchronizer
	 */
	public TransactionSynchronizer getTransactionSynchronizer();

	/**
	 * Returns the data model for this flow model, suitable for exposing to web
	 * views.
	 * @return Map of model attributes for this flow model.
	 */
	public Map getModel();
}