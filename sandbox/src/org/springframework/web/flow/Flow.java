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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.DefaultObjectStyler;
import org.springframework.util.ToStringCreator;

/**
 * Singleton definition of a web flow.
 * <p>
 * At a high level, a Flow captures the definition (configuration information)
 * of a logical page flow within a web application. A logical page flow
 * typically fulfills a business process that takes place over a series of steps
 * (modeled as states.)
 * <p>
 * Structurally, a Flow is composed of a set of states. A state is a point in
 * the flow where something happens; for instance, showing a view, executing an
 * action, or spawning a sub flow.
 * <p>
 * Each state has one or more transitions that are used to move to another
 * state. A transition is triggered by an event. An event is a string identifier
 * signalling the occurence of something: e.g "submit", "back", "success",
 * "error".
 * <p>
 * Each Flow has exactly one start state. A start state is simply a marker for
 * the state the Flow execution should start in.
 * <p>
 * Instances of this class are typically built by a FlowBuilder implementation,
 * but may also be subclassed. It, and the rest of the web.flow system, has been
 * purposefully designed with minimal dependencies on other parts of Spring, and
 * is easily usable in a standalone fashion (and in the context of other
 * request-driven frameworks like Struts or Webwork, for example).
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @author Erwin Vervaet
 * 
 * @see org.springframework.web.flow.ActionState
 * @see org.springframework.web.flow.ViewState
 * @see org.springframework.web.flow.SubFlowState
 * @see org.springframework.web.flow.EndState
 * @see org.springframework.web.flow.Transition
 * @see org.springframework.web.flow.FlowExecution
 * @see org.springframework.web.flow.config.FlowBuilder
 * @see org.springframework.web.flow.config.AbstractFlowBuilder
 * @see org.springframework.web.flow.config.XmlFlowBuilder
 */
public class Flow implements Serializable {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow identifier uniquely identifying this flow among all other flows.
	 */
	private String id;

	/**
	 * The default start state for this flow.
	 */
	private TransitionableState startState;

	/**
	 * The set of state definitions for this flow.
	 */
	private Set states = new LinkedHashSet(6);

	/**
	 * The default listener list for all flow executions created for this flow.
	 */
	private FlowExecutionListenerList flowExecutionListenerList = new FlowExecutionListenerList();

	/**
	 * Construct a new flow definition with the given id. The id should be
	 * unique among all flows.
	 * @param id The flow identifier.
	 */
	public Flow(String id) {
		setId(id);
	}

	/**
	 * @return The unique id of this flow.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id The unique id of this flow.
	 */
	protected void setId(String id) {
		Assert.notNull(id, "The flow id is required");
		this.id = id;
	}

	/**
	 * Returns the <i>default </i> set of flow execution listeners for all
	 * executions created for this flow. The set returned is a mutable list
	 * object.
	 * <p>
	 * This is really a convenience feature since flow execution listeners are
	 * managed and notified by a <code>FlowExecution</code> and not by the
	 * flow itself! You can use this when you want certain listeners to always
	 * be notified when a flow execution is created for this flow, irrespective
	 * of the web controller that is driving the execution.
	 * 
	 * @return The set of flow execution listeners
	 */
	public FlowExecutionListenerList getFlowExecutionListenerList() {
		return this.flowExecutionListenerList;
	}

	/**
	 * Add the state definition to this flow definition. Marked protected, as
	 * this method is to be called by the (privileged) state definition classes
	 * themselves during state construction as part of a FlowBuilder invocation.
	 * 
	 * @param state The state, if already added noting happens, if another
	 *        instance is added with the same id, an exception is thrown
	 * @throws IllegalArgumentException when the state cannot be added to the
	 *         flow; specifically, if another state shares the same ID as the
	 *         one provided
	 */
	protected void add(AbstractState state) throws IllegalArgumentException {
		if (this != state.getFlow()) {
			throw new IllegalArgumentException("State " + state + " cannot be added to this flow '" + getId()
					+ "' - it already belongs to a different flow");
		}
		if (containsInstance(state)) {
			return;
		}
		if (containsState(state.getId())) {
			throw new IllegalArgumentException(
					"This flow '"
							+ getId()
							+ "' already contains a state with id '"
							+ state.getId()
							+ "' - state ids must be locally unique to the flow definition; existing stateIds of this flow include: "
							+ DefaultObjectStyler.call(getStateIds()));
		}
		boolean firstAdd = states.isEmpty();
		this.states.add(state);
		if (firstAdd) {
			if (state.isTransitionable()) {
				setStartState((TransitionableState)state);
			}
		}
	}

	/**
	 * Returns the number of states managed by this flow.
	 * @return The state count.
	 */
	public int getStateCount() {
		return this.states.size();
	}

	/**
	 * Returns an ordered iterator over the state definitions of this flow. The
	 * order is determined by the order in which the states were added.
	 * @return The states iterator
	 */
	public Iterator statesIterator() {
		return this.states.iterator();
	}

	/**
	 * Set the start state for this flow to the state with the provided
	 * <code>stateId</code>; a state must exist by the provided
	 * <code>stateId</code> and it must be transitionable.
	 * @param stateId The new start state
	 * @throws NoSuchFlowStateException No state exists with the id you provided
	 */
	public void setStartState(String stateId) throws NoSuchFlowStateException {
		setStartState(getRequiredTransitionableState(stateId));
	}

	/**
	 * Set the start state for this flow to the state provided; any
	 * transitionable state may be the start state.
	 * @param state The new start state
	 * @throws NoSuchFlowStateException The state has not been added to this
	 *         flow
	 */
	public void setStartState(TransitionableState state) throws NoSuchFlowStateException {
		assertValidState(state);
		if (logger.isDebugEnabled()) {
			logger.debug("Setting start state for flow '" + getId() + "' to '" + state.getId() + "'");
		}
		this.startState = state;
	}

	/**
	 * Return the start state, throwing an exception if it has not yet been
	 * marked.
	 * @return The start state
	 * @throws IllegalStateException No start state has been marked.
	 */
	public TransitionableState getStartState() throws IllegalStateException {
		if (startState == null) {
			throw new IllegalStateException(
					"No start state has been set for this Flow; flow builder configuration error?");
		}
		return startState;
	}

	/**
	 * Check that given state is present in the flow and throw an exception if
	 * it's not.
	 */
	private void assertValidState(AbstractState state) throws NoSuchFlowStateException {
		getRequiredState(state.getId());
	}

	/**
	 * Return the state with the provided id, throwing a exception if no state
	 * exists with that id.
	 * @param stateId the state id
	 * @return The state with that id
	 * @throws NoSuchFlowStateException No state exists with that id.
	 */
	public AbstractState getRequiredState(String stateId) throws NoSuchFlowStateException {
		AbstractState state = getState(stateId);
		if (state == null) {
			throw new NoSuchFlowStateException(this, stateId);
		}
		return state;
	}

	/**
	 * Return the state with the provided id, returning <code>null</code> if
	 * no state exists with that id.
	 * @param stateId The state id
	 * @return The state with that id, or null if none exists.
	 */
	public AbstractState getState(String stateId) {
		Iterator it = statesIterator();
		while (it.hasNext()) {
			AbstractState state = (AbstractState)it.next();
			if (state.getId().equals(stateId)) {
				return state;
			}
		}
		return null;
	}

	/**
	 * Is this state instance present in this flow? Does a "same" (==) check.
	 * @param state the state
	 * @return true if yes (the same instance is present), false otherwise
	 */
	protected boolean containsInstance(AbstractState state) {
		Iterator it = statesIterator();
		while (it.hasNext()) {
			AbstractState s = (AbstractState)it.next();
			if (s == state) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Is a state with the provided id present in this flow?
	 * @param stateId the state id
	 * @return true if yes, false otherwise
	 */
	public boolean containsState(String stateId) {
		return getState(stateId) != null;
	}

	/**
	 * Return the <code>TransitionableState</code> with this <id>stateId
	 * </id>, throwing an exception if not found.
	 * @param stateId Id of the state to look up
	 * @return The transitionableState
	 * @throws NoSuchFlowStateException No transitionable state exists by this
	 *         id
	 */
	public TransitionableState getRequiredTransitionableState(String stateId) throws NoSuchFlowStateException {
		AbstractState state = getRequiredState(stateId);
		Assert.state(state.isTransitionable(), "This state '" + stateId + "' of flow '" + getId()
				+ "' must be transitionable");
		return (TransitionableState)state;
	}

	/**
	 * Convenience accessor that returns an ordered array of the String
	 * <code>ids</code> for the state definitions associated with this flow
	 * definition.
	 * @return The state ids
	 */
	public String[] getStateIds() {
		Iterator it = statesIterator();
		List stateIds = new ArrayList();
		while (it.hasNext()) {
			stateIds.add(((AbstractState)it.next()).getId());
		}
		return (String[])stateIds.toArray(new String[0]);
	}

	/**
	 * Returns true if this flow equals the other one; two flows are treated as
	 * equal if they share the same ID.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Flow)) {
			return false;
		}
		Flow flow = (Flow)o;
		return id.equals(flow.id);
	}

	/**
	 * Factory method that produces a new <code>FlowExecution</code> instance
	 * for this flow on every invocation. Typically called by controller clients
	 * that need to manage a new flow execution; might also be called by flow
	 * execution test code.
	 * @return A new flow execution, used by caller to manage a single client
	 *         instance of an executing flow (typically managed in the http
	 *         session.)
	 */
	public FlowExecution createExecution() {
		return new FlowExecutionStack(this);
	}

	public int hashCode() {
		return id.hashCode();
	}

	public String toString() {
		return new ToStringCreator(this).append("id", id).append("startState", startState)
				.append("states", this.states).toString();
	}
}