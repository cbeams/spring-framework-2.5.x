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

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.ToStringCreator;

/**
 * A local parameter object that allows for storing abritrary properties about a
 * target <code>Action</code> implementation for use within exactly one
 * <code>ActionState</code> definition.
 * <p>
 * Note: this object should NOT be reused between <code>ActionStates</code>.
 * Attempting to do so will generate a <code>IllegalStateException</code>.
 * 
 * @author Keith Donald
 */
public class ActionStateAction {

	public static final String NAME_PROPERTY = "name";

	public static final String DESCRIPTION_PROPERTY = "description";

	/**
	 * The owning state that executes the action when entered.
	 */
	private ActionState state;

	/**
	 * The action to execute when the action state is entered.
	 */
	private Action targetAction;

	/**
	 * Contextual properties about the configured action's use within the
	 * configured state.
	 */
	private Map properties;

	/**
	 * Creates a new action state info parameter object for the specified
	 * action. No contextual properties are provided.
	 * @param state the state
	 * @param targetAction The action
	 */
	public ActionStateAction(ActionState state, Action targetAction) {
		this.state = state;
		this.targetAction = targetAction;
	}

	/**
	 * Creates a new action state info parameter object for the specified
	 * action. No contextual properties are provided.
	 * @param targetAction The action
	 */
	public ActionStateAction(Action targetAction) {
		this.targetAction = targetAction;
	}

	/**
	 * Creates a new action state info parameter object for the specified
	 * action. The 'name' property is provided.
	 * @param targetAction The action
	 * @param name the name of the action
	 */
	public ActionStateAction(Action targetAction, String name) {
		this.properties = new HashMap(1);
		setName(name);
		this.targetAction = targetAction;
	}

	/**
	 * Creates a new action state info parameter object for the specified
	 * action. The 'name' and 'description' properties are provided.
	 * @param targetAction The action
	 * @param name the name of the action
	 * @param description the description of the action
	 */
	public ActionStateAction(Action targetAction, String name, String description) {
		this.properties = new HashMap(1);
		setName(name);
		setDescription(description);
		this.targetAction = targetAction;
	}

	/**
	 * Creates a new action state info parameter object for the specified
	 * action. The map of properties is provided.
	 * @param targetAction The action
	 * @param properties the properties describing usage of this action in this
	 *        state
	 */
	public ActionStateAction(Action targetAction, Map properties) {
		this.properties = new HashMap(properties);
		this.targetAction = targetAction;
	}

	/**
	 * Set the action state; this may only be set once and is required.
	 * @param state The action state
	 * @throws IllegalStateException if the action state has already been set
	 */
	protected void setState(ActionState state) {
		Assert.state(this.state == null, "The action state must not already be set--it may only be set once");
		this.state = state;
	}

	private void setName(String name) {
		if (StringUtils.hasText(name)) {
			this.properties.put(NAME_PROPERTY, name);
		}
		else {
			this.properties.remove(NAME_PROPERTY);
		}
	}

	private void setDescription(String description) {
		if (StringUtils.hasText(description)) {
			this.properties.put(DESCRIPTION_PROPERTY, description);
		}
		else {
			this.properties.remove(DESCRIPTION_PROPERTY);
		}
	}

	/**
	 * Returns the owning action state
	 * @return the action state
	 */
	public ActionState getState() {
		Assert.state(this.state != null, "The action state is required - this should not happen");
		return state;
	}

	/**
	 * Returns the action
	 * @return the action
	 */
	public Action getTargetAction() {
		return targetAction;
	}

	/**
	 * Returns the logical name of the action in the action state. Often used
	 * when mapping action result events to transitions. Also used for
	 * dispatching multi-action executions to target methods on the configured
	 * Action object.
	 * @return the action name
	 */
	public String getName() {
		return (String)getProperty(NAME_PROPERTY);
	}

	/**
	 * Returns true when the wrapped action is named, false otherwise.
	 */
	public boolean isNamed() {
		return StringUtils.hasText(getName());
	}

	/**
	 * Returns the logical description of this action in this action state.
	 * @return
	 */
	public String getDescription() {
		return (String)getProperty(DESCRIPTION_PROPERTY);
	}

	/**
	 * Gets the property of the specified name, returning <code>null</code> if
	 * not found.
	 * @param propertyName The property name
	 * @return the property value, or <code>null</code> if not found
	 */
	public Object getProperty(String propertyName) {
		if (properties == null) {
			return null;
		}
		return properties.get(propertyName);
	}

	public String toString() {
		return new ToStringCreator(this).append("action", targetAction).append("properties", properties).toString();
	}
}