/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

/**
 * @author Keith Donald
 */
public class FlowUtils {
	public static FlowSessionExecutionInfo getFlowSessionExecutionInfo(AttributesAccessor model)
			throws IllegalStateException {
		return (FlowSessionExecutionInfo)model
				.getRequiredAttribute(FlowSessionExecutionInfo.FLOW_SESSION_EXECUTION_INFO_ATTRIBUTE_NAME);
	}
}