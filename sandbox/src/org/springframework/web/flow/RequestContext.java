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

import org.springframework.binding.AttributeSource;

/**
 * Central interface that allows clients to access contextual information about
 * an ongoing flow execution within the context of a client request. The term
 * <i>request</i> is used to symbolize a call into the flow system to
 * manipulate a FlowExecution.
 * <p>
 * A new request context is created when one of the entry points on the
 * FlowExecution facade interface is invoked, either
 * ({@link org.springframework.web.flow.execution.FlowExecution#start(Event)}
 * to activate a new executing flow, or
 * {@link org.springframework.web.flow.execution.FlowExecution#signalEvent(Event)}) to
 * manipulate the state of an already executing flow.
 * <p>
 * Once created, this context interface is passed around throughout request
 * processing, where it may be referenced and reasoned upon, typically by
 * user-implemented action code and state transition criteria. The request
 * context is disposed when a entry-point call into a flow execution returns.
 * This fact means the request context is an internal artifact used within the
 * flow system--the context object will not be exposed to external client code.
 * <p>
 * Note that a <i>request</i> context is in no way linked to an HTTP request!
 * It just uses the familiar request naming convention.
 * 
 * @see org.springframework.web.flow.execution.FlowExecution
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface RequestContext {

	/**
	 * Returns the root flow definition of the flow execution that is executing
	 * this request.
	 * @return the root flow definition
	 */
	public Flow getRootFlow();

	/**
	 * Is the root flow of the flow execution executing this request active?
	 * @return true or false
	 */
	public boolean isRootFlowActive();

	/**
	 * Returns the flow definition of the active flow in the flow execution that
	 * is executing this request.
	 * @return the active flow definition
	 * @throws IllegalStateException the flow execution is not active
	 */
	public FlowSession getActiveSession() throws IllegalStateException;

	/**
	 * Is a flow actively executing?
	 * @return true if yes, false otherwise
	 */
	public boolean isActive();

	/**
	 * Returns the client event that originated (triggered) this request.
	 * @return the originating event, the one that triggered the current
	 *         execution request
	 */
	public Event getOriginatingEvent();

	/**
	 * Returns the last event signaled during this request. The event may or may
	 * not have caused a state transition to happen.
	 * @return the last signaled event
	 */
	public Event getLastEvent();

	/**
	 * Returns the last state transition executed in this request.
	 * @return the last transition, or <code>null</code> if none has occured yet.
	 */
	public Transition getLastTransition();
	
	/**
	 * Returns a holder for properties about the currently executing
	 * <code>Action</code> in the context of the current request.
	 * <p>
	 * An <code>Action</code> can use these properties to influence its behavior
	 * based on the current state of the request context.
	 * @return the action attributes, or empty if not set
	 */
	public AttributeSource getExecutionProperties();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * request scope.
	 * @return the request scope
	 */
	public Scope getRequestScope();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * flow scope.
	 * @return the flow scope
	 */
	public Scope getFlowScope();

	/**
	 * Returns a synchronizer for demarcating application transactions within
	 * the flow execution associated with this context.
	 * @return the transaction synchronizer
	 */
	public TransactionSynchronizer getTransactionSynchronizer();
}