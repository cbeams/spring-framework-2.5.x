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
	private FlowExecution execution;

	private ModelAndView startingView;

	public FlowExecutionStartResult(FlowExecution execution, ModelAndView startingView) {
		this.execution = execution;
		this.startingView = startingView;
	}

	/**
	 * @return Returns the sessionInfo.
	 */
	public FlowExecution getFlowExecution() {
		return execution;
	}

	/**
	 * @return Returns the startingView.
	 */
	public ModelAndView getStartingView() {
		return startingView;
	}
}