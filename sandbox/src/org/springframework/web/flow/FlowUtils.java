/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.WebUtils;

/**
 * @author Keith Donald
 */
public class FlowUtils {
	public static FlowSessionExecutionInfo getFlowSessionExecutionInfo(HttpServletRequest request,
			AttributesAccessor model) throws IllegalStateException {
		return (FlowSessionExecutionInfo)WebUtils.getRequiredSessionAttribute(request, (String)model
				.getRequiredAttribute(FlowSession.FLOW_SESSION_ID_ATTRIBUTE_NAME));
	}
}