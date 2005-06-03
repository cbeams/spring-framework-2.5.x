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
package org.springframework.web.flow.action;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.execution.ActionLocator;

/**
 * A action that delegates to another action instance managed in flow scope.
 * 
 * @author Keith Donald
 */
public class DelegatingAction implements Action, BeanNameAware {

	/**
	 * The name of the action locator in flow scope.
	 */
	private String actionLocatorAttributeName = "actionLocator";

	/**
	 * The id of the action to delegate to.
	 */
	private String actionId;

	/**
	 * Default constructor for bean style usage.
	 */
	public DelegatingAction() {
	}

	/**
	 * Create a new delegating action using the action locator
	 * stored using given attribute name in the flow scope.
	 * @param actionLocatorAttribute the name of the action locator
	 *        in flow scope
	 */
	public DelegatingAction(String actionLocatorAttribute) {
		setActionLocatorAttributeName(actionLocatorAttribute);
	}

	/**
	 * Create a new delegating action using the action locator
	 * stored using given attribute name in the flow scope. The locator
	 * will be used to lookup identified action bean.
	 * @param actionLocatorAttribute the name of the action locator
	 *        in flow scope
	 * @param actionBeanName the id of the bean to look up
	 */
	public DelegatingAction(String actionLocatorAttribute, String actionBeanName) {
		setActionLocatorAttributeName(actionLocatorAttribute);
		setActionId(actionBeanName);
	}

	/**
	 * Returns the attribute name of the action locator in flow scope.
	 */
	public String getActionLocatorAttribute() {
		return actionLocatorAttributeName;
	}

	/**
	 * Set the attribute name of the action locator in flow scope.
	 */
	public void setActionLocatorAttributeName(String actionLocatorAttribute) {
		this.actionLocatorAttributeName = actionLocatorAttribute;
	}

	/**
	 * Returns the id of the action to lookup and delegate to.
	 */
	public String getActionId() {
		return actionId;
	}

	/**
	 * Set the id of the action to lookup and delegate to.
	 */
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public void setBeanName(String name) {
		if (this.actionId == null || this.actionId.length() == 0) {
			this.actionId = name;
		}
	}

	/*
	 * Looks up the action locator in flow scope and resolves the delegate
	 * action and executes it.
	 */
	public Event execute(RequestContext context) throws Exception {
		Object locator = (ApplicationContext)context.getFlowScope().getRequiredAttribute(getActionLocatorAttribute());
		Action action;
		if (locator instanceof ActionLocator) {
			action = ((ActionLocator)locator).getAction(actionId);
		}
		else if (locator instanceof BeanFactory) {
			action = (Action)((BeanFactory)locator).getBean(actionId, Action.class);
		}
		else {
			throw new IllegalStateException("Attribute with name '" + actionLocatorAttributeName
					+ "' must be an ActionLocator or a BeanFactory but was a " + locator);
		}
		return action.execute(context);
	}

}