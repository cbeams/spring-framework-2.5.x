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
 * Constants used by the Spring web flow system.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class FlowConstants {

	/**
	 * Protected constructor: no need to instantiate this class but allow for
	 * subclassing.
	 */
	protected FlowConstants() {
	}

	// general purpose constants

	/**
	 * Separator (".").
	 */
	public static final String SEPARATOR = ".";

	// controller constants
	
	/**
	 * The flow execution itself will be exposed to the view in a model
	 * attribute with this name ("flowExecution").
	 */
	public static final String FLOW_EXECUTION_ATTRIBUTE = "flowExecution";

	/**
	 * The id of the flow execution will be exposed to the view in a model
	 * attribute with this name ("flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_ATTRIBUTE = "flowExecutionId";

	/**
	 * Clients can send the flow execution id to a controller in a request
	 * parameter with this name ("_flowExecutionId").
	 */
	public static final String FLOW_EXECUTION_ID_PARAMETER = "_flowExecutionId";

	/**
	 * The current state of the flow execution will be exposed to the view in a
	 * model attribute with this name ("currentStateId").
	 */
	public static final String CURRENT_STATE_ID_ATTRIBUTE = "currentStateId";

	/**
	 * Clients can send the current state to a controller in a request parameter
	 * with this name ("_currentStateId").
	 */
	public static final String CURRENT_STATE_ID_PARAMETER = "_currentStateId";

	/**
	 * Clients can send the event to be signaled to a controller in a request
	 * parameter with this name ("_eventId").
	 */
	public static final String EVENT_ID_PARAMETER = "_eventId";

	/**
	 * The event to signaled can also be sent to a controller using a request
	 * attribute set by an intercepting filter, with this name
	 * ("_mapped_eventId"). Use this when you can't use the EVENT_ID_PARAMETER
	 * to pass in the event -- for example, when using image buttons with
	 * javascript restrictions.
	 */
	public static final String EVENT_ID_REQUEST_ATTRIBUTE = "_mapped_eventId";

	/**
	 * Event id value indicating that the event has not been set ("@NOT_SET@").
	 */
	public static final String NOT_SET_EVENT_ID = "@NOT_SET@";

	/**
	 * Clients can send the id (name) of the flow to be started to a controller
	 * using a request parameter with this name ("_flowId").
	 */
	public static final String FLOW_ID_PARAMETER = "_flowId";

	// transaction management constants

	/**
	 * The transaction synchronizer token will be stored in the model using an
	 * attribute with this name ("txToken").
	 */
	public static final String TRANSACTION_TOKEN_ATTRIBUTE_NAME = "txToken";

	/**
	 * A client can send the transaction synchronizer token to a controller
	 * using a request parameter with this name ("_txToken").
	 */
	public static final String TRANSACTION_TOKEN_PARAMETER_NAME = "_txToken";

}