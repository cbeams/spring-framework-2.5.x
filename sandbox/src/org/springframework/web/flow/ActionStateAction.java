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
import org.springframework.util.ToStringCreator;

/**
 * A local parameter object that allows for storing arbitrary properties about a
 * target <code>Action</code> implementation for use within exactly one
 * <code>ActionState</code> definition.
 * <p>
 * Note: this object should NOT be reused between <code>ActionStates</code>.
 * Attempting to do so will generate a <code>IllegalStateException</code>.
 * 
 * @author Keith Donald
 */
public class ActionStateAction {

	/**
	 * The action caption (short description / tooltip) property.
	 */
	public static final String CAPTION_PROPERTY = "caption";

	/**
	 * The action long description property.
	 */
	public static final String DESCRIPTION_PROPERTY = "description";

	/**
	 * The action result qualifier string property.
	 */
	public static final String RESULT_QUALIFIER_PROPERTY = "resultQualifier";

	/**
	 * Property storing ther name of the method that should be handle action
	 * execution.
	 */
	public static final String EXECUTE_METHOD_NAME_PROPERTY = "executeMethodName";

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
		Assert.notNull(state, "The action state is required");
		this.state = state;
		setTargetAction(targetAction);
	}

	/**
	 * Creates a new action state info parameter object for the specified
	 * action. No contextual properties are provided.
	 * @param targetAction The action
	 */
	public ActionStateAction(Action targetAction) {
		setTargetAction(targetAction);
	}

	/**
	 * Creates a new action state info parameter object for the specified
	 * action. The map of properties is provided.
	 * @param targetAction The action
	 * @param properties the properties describing usage of this action in this
	 *        state
	 */
	public ActionStateAction(Action targetAction, Map properties) {
		setTargetAction(targetAction);
		if (properties != null) {
			this.properties = new HashMap(properties);
		}
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

	private void setTargetAction(Action targetAction) {
		Assert.notNull(targetAction, "The target Action instance is required");
		this.targetAction = targetAction;
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
	 * Sets the short description for the action state action.
	 * @param caption the caption
	 */
	public void setCaption(String caption) {
		setProperty(CAPTION_PROPERTY, caption);
	}

	/**
	 * Sets the long description for the action state action.
	 * @param description the long description
	 */
	public void setDescription(String description) {
		setProperty(DESCRIPTION_PROPERTY, description);
	}

	/**
	 * Sets a qualifier for result identifiers when the target action is
	 * executed.
	 * @param resultQualifier the result qualifier.
	 */
	public void setResultQualifier(String resultQualifier) {
		setProperty(RESULT_QUALIFIER_PROPERTY, resultQualifier);
	}

	/**
	 * Sets the name of the handler method on the target action instance to
	 * invoke when this action is executed.
	 * @param executeMethodName the method name, with the signature
	 *        <code>Event ${methodName}(RequestContext context)</code>
	 */
	public void setExecuteMethodName(String executeMethodName) {
		setProperty(EXECUTE_METHOD_NAME_PROPERTY, executeMethodName);
	}

	/**
	 * Returns the logical name of the action in the action state. Often used
	 * when mapping action result events to transitions. Also used for
	 * dispatching multi-action executions to target methods on the configured
	 * Action object.
	 * @return the action name
	 */
	public String getCaption() {
		return (String)getProperty(CAPTION_PROPERTY);
	}

	/**
	 * Returns the logical description of this action in this action state.
	 * @return
	 */
	public String getDescription() {
		return (String)getProperty(DESCRIPTION_PROPERTY);
	}

	/**
	 * Returns the value of the action result qualifier id property.
	 * @return
	 */
	public String getResultQualifier() {
		return (String)getProperty(RESULT_QUALIFIER_PROPERTY);
	}

	/**
	 * Returns the name of the handler method to invoke on the target action
	 * instance to handle action execution for this state.
	 * @return the execute method name
	 */
	public String getExecuteMethodName() {
		return (String)getProperty(EXECUTE_METHOD_NAME_PROPERTY);
	}

	/**
	 * Gets the property of the specified name, returning <code>null</code> if
	 * not found.
	 * @param propertyName The property name
	 * @return the property value, or <code>null</code> if not found
	 */
	public Object getProperty(String propertyName) {
		return getProperties().get(propertyName);
	}

	protected Map getProperties() {
		if (properties == null) {
			this.properties = new HashMap();
		}
		return properties;
	}

	/**
	 * Sets the property of the specified name to the specified value
	 * @param propertyName The property name
	 * @param value The property value
	 */
	public void setProperty(String propertyName, Object value) {
		getProperties().put(propertyName, value);
	}

	public String toString() {
		return new ToStringCreator(this).append("stateId", state.getId()).append("targetAction", targetAction).append(
				"properties", properties).toString();
	}
}