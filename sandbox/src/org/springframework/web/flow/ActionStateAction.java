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

import java.util.Properties;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.ToStringCreator;

/**
 * A parameter object that allows for storing arbitrary properties about a
 * target <code>Action</code> implementation for use within exactly one
 * <code>ActionState</code> definition.
 * <p>
 * Note: these objects should NOT be reused between <code>ActionStates</code>.
 * Attempting to do so will generate an <code>IllegalStateException</code>.
 * 
 * @author Keith Donald
 */
public class ActionStateAction {

	// predefined properties

	/**
	 * The name of a named action.
	 */
	public static final String NAME_PROPERTY = "name";

	/**
	 * The action caption (short description / tooltip) property.
	 */
	public static final String CAPTION_PROPERTY = "caption";

	/**
	 * The action long description property.
	 */
	public static final String DESCRIPTION_PROPERTY = "description";

	/**
	 * Property storing the name of the method that should handle action
	 * execution when using a multi-action. A multi-action is an action
	 * that groups several action execute methods together on a single
	 * class. The methods follow the following signature:
	 * <pre>
	 * public Event ${executeMethodName}(RequestContext context)
	 * </pre>
	 */
	public static final String METHOD_PROPERTY = "method";

	/**
	 * The owning state that executes the action when entered.
	 */
	private ActionState state;

	/**
	 * The action to execute when the action state is entered.
	 */
	private Action targetAction;

	/**
	 * Contextual properties about the configured action's use within the owning
	 * state.
	 */
	private Properties properties;

	/**
	 * Creates a new action state action-info object for the specified action.
	 * No contextual properties are provided.
	 * @param state the owning state
	 * @param targetAction the action
	 */
	public ActionStateAction(ActionState state, Action targetAction) {
		this(state, targetAction, null);
	}

	/**
	 * Creates a new action state action-info object for the specified action.
	 * The map of properties is provided.
	 * @param state the owning state
	 * @param targetAction the action
	 * @param properties the properties describing usage of the action
	 */
	public ActionStateAction(ActionState state, Action targetAction, Properties properties) {
		Assert.notNull(state, "The action state is required");
		this.state = state;
		setTargetAction(targetAction);
		if (properties != null) {
			this.properties = new Properties(properties);
		}
	}

	/**
	 * Creates a new action state action-info object for the specified action.
	 * No contextual properties are provided. The owning state is not yet
	 * specified so this object should be associated with a state later on using
	 * the <code>setState()</code> method.
	 * @param targetAction the action
	 */
	public ActionStateAction(Action targetAction) {
		this(targetAction, null);
	}

	/**
	 * Creates a new action state action-info object for the specified action.
	 * The map of properties is provided. The owning state is not yet specified
	 * so this object should be associated with a state later on using the
	 * <code>setState()</code> method.
	 * @param targetAction the action
	 * @param properties the properties describing usage of the action
	 */
	public ActionStateAction(Action targetAction, Properties properties) {
		setTargetAction(targetAction);
		if (properties != null) {
			this.properties = new Properties(properties);
		}
	}

	/**
	 * Set the owning action state; this may only be set once and is required.
	 * @param state the owning action state
	 * @throws IllegalStateException if the action state has already been set
	 */
	protected void setState(ActionState state) throws IllegalStateException {
		Assert.state(this.state == null, "The action state must not already be set -- it may only be set once");
		this.state = state;
	}

	/**
	 * Set the target action wrapped by this object.
	 */
	private void setTargetAction(Action targetAction) {
		Assert.notNull(targetAction, "The target Action instance is required");
		this.targetAction = targetAction;
	}

	/**
	 * Returns the owning action state
	 * @return the owning action state
	 */
	public ActionState getState() {
		Assert.state(this.state != null, "The action state is required - this should not happen");
		return state;
	}

	/**
	 * Returns the wrapped target action.
	 * @return the action
	 */
	public Action getTargetAction() {
		return targetAction;
	}

	/**
	 * Sets the name of a named action. This is optional and can be
	 * <code>null</code>.
	 * @param name the action name
	 */
	public void setName(String name) {
		setProperty(NAME_PROPERTY, name);
	}

	/**
	 * Sets the short description for the action in the owning action state.
	 * @param caption the caption
	 */
	public void setCaption(String caption) {
		setProperty(CAPTION_PROPERTY, caption);
	}

	/**
	 * Sets the long description for the action in the owning action state.
	 * @param description the long description
	 */
	public void setDescription(String description) {
		setProperty(DESCRIPTION_PROPERTY, description);
	}

	/**
	 * Sets the name of the handler method on the target action instance to
	 * invoke when this action is executed. Only used by multi-actions.
	 * @param methodName the method name, with the signature
	 *        <code>Event ${methodName}(RequestContext context)</code>
	 */
	public void setMethod(String methodName) {
		setProperty(METHOD_PROPERTY, methodName);
	}

	/**
	 * Returns the name of a named action, or <code>null</code> if the action
	 * is unnamed in the owning state. Used when mapping action result events to
	 * transitions.
	 */
	public String getName() {
		return (String)getProperty(NAME_PROPERTY);
	}

	/**
	 * Returns whether or not the wrapped target action is a named action in the
	 * owning action state.
	 */
	public boolean isNamed() {
		return StringUtils.hasText(getName());
	}

	/**
	 * Returns the short description of the action in the action state.
	 */
	public String getCaption() {
		return (String)getProperty(CAPTION_PROPERTY);
	}

	/**
	 * Returns the logical description of this action in this action state.
	 */
	public String getDescription() {
		return (String)getProperty(DESCRIPTION_PROPERTY);
	}

	/**
	 * Returns the name of the handler method to invoke on the target action
	 * instance to handle action execution for this state. Only used by
	 * multi-actions.
	 * @return the execute method name
	 */
	public String getMethod() {
		return (String)getProperty(METHOD_PROPERTY);
	}

	/**
	 * Gets the property of the specified name, returning <code>null</code> if
	 * not found.
	 * @param propertyName the property name
	 * @return the property value, or <code>null</code> if not found
	 */
	public String getProperty(String propertyName) {
		return (String)getProperties().get(propertyName);
	}

	/**
	 * Returns the properties associated with the target action in the owning
	 * state.
	 */
	protected Properties getProperties() {
		if (properties == null) {
			this.properties = new Properties();
		}
		return properties;
	}

	/**
	 * Sets the property of the specified name to the specified value.
	 * @param propertyName the property name
	 * @param value the property value
	 */
	public void setProperty(String propertyName, String value) {
		getProperties().setProperty(propertyName, value);
	}

	/**
	 * Does this action state action have the specified property present?
	 * @param property the property
	 * @return true if present, false if not present
	 */
	public boolean containsProperty(String propertyName) {
		return getProperties().containsKey(propertyName);
	}

	public String toString() {
		return new ToStringCreator(this).append("stateId", state.getId()).append("targetAction", targetAction).append(
				"properties", properties).toString();
	}
}