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
	private FlowExecutionInfo sessionInfo;

	private ViewDescriptor startingView;

	public FlowExecutionStartResult(FlowExecutionInfo sessionInfo, ViewDescriptor startingView) {
		this.sessionInfo = sessionInfo;
		this.startingView = startingView;
	}

	/**
	 * @return Returns the sessionInfo.
	 */
	public FlowExecutionInfo getFlowSessionExecutionInfo() {
		return sessionInfo;
	}

	/**
	 * @return Returns the startingView.
	 */
	public ViewDescriptor getStartingView() {
		return startingView;
	}
}