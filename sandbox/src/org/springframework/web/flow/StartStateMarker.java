/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.web.flow;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.DefaultObjectStyler;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Keith Donald
 */
public class StartStateMarker implements Serializable {
	private static final Log logger = LogFactory.getLog(StartStateMarker.class);

	private Flow flow;

	private TransitionableState state;

	public StartStateMarker(Flow flow, TransitionableState state) {
		Assert.notNull(flow, "The flow is required");
		Assert.notNull(state, "The start state is required");
		this.flow = flow;
		this.state = state;
	}

	protected TransitionableState getStartState() {
		return state;
	}

	public FlowExecutionStartResult start(HttpServletRequest request, HttpServletResponse response, Map inputAttributes) {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting new session for flow '" + flow.getId() + "' in start state '" + getStartState()
					+ "' with input attributes '" + DefaultObjectStyler.call(inputAttributes) + "'");
		}
		FlowExecutionStack sessionExecution = createFlowSessionExecutionStack();
		ModelAndView startingView = startIn(sessionExecution, request, response, inputAttributes);
		return new FlowExecutionStartResult(sessionExecution, startingView);
	}

	public ModelAndView startIn(FlowExecutionStack sessionExecution, HttpServletRequest request,
			HttpServletResponse response, Map inputAttributes) {
		sessionExecution.activate(createSession(flow, inputAttributes));
		return getStartState().enter(sessionExecution, request, response);
	}

	protected FlowExecutionStack createFlowSessionExecutionStack() {
		return new FlowExecutionStack();
	}

	protected FlowSession createSession(Flow flow, Map input) {
		return new FlowSession(flow, input);
	}

}