/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.io.Serializable;

/**
 * @author Keith Donald
 */
public class FlowSessionExecutionStartResult implements Serializable {
	private FlowSessionExecutionInfo sessionInfo;

	private ViewDescriptor startingView;

	public FlowSessionExecutionStartResult(FlowSessionExecutionInfo sessionInfo, ViewDescriptor startingView) {
		this.sessionInfo = sessionInfo;
		this.startingView = startingView;
	}

	/**
	 * @return Returns the sessionInfo.
	 */
	public FlowSessionExecutionInfo getFlowSessionExecutionInfo() {
		return sessionInfo;
	}

	/**
	 * @return Returns the startingView.
	 */
	public ViewDescriptor getStartingView() {
		return startingView;
	}
}