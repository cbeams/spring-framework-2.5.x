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
 * 
 * At a high level, a Flow captures the definition (configuration information)
 * of a logical page flow within a web application. A logical page flow
 * typically fulfills a business process that takes place over a series of steps
 * (modeled as states.)
 * 
 * Structurally, a Flow is composed of a set of states. A state is a point in
 * the flow where something happens; for instance, showing a view, executing an
 * action, or spawning a subflow.
 * 
 * Each state has one or more transitions that are used to move to another
 * state. A transition is triggered by an event.
 * 
 * Each Flow has exactly one start state. A start state is simply a marker for
 * the state the Flow should transition to when a start event is signaled.
 * 
 * When a start event is signaled by a requesting client, a new
 * <code>FlowExecution</code> is created, which tracks a single client
 * instance of this flow.
 * 
 * To give you an example of what a web flow definition might look like, the
 * following piece of java code defines a web flow equivalent to the work flow
 * implemented by Spring MVC's simple form controller:
 * <p>
 * 
 * <pre>
 * public class EditPersonDetailsFlowBuilder extends AbstractFlowBuilder {
 *
 *   public static final String PERSON_DETAILS = "personDetails";
 * 
 *   protected String flowId() {
 *       return PERSON_DETAILS;
 *   }
 *   
 *   public void buildStates() {
 *       addGetState(PERSON_DETAILS));
 *       addViewState(PERSON_DETAILS));
 *       addBindAndValidateState(PERSON_DETAILS));
 *       addDefaultEndState());
 *   }
 * </pre>
 * 
 * What this java-based FlowBuilder implementation does is add 4 states to the
 * "personDetails" flow -- a "get action" state (the start state), a "view"
 * state, a "bind and validate" action state, and a end marker state.
 * 
 * The first state, an action state, will be assigned the indentifier as
 * 'personDetails.get'. This action state will automatically be configured with
 * the following defaults:
 * <ol>
 * <li>A action bean named 'personDetails.get' - this is the name of the
 * <code>ActionBean</code> instance that will execute when this state is
 * entered. In this example, the <code>ActionBean</code> will go out to the
 * DB, load the Person, and put it in the Flow's data model.
 * <li>An "success" transition to a default view state, called
 * 'personDetails.view'. This means when <code>ActionBean</code> returns a
 * "success" result event (aka outcome), the 'viewPersonDetails' state will be
 * transitioned to.
 * <li>It will act as the start state for this flow.
 * </ol>
 * 
 * The second state, a view state, will be identified as 'personDetails.view'.
 * This view state will automatically be configured with the following defaults:
 * <ol>
 * <li>A view name called 'personDetails.view' - this is the logical name of a
 * view resource. This logical view name gets mapped to a physical view resource
 * (jsp, etc.) by the calling front controller.
 * <li>A "submit" transition to a bind and validate action state, indentified
 * by the default ID 'personDetails.bindAndValidate'. This means when a 'submit'
 * event is signaled by the view (for example, on a submit button click), the
 * bindAndValidate action state will be entered and the '
 * <code>personDetails.bindAndValidate</code>'<code>ActionBean</code> will
 * be executed.
 * </ol>
 * 
 * The third state, an action state, will be indentified as
 * 'personDetails.bindAndValidate'. This action state will automatically be
 * configured with the following defaults:
 * <ol>
 * <li>A action bean named 'personDetails.bindAndValidate' - this is the name
 * of the <code>ActionBean</code> instance that will execute when this state
 * is entered. In this example, the <code>ActionBean</code> will bind form
 * input to a backing Person form object, validate it, and update the DB.
 * <li>A "success" transition to a default end state, called 'finish'. This
 * means if the <code>ActionBean</code> returns a "success" event, the
 * 'finish' end state will be transitioned to and the flow will terminate.
 * </ol>
 * 
 * The fourth and last state, a end state, will be indentified with the default
 * end state ID 'finish'. This end state is a marker that signals the end of the
 * flow. When entered, the flow session terminates, and if this flow is acting
 * as a root flow in the current flow execution, any flow-allocated resources
 * will be cleaned up. An end state can optionally be configured with a logical
 * view name to forward to when entered. It will also trigger a state transition
 * in a resuming parent flow, if this flow was participating as a spawned
 * 'subflow' within a suspended parent flow.
 * 
 * Instances of class are typically built by a FlowBuilder implementation, but
 * may also be subclassed. It, and the rest of the web.flow syste, has been
 * designed with minimal dependencies on other parts of Spring, easily usable in
 * a standalone fashion.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @author Erwin Vervaet
 * 
 * @see ActionState
 * @see ViewState
 * @see SubFlowState
 * @see EndState
 * @see Transition
 * @see FlowExecution
 * @see FlowBuilder
 * @see AbstractFlowBuilder
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
	 * Construct a new flow definition with the given id. The id should be
	 * unique among all flows.
	 * @param id The flow identifier.
	 */
	public Flow(String id) {
		setId(id);
	}

	public String getId() {
		return id;
	}

	protected void setId(String id) {
		Assert.notNull(id, "The flow id is required");
		this.id = id;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Flow)) {
			return false;
		}
		Flow flow = (Flow)o;
		return id.equals(flow.id);
	}

	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * Add the state definition to this flow definition. Marked protected, as
	 * this method is to be called by the (privileged) state definition classes
	 * themselves during state construction as part of a FlowBuilder invocation.
	 * 
	 * @param state The state, if already added noting happens, if another
	 *        instance is added with the same id, an exception is thrown
	 * @throws IllegalArgumentException when the state cannot be added to the
	 *         flow
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
	 * Returns an ordered iterator over the state definitions of this flow. The
	 * order is determined by the order in which the states were added.
	 * @return The states iterator
	 */
	public Iterator statesIterator() {
		return this.states.iterator();
	}

	/**
	 * Set the start state for this flow to the state with the provided
	 * <code>stateId</code>
	 * @param stateId The new start state
	 * @throws NoSuchFlowStateException No state exists with the id you provided
	 */
	public void setStartState(String stateId) throws NoSuchFlowStateException {
		setStartState(getRequiredTransitionableState(stateId));
	}

	/**
	 * Set the start state for this flow to the state provided
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

	private void assertValidState(AbstractState state) throws NoSuchFlowStateException {
		getRequiredState(state.getId());
	}

	/**
	 * Return the state with the provided id, throwing a exception if no state
	 * exists with that id.
	 * @param stateId the state id
	 * @return The state with that ID
	 * @throws NoSuchFlowStateException No state exists with that ID.
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
	 * Is this state instance present in this flow?
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
	 * @param stateId
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

	public String toString() {
		return new ToStringCreator(this).append("id", id).append("startState", startState)
				.append("states", this.states).toString();
	}
}