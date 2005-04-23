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

import org.springframework.binding.AttributeSource;
import org.springframework.binding.MutableAttributeSource;
import org.springframework.binding.support.EmptyAttributeSource;
import org.springframework.binding.support.MapAttributeSource;
import org.springframework.web.flow.Action;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.StateContext;
import org.springframework.web.flow.TransitionCriteria;

/**
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

	public ParameterizableActionTransitionPrecondition(Action action) {
		this.actionPrecondition = new ActionTransitionPrecondition(action);
	}

	public ParameterizableActionTransitionPrecondition(Action action, Map actionExecutionAttributes) {
		this.actionPrecondition = new ActionTransitionPrecondition(action);
		setAttributes(actionExecutionAttributes);
	}

	public void setAttributes(Map actionExecutionAttributes) {
		this.actionExecutionAttributes = new MapAttributeSource(actionExecutionAttributes);
	}

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
		((StateContext)context).setActionExecutionAttributes(actionExecutionAttributes);
		return this.actionPrecondition.test(context);
	}
}