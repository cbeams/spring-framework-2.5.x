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

import org.springframework.util.Styler;

/**
 * Thrown when the event signaled does not map to a valid transition in the
 * current state. That is, there is no "handler" transition for the given event
 * in the current state.
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class EventNotSupportedException extends FlowNavigationException {

	/**
	 * The state this exception was thrown.
	 */
	private TransitionableState state;

	/**
	 * The event that did not map to any state transition.
	 */
	private Event event;

	/**
	 * Create a new unsupported event exception.
	 * @param state State that does not support the event
	 * @param eventId Unsupported event
	 */
	public EventNotSupportedException(TransitionableState state, Event event) {
		super(state.getFlow());
		this.state = state;
		this.event = event;
	}

	/**
	 * Create a new unsupported event exception.
	 * @param state State that does not support the event
	 * @param eventId Unsupported event
	 * @param cause Underlying cause of this exception
	 */
	public EventNotSupportedException(TransitionableState state, Event event, Throwable cause) {
		super(state.getFlow(), cause);
		this.state = state;
		this.event = event;
	}

	public String getMessage() {
		return "No transition found for event '" + event.getId() + "' in state '" + state.getId() + "' of flow '"
				+ getFlow().getId() + "' -- valid transitional event criteria are "
				+ Styler.call(state.getEventIdCriteria()) + " -- programmer error?";
	}
}