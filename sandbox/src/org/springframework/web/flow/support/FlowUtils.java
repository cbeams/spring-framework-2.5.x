/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow.support;

import org.springframework.web.flow.AttributesAccessor;
import org.springframework.web.flow.FlowExecutionInfo;

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
	public static FlowExecutionInfo getFlowExecutionInfo(AttributesAccessor model)
			throws IllegalStateException {
		return (FlowExecutionInfo)model.getRequiredAttribute(FlowExecutionInfo.ATTRIBUTE_NAME);
	}
}