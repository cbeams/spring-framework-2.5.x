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

import org.springframework.core.Styler;

/**
 * Throw when no transition can be matched given the occurence of an event in
 * the context of a flow execution request.
 * <p>
 * Typically this happens because there is no "handler" transition for the last
 * event that occured.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class NoMatchingTransitionException extends FlowNavigationException {

	/**
	 * The state this exception was thrown in.
	 */
	private TransitionableState state;

	/**
	 * The context that did not match a supported state transition when
	 * evaluated by the <code>TransitionCriteria</code> for the set of
	 * possible Transitions out of the <code>TransitionableState</code>.
	 */
	private RequestContext context;

	/**
	 * Create a new no matching transition exception.
	 * @param state the state that could not be transitioned out of
	 * @param context the request context that did not trigger a valid
	 *        transition
	 */
	public NoMatchingTransitionException(TransitionableState state, RequestContext context) {
		this(state, context, (Throwable)null);
	}

	/**
	 * Create a new no matching transition exception.
	 * @param state the state that could not be transitioned out of
	 * @param context the request context that did not trigger a valid
	 *        transition
	 * @param cause the underlying cause
	 */
	public NoMatchingTransitionException(TransitionableState state, RequestContext context, Throwable cause) {
		super(state.getFlow(), "No transition found for event '" + context.getLastEvent().getId() + "' in state '"
				+ state.getId() + "' of flow '" + state.getFlow().getId() + "' -- valid transitional criteria are "
				+ Styler.call(state.getTransitionMatchingCriteria())
				+ " -- likely programmer error, check the set of TransitionCriteria for this state", cause);
		this.state = state;
		this.context = context;
	}

	/**
	 * Create a new no matching transition exception.
	 * @param state the state that could not be transitioned out of
	 * @param context the request context that did not trigger a valid
	 *        transition
	 * @param message the message
	 */
	public NoMatchingTransitionException(TransitionableState state, RequestContext context, String message) {
		this(state, context, message, null);
	}

	/**
	 * Create a new no matching transition exception.
	 * @param state the state that could not be transitioned out of
	 * @param context the request context that did not trigger a valid
	 *        transition
	 * @param message the message
	 * @param cause the underlying cause
	 */
	public NoMatchingTransitionException(TransitionableState state, RequestContext context, String message,
			Throwable cause) {
		super(state.getFlow(), message, cause);
		this.state = state;
		this.context = context;
	}

	/**
	 * Returns the state that could not execute a transition on the occurence of
	 * the event in the context of the current request.
	 */
	public TransitionableState getState() {
		return state;
	}

	/**
	 * Returns the context for the current request that did not trigger any
	 * supported transition out of the set state.
	 * @return the request context
	 */
	public RequestContext getRequestContext() {
		return context;
	}
}