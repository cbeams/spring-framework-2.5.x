/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.config;

/**
 * @author Keith Donald
 */
public abstract class FlowConstants {

	// controller constants
	
	public static final String CURRENT_STATE_ID_ATTRIBUTE = "currentStateId";

	public static final String FLOW_EXECUTION_ID_ATTRIBUTE = "flowExecutionId";

	public static final String EVENT_ID_ATTRIBUTE = "_mapped_eventId";

	public static final String FLOW_ID_PARAMETER = "_flowId";

	public static final String FLOW_EXECUTION_ID_PARAMETER = "_flowExecutionId";

	public static final String CURRENT_STATE_ID_PARAMETER = "_currentStateId";

	public static final String EVENT_ID_PARAMETER = "_eventId";

	public static String NOT_SET_EVENT_ID = "@NOT_SET@";

	// event and state constants
	
	/**
	 * The <code>ADD</code> action state/event identifier.
	 */
	public static final String ADD = "add";

	/**
	 * The <code>BACK</code> action state/event identifier.
	 */
	public static final String BACK = "back";

	/**
	 * The <code>BIND_AND_VALIDATE</code> action state/event identifier.
	 */
	public static final String BIND_AND_VALIDATE = "bindAndValidate";

	/**
	 * The <code>CANCEL</code> action state/event identifier.
	 */
	public static final String CANCEL = "cancel";

	/**
	 * The <code>CREATE</code> action state/event identifier.
	 */
	public static final String CREATE = "create";

	/**
	 * The <code>DELETE</code> action state/event identifier.
	 */
	public static final String DELETE = "delete";

	/**
	 * The <code>EDIT</code> action state/event identifier.
	 */
	public static final String EDIT = "edit";

	/**
	 * The <code>ERROR</code> event id
	 */
	public static final String ERROR = "error";

	/**
	 * The <code>FINISH</code> action state/event identifier.
	 */
	public static final String FINISH = "finish";

	/**
	 * The <code>GET</code> action state/event identifier.
	 */
	public static final String GET = "get";

	/**
	 * The <code>SET</code> action state/event identifier.
	 */
	public static final String SET = "set";

	/**
	 * The <code>SETUP</code> action state/event identifier.
	 */
	public static final String SETUP = "setup";

	/**
	 * The <code>PUT</code> action state/event identifier.
	 */
	public static final String PUT = "put";

	/**
	 * The <code>LOAD</code> event identifier.
	 */
	public static final String LOAD = "load";

	/**
	 * The <code>FIND</code> action state/event identifier.
	 */
	public static final String FIND = "find";

	/**
	 * The <code>LINK</code> action state/event identifier.
	 */
	public static final String LINK = "link";

	/**
	 * The <code>REMOVE</code> action state/event identifier.
	 */
	public static final String REMOVE = "remove";

	/**
	 * The <code>POPULATE</code> form action state/event identifier.
	 */
	public static final String POPULATE = "populate";

	/**
	 * The <code>RESET</code> action state/event identifier.
	 */
	public static final String RESET = "reset";

	/**
	 * The <code>RESUME</code> action state/event identifier.
	 */
	public static final String RESUME = "resume";

	/**
	 * The <code>SAVE</code> action state/event identifier.
	 */
	public static final String SAVE = "save";

	/**
	 * The <code>SEARCH</code> action state/event identifier.
	 */
	public static final String SEARCH = "search";

	/**
	 * The <code>SUCCESS</code> action state/event identifier.
	 */
	public static final String SUCCESS = "success";

	/**
	 * The <code>SUBMIT</code> action state/event identifier.
	 */
	public static final String SUBMIT = "submit";

	/**
	 * The <code>UNLINK</code> action state/event identifier.
	 */
	public static final String UNLINK = "unlink";

	/**
	 * The <code>BIND</code> action state/event identifier.
	 */
	public static final String BIND = "bind";

	/**
	 * The <code>VALIDATE</code> action state/event identifier.
	 */
	public static final String VALIDATE = "validate";

	/**
	 * The <code>VIEW</code> view state identifier.
	 */
	public static final String VIEW = "view";

	/**
	 * The <code>SELECT</code> event identifier.
	 */
	public static final String SELECT = "select";

	/**
	 * The default <code>ATTRIBUTES_MAPPER_ID_SUFFIX</code>
	 */
	public static final String ATTRIBUTES_MAPPER_ID_SUFFIX = "attributesMapper";

	public static final String TRANSACTION_TOKEN_ATTRIBUTE_NAME = "txToken";

	public static final String TRANSACTION_TOKEN_PARAMETER_NAME = "_txToken";

	protected FlowConstants() {

	}
}