/*
 * Copyright 2002-2005 the original author or authors.
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
 * Throw when no transition can be found in a state that fires for a certain
 * flow execution request context. Typically this is because there is no
 * "handler" transition for the last event that occured in the request context.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoSuchTransitionException extends FlowNavigationException {

	/**
	 * The state this exception was thrown in.
	 */
	private TransitionableState state;

	/**
	 * The event that did not map to any state transition.
	 */
	private Event event;

	/**
	 * Create a new unsupported event exception.
	 * @param state state that does not support the event
	 * @param event unsupported event
	 */
	public NoSuchTransitionException(TransitionableState state, Event event) {
		this(state, event, null);
	}

	/**
	 * Create a new unsupported event exception.
	 * @param state state that does not support the event
	 * @param event unsupported event
	 * @param cause underlying cause of this exception
	 */
	public NoSuchTransitionException(TransitionableState state, Event event, Throwable cause) {
		super(state.getFlow(), "No transition found for event '" + event.getId() + "' in state '" + state.getId()
				+ "' of flow '" + state.getFlow().getId() + "' -- valid transitional criteria are "
				+ Styler.call(state.getTransitionalCriteria())
				+ " -- likely programmer error, check the transition criteria for this state", cause);
		this.state = state;
		this.event = event;
	}

	/**
	 * Returns the event that occured that could not be handled.
	 */
	public Event getEvent() {
		return event;
	}

	/**
	 * Returns the state that could not execute a transition on the
	 * occurence of the event.
	 */
	public TransitionableState getState() {
		return state;
	}

}