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

import java.util.Map;

import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.StateContext;
import org.springframework.web.flow.TransitionCriteria;

/**
 * A action transition precondition that can be parameterized with attributes that can influence
 * the behaivor of the action when it is executed.
 * @author Keith Donald
 */
public class ParameterizableActionTransitionPrecondition implements TransitionCriteria {

	/**
	 * The wrapped precondition.
	 */
	private ActionTransitionPrecondition actionPrecondition;

	/**
	 * The action execution attributes.
	 */
	private MutableAttributeSource actionExecutionAttributes = new MapAttributeSource();

	/**
	 * Create an action precondition delegating to the specified action.
	 * @param action the action
	 */
	public ParameterizableActionTransitionPrecondition(Action action) {
		this.actionPrecondition = new ActionTransitionPrecondition(action);
	}

	/**
	 * Create a parameterized action precondition delegating to the specified action.
	 * @param action the action
	 * @param actionExecutionAttributes the action execution properties
	 */
	public ParameterizableActionTransitionPrecondition(Action action, Map actionExecutionAttributes) {
		this.actionPrecondition = new ActionTransitionPrecondition(action);
		setAttributes(actionExecutionAttributes);
	}

	/**
	 * @param actionExecutionAttributes
	 */
	public void setAttributes(Map actionExecutionAttributes) {
		this.actionExecutionAttributes = new MapAttributeSource(actionExecutionAttributes);
	}

	/**
	 * @param attributeName
	 * @param attributeValue
	 */
	public void setAttribute(String attributeName, Object attributeValue) {
		this.actionExecutionAttributes.setAttribute(attributeName, attributeValue);
	}

	/**
	 * Returns the action result <code>eventId</code> that should cause this
	 * precondition to return true (it will return false otherwise).
	 */
	public String getTrueEventId() {
		return this.actionPrecondition.getTrueEventId();
	}

	/**
	 * Sets the action result <code>eventId</code> that should cause this
	 * precondition to return true (it will return false otherwise).
	 * 
	 * @param trueEventId
	 *            the true result event ID
	 */
	public void setTrueEventId(String trueEventId) {
		this.actionPrecondition.setTrueEventId(trueEventId);
	}

	public boolean test(RequestContext context) {
		// not sure if we like this down cast
		((StateContext)context).setActionExecutionAttributes(actionExecutionAttributes);
		return this.actionPrecondition.test(context);
	}
}