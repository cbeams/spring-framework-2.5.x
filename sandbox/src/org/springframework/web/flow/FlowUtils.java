/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * Utility class providing convenience methods for the Flow system.
 * @author Keith Donald
 */
public class FlowUtils {
	/**
	 * Retrieve information about the current flow session execution.
	 * @param model The model for the executing flow.
	 * @return The session info
	 * @throws IllegalStateException should not happen
	 */
	public static FlowSessionExecutionInfo getFlowSessionExecutionInfo(AttributesAccessor model)
			throws IllegalStateException {
		return (FlowSessionExecutionInfo)model.getRequiredAttribute(FlowSessionExecutionInfo.ATTRIBUTE_NAME);
	}
}