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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * @author Keith Donald
 */
public class ActionState extends TransitionableState {

	private Set actionBeans;

	public ActionState(String id, Transition transition) {
		super(id, transition);
		setActionBeanName(buildActionBeanNameFromStateId(id));
	}

	public ActionState(String id, Transition[] transitions) {
		super(id, transitions);
		setActionBeanName(buildActionBeanNameFromStateId(id));
	}

	protected String buildActionBeanNameFromStateId(String stateId) {
		// do nothing, subclasses may override
		return stateId;
	}

	public ActionState(String id, ActionBean actionBean, Transition transition) {
		super(id, transition);
		setActionBean(actionBean);
	}

	public ActionState(String id, ActionBean actionBean, Transition[] transitions) {
		super(id, transitions);
		setActionBean(actionBean);
	}

	public ActionState(String id, String actionBeanName, Transition transition) {
		super(id, transition);
		setActionBeanName(actionBeanName);
	}

	public ActionState(String id, String actionBeanName, Transition[] transitions) {
		super(id, transitions);
		setActionBeanName(actionBeanName);
	}

	private static class ActionBeanHolder {
		private String actionBeanName;

		private ActionBean actionBean;

		private ActionBeanHolder(String actionBeanName) {
			this.actionBeanName = actionBeanName;
		}

		private ActionBeanHolder(ActionBean actionBean) {
			this.actionBean = actionBean;
		}

		private ActionBeanHolder(String actionBeanName, ActionBean actionBean) {
			this.actionBeanName = actionBeanName;
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
		Assert.notNull(actionBean, "The action bean instance is required");
		this.actionBeans = new HashSet(1);
		this.actionBeans.add(new ActionBeanHolder(actionBean));
	}

	public void setActionBeanName(String actionBeanName) {
		Assert.hasText(actionBeanName, "The action bean name is required");
		this.actionBeans = new HashSet(1);
		this.actionBeans.add(new ActionBeanHolder(actionBeanName));
	}

	public void setActionBeans(ActionBean[] actionBeans) {
		Collection holders = new LinkedHashSet(actionBeans.length);
		for (int i = 0; i < actionBeans.length; i++) {
			holders.add(new ActionBeanHolder(actionBeans[i]));
		}
		this.actionBeans = new HashSet(holders);
	}

	public void setActionBeanNames(String[] beanNames) {
		Collection holders = new LinkedHashSet(beanNames.length);
		for (int i = 0; i < beanNames.length; i++) {
			holders.add(new ActionBeanHolder(beanNames[i]));
		}
		this.actionBeans = new HashSet(holders);
	}

	protected Iterator actionBeanIterator(final Flow flow) {
		final Iterator it = this.actionBeans.iterator();
		return new Iterator() {
			public Object next() {
				ActionBeanHolder holder = (ActionBeanHolder)it.next();
				if (holder.actionBean != null) {
					return holder.actionBean;
				}
				else {
					try {
						ActionBean actionBean = (ActionBean)flow.getFlowDao().getActionBean(holder.actionBeanName);
						Assert.notNull(actionBean, "The action bean retrieved from the registry must not be null");
						return actionBean;
					}
					catch (NoSuchBeanDefinitionException e) {
						throw new NoSuchActionBeanException(flow, ActionState.this, e);
					}
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

	protected ViewDescriptor doEnterState(Flow flow, FlowSessionExecutionStack sessionExecution,
			HttpServletRequest request, HttpServletResponse response) {
		Iterator it = actionBeanIterator(flow);
		while (it.hasNext()) {
			ActionBean actionBean = (ActionBean)it.next();
			if (logger.isDebugEnabled()) {
				logger.debug("Executing action bean '" + actionBean + "'");
			}
			ActionBeanEvent event = actionBean.execute(request, response, sessionExecution);
			if (triggersTransition(event, flow)) {
				return getTransition(event, flow).execute(flow, sessionExecution, request, response);
			}
			else {
				if (event != null && logger.isWarnEnabled()) {
					logger.warn("Event '" + event + "' returned by action bean " + actionBean
							+ "' does not map to a valid state transition for action state '" + getId() + "' in flow '"
							+ flow.getId() + "'");
				}
			}
		}
		throw new CannotExecuteStateTransitionException(flow, getId(), new IllegalStateException(
				"No valid event was signaled by the action bean(s) that executed in this action state '" + getId()
						+ "' of flow '" + flow.getId() + "' - programmer error?"));
	}

	protected boolean triggersTransition(ActionBeanEvent event, Flow flow) {
		return getTransition(event, flow) != null;
	}

	protected Transition getTransition(ActionBeanEvent event, Flow flow) {
		if (event == null) {
			return null;
		}
		return getTransition(event.getId(), flow);
	}
}