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
 * An instance of a single executing <code>Flow</code> participating in a
 * flow execution. Also acts as a "flow-scope" data model.
 * <p>
 * The stack of executing flow sessions represents the complete state of an
 * ongoing flow execution.
 * <p>
 * A flow session will go through several states during its lifecycle.
 * Initially it will be {@link FlowSessionStatus#CREATED}. Once the flow
 * session is activated in a flow execution, it becomes
 * {@link FlowSessionStatus#ACTIVE}. When control returns to the client for
 * user think time, the status becomes {@link FlowSessionStatus#PAUSED}.
 * If the flow session is deactivated because a subflow was spawned, it will become
 * {@link FlowSessionStatus#SUSPENDED} until the subflow returns (ends).
 * When the flow session is terminated by the flow execution, its status becomes
 * {@link FlowSessionStatus#ENDED}, ending its lifecycle.
 * <p>
 * Note that a flow <i>session</i> is in no way linked to an HTTP session! It
 * just uses the familiar "request/session" naming convention.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public interface FlowSession {
	
	/**
	 * Returns the flow associated with this flow session.
	 */
	public Flow getFlow();

	/**
	 * Returns the state that is currently active in this flow session.
	 */
	public State getCurrentState();
	
	/**
	 * Returns the current status of this flow session.
	 */
	public FlowSessionStatus getStatus();

	/**
	 * Return the session attributes -- "flow scope".
	 * @return the flow scope attributes
	 */
	public Scope getScope();

	/**
	 * Returns the parent flow session in the current flow execution,
	 * or <code>null</code> if there is not parent flow session.
	 */
	public FlowSession getParent();
	
	/**
	 * Returns whether this flow session is the root flow session in 
	 * the ongoing flow execution. The root flow session does not have
	 * a parent flow session. 
	 */
	public boolean isRoot();	
}