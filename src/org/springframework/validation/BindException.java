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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Default implementation of the Errors interface, supporting
 * registration and evaluation of binding errors.
 * Slightly unusual, as it <i>is</i> an exception.
 *
 * <p>This is mainly a framework-internal class. Normally, application
 * code will work with the Errors interface, or a DataBinder that in
 * turn exposes a BindException via <code>getErrors()</code>.
 *
 * <p>Supports exporting a model, suitable for example for web MVC.
 * Thus, it is sometimes used as parameter type instead of the Errors interface
 * itself - if extracting the model makes sense in the respective context.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getModel
 * @see DataBinder#getErrors
 */
public class BindException extends Exception implements Errors {

	/**
	 * Prefix for the name of the Errors instance in a model,
	 * followed by the object name.
	 */
	public static final String ERROR_KEY_PREFIX = BindException.class.getName() + ".";

	private final List errors = new LinkedList();

	private final BeanWrapper beanWrapper;

	private final String objectName;

	private MessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();

	private String nestedPath = "";


	/**
	 * Create a new BindException instance.
	 * @param target target object to bind onto
	 * @param name name of the target object
	 * @see DefaultMessageCodesResolver
	 */
	public BindException(Object target, String name) {
		this.beanWrapper = new BeanWrapperImpl(target);
		this.objectName = name;
		this.nestedPath = "";
	}

	/**
	 * Return the BeanWrapper that this instance uses.
	 */
	protected BeanWrapper getBeanWrapper() {
		return beanWrapper;
	}

	/**
	 * Return the wrapped target object.
	 */
	public Object getTarget() {
		return this.beanWrapper.getWrappedInstance();
	}

	public String getObjectName() {
		return objectName;
	}

	/**
	 * Set the strategy to use for resolving errors into message codes.
	 * Default is DefaultMessageCodesResolver.
	 * @see DefaultMessageCodesResolver
	 */
	public void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
		this.messageCodesResolver = messageCodesResolver;
	}

	/**
	 * Return the strategy to use for resolving errors into message codes.
	 */
	public MessageCodesResolver getMessageCodesResolver() {
		return messageCodesResolver;
	}

	public void setNestedPath(String nestedPath) {
		if (nestedPath == null) {
			nestedPath = "";
		}
		if (nestedPath.length() > 0 && !nestedPath.endsWith(".")) {
			nestedPath += ".";
		}
		this.nestedPath = nestedPath;
	}

	public String getNestedPath() {
		return nestedPath;
	}

	/**
	 * Transform the given field into its full path,
	 * regarding the nested path of this instance.
	 */
	protected String fixedField(String field) {
		return this.nestedPath + field;
	}


	public void reject(String errorCode, String defaultMessage) {
		reject(errorCode, null, defaultMessage);
	}

	public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
		addError(new ObjectError(this.objectName, resolveMessageCodes(errorCode), errorArgs, defaultMessage));
	}

	public void rejectValue(String field, String errorCode, String defaultMessage) {
		rejectValue(field, errorCode, null, defaultMessage);
	}

	public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
		String fixedField = fixedField(field);
		Object newVal = getBeanWrapper().getPropertyValue(fixedField);
		FieldError fe = new FieldError(this.objectName, fixedField, newVal, false,
																	 resolveMessageCodes(errorCode, field), errorArgs, defaultMessage);
		addError(fe);
	}

	protected String[] resolveMessageCodes(String errorCode) {
		return this.messageCodesResolver.resolveMessageCodes(errorCode, this.objectName);
	}

	protected String[] resolveMessageCodes(String errorCode, String field) {
		String fixedField = fixedField(field);
		Class fieldType = this.beanWrapper.getPropertyType(fixedField);
		return this.messageCodesResolver.resolveMessageCodes(errorCode, this.objectName, fixedField, fieldType);
	}

	/**
	 * Add a FieldError to the errors list.
	 * Intended to be used by subclasses like DataBinder.
	 */
	protected void addError(ObjectError error) {
		this.errors.add(error);
	}


	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	public int getErrorCount() {
		return this.errors.size();
	}

	public List getAllErrors() {
		return Collections.unmodifiableList(this.errors);
	}

	public boolean hasGlobalErrors() {
		return (getGlobalErrorCount() > 0);
	}

	public int getGlobalErrorCount() {
		return getGlobalErrors().size();
	}

	public List getGlobalErrors() {
		List result = new LinkedList();
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			Object error = it.next();
			if (!(error instanceof FieldError)) {
				result.add(error);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public ObjectError getGlobalError() {
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			ObjectError objectError = (ObjectError) it.next();
			if (!(objectError instanceof FieldError)) {
				return objectError;
			}
		}
		return null;
	}

	public boolean hasFieldErrors(String field) {
		return (getFieldErrorCount(field) > 0);
	}

	public int getFieldErrorCount(String field) {
		return getFieldErrors(field).size();
	}

	public List getFieldErrors(String field) {
		List result = new LinkedList();
		field = fixedField(field);
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			Object error = it.next();
			if (error instanceof FieldError && isMatchingFieldError(field, ((FieldError) error))) {
				result.add(error);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public FieldError getFieldError(String field) {
		field = fixedField(field);
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			Object error = it.next();
			if (error instanceof FieldError) {
				FieldError fe = (FieldError) error;
				if (isMatchingFieldError(field, fe)) {
					return fe;
				}
			}
		}
		return null;
	}

	/**
	 * Check whether the given FieldError matches the given field.
	 * @param field the field that we are looking up FieldErrors for
	 * @param fieldError the candidate FieldError
	 * @return whether the FieldError matches the given field
	 */
	protected boolean isMatchingFieldError(String field, FieldError fieldError) {
		return (field.equals(fieldError.getField()) ||
				(field.endsWith("*") && fieldError.getField().startsWith(field.substring(0, field.length() - 1))));
	}

	public Object getFieldValue(String field) {
		field = fixedField(field);
		FieldError fe = getFieldError(field);
		// use rejected value in case of error, current bean property value else
		Object value = (fe != null) ? fe.getRejectedValue() : getBeanWrapper().getPropertyValue(field);
		// apply custom editor, but not on binding failures like type mismatches
		if (value != null && (fe == null || !fe.isBindingFailure())) {
			PropertyEditor customEditor = getBeanWrapper().findCustomEditor(null, field);
			if (customEditor != null) {
				customEditor.setValue(value);
				return customEditor.getAsText();
			}
		}
		return value;
	}

	/**
	 * Retrieve the custom PropertyEditor for the given field, if any.
	 * @param field the field name
	 * @return the custom PropertyEditor, or null
	 */
	public PropertyEditor getCustomEditor(String field) {
		field = fixedField(field);
		FieldError fe = getFieldError(field);
		return (fe == null ? getBeanWrapper().findCustomEditor(null, field) : null);
	}


	/**
	 * Return a model Map for the obtained state, exposing an Errors
	 * instance as '{@link #ERROR_KEY_PREFIX ERROR_KEY_PREFIX} + objectName'
	 * and the object itself.
	 * <p>Note that the Map is constructed each time you're calling this method,
	 * adding things to the map and then re-calling it will not do...
	 * @see #getObjectName
	 * @see #ERROR_KEY_PREFIX
	 */
	public final Map getModel() {
		Map model = new HashMap();
		// errors instance, even if no errors
		model.put(ERROR_KEY_PREFIX + this.objectName, this);
		// mapping from name to target object
		model.put(this.objectName, this.beanWrapper.getWrappedInstance());
		return model;
	}

	/**
	 * Returns diagnostic information about the errors held in this object.
	 */
	public String getMessage() {
		StringBuffer sb = new StringBuffer("BindException: " + getErrorCount() + " errors");
		Iterator it = this.errors.iterator();
		while (it.hasNext()) {
			sb.append("; " + it.next());
		}
		return sb.toString();
	}

}
