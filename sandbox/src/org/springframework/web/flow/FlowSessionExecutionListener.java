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

import javax.servlet.http.HttpServletRequest;

/**
 * Interface to be implemented by objects that can listen to the operation of a
 * Flow object.
 * @author Keith Donald
 */
public interface FlowSessionExecutionListener {

	public void started(FlowSessionExecution sessionExecution);

	public void requestSubmitted(FlowSessionExecution sessionExecution, HttpServletRequest request);

	public void requestProcessed(FlowSessionExecution sessionExecution, HttpServletRequest request);

	public void eventSignaled(FlowSessionExecution sessionExecution, String eventId);

	public void stateTransitioned(FlowSessionExecution sessionExecution, AbstractState previousState,
			AbstractState newState);

	public void subFlowSpawned(FlowSessionExecution sessionExecution);

	public void subFlowEnded(FlowSessionExecution sessionExecution, FlowSession endedSession);

	public void ended(FlowSessionExecution sessionExecution, FlowSession endedRootFlowSession);

}