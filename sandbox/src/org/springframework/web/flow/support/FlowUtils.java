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
	 */
	public static FlowExecutionInfo getFlowExecutionInfo(AttributesAccessor model) {
		return (FlowExecutionInfo)model.getRequiredAttribute(FlowExecutionInfo.ATTRIBUTE_NAME);
	}
}