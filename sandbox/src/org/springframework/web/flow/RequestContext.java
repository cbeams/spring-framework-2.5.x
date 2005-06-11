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

import java.util.Map;

import org.springframework.binding.AttributeSource;

/**
 * Central interface that allows clients to access contextual information about
 * an ongoing flow execution within the context of a client request. The term
 * <i>request</i> is used to symbolize a call into the flow system to
 * manipulate an executing flow.
 * <p>
 * A new request context is created when one of the entry points on the
 * <code>FlowExecution</code> facade interface is invoked, either
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
 * flow system -- the context object will not be exposed to external client code.
 * <p>
 * Note that a <i>request</i> context is in no way linked to an HTTP request!
 * It just uses the familiar "request" naming convention.
 * 
 * @see org.springframework.web.flow.execution.FlowExecution
 * @see org.springframework.web.flow.Action
 * @see org.springframework.web.flow.TransitionCriteria
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface RequestContext {

	/**
	 * Returns the client event that originated (or triggered) this request.  This 
	 * event may contain parameters provided as input by the client.  In addition,
	 * this event may be downcastable to a specific event type for a specific client environment, like
	 * a HttpServletEvent for servlets or a PortletEvent for portlets.  Such downcasting will give
	 * you full access to a native HttpServletRequest, for example. Noting that, avoid coupling your
	 * flow artifacts to a specific deployment where possible.
	 * @return the originating event, the one that triggered the current
	 *         execution request
	 */
	public Event getSourceEvent();

	/**
	 * Returns additional contextual information about the executing flow.
	 * @return the flow execution context
	 */
	public FlowContext getFlowContext();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * request scope. Request scoped attributes exist for the duration of this request.
	 * @return the request scope
	 */
	public Scope getRequestScope();

	/**
	 * Returns a mutable accessor for accessing and/or setting attributes in
	 * flow scope. Flow scoped attributes exist for the life of the executing flow.
	 * @return the flow scope
	 */
	public Scope getFlowScope();

	/**
	 * Returns the last event signaled during this request. The event may or may
	 * not have caused a state transition to happen.
	 * @return the last signaled event
	 */
	public Event getLastEvent();

	/**
	 * Returns the last state transition executed in this request.
	 * @return the last transition, or <code>null</code> if none has occured yet
	 */
	public Transition getLastTransition();

	/**
	 * Returns a holder for arbitrary execution properties set for the current request.
	 * @return the execution properties, or empty if not set
	 */
	public AttributeSource getProperties();
	
	/**
	 * Update contextual execution properties for given request context.
	 * @param properties the execution properties
	 */
	public void setProperties(AttributeSource properties);
	
	/**
	 * Returns the data model for this context, suitable for exposing to clients
	 * (e.g. web views). Typically the model will contain the union of the data
	 * available in request scope and flow scope.
	 * @return the model that can be exposed to a client
	 */
	public Map getModel();
	
	// application transaction demarcation

	/**
	 * Is the caller participating in the application transaction currently
	 * active in the flow execution?
	 * @param end indicates whether or not the transaction should end after
	 *        checking it
	 * @return true if it is participating in the active transaction, false
	 *         otherwise
	 */
	public boolean inTransaction(boolean end);

	/**
	 * Assert that there is an active application transaction in the flow
	 * execution and that the caller is participating in it.
	 * @param end indicates whether or not the transaction should end after
	 *        checking it
	 * @throws IllegalStateException there is no active transaction in the
	 *         flow execution
	 */
	public void assertInTransaction(boolean end) throws IllegalStateException;

	/**
	 * Start a new transaction in the flow execution.
	 */
	public void beginTransaction();

	/**
	 * End the active transaction in the flow execution.
	 */
	public void endTransaction();
}