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

package org.springframework.validation;

import java.beans.PropertyEditor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessExceptionsException;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.util.StringUtils;

/**
 * Binder that allows for binding property values to a target object.
 * The binding process can be customized through specifying allowed fields,
 * required fields, and custom editors.
 *
 * <p>Note that there are potential security implications in failing to set
 * an array of allowed fields. In the case of HTTP form POST data for example,
 * malicious clients can attempt to subvert an application by supplying values
 * for fields or properties that do not exist on the form. In some cases this
 * could lead to illegal data being set on command objects <i>or their nested
 * objects</i>. For this reason, it is <b>highly recommended to specify the
 * {@link #setAllowedFields allowedFields} property</b> on the DataBinder.
 *
 * <p>The binding results can be examined via the Errors interface,
 * available as BindException instance. Missing field errors and property
 * access exceptions will be converted to FieldErrors, collected in the
 * Errors instance, with the following error codes:
 *
 * <ul>
 * <li>Missing field error: "required"
 * <li>Type mismatch error: "typeMismatch"
 * <li>Method invocation error: "methodInvocation"
 * </ul>
 *
 * <p>Custom validation errors can be added afterwards. You will typically
 * want to resolve such error codes into proper user-visible error messages;
 * this can be achieved through resolving each error via a MessageSource.
 * The list of message codes to try can be customized through the
 * MessageCodesResolver strategy. DefaultMessageCodesResolver's javadoc
 * gives details on the default resolution rules.
 *
 * <p>By default, binding errors are resolved through the binding error processor
 * for required binding errors and property access exceptions. You can override
 * those if needed, for example to generate different error codes.
 *
 * <p>This generic data binder can be used in any sort of environment.
 * It is heavily used by Spring's web MVC controllers, via the subclass
 * <code>org.springframework.web.bind.ServletRequestDataBinder</code>.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setAllowedFields
 * @see #setRequiredFields
 * @see #registerCustomEditor
 * @see #setMessageCodesResolver
 * @see #setBindingErrorProcessor
 * @see #bind
 * @see #getErrors
 * @see DefaultMessageCodesResolver
 * @see DefaultBindingErrorProcessor
 * @see org.springframework.context.MessageSource
 * @see org.springframework.web.bind.ServletRequestDataBinder
 */
public class DataBinder implements PropertyEditorRegistry {

	/** Default object name used for binding: "target" */
	public static final String DEFAULT_OBJECT_NAME = "target";


	/**
	 * We'll create a lot of DataBinder instances: Let's use a static logger.
	 */
	protected static final Log logger = LogFactory.getLog(DataBinder.class);

	private final BindException errors;

	private boolean ignoreUnknownFields = true;

	private String[] allowedFields;

	private String[] requiredFields;

	private BindingErrorProcessor bindingErrorProcessor = new DefaultBindingErrorProcessor();


	/**
	 * Create a new DataBinder instance, with default object name.
	 * @param target target object to bind onto
	 * @see #DEFAULT_OBJECT_NAME
	 */
	public DataBinder(Object target) {
		this(target, DEFAULT_OBJECT_NAME);
	}

	/**
	 * Create a new DataBinder instance.
	 * @param target target object to bind onto
	 * @param objectName name of the target object
	 */
	public DataBinder(Object target, String objectName) {
		this.errors = createErrors(target, objectName);
		setExtractOldValueForEditor(true);
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
	 * Set whether to ignore unknown fields, i.e. whether to ignore request
	 * parameters that don't have corresponding fields in the target object.
	 */
	public void setIgnoreUnknownFields(boolean ignoreUnknownFields) {
		this.ignoreUnknownFields = ignoreUnknownFields;
	}

	/**
	 * Return whether to ignore unknown fields, i.e. whether to ignore request
	 * parameters that don't have corresponding fields in the target object.
	 */
	public boolean isIgnoreUnknownFields() {
		return ignoreUnknownFields;
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
		if (logger.isDebugEnabled()) {
			logger.debug("DataBinder restricted to binding allowed fields [" +
					StringUtils.arrayToCommaDelimitedString(allowedFields) + "]");
		}
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
	 * <p>If one of the specified fields is not contained in the list of
	 * incoming property values, a corresponding "missing field" error
	 * will be created, with error code "required" (by the default
	 * binding error processor).
	 * @param requiredFields array of field names
	 * @see #setBindingErrorProcessor
	 * @see DefaultBindingErrorProcessor#MISSING_FIELD_ERROR_CODE
	 */
	public void setRequiredFields(String[] requiredFields) {
		this.requiredFields = requiredFields;
		if (logger.isDebugEnabled()) {
			logger.debug("DataBinder requires binding of required fields [" +
					StringUtils.arrayToCommaDelimitedString(requiredFields) + "]");
		}
	}

	/**
	 * Return the fields that are required for each binding process.
	 * @return array of field names
	 */
	public String[] getRequiredFields() {
		return requiredFields;
	}

	/**
	 * Set whether to extract the old field value when applying a
	 * property editor to a new value for a field.
	 * <p>Default is "true", exposing previous field values to custom editors.
	 * Turn this to "false" to avoid side effects caused by getters.
	 */
	public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
		this.errors.getBeanWrapper().setExtractOldValueForEditor(extractOldValueForEditor);
	}

	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		this.errors.getBeanWrapper().registerCustomEditor(requiredType, propertyEditor);
	}

	public void registerCustomEditor(Class requiredType, String field, PropertyEditor propertyEditor) {
		getBeanWrapper().registerCustomEditor(requiredType, field, propertyEditor);
	}

	public PropertyEditor findCustomEditor(Class requiredType, String propertyPath) {
		return getBeanWrapper().findCustomEditor(requiredType, propertyPath);
	}

	/**
	 * Set the strategy to use for resolving errors into message codes.
	 * Applies the given strategy to the underlying errors holder.
	 * <p>Default is a DefaultMessageCodesResolver.
	 * @see BindException#setMessageCodesResolver
	 * @see DefaultMessageCodesResolver
	 */
	public void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
		this.errors.setMessageCodesResolver(messageCodesResolver);
	}

	/**
	 * Set the strategy to use for processing binding errors, that is,
	 * required field errors and <code>PropertyAccessException</code>s.
	 * <p>Default is a DefaultBindingErrorProcessor.
	 * @see DefaultBindingErrorProcessor
	 */
	public void setBindingErrorProcessor(BindingErrorProcessor bindingErrorProcessor) {
		this.bindingErrorProcessor = bindingErrorProcessor;
	}

	/**
	 * Return the strategy for processing binding errors.
	 */
	public BindingErrorProcessor getBindingErrorProcessor() {
		return bindingErrorProcessor;
	}


	/**
	 * Bind the given property values to this binder's target.
	 * <p>This call can create field errors, representing basic binding
	 * errors like a required field (code "required"), or type mismatch
	 * between value and bean property (code "typeMismatch").
	 * <p>Note that the given PropertyValues should be a throwaway instance:
	 * For efficiency, it will be modified to just contain allowed fields if it
	 * implements the MutablePropertyValues interface; else, an internal mutable
	 * copy will be created for this purpose. Pass in a copy of the PropertyValues
	 * if you want your original instance to stay unmodified in any case.
	 * @param pvs property values to bind
	 * @see #doBind(org.springframework.beans.MutablePropertyValues)
	 */
	public void bind(PropertyValues pvs) {
		MutablePropertyValues mpvs = (pvs instanceof MutablePropertyValues) ?
				(MutablePropertyValues) pvs : new MutablePropertyValues(pvs);
		doBind(mpvs);
	}

	/**
	 * Actual implementation of the binding process, working with the
	 * passed-in MutablePropertyValues instance.
	 * @param mpvs the property values to bind,
	 * as MutablePropertyValues instance
	 * @see #checkAllowedFields
	 * @see #checkRequiredFields
	 * @see #applyPropertyValues
	 */
	protected void doBind(MutablePropertyValues mpvs) {
		checkAllowedFields(mpvs);
		checkRequiredFields(mpvs);
		applyPropertyValues(mpvs);
	}

	/**
	 * Check the given property values against the allowed fields,
	 * removing values for fields that are not allowed.
	 * @param mpvs the property values to be bound (can be modified)
	 * @see #getAllowedFields
	 * @see #isAllowed(String)
	 */
	protected void checkAllowedFields(MutablePropertyValues mpvs) {
		List allowedFieldsList = (getAllowedFields() != null) ? Arrays.asList(getAllowedFields()) : null;
		PropertyValue[] pvArray = mpvs.getPropertyValues();
		for (int i = 0; i < pvArray.length; i++) {
			String field = pvArray[i].getName();
			if (!((allowedFieldsList != null && allowedFieldsList.contains(field)) || isAllowed(field))) {
				mpvs.removePropertyValue(pvArray[i]);
				if (logger.isDebugEnabled()) {
					logger.debug("Field [" + pvArray[i] + "] has been removed from PropertyValues " +
							"and will not be bound, because it has not been found in the list of allowed fields " +
							allowedFieldsList);
				}
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
		if (getAllowedFields() != null) {
			String[] allowedFields = getAllowedFields();
			for (int i = 0; i < allowedFields.length; i++) {
				String allowed = allowedFields[i];
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
	 * Check the given property values against the required fields,
	 * generating missing field errors where appropriate.
	 * @param mpvs the property values to be bound (can be modified)
	 * @see #getRequiredFields
	 * @see #getBindingErrorProcessor
	 * @see BindingErrorProcessor#processMissingFieldError
	 */
	protected void checkRequiredFields(MutablePropertyValues mpvs) {
		if (getRequiredFields() != null) {
			String[] requiredFields = getRequiredFields();
			for (int i = 0; i < requiredFields.length; i++) {
				PropertyValue pv = mpvs.getPropertyValue(requiredFields[i]);
				if (pv == null || pv.getValue() == null ||
						(pv.getValue() instanceof String && !StringUtils.hasText((String) pv.getValue()))) {
					// Use bind error processor to create FieldError.
					String field = requiredFields[i];
					getBindingErrorProcessor().processMissingFieldError(field, getErrors());
					// Remove property from property values to bind:
					// It has already caused a field error with a rejected value.
					mpvs.removePropertyValue(field);
				}
			}
		}
	}

	/**
	 * Apply given property values to the target object.
	 * <p>Default implementation applies them all of them as bean property
	 * values via the corresponding BeanWrapper. Unknown fields will by
	 * default be ignored.
	 * @param mpvs the property values to be bound (can be modified)
	 * @see #getTarget
	 * @see #getBeanWrapper
	 * @see #isIgnoreUnknownFields
	 * @see #getBindingErrorProcessor
	 * @see BindingErrorProcessor#processPropertyAccessException
	 */
	protected void applyPropertyValues(MutablePropertyValues mpvs) {
		try {
			// Bind request parameters onto target object.
			getBeanWrapper().setPropertyValues(mpvs, isIgnoreUnknownFields());
		}
		catch (PropertyAccessExceptionsException ex) {
			// Use bind error processor to create FieldErrors.
			PropertyAccessException[] exs = ex.getPropertyAccessExceptions();
			for (int i = 0; i < exs.length; i++) {
				getBindingErrorProcessor().processPropertyAccessException(exs[i], getErrors());
			}
		}
	}

	/**
	 * Close this DataBinder, which may result in throwing
	 * a BindException if it encountered any errors
	 * @return the model Map, containing target object and Errors instance
	 * @throws BindException if there were any errors in the bind operation
	 * @see BindException#getModel
	 */
	public Map close() throws BindException {
		if (getErrors().hasErrors()) {
			throw getErrors();
		}
		return getErrors().getModel();
	}

}
