/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.web.flow.execution;

import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.web.flow.FlowContext;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.State;

/**
 * Mock implementation of the <code>FlowExecutionListener</code> interface for
 * use in unit tests.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class MockFlowExecutionListener implements FlowExecutionListener {

	private boolean started;

	private boolean executing;
	
	private boolean expired;
	
	private int flowNestingLevel;

	private boolean requestInProcess;

	private int requestsSubmitted;

	private int requestsProcessed;

	private int eventsSignaled;

	private int stateTransitions;

	/**
	 * Make sure the flow execution has already been started.
	 */
	protected void assertStarted() {
		Assert.state(started, "The flow execution has not yet been started");
	}

	public void requestSubmitted(RequestContext context) {
		Assert.state(!requestInProcess, "There is already a request being processed");
		requestsSubmitted++;
		requestInProcess = true;
	}

	public void sessionStarting(RequestContext context, State startState, Map input) throws EnterStateVetoException {
		if (!context.getFlowContext().isActive()) {
			Assert.state(!started, "The flow execution was already started");
			flowNestingLevel = 0;
			eventsSignaled = 0;
			stateTransitions = 0;
			executing = true;
			expired = false;
		}
	}
	
	public void sessionStarted(RequestContext context) {
		if (context.getFlowContext().getActiveSession().isRoot()) {
			Assert.state(!started, "The flow execution was already started");
			started = true;
		} else {
			assertStarted();
			flowNestingLevel++;
		}
	}

	public void requestProcessed(RequestContext context) {
		Assert.state(requestInProcess, "There is no request being processed");
		requestsProcessed++;
		requestInProcess = false;
	}

	public void eventSignaled(RequestContext context) {
		eventsSignaled++;
	}

	public void stateEntering(RequestContext context, State nextState) throws EnterStateVetoException {
	}

	public void stateEntered(RequestContext context, State previousState, State newState) {
		stateTransitions++;
	}


	public void paused(RequestContext context) {
		executing = false;
	}

	public void resumed(RequestContext context) {
		executing = true;
	}

	public void ended(RequestContext context, FlowSession endedSession) {
		assertStarted();
		if (endedSession.isRoot()) {
			Assert.state(flowNestingLevel == 0, "The flow execution should have ended");
			started = false;
			executing = false;
		} else {
			flowNestingLevel--;
			Assert.state(started, "The flow execution prematurely ended");
		}
	}

	public void expired(FlowContext flowContext) {
		expired = true;
	}


	/**
	 * Is the flow execution running, e.g. it has started but not yet ended.
	 */
	public boolean isStarted() {
		return started;
	}

	/**
	 * @return
	 */
	public boolean isExecuting() {
		return executing;
	}
	/**
	 * Returns the nesting level of the currently active flow in the flow
	 * execution. The root flow is at level 0, a sub flow of the root flow
	 * is at level 1, and so on.
	 */
	public int getFlowNestingLevel() {
		return flowNestingLevel;
	}

	/**
	 * Is there a request in process, e.g. it was submitted but has not yet
	 * completed processing.
	 */
	public boolean isRequestInProcess() {
		return requestInProcess;
	}

	/**
	 * Returns the number of requests submitted so far.
	 */
	public int getRequestsSubmittedCount() {
		return requestsSubmitted;
	}

	/**
	 * Returns the number of requests processed so far.
	 */
	public int getRequestsProcessedCount() {
		return requestsProcessed;
	}

	/**
	 * Returns the number of events signaled so far.
	 */
	public int getEventsSignaledCount() {
		return eventsSignaled;
	}

	/**
	 * Returns the number of state transitions executed so far.
	 */
	public int getTransitionCount() {
		return stateTransitions;
	}
	
	public void reset() {
		started = false;
		executing = false;
		requestsSubmitted = 0;
		requestsProcessed = 0;
	}
}