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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * A state that executes one or more action beans when entered.
 * @author Keith Donald
 */
public class ActionState extends TransitionableState {

	private Set actionBeans;

	public ActionState(Flow flow, String id) {
		super(flow, id);
	}
	
	public ActionState(Flow flow, String id, Transition transition) {
		super(flow, id, transition);
		setActionBeanName(buildActionBeanNameFromStateId(id));
	}

	public ActionState(Flow flow, String id, Transition[] transitions) {
		super(flow, id, transitions);
		setActionBeanName(buildActionBeanNameFromStateId(id));
	}

	protected String buildActionBeanNameFromStateId(String stateId) {
		// do nothing, subclasses may override
		return stateId;
	}

	public ActionState(Flow flow, String id, ActionBean actionBean, Transition transition) {
		super(flow, id, transition);
		setActionBean(actionBean);
	}

	public ActionState(Flow flow, String id, ActionBean actionBean, Transition[] transitions) {
		super(flow, id, transitions);
		setActionBean(actionBean);
	}

	public ActionState(Flow flow, String id, ActionBean[] actionBeans, Transition[] transitions) {
		super(flow, id, transitions);
		setActionBeans(actionBeans);
	}

	public ActionState(Flow flow, String id, String actionBeanName, Transition transition) {
		super(flow, id, transition);
		setActionBeanName(actionBeanName);
	}

	public ActionState(Flow flow, String id, String actionBeanName, Transition[] transitions) {
		super(flow, id, transitions);
		setActionBeanName(actionBeanName);
	}

	public ActionState(Flow flow, String id, String[] actionBeanNames, Transition[] transitions) {
		super(flow, id, transitions);
		setActionBeanNames(actionBeanNames);
	}

	private static class ActionBeanHolder {
		private String actionBeanName;

		private ActionBean actionBean;

		private ActionBeanHolder(String actionBeanName) {
			Assert.hasText(actionBeanName, "The action bean name is required");
			this.actionBeanName = actionBeanName;
		}

		private ActionBeanHolder(ActionBean actionBean) {
			Assert.notNull(actionBean, "The action bean instance is required");
			this.actionBean = actionBean;
		}

		public boolean equals(Object o) {
			if (!(o instanceof ActionBeanHolder)) {
				return false;
			}
			ActionBeanHolder holder = (ActionBeanHolder)o;
			return ObjectUtils.nullSafeEquals(actionBeanName, holder.actionBeanName)
					&& ObjectUtils.nullSafeEquals(actionBean, holder.actionBean);
		}

		public int hashCode() {
			return ((actionBeanName != null ? actionBeanName.hashCode() : 0) + (actionBean != null ? actionBean
					.hashCode() : 0));
		}
	}

	public boolean isActionState() {
		return true;
	}

	public void setActionBean(ActionBean actionBean) {
		this.actionBeans = new LinkedHashSet(1);
		addActionBean(actionBean);
	}

	public void setActionBeanName(String actionBeanName) {
		this.actionBeans = new LinkedHashSet(1);
		addActionBeanName(actionBeanName);
	}

	public void setActionBeans(ActionBean[] actionBeans) {
		this.actionBeans = new LinkedHashSet(actionBeans.length);
		addActionBeans(actionBeans);
	}

	public void setActionBeanNames(String[] actionBeanNames) {
		this.actionBeans = new LinkedHashSet(actionBeanNames.length);
		addActionBeanNames(actionBeanNames);
	}

	public boolean addActionBeanName(String actionBeanName) {
		return this.actionBeans.add(new ActionBeanHolder(actionBeanName));
	}

	public boolean addActionBean(ActionBean actionBean) {
		return this.actionBeans.add(new ActionBeanHolder(actionBean));
	}

	public boolean addActionBeanNames(String[] actionBeanNames) {
		Collection holders = new LinkedHashSet(actionBeanNames.length);
		boolean changed = false;
		for (int i = 0; i < actionBeanNames.length; i++) {
			if (holders.add(new ActionBeanHolder(actionBeanNames[i]))) {
				changed = true;
			}
		}
		return changed;
	}

	public boolean addActionBeans(ActionBean[] actionBeans) {
		Collection holders = new LinkedHashSet(actionBeans.length);
		boolean changed = false;
		for (int i = 0; i < actionBeans.length; i++) {
			if (holders.add(new ActionBeanHolder(actionBeans[i]))) {
				changed = true;
			}
		}
		return changed;
	}

	public boolean removeActionBeanName(String actionBeanName) {
		return this.actionBeans.remove(new ActionBeanHolder(actionBeanName));
	}

	public boolean removeActionBean(ActionBean actionBean) {
		return this.actionBeans.remove(new ActionBeanHolder(actionBean));
	}

	/**
	 * Hook method implementation that initiates state processing.
	 * 
	 * This implementation iterators over each configured ActionBean for this
	 * state and executes it. If the <code>actionBeanName</code> is provided
	 * and not the ActionBean instance, the instance is retrieved from the
	 * <code>FlowServiceLocator</code>
	 */
	protected ViewDescriptor doEnterState(FlowExecutionStack sessionExecution, HttpServletRequest request,
			HttpServletResponse response) {
		Iterator it = actionBeanIterator();
		int beanExecutionCount = 0;
		while (it.hasNext()) {
			ActionBean actionBean = (ActionBean)it.next();
			if (logger.isDebugEnabled()) {
				logger.debug("Executing action bean '" + actionBean + "'");
			}
			ActionBeanEvent event = actionBean.execute(request, response, sessionExecution);
			beanExecutionCount++;
			if (event != null) {
				return execute(event.getId(), sessionExecution, request, response);
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Action bean execution #" + beanExecutionCount + " resulted in no event - "
							+ "I will attempt to proceed to the next action in the chain");
				}
			}
		}
		if (beanExecutionCount > 0) {
			throw new CannotExecuteStateTransitionException(this, new IllegalStateException(
					"No valid event was signaled by any of the " + beanExecutionCount
							+ " action bean(s) that executed in this action state '" + getId() + "' of flow '"
							+ getFlow().getId() + "' -- programmer error?"));
		}
		else {
			throw new CannotExecuteStateTransitionException(this, new IllegalStateException(
					"No action beans executed, thus I cannot execute any state transition "
							+ "-- programmer configuration error"));
		}
	}

	/**
	 * @return An iterator that returns the set of action beans to execute for
	 *         this state.
	 */
	protected Iterator actionBeanIterator() {
		final Iterator it = this.actionBeans.iterator();
		return new Iterator() {
			public Object next() {
				ActionBeanHolder holder = (ActionBeanHolder)it.next();
				if (holder.actionBean != null) {
					return holder.actionBean;
				}
				else {
					ActionBean actionBean = (ActionBean)getFlowServiceLocator().getActionBean(holder.actionBeanName);
					Assert.notNull(actionBean, "The action bean retrieved from the registry must not be null");
					return actionBean;
				}
			}

			public boolean hasNext() {
				return it.hasNext();
			}

			public void remove() {
				throw new UnsupportedOperationException("Remove not allowed");
			}
		};
	}

	/**
	 * @return
	 */
	protected String getActionBeanName() {
		return ((ActionBeanHolder)actionBeans.iterator().next()).actionBeanName;
	}
}