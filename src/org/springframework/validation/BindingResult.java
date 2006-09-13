/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.validation;

import java.util.Map;

import org.springframework.beans.PropertyEditorRegistry;

/**
 * General interface that represents binding results.
 * Extends the Errors interface for error registration capabilities
 * and adds binding-specific functionality.
 *
 * <p>Serves as result holder for DataBinder. Implementations can
 * also be used directly, for example to invoke a Validator on it.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see DataBinder#getBindingResult()
 * @see Errors
 * @see Validator
 * @see BeanPropertyBindingResult
 * @see MapBindingResult
 */
public interface BindingResult extends Errors {

	/**
	 * Prefix for the name of the BindingResult instance in a model,
	 * followed by the object name.
	 */
	String MODEL_KEY_PREFIX = BindingResult.class.getName() + ".";


	/**
	 * Return the wrapped target object.
	 */
	Object getTarget();

	/**
	 * Return a model Map for the obtained state, exposing a BindingResult
	 * instance as '{@link #MODEL_KEY_PREFIX MODEL_KEY_PREFIX} + objectName'
	 * and the object itself as 'objectName'.
	 * <p>Note that the Map is constructed every time you're calling this method.
	 * Adding things to the map and then re-calling this method will not work.
	 * <p>The attributes in the model Map returned by this method are usually
	 * included in the ModelAndView for a form view that uses Spring's bind tag,
	 * which needs access to the BindingResult instance. Spring's pre-built
	 * form controllers will do this for you when rendering a form view.
	 * When building the ModelAndView yourself, you need to include the attributes
	 * from the model Map returned by this method yourself.
	 * @see #getObjectName
	 * @see #MODEL_KEY_PREFIX
	 * @see org.springframework.web.servlet.ModelAndView
	 * @see org.springframework.web.servlet.tags.BindTag
	 * @see org.springframework.web.servlet.mvc.SimpleFormController
	 */
	Map getModel();

	/**
	 * Return the underlying PropertyEditorRegistry.
	 * @throws UnsupportedOperationException if the BindingResult
	 * does not support a PropertyEditorRegistry
	 */
	PropertyEditorRegistry getPropertyEditorRegistry();

	/**
	 * Mark the specified disallowed field as suppressed.
	 * <p>The data binder invokes this for each field value that was
	 * detected to target a disallowed field.
	 * @see DataBinder#setAllowedFields
	 */
	void recordSuppressedField(String fieldName);

	/**
	 * Return the list of fields that were suppressed during the bind process.
	 * <p>Can be used to determine whether any field values were targetting
	 * disallowed fields.
	 * @see DataBinder#setAllowedFields
	 */
	String[] getSuppressedFields();

	/**
	 * Add an ObjectError or FieldError to the errors list.
	 * <p>Intended to be used by subclasses like DataBinder,
	 * or by cooperating strategies like a BindingErrorProcessor.
	 * @see ObjectError
	 * @see FieldError
	 * @see DataBinder
	 * @see BindingErrorProcessor
	 */
	void addError(ObjectError error);

	/**
	 * Resolve the given error code into message codes for the given field.
	 * Calls the MessageCodesResolver with appropriate parameters.
	 * @param errorCode the error code to resolve into message codes
	 * @param field the field to resolve message codes for
	 * @return the resolved message codes
	 */
	String[] resolveMessageCodes(String errorCode, String field);

}
