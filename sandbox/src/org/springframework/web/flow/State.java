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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.binding.AttributeSource;
import org.springframework.binding.support.EmptyAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A base super class for state definitions. Each state is associated with
 * exactly one owning flow definition. Standard types of states include action
 * states, view states, subflow states, and end states.
 * <p>
 * Subclasses of this class capture all the configuration information needed for
 * a specific type of state.
 * <p>
 * Subclasses should implement the <code>doEnter</code> method to execute
 * the processing that should occur when this state is entered, acting on its
 * configuration information. The ability to plugin custom state types that
 * execute different behaviour polymorphically is the classic GoF state pattern.
 * <p>
 * Why is this class abstract and not an interface? A specific design choice. An
 * state does not define a generic contract or role, it is expected that
 * specializations of this base class be "States" and not part of some other
 * inheritence hierarchy.
 * 
 * @see org.springframework.web.flow.TransitionableState
 * @see org.springframework.web.flow.ActionState
 * @see org.springframework.web.flow.ViewState
 * @see org.springframework.web.flow.SubflowState
 * @see org.springframework.web.flow.EndState
 * 
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
	 * Additional properties further describing this state. 
	 */
	private AttributeSource properties = EmptyAttributeSource.INSTANCE;

	/**
	 * Creates a state for the provided <code>flow</code> identified by the
	 * provided <code>id</code>. The id must be locally unique to the owning
	 * flow. The flow state will be automatically added to the flow.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @throws IllegalArgumentException if this state cannot be added to the
	 *         flow
	 */
	protected State(Flow flow, String id) throws IllegalArgumentException {
		this(flow, id, null);
	}

	/**
	 * Creates a state for the provided <code>flow</code> identified by the
	 * provided <code>id</code>. The id must be locally unique to the owning
	 * flow. The flow state will be automatically added to the flow.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException if this state cannot be added to the
	 *         flow
	 */
	protected State(Flow flow, String id, Map properties) throws IllegalArgumentException {
		setId(id);
		setFlow(flow);
		flow.add(this);
		if (properties != null) {
			this.properties = new MapAttributeSource(new HashMap(properties));
		}
	}

	/**
	 * Returns the state identifier, unique to the owning flow.
	 * @return the state identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the state identifier, unique to the owning flow.
	 * @param id the state identifier.
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
	 * Returns the additional properties describing this state.
	 */
	public AttributeSource getProperties() {
		return this.properties;
	}

	/**
	 * Returns the value of given additional state property, or null if
	 * if not found.
	 */
	public Object getProperty(String propertyName) {
		return this.properties.getAttribute(propertyName);
	}

	/**
	 * Is this state transitionable? That is, is this state capable of executing
	 * a transition to another state on the occurence of an event? All
	 * subclasses of <code>TransitionableState</code> are transitionable.
	 * @return true when this state is a <code>TransitionableState</code>, false
	 *         otherwise
	 */
	public boolean isTransitionable() {
		return this instanceof TransitionableState;
	}

	/**
	 * Is this state interactive? That is, when this state is entered, is
	 * the flow paused and control returned to the client for interaction? 
	 * @return true when this state is an interactive state, false otherwise
	 */
	public boolean isInteractive() {
		return false;
	}

	/**
	 * Enter this state in the provided flow execution request.
	 * @param context the state context in an executing flow (a client instance of a flow)
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the state processing
	 */
	public ViewDescriptor enter(StateContext context) {
		if (logger.isDebugEnabled()) {
			logger.debug("Entering state '" + getId() + "' in flow '" + getFlow() + "'");
		}
		context.setCurrentState(this);
		return doEnter(context);
	}

	/**
	 * Hook method to execute custom behaivior as a result of entering this state.
	 * @param context the state context in an executing flow (a client instance of a flow)
	 * @return a view descriptor containing model and view information needed to
	 *         render the results of the state processing
	 */
	protected abstract ViewDescriptor doEnter(StateContext context);

	public String toString() {
		ToStringCreator creator = new ToStringCreator(this).append("id", getId()).append("flow", flow.getId());
		createToString(creator);
		return creator.toString();
	}

	/**
	 * Subclasses may override this hook method to stringify their internal
	 * state. This default implementation does nothing.
	 * @param creator the toString creator, to stringify properties.
	 */
	protected void createToString(ToStringCreator creator) {
	}
}