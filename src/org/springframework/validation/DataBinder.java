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

package org.springframework.validation;

import java.beans.PropertyEditor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessExceptionsException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * Binder that allows for binding property values to a target object.
 * The binding process can be customized through specifying allowed fields,
 * required fields, and custom editors.
 *
 * <p>The binding results can be examined via the Errors interface,
 * available as BindException instance. Missing field errors and property
 * access exceptions will be converted to FieldErrors, collected in the
 * Errors instance. Custom validation errors can be added afterwards.
 *
 * <p>This generic data binder can be used in any sort of environment.
 * It is heavily used by Spring's web binding features, via the subclass
 * ServletRequestDataBinder.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #bind
 * @see #getErrors
 * @see org.springframework.web.bind.ServletRequestDataBinder
 */
public class DataBinder {

	public static final String MISSING_FIELD_ERROR_CODE = "required";

	private BindException errors;

	private String[] allowedFields;

	private String[] requiredFields;


	/**
	 * Create a new DataBinder instance.
	 * @param target target object to bind onto
	 * @param objectName name of the target object
	 */
	public DataBinder(Object target, String objectName) {
		this.errors = createErrors(target, objectName);
	}

	/**
	 * Create a new Errors instance for this data binder.
	 * Can be overridden in subclasses to.
	 * Needs to be a subclass of BindException.
	 * @param target target object to bind onto
	 * @param objectName name of the target object
	 * @return the Errors instance
	 * @see #close
	 */
	protected BindException createErrors(Object target, String objectName) {
		return new BindException(target, objectName);
	}

	/**
	 * Return the wrapped target object.
	 */
	public Object getTarget() {
		return this.errors.getTarget();
	}

	/**
	 * Return the name of the bound object.
	 */
	public String getObjectName() {
		return this.errors.getObjectName();
	}

	/**
	 * Return the Errors instance for this data binder.
	 * @return the Errors instance, to be treated as Errors or as BindException
	 * @see Errors
	 */
	public BindException getErrors() {
		return errors;
	}

	/**
	 * Return the underlying BeanWrapper of the Errors object.
	 * To be used by binder subclasses that need bean property checks.
	 */
	protected BeanWrapper getBeanWrapper() {
		return this.errors.getBeanWrapper();
	}

	/**
	 * Register fields that should be allowed for binding. Default is all
	 * fields. Restrict this for example to avoid unwanted modifications
	 * by malicious users when binding HTTP request parameters.
	 * <p>Supports "xxx*" and "*xxx" patterns. More sophisticated matching
	 * can be implemented by overriding the isAllowed method.
	 * @param allowedFields array of field names
	 * @see org.springframework.web.bind.ServletRequestDataBinder
	 * @see #isAllowed
	 */
	public void setAllowedFields(String[] allowedFields) {
		this.allowedFields = allowedFields;
	}

	/**
	 * Return the fields that should be allowed for binding.
	 * @return array of field names
	 */
	public String[] getAllowedFields() {
		return allowedFields;
	}

	/**
	 * Register fields that are required for each binding process.
	 * @param requiredFields array of field names
	 */
	public void setRequiredFields(String[] requiredFields) {
		this.requiredFields = requiredFields;
	}

	/**
	 * Return the fields that are required for each binding process.
	 * @return array of field names
	 */
	public String[] getRequiredFields() {
		return requiredFields;
	}

	/**
	 * Register the given custom property editor for all properties
	 * of the given type.
	 * @param requiredType type of the property
	 * @param propertyEditor editor to register
	 * @see org.springframework.beans.BeanWrapper#registerCustomEditor
	 */
	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		this.errors.getBeanWrapper().registerCustomEditor(requiredType, propertyEditor);
	}

	/**
	 * Register the given custom property editor for the given type and
	 * field, or for all fields of the given type.
	 * <p>If the field denotes an array or Collection, the PropertyEditor
	 * will get applied either to the array/Collection itself (the
	 * PropertyEditor has to create an array or Collection value) or to
	 * each element (the PropertyEditor has to create the element type),
	 * depending on the specified required type.
	 * <p>Note: Only one single registered custom editor per property path
	 * is supported. In case of a Collection/array, do not register an editor
	 * for both the Collection/array and each element on the same property.
	 * @param requiredType type of the property (can be null if a field is
	 * given but should be specified in any case for consistency checking)
	 * @param field name of the field (can also be a nested path), or
	 * null if registering an editor for all fields of the given type
	 * @param propertyEditor editor to register
	 * @see org.springframework.beans.BeanWrapper#registerCustomEditor
	 */
	public void registerCustomEditor(Class requiredType, String field, PropertyEditor propertyEditor) {
		this.errors.getBeanWrapper().registerCustomEditor(requiredType, field, propertyEditor);
	}

	/**
	 * Set the strategy to use for resolving errors into message codes.
	 * Applies the given strategy to the underlying errors holder.
	 * @see BindException#setMessageCodesResolver
	 */
	public void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
		this.errors.setMessageCodesResolver(messageCodesResolver);
	}


	/**
	 * Bind the given property values to this binder's target.
	 * This call can create field errors, representing basic binding
	 * errors like a required field (code "required"), or type mismatch
	 * between value and bean property (code "typeMismatch").
	 * <p>Note that the given PropertyValues should be a throwaway instance:
	 * For efficiency, it will be modified to just contain allowed fields if it
	 * implements the MutablePropertyValues interface; else, an internal mutable
	 * copy will be created for this purpose. Pass in a copy of the PropertyValues
	 * if you want your original instance to stay unmodified in any case.
	 * @param pvs property values to bind.
	 */
	public void bind(PropertyValues pvs) {
		// check for fields to bind
		List allowedFieldsList = (this.allowedFields != null) ? Arrays.asList(this.allowedFields) : null;
		MutablePropertyValues mpvs = (pvs instanceof MutablePropertyValues) ?
		    (MutablePropertyValues) pvs : new MutablePropertyValues(pvs);
		PropertyValue[] pvArray = pvs.getPropertyValues();
		for (int i = 0; i < pvArray.length; i++) {
			String field = pvArray[i].getName();
			if (!((allowedFieldsList != null && allowedFieldsList.contains(field)) || isAllowed(field))) {
				mpvs.removePropertyValue(pvArray[i]);
			}
		}
		pvs = mpvs;

		// check for missing fields
		if (this.requiredFields != null) {
			for (int i = 0; i < this.requiredFields.length; i++) {
				PropertyValue pv = pvs.getPropertyValue(this.requiredFields[i]);
				if (pv == null || "".equals(pv.getValue()) || pv.getValue() == null) {
					// create field error with code "required"
					String field = this.requiredFields[i];
					this.errors.addError(
							new FieldError(this.errors.getObjectName(), field, "", true,
														 this.errors.resolveMessageCodes(MISSING_FIELD_ERROR_CODE, field),
														 getArgumentsForBindingError(field), "Field '" + field + "' is required"));
				}
			}
		}

		try {
			// bind request parameters onto params, ignoring unknown properties
			this.errors.getBeanWrapper().setPropertyValues(pvs, true);
		}
		catch (PropertyAccessExceptionsException ex) {
			PropertyAccessException[] exs = ex.getPropertyAccessExceptions();
			for (int i = 0; i < exs.length; i++) {
				// create field with the exceptions's code, e.g. "typeMismatch"
				String field = exs[i].getPropertyChangeEvent().getPropertyName();
				this.errors.addError(
						new FieldError(this.errors.getObjectName(), field, exs[i].getPropertyChangeEvent().getNewValue(), true,
													 this.errors.resolveMessageCodes(exs[i].getErrorCode(), field),
													 getArgumentsForBindingError(field), exs[i].getLocalizedMessage()));
			}
		}
	}

	/**
	 * Return if the given field is allowed for binding.
	 * Invoked for each passed-in property value.
	 * <p>The default implementation checks for "xxx*" and "*xxx" matches.
	 * Can be overridden in subclasses.
	 * <p>If the field is found in the allowedFields array as direct match,
	 * this method will not be invoked.
	 * @param field the field to check
	 * @return if the field is allowed
	 * @see #setAllowedFields
	 */
	protected boolean isAllowed(String field) {
		if (this.allowedFields != null) {
			for (int i = 0; i < this.allowedFields.length; i++) {
				String allowed = this.allowedFields[i];
				if ((allowed.endsWith("*") && field.startsWith(allowed.substring(0, allowed.length() - 1))) ||
						(allowed.startsWith("*") && field.endsWith(allowed.substring(1, allowed.length())))) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Return FieldError arguments for a binding error on the given field.
	 * Invoked for each missing required fields and each type mismatch.
	 * <p>Default implementation returns a DefaultMessageSourceResolvable
	 * with "objectName.field" and "field" as codes.
	 * @param field the field that caused the binding error
	 * @return the Object array that represents the FieldError arguments
	 * @see FieldError#getArguments
	 * @see org.springframework.context.support.DefaultMessageSourceResolvable
	 */
	protected Object[] getArgumentsForBindingError(String field) {
		return new Object[] {
			new DefaultMessageSourceResolvable(new String[] {getObjectName() + Errors.NESTED_PATH_SEPARATOR + field, field},
																				 null, field)
		};
	}

	/**
	 * Close this DataBinder, which may result in throwing
	 * a BindException if it encountered any errors
	 * @return the model Map, containing target object and Errors instance
	 * @throws BindException if there were any errors in the bind operation
	 * @see BindException#getModel
	 */
	public Map close() throws BindException {
		if (this.errors.hasErrors()) {
			throw this.errors;
		}
		return this.errors.getModel();
	}

}
