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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class ActionState extends TransitionableState {
    
    public static final String STATE_ID_ATTRIBUTE = "_stateId";

	private Set actionBeanNames;

	public ActionState(String id, Transition transition) {
		super(id, transition);
		setActionBeanName(buildActionBeanName(id));
	}

	public ActionState(String id, Transition[] transitions) {
		super(id, transitions);
		setActionBeanName(buildActionBeanName(id));
	}

	public ActionState(String id, String actionBeanName, Transition transition) {
		super(id, transition);
		setActionBeanName(actionBeanName);
	}

	public ActionState(String id, String actionBeanName, Transition[] transitions) {
		super(id, transitions);
		setActionBeanName(actionBeanName);
	}

	public boolean isActionState() {
		return true;
	}

	public void setActionBeanName(String actionBeanName) {
		Assert.hasText(actionBeanName, "The action bean name is required");
		this.actionBeanNames = new HashSet(1);
		this.actionBeanNames.add(actionBeanName);
	}

	public void setActionBeanNames(String[] beanNames) {
		this.actionBeanNames = new HashSet(Arrays.asList(beanNames));
	}

	protected String buildActionBeanName(String stateId) {
		// do nothing, subclasses may override
		return stateId;
	}
	
	protected Set getActionBeanNames() {
		return actionBeanNames;
	}

	protected String getActionBeanName() {
		Assert.notEmpty(this.actionBeanNames, "The action beans collection is empty");
		return (String)this.actionBeanNames.iterator().next();
	}

	protected ViewDescriptor doEnterState(Flow flow, FlowSessionExecutionStack sessionExecutionStack,
			HttpServletRequest request, HttpServletResponse response) {
		Iterator it = this.actionBeanNames.iterator();
		while (it.hasNext()) {
			String actionBeanName = (String)it.next();
			ActionBean actionBean = (ActionBean)flow.getFlowDao().getActionBean(actionBeanName);
			if (logger.isDebugEnabled()) {
				logger.debug("Executing action bean with name '" + actionBeanName + "'");
			}
			sessionExecutionStack.setAttribute(STATE_ID_ATTRIBUTE, getId());
            ActionBeanEvent event = actionBean.execute(request, response, sessionExecutionStack);
            sessionExecutionStack.setAttribute(STATE_ID_ATTRIBUTE, null);
			if (triggersTransition(event, flow)) {
				return getTransition(event, flow).execute(flow, sessionExecutionStack, request, response);
			}
			else {
				if (event != null && logger.isWarnEnabled()) {
					logger.warn("Event '" + event + "' returned by action bean " + actionBean
							+ "' does not map to a valid state transition for action state '" + getId() + "' in flow '"
							+ flow.getId() + "'");
				}
			}
		}
		throw new IllegalStateException(
				"No valid event was signaled by the action bean(s) associated with action state '" + getId()
						+ "' of flow '" + flow.getId() + "' - programmer error?");
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