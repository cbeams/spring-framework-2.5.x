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
import org.springframework.util.EventListenerListHelper;
import org.springframework.util.ToStringCreator;
import org.springframework.util.closure.ProcessTemplate;

/**
 * Singleton definition of a web flow.
 * 
 * At a high level, a Flow captures the definition (configuration) of a logical
 * page flow within a web application. A logical page flow typically fulfills a
 * business process that takes place over a series of steps (modeled as states.)
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
 * <code>FlowSession</code> is created, which tracks a single client instance
 * of this flow. A HTTP-session-scoped <code>FlowSessionExecution</code>
 * provides a call stack that tracks the current state of this flow session's
 * execution, including any subflows that have been spawned.
 * 
 * To give you an example of what a web flow definition might look like, the
 * following piece of java code defines a web flow equivalent to the work flow
 * implemented by Spring MVC's simple form controller:
 * <p>
 * 
 * <pre>
 * public class EditPersonDetailsFlow extends Flow {
 *
 *   public static final String PERSON_DETAILS = "personDetails";
 * 
 *   public EditPersonDetailsFlow() {
 *      super(PERSON_DETAILS);
 *   }
 *
 *   protected void init() {
 *      add(createGetState(PERSON_DETAILS));
 *      add(createViewState(PERSON_DETAILS));
 *      add(createBindAndValidateState(PERSON_DETAILS));
 *      add(createDefaultEndState());
 *   }
 * </pre>
 * 
 * What this does is add 4 states to the "EditPersonDetailsFlow"--a "get action"
 * state (the start state), a "view" state, a "bind and validate" action state,
 * and a end marker state.
 * 
 * The first state, an action state, will be indentified as 'personDetails.get'.
 * This action state will automatically be configured with the following
 * defaults:
 * <ol>
 * <li>A action bean named 'personDetails.get' - this is the name of the
 * <code>ActionBean</code> instance that will execute when this state is
 * entered. In this example, the <code>ActionBean</code> will go out to the DB ,
 * load the Person, and put it in the Flow's data model.
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
 * flow. When entered, the flow terminates, and if this flow is acting as a root
 * flow in the current flow session execution, any flow-allocated resources will
 * be cleaned up. A end state can optionally be configured with a logical view
 * name to forward to when entered. It will also trigger a state transition in a
 * resuming parent flow, if this flow was participating as a spawned 'subflow'
 * within a suspended parent flow.
 * 
 * This class is directly instantitable as it is fully configurable for use -
 * either externally or via a specific subclass. It has been designed with
 * minimal dependencies on other parts of Spring, easily usable in a standalone
 * fashion.
 * 
 * @author Keith Donald
 * @author Colin Sampaleanu
 * @see FlowExecutionFactory
 */
public class Flow implements Serializable {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The flow identifier uniquely identifying this flow among all other flows
	 */
	private String id;

	/**
	 * The default start state for this flow.
	 */
	private TransitionableState startState;

	/**
	 * The set of state definitions for this flow
	 */
	private Set states = new LinkedHashSet(6);

	/**
	 * The list of listeners that should receive event callbacks during managed
	 * flow executions (client sessions.)
	 */
	private transient EventListenerListHelper flowExecutionListeners = new EventListenerListHelper(
			FlowExecutionListener.class);

	/**
	 * Construct a new flow definition with the given id. The id should be
	 * unique among all flows.
	 * @param id The flow identifier.
	 */
	public Flow(String id) {
		setId(id);
	}

	protected void setId(String id) {
		Assert.notNull("The flow id is required");
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
	 * Add a flow execution listener; the added listener will receive callbacks
	 * on events occuring in all client flow executions created for this flow
	 * definition.
	 * 
	 * @param listener The execution listener to add
	 */
	public void addFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners.add(listener);
	}

	/**
	 * Remove an existing flow execution listener; the removed listener will no
	 * longer receive callbacks and if left unreferenced will be eligible for
	 * garbage collection.
	 * @param listener The execution listener to remove.
	 */
	public void removeFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners.remove(listener);
	}

	/**
	 * Returns the number of execution listeners associated with this flow
	 * definition.
	 * @return The flow execution listener count
	 */
	public int getFlowExecutionListenerCount() {
		return flowExecutionListeners.getListenerCount();
	}

	/**
	 * Is at least one instance of the provided FlowExecutionListener
	 * implementation present in the listener list?
	 * @param listenerImplementationClass The flow execution listener
	 *        implementation, must be a impl of FlowExecutionListener
	 * @return true if present, false otherwise
	 */
	public boolean isFlowExecutionListenerAdded(Class listenerImplementationClass) {
		Assert.isTrue(FlowExecutionListener.class.isAssignableFrom(listenerImplementationClass),
				"Listener class must be a FlowSessionExecutionListener");
		return this.flowExecutionListeners.isAdded(listenerImplementationClass);
	}

	/**
	 * Is the provid FlowExecutionListener instance present in the listener
	 * list?
	 * @param listener The execution listener
	 * @return true if present, false otherwise.
	 */
	public boolean isFlowExecutionListenerAdded(FlowExecutionListener listener) {
		return this.flowExecutionListeners.isAdded(listener);
	}

	/**
	 * Return a process template that knows how to iterate over the list of flow
	 * execution listeners and dispatch each listener to a handler callback for
	 * processing.
	 * @return The iterator process template.
	 */
	public ProcessTemplate getFlowExecutionListenerIteratorTemplate() {
		return flowExecutionListeners;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * Add the state definition to this flow definition. Marked protected, as
	 * this method is to be called by the (privileged) state definition classes
	 * themselves during state construction as part of a FlowBuilder invocation.
	 * 
	 * @param state The state, if already added noting happens, if another
	 *        instance is added with the same id, an exception is thrown
	 * @throws IllegalStateException another state exists with the same ID as
	 *         the one provided
	 */
	protected void add(AbstractState state) {
		if (containsInstance(state)) {
			return;
		}
		if (containsState(state.getId())) {
			throw new IllegalStateException(
					"This flow '"
							+ getId()
							+ "' already contains a state with id '"
							+ id
							+ "' - state ids must be locally unique to the flow definition; existing stateIds of this flow include: "
							+ DefaultObjectStyler.call(getStateIds()));
		}
		boolean firstAdd;
		if (states.isEmpty()) {
			firstAdd = true;
		}
		else {
			firstAdd = false;
		}
		state.setFlow(this);
		this.states.add(state);
		if (firstAdd) {
			AbstractState firstState = (AbstractState)statesIterator().next();
			if (firstState.isTransitionable()) {
				setStartState((TransitionableState)firstState);
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
	protected void setStartState(String stateId) throws NoSuchFlowStateException {
		setStartState(getRequiredTransitionableState(stateId));
	}

	/**
	 * Set the start state for this flow to the state provided
	 * @param state The new start state
	 * @throws NoSuchFlowStateException The state has not been added to this
	 *         flow
	 */
	protected void setStartState(TransitionableState state) throws NoSuchFlowStateException {
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