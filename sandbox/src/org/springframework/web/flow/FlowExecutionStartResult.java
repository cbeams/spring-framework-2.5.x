/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.io.Serializable;

/**
 * A parameter object that encapsulates the result of starting a new session
 * execution for a flow.
 * @author Keith Donald
 */
public class FlowExecutionStartResult implements Serializable {
	private FlowExecutionInfo executionInfo;

	private ViewDescriptor startingView;

	public FlowExecutionStartResult(FlowExecutionInfo executionInfo, ViewDescriptor startingView) {
		this.executionInfo = executionInfo;
		this.startingView = startingView;
	}

	/**
	 * @return Returns the sessionInfo.
	 */
	public FlowExecutionInfo getFlowExecutionInfo() {
		return executionInfo;
	}

	/**
	 * @return Returns the startingView.
	 */
	public ViewDescriptor getStartingView() {
		return startingView;
	}
}