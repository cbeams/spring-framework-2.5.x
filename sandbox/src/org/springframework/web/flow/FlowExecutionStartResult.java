/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

import java.io.Serializable;

import org.springframework.web.servlet.ModelAndView;

/**
 * A parameter object that encapsulates the result of starting a new session
 * execution for a flow.
 * @author Keith Donald
 */
public class FlowExecutionStartResult implements Serializable {
	private FlowExecutionInfo executionInfo;

	private ModelAndView startingView;

	public FlowExecutionStartResult(FlowExecutionInfo executionInfo, ModelAndView startingView) {
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
	public ModelAndView getStartingView() {
		return startingView;
	}
}