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

	private String id;

	private TransitionableState startState;

	private Set states = new LinkedHashSet(6);

	private transient EventListenerListHelper flowExecutionListeners = new EventListenerListHelper(
			FlowExecutionListener.class);

	/**
	 * @param id
	 */
	public Flow(String id) {
		setId(id);
	}

	/**
	 * @param id
	 * @param startStateId
	 * @param states
	 */
	public Flow(String id, AbstractState[] states, String startStateId) {
		setId(id);
		addAll(states);
		setStartState(startStateId);
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
	 * @param listener
	 */
	public void addFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners.add(listener);
	}

	/**
	 * @param listener
	 */
	public void removeFlowExecutionListener(FlowExecutionListener listener) {
		this.flowExecutionListeners.remove(listener);
	}

	/**
	 * @return
	 */
	public int getFlowExecutionListenerCount() {
		return flowExecutionListeners.getListenerCount();
	}

	/**
	 * @param listenerClass
	 * @return
	 */
	public boolean isFlowExecutionListenerAdded(Class listenerClass) {
		Assert.isTrue(FlowExecutionListener.class.isAssignableFrom(listenerClass),
				"Listener class must be a FlowSessionExecutionListener");
		return this.flowExecutionListeners.isAdded(listenerClass);
	}

	/**
	 * @param listener
	 * @return
	 */
	public boolean isFlowExecutionListenerAdded(FlowExecutionListener listener) {
		return this.flowExecutionListeners.isAdded(listener);
	}

	/**
	 * @return
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
			firstAdd = true;
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

	protected void addAll(AbstractState[] states) {
		for (int i = 0; i < states.length; i++) {
			add(states[i]);
		}
	}

	public Iterator statesIterator() {
		return this.statesIterator();
	}

	/**
	 * @param startStateId
	 * @throws NoSuchFlowStateException
	 */
	protected void setStartState(String startStateId) throws NoSuchFlowStateException {
		setStartState(getRequiredTransitionableState(startStateId));
	}

	/**
	 * @param state
	 * @throws NoSuchFlowStateException
	 */
	protected void setStartState(TransitionableState state) throws NoSuchFlowStateException {
		assertValidState(state);
		if (logger.isDebugEnabled()) {
			logger.debug("Setting start state for flow '" + getId() + "' as '" + state + "'");
		}
		this.startState = state;
	}

	/**
	 * @param state
	 * @throws NoSuchFlowStateException
	 */
	private void assertValidState(AbstractState state) throws NoSuchFlowStateException {
		getRequiredState(state.getId());
	}

	/**
	 * @param stateId
	 * @return
	 * @throws NoSuchFlowStateException
	 */
	public AbstractState getRequiredState(String stateId) throws NoSuchFlowStateException {
		AbstractState state = getState(stateId);
		if (state == null) {
			throw new NoSuchFlowStateException(this, stateId);
		}
		return state;
	}

	/**
	 * @param stateId
	 * @return
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
	 * @param stateId
	 * @return
	 */
	public boolean containsState(String stateId) {
		return getState(stateId) != null;
	}

	/**
	 * @param stateId
	 * @return
	 * @throws NoSuchFlowStateException
	 */
	public TransitionableState getRequiredTransitionableState(String stateId) throws NoSuchFlowStateException {
		AbstractState state = getRequiredState(stateId);
		Assert.state(state.isTransitionable(), "This state '" + stateId + "' of flow '" + getId()
				+ "' must be transitionable");
		return (TransitionableState)state;
	}

	/**
	 * @return
	 * @throws IllegalStateException
	 */
	public TransitionableState getStartState() throws IllegalStateException {
		return startState;
	}

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