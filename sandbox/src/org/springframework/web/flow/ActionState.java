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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A state that executes one or more action beans when entered.
 * @author Keith Donald
 */
public class ActionState extends TransitionableState {

	private Set actionBeans = new LinkedHashSet(1);

	public ActionState(Flow flow, String id, ActionBean actionBean, Transition transition) {
		super(flow, id, transition);
		addActionBean(actionBean);
	}

	public ActionState(Flow flow, String id, ActionBean actionBean, Transition[] transitions) {
		super(flow, id, transitions);
		addActionBean(actionBean);
	}

	public ActionState(Flow flow, String id, ActionBean[] actionBeans, Transition[] transitions) {
		super(flow, id, transitions);
		addActionBeans(actionBeans);
	}

	public boolean isActionState() {
		return true;
	}

	protected void addActionBean(ActionBean actionBean) {
		this.actionBeans.add(actionBean);
	}

	protected void addActionBeans(ActionBean[] actionBeans) {
		for (int i = 0; i < actionBeans.length; i++) {
			this.actionBeans.add(actionBeans[i]);;
		}
	}

	protected ActionBean getActionBean() {
		Iterator it = actionBeanIterator();
		return (ActionBean)it.next();
	}

	/**
	 * @return An iterator that returns the set of action beans to execute for
	 *         this state.
	 */
	protected Iterator actionBeanIterator() {
		return actionBeans.iterator();
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
					"No action beans were executed, thus I cannot execute any state transition "
							+ "-- programmer configuration error; "
							+ "make sure you add at least one action bean to this state"));
		}
	}
}