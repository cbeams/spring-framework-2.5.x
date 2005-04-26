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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Styler;
import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;

/**
 * A transitionable state that executes one or more actions when entered. If
 * more than one action is specified, they are executed in an ordered chain
 * until one returns a result event that matches a valid state transition out of
 * this state. This is a form of the Chain of Responsibility (CoR) pattern.
 * <p>
 * As mentioned, the result of an action's execution is typically treated as a
 * contributing criterion for a state transition. In addition, anything else in
 * the Flow's <code>RequestContext</code> may be tested as part of custom
 * transitional criteria.
 * <p>
 * Each action executed by this action state may be qualified with a set of
 * arbitrary properties. For example, an identifying name and description.
 * <p>
 * By default, the 'name' property is used as a qualifier for a given action
 * result event. For example, if an action named <code>myAction</code> returns
 * a <code>success</code> result, a transition for event
 * <code>myAction.success</code> will be searched, and if found, executed. If
 * the action is not named, a transition for the base <code>success</code>
 * event will be searched, and if found, executed.
 * <p>
 * Also, the value of the 'method' property is used by the <code>MultiAction</code>
 * implementation to dispatch calls on a target action instance to a particular
 * handler method.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ActionState extends TransitionableState {

	/**
	 * The set of actions to be executed when this action state is entered.
	 */
	private Set actionExecutors = new LinkedHashSet(1);

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param targetAction the raw target action instance to execute in this
	 *        state when entered
	 * @param transition the sole transition (path) out of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, Action targetAction, Transition transition)
			throws IllegalArgumentException {
		super(flow, id, transition);
		addAction(targetAction);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param action the action and any configuration properties for use within
	 *        this state
	 * @param transition the sole transition (path) out of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, AnnotatedAction action, Transition transition)
			throws IllegalArgumentException {
		super(flow, id, transition);
		addAction(action);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param targetAction the raw target action instance to execute in this
	 *        state when entered
	 * @param transitions the transitions out of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, Action targetAction, Transition[] transitions)
			throws IllegalArgumentException {
		super(flow, id, transitions);
		addAction(targetAction);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param targetAction the raw target action instance to execute in this
	 *        state when entered
	 * @param transitions the transitions out of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, Action targetAction, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		super(flow, id, transitions, properties);
		addAction(targetAction);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param action the action and any configuration properties for use within
	 *        this state
	 * @param transitions the transitions out of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, AnnotatedAction action, Transition[] transitions)
			throws IllegalArgumentException {
		super(flow, id, transitions);
		addAction(action);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param action the action and any configuration properties for use within
	 *        this state
	 * @param transitions the transitions out of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, AnnotatedAction action, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		super(flow, id, transitions, properties);
		addAction(action);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param targetActions the raw, target actions to execute in this state
	 * @param transition the sole transition (path) out of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, Action[] targetActions, Transition transition)
			throws IllegalArgumentException {
		super(flow, id, transition);
		addActions(targetActions);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param targetActions the raw actions to execute in this state
	 * @param transitions the transitions (paths) out of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, Action[] targetActions, Transition[] transitions)
			throws IllegalArgumentException {
		super(flow, id, transitions);
		addActions(targetActions);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param targetActions the raw actions to execute in this state
	 * @param transitions the transitions (paths) out of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, Action[] targetActions, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		super(flow, id, transitions, properties);
		addActions(targetActions);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param actions the actions with any configuration properties for use
	 *        within this state
	 * @param transition the transitions (paths) out of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, AnnotatedAction[] actions, Transition transition)
			throws IllegalArgumentException {
		super(flow, id, transition);
		addActions(actions);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param actions the actions with any configuration properties for use
	 *        within this state
	 * @param transitions the transitions (paths) out of this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, AnnotatedAction[] actions, Transition[] transitions)
			throws IllegalArgumentException {
		super(flow, id, transitions);
		addActions(actions);
	}

	/**
	 * Create a new action state.
	 * @param flow the owning flow
	 * @param id the state identifier (must be unique to the flow)
	 * @param actions the actions with any configuration properties for use
	 *        within this state
	 * @param transitions the transitions (paths) out of this state
	 * @param properties additional properties describing this state
	 * @throws IllegalArgumentException when this state cannot be added to given
	 *         flow
	 */
	public ActionState(Flow flow, String id, AnnotatedAction[] actions, Transition[] transitions, Map properties)
			throws IllegalArgumentException {
		super(flow, id, transitions, properties);
		addActions(actions);
	}

	/**
	 * Add a target action instance to this state.
	 * @param action the action to add
	 */
	protected void addAction(Action action) {
		this.actionExecutors.add(new ActionExecutor(this, new AnnotatedAction(action)));
	}

	/**
	 * Add an action instance to this state.
	 * @param action the state action to add
	 */
	protected void addAction(AnnotatedAction action) {
		this.actionExecutors.add(new ActionExecutor(this, action));
	}

	/**
	 * Add a collection of target action instances to this state.
	 * @param actions the actions to add
	 */
	protected void addActions(Action[] actions) {
		Assert.notEmpty(actions, "You must add at least one action");
		for (int i = 0; i < actions.length; i++) {
			addAction(actions[i]);
		}
	}

	/**
	 * Add a collection of actions to this state.
	 * @param actions the actions to add
	 */
	protected void addActions(AnnotatedAction[] actions) {
		Assert.notEmpty(actions, "You must add at least one action");
		for (int i = 0; i < actions.length; i++) {
			addAction(actions[i]);
		}
	}

	/**
	 * Returns an iterator that lists the set of actions to execute for this
	 * state. Returns a iterator over a collection of
	 * {@link ActionState.ActionExecutor} objects.
	 * @return the ActionExecutor iterator
	 */
	private Iterator actionExecutors() {
		return this.actionExecutors.iterator();
	}

	/**
	 * Returns the number of actions executed by this action state when it is
	 * entered.
	 * @return the action count
	 */
	public int getActionCount() {
		return actionExecutors.size();
	}

	/**
	 * Returns the first action executed by this action state.
	 * @return the first action
	 */
	public AnnotatedAction getAction() {
		return getActions()[0];
	}

	/**
	 * Returns the list of actions executed by this action state.
	 * @return the action list, as a typed array
	 */
	public AnnotatedAction[] getActions() {
		AnnotatedAction[] actions = new AnnotatedAction[actionExecutors.size()];
		int i = 0;
		for (Iterator it = actionExecutors(); it.hasNext();) {
			actions[i++] = ((ActionExecutor)it.next()).getAction();
		}
		return actions;
	}

	/**
	 * Specialization of State's <code>doEnterState</code> template method
	 * that executes behaviour specific to this state type in polymorphic
	 * fashion.
	 * <p>
	 * This implementation iterates over each configured <code>Action</code>
	 * instance and executes it. Execution continues until a <code>Action</code>
	 * returns a result event that matches a state transition in this request
	 * context, or the set of all actions is exhausted.
	 * @param context the state execution context
	 * @return ViewDescriptor a view descriptor signaling that control should be
	 *         returned to the client and a view rendered
	 * @throws CannotExecuteStateTransitionException when no action execution
	 *         resulted in a outcome event that could be mapped to a valid state
	 *         transition
	 */
	protected ViewDescriptor doEnterState(StateContext context) {
		Iterator it = actionExecutors();
		int executionCount = 0;
		String[] eventIds = new String[actionExecutors.size()];
		while (it.hasNext()) {
			ActionExecutor actionExecutor = (ActionExecutor)it.next();
			Event event = actionExecutor.execute(context);
			executionCount++;
			if (event != null) {
				eventIds[executionCount - 1] = event.getId();
				try {
					return onEvent(event, context);
				}
				catch (NoMatchingTransitionException e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Action execution #" + executionCount + " resulted in no transition on event '"
								+ eventIds[executionCount] + "' -- "
								+ "I will proceed to the next action in the chain");
					}
				}
			}
			else {
				eventIds[executionCount] = null;
			}
		}
		if (executionCount > 0) {
			throw new NoMatchingTransitionException(this, context, "No transition was matched to the event(s) "
					+ "signaled by the " + executionCount + " action(s) that executed in this action state '" + getId()
					+ "' of flow '" + getFlow().getId()
					+ "'; transitions must be defined to handle action result outcomes -- "
					+ "possible flow configuration error? Note: the eventIds signaled were: '" + Styler.call(eventIds)
					+ "', while the supported set of transitional criteria for this action state is '"
					+ Styler.call(getTransitionalCriteria()) + "'");
		}
		else {
			throw new IllegalStateException("No actions were executed, thus I cannot execute any state transition "
					+ "-- programmer configuration error; " + "make sure you add at least one action to this state");
		}
	}

	/**
	 * Internal action executor, encapsulating a single action's execution and
	 * result handling logic.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 */
	private static class ActionExecutor {

		protected final Log logger = LogFactory.getLog(ActionExecutor.class);

		private ActionState actionState;

		private AnnotatedAction action;

		/**
		 * Create a new action executor.
		 * @param action the action to wrap
		 */
		public ActionExecutor(ActionState actionState, AnnotatedAction action) {
			Assert.notNull(action, "The action state's action is required");
			this.actionState = actionState;
			this.action = action;
		}

		/**
		 * Returns the wrapped action.
		 */
		public AnnotatedAction getAction() {
			return action;
		}

		/**
		 * Execute the wrapped action.
		 * @param context the flow execution request context
		 * @return result of execution
		 */
		protected Event execute(StateContext context) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Executing action '" + this + "'");
				}
				context.setActionProperties(action);
				return decorateResult(action.getTargetAction().execute(context));
			}
			catch (Exception e) {
				throw new ActionExecutionException(actionState, action, e);
			}
			finally {
				context.setActionProperties(null);
			}
		}

		/**
		 * Get the event id to be used as grounds for a transition in the
		 * containing state, based on given result returned from action
		 * execution.
		 * <p>
		 * If the wrapped action is named, the name will be used as a qualifier
		 * for the event (e.g. "myAction.success").
		 * @param resultEvent the action result event
		 */
		protected Event decorateResult(Event resultEvent) {
			if (resultEvent == null) {
				return null;
			}
			if (action.isNamed()) {
				return new ActionNameQualifiedEvent(action.getName(), resultEvent);
			}
			else {
				return resultEvent;
			}
		}

		/**
		 * Wrapper to wrap an event as an event signaled by a named action.
		 */
		private static class ActionNameQualifiedEvent extends Event {

			private String actionName;

			private Event resultEvent;

			/**
			 * Create a new action name qualified event.
			 * @param actionName the name of the named action
			 * @param resultEvent the event to qualify
			 */
			public ActionNameQualifiedEvent(String actionName, Event resultEvent) {
				super(resultEvent.getSource());
				this.actionName = actionName;
				this.resultEvent = resultEvent;
			}

			public String getId() {
				return actionName + FlowConstants.SEPARATOR + resultEvent.getId();
			}

			public long getTimestamp() {
				return resultEvent.getTimestamp();
			}

			public String getStateId() {
				return resultEvent.getStateId();
			}

			public Object getParameter(String parameterName) {
				return resultEvent.getParameter(parameterName);
			}

			public Map getParameters() {
				return resultEvent.getParameters();
			}
		}

		public String toString() {
			return action.toString();
		}
	}

	protected void createToString(ToStringCreator creator) {
		creator.append("actions", actionExecutors);
		super.createToString(creator);
	}
}