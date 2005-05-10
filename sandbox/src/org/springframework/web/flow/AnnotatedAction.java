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

import org.springframework.core.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A parameter object that allows for storing arbitrary properties about a
 * target <code>Action</code> implementation for use in exactly one
 * context, for example an <code>ActionState</code> definition, a
 * <code>TransitionCriteria</code> definition, or in a test environment.
 * 
 * @author Keith Donald
 */
public class AnnotatedAction extends AnnotatedObject {
	
	// well known properties

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
	 * execution when using a multi-action. A multi-action is an action that
	 * groups several action execute methods together on a single class. The
	 * methods follow the following signature:
	 * <pre>
	 *    public Event ${method}(RequestContext context)
	 * </pre>
	 */
	public static final String METHOD_PROPERTY = "method";
	
	/**
	 * The action to execute.
	 */
	private Action targetAction;

	/**
	 * Creates a new annotated action object for the specified action.
	 * No contextual properties are provided.
	 * @param targetAction the action
	 */
	public AnnotatedAction(Action targetAction) {
		this(targetAction, new HashMap(3));
	}

	/**
	 * Creates a new annotated action object for the specified action.
	 * The map of properties is provided.
	 * @param targetAction the action
	 * @param properties the properties describing usage of the action
	 */
	public AnnotatedAction(Action targetAction, Map properties) {
		setTargetAction(targetAction);
		setProperties(properties);
	}

	/**
	 * Set the target action wrapped by this object.
	 */
	private void setTargetAction(Action targetAction) {
		Assert.notNull(targetAction, "The target Action instance is required");
		this.targetAction = targetAction;
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
	 * Sets the short description for the action.
	 * @param caption the caption
	 */
	public void setCaption(String caption) {
		setProperty(CAPTION_PROPERTY, caption);
	}

	/**
	 * Sets the long description for the action.
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
	 * Returns the wrapped target action.
	 * @return the action
	 */
	public Action getTargetAction() {
		return targetAction;
	}

	/**
	 * Returns the name of a named action, or <code>null</code> if the action
	 * is unnamed. Used when mapping action result events to transitions.
	 */
	public String getName() {
		return (String)getProperty(NAME_PROPERTY);
	}

	/**
	 * Returns whether or not the wrapped target action is a named action.
	 */
	public boolean isNamed() {
		return StringUtils.hasText(getName());
	}

	/**
	 * Returns the short description of the action.
	 */
	public String getCaption() {
		return (String)getProperty(CAPTION_PROPERTY);
	}

	/**
	 * Returns the long description of this action.
	 */
	public String getDescription() {
		return (String)getProperty(DESCRIPTION_PROPERTY);
	}

	/**
	 * Returns the name of the handler method to invoke on the target action
	 * instance to handle action execution. Only used by multi-actions.
	 * @return the execute method name
	 */
	public String getMethod() {
		return (String)getProperty(METHOD_PROPERTY);
	}

	public String toString() {
		return new ToStringCreator(this).append("action", targetAction).append("properties", getProperties())
				.toString();
	}
}