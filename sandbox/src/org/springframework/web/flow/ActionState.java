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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.flow.config.FlowConstants;
import org.springframework.web.servlet.ModelAndView;

/**
 * A transitionable state that executes one or more actions when entered.
 * @author Keith Donald
 */
public class ActionState extends TransitionableState {

	private Set namedActions = new LinkedHashSet(1);

	public ActionState(Flow flow, String id, Action action, Transition transition) {
		super(flow, id, transition);
		addAction(action);
	}

	public ActionState(Flow flow, String id, String actionName, Action action, Transition transition) {
		super(flow, id, transition);
		addAction(actionName, action);
	}

	public ActionState(Flow flow, String id, Action action, Transition[] transitions) {
		super(flow, id, transitions);
		addAction(action);
	}

	public ActionState(Flow flow, String id, String actionName, Action action, Transition[] transitions) {
		super(flow, id, transitions);
		addAction(actionName, action);
	}

	public ActionState(Flow flow, String id, Action[] actions, Transition[] transitions) {
		super(flow, id, transitions);
		addActions(actions);
	}

	public ActionState(Flow flow, String id, String[] actionNames, Action[] actions, Transition[] transitions) {
		super(flow, id, transitions);
		addActions(actionNames, actions);
	}

	public boolean isActionState() {
		return true;
	}

	protected void addAction(Action action) {
		this.namedActions.add(createNamedAction(null, action));
	}

	protected void addAction(String actionName, Action action) {
		this.namedActions.add(createNamedAction(actionName, action));
	}

	protected void addActions(Action[] actions) {
		Assert.notEmpty(actions, "You must add at least one action");
		for (int i = 0; i < actions.length; i++) {
			addAction(actions[i]);
		}
	}

	protected void addActions(String[] names, Action[] actions) {
		Assert.notEmpty(names, "You must add at least one action");
		Assert.notEmpty(actions, "You must add at least one action");
		Assert.isTrue(names.length == actions.length, "The name->action arrays must be equal in length");
		for (int i = 0; i < actions.length; i++) {
			addAction(names[i], actions[i]);
		}
	}

	protected NamedAction createNamedAction(String actionName, Action action) {
		return new NamedAction(this, actionName, action);
	}

	/**
	 * @return An iterator that returns the set of actions to execute for this
	 *         state.
	 */
	protected Iterator namedActionIterator() {
		return this.namedActions.iterator();
	}

	/**
	 * @return The number of actions executed by this action state when it is entered.
	 */
	public int getActionCount() {
		return namedActions.size();
	}
	
	/**
	 * @return The list of actions executed by this action state.
	 */
	public Action[] getActions() {
		Action[] actions=new Action[namedActions.size()];
		int i=0;
		for (Iterator it=namedActionIterator(); it.hasNext(); ) {
			actions[i++]=((NamedAction)it.next()).getAction();
		}
		return actions;
	}
	
	/**
	 * @param action the action for which the named should be looked up
	 * @return the name of given action or null if the action does not have a name
	 * @throws NoSuchElementException when given action is not an action executed
	 *         by this state
	 */
	public String getActionName(Action action) {
		Assert.notNull(action, "The action should not be [null]");
		for (Iterator it=namedActionIterator(); it.hasNext(); ) {
			NamedAction namedAction=(NamedAction)it.next();
			if (action==namedAction.getAction()) {
				return namedAction.getName();
			}
		}
		throw new NoSuchElementException("action '" + action + "' is not an action executed by state '" + this + "'");
	}

	/**
	 * Hook method implementation that initiates state processing.
	 * 
	 * This implementation iterators over each configured Action for this state
	 * and executes it.
	 */
	protected ModelAndView doEnterState(FlowExecutionStack flowExecution, HttpServletRequest request,
			HttpServletResponse response) {
		Iterator it = namedActionIterator();
		int executionCount = 0;
		while (it.hasNext()) {
			NamedAction namedAction = (NamedAction)it.next();
			ActionResult result = namedAction.execute(request, response, flowExecution);
			executionCount++;
			String eventId = namedAction.getEventId(result);
			Transition transition = getTransition(eventId);
			if (transition != null) {
				flowExecution.setLastEventId(eventId);
				return transition.execute(flowExecution, request, response);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Action execution #" + executionCount + " resulted in no transition on event '"
							+ eventId + "' - " + "I will proceed to the next action in the chain");
				}
			}
		}
		if (executionCount > 0) {
			throw new CannotExecuteStateTransitionException(this, new IllegalStateException(
					"No supported event was signaled by any of the " + executionCount
							+ " actions that executed in this action state '" + getId() + "' of flow '"
							+ getFlow().getId() + "' -- programmer error?"));
		}
		else {
			throw new CannotExecuteStateTransitionException(this, new IllegalStateException(
					"No actions were executed, thus I cannot execute any state transition "
							+ "-- programmer configuration error; "
							+ "make sure you add at least one action bean to this state"));
		}
	}

	protected static class NamedAction implements Serializable {
		protected static final Log logger = LogFactory.getLog(NamedAction.class);

		private ActionState actionState;

		private String name;

		private Action action;

		public NamedAction(ActionState actionState, String name, Action action) {
			Assert.notNull(actionState, "The owning action state is required");
			Assert.notNull(action, "The action is required");
			this.actionState = actionState;
			this.name = name;
			this.action = action;
		}

		protected String getCaption() {
			return (isNameSet() ? "[name='" + name + "', class='" + action.getClass().getName() + "']" : "[class='"
					+ action.getClass().getName() + "']");
		}

		protected String getName() {
			return name;
		}

		protected Action getAction() {
			return action;
		}

		public boolean isNameSet() {
			return StringUtils.hasText(name);
		}

		protected String getEventId(ActionResult result) {
			if (result == null) {
				return null;
			}
			if (isNameSet()) {
				return name + FlowConstants.DOT_SEPARATOR + result.getId();
			}
			else {
				return result.getId();
			}
		}

		protected ActionResult execute(HttpServletRequest request, HttpServletResponse response,
				FlowExecutionStack flowExecution) {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Executing action '" + getCaption() + "'");
				}
				return action.execute(request, response, flowExecution);
			}
			catch (Exception e) {
				throw new ActionExecutionException(actionState, this, e);
			}
		}

		public String toString() {
			return getCaption();
		}
	}
}