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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ToStringCreator;

/**
 * A base super class for state definitions. Each state is associated with
 * exactly one owning flow definition. Standard types of states include action
 * states, view states, subflow states, and end states.
 * <p>
 * Subclasses of this class capture all the configuration information needed for
 * a specific type of state.
 * <p>
 * Subclasses should override the <code>doEnterState</code> method to execute
 * the processing that should occur when this state is entered, acting on its
 * configuration information. The ability to plugin custom state types that
 * execute different behaivior polymorphically is the classic GoF State pattern.
 * <p>
 * Why is this class abstract and not an interface? A specific design choice. An
 * state does not define a generic contract or role, it is expected that
 * specializations of this base class be "States" and not part of some other
 * inheritence hierarchy.
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public abstract class State {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The state's owning flow.
	 */
	private Flow flow;

	/**
	 * The state identifier, unique to the owning flow.
	 */
	private String id;

	/**
	 * Creates a state for the provided <code>flow</code> identified by the
	 * provided <code>id</code>. The id must be locally unique to the owning
	 * flow. The flow state will be automatically added to the flow.
	 * 
	 * @param flow The owning flow
	 * @param id The state identifier (must be unique to the flow)
	 * @throws IllegalArgumentException if this state cannot be added to the
	 *         flow
	 */
	public State(Flow flow, String id) throws IllegalArgumentException {
		setId(id);
		setFlow(flow);
		flow.add(this);
	}

	/**
	 * Returns the state identifier, unique to the owning flow.
	 * @return The state identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the state identifier, unique to the owning flow.
	 * @param id The state identifier.
	 */
	private void setId(String id) {
		Assert.hasText(id, "The state must have a valid identifier");
		this.id = id;
	}

	/**
	 * Returns the owning flow.
	 */
	public Flow getFlow() {
		return flow;
	}

	/**
	 * Set the owning flow.
	 */
	protected void setFlow(Flow flow) {
		Assert.notNull(flow, "The owning flow is required");
		this.flow = flow;
	}

	/**
	 * Is this state transitionable? That is, is this state capable of executing
	 * a transition to another state on the occurence of an event? All
	 * subclasses of <code>TransitionableState</code> are transitionable.
	 * @return true when this is a <code>TransitionableState</code>, false
	 *         otherwise
	 */
	public boolean isTransitionable() {
		return this instanceof TransitionableState;
	}

	/**
	 * Requesting entering of this state in the provided flow execution.
	 * @param context The flow execution stack, tracking a ongoing flow
	 *        execution (client instance of a flow)
	 * @param request The client http request
	 * @param response The server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 */
	protected final ViewDescriptor enter(StateContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Entering state '" + getId() + "' in flow '" + getFlow() + "'");
		}
		context.setCurrentState(this);
		return doEnterState(context);
	}

	/**
	 * Hook method to do any processing as a result of entering this state.
	 * @param context The session execution stack, tracking the current active
	 *        flow session
	 * @param request The client http request
	 * @param response The server http response
	 * @return A view descriptor containing model and view information needed to
	 *         render the results of the event execution.
	 */
	protected abstract ViewDescriptor doEnterState(StateContext context);

	public String toString() {
		ToStringCreator creator = new ToStringCreator(this).append("id", getId());
		createToString(creator);
		return creator.toString();
	}

	/**
	 * Subclasses may override this hook method to stringfy their internal
	 * state. This default implementation does nothing.
	 * @param creator The toString creator, to stringify properties.
	 */
	protected void createToString(ToStringCreator creator) {
	}
}