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
package org.springframework.web.flow.support;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.flow.AbstractState;
import org.springframework.web.flow.FlowSession;
import org.springframework.web.flow.FlowExecution;
import org.springframework.web.flow.FlowExecutionListener;

public class FlowExecutionListenerAdapter implements FlowExecutionListener {

	public void started(FlowExecution sessionExecution) {

	}

	public void requestSubmitted(FlowExecution sessionExecution, HttpServletRequest request) {

	}

	public void requestProcessed(FlowExecution sessionExecution, HttpServletRequest request) {

	}

	public void eventSignaled(FlowExecution sessionExecution, String id) {

	}

	public void stateTransitioned(FlowExecution sessionExecution, AbstractState previousState,
			AbstractState newState) {

	}

	public void subFlowEnded(FlowExecution sessionExecution, FlowSession endedSession) {

	}

	public void subFlowSpawned(FlowExecution sessionExecution) {

	}

	public void ended(FlowExecution sessionExecution, FlowSession endedRootFlowSession) {

	}

}