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

import org.springframework.web.flow.AbstractState;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowExecutionContext;
import org.springframework.web.flow.FlowExecutionListener;
import org.springframework.web.flow.FlowSession;

/**
 * An abstract adapter class for listeners (observers) of flow execution
 * lifecycle events. The methods in this class are empty. This class exists as
 * convenience for creating listener objects; subclass it and override what you
 * need.
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public abstract class FlowExecutionListenerAdapter implements FlowExecutionListener {

	public void started(FlowExecutionContext context) {
	}

	public void requestSubmitted(FlowExecutionContext context, Event event) {
	}

	public void requestProcessed(FlowExecutionContext context, Event event) {
	}

	public void eventSignaled(FlowExecutionContext context, Event event) {
	}

	public void stateTransitioned(FlowExecutionContext context, AbstractState previousState, AbstractState newState) {
	}

	public void subFlowEnded(FlowExecutionContext context, FlowSession endedSession) {
	}

	public void subFlowSpawned(FlowExecutionContext context) {
	}

	public void ended(FlowExecutionContext context, FlowSession endedRootFlowSession) {
	}
}