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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

	private List errors = new ArrayList();

	private BeanWrapper beanWrapper;

	private String objectName;

	private String nestedPath = "";

	/**
	 * Create a new BindException instance.
	 * @param target target object to bind onto
	 * @param name name of the target object
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
	 * Transform the given field into its full path,
	 * regarding the nested path of this instance.
	 */
	private String fixedField(String field) {
		return this.nestedPath + field;
	}

	/**
	 * Add a FieldError to the errors list.
	 * Intended to be used by subclasses like DataBinder.
	 */
	protected void addFieldError(FieldError fe) {
		this.errors.add(fe);
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

	public void reject(String errorCode, String defaultMessage) {
		reject(errorCode, null, defaultMessage);
	}

	public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
		this.errors.add(new ObjectError(this.objectName, errorCode, errorArgs, defaultMessage));
	}

	public void rejectValue(String field, String errorCode, String defaultMessage) {
		rejectValue(field, errorCode, null, defaultMessage);
	}

	public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
		field = fixedField(field);
		Object newVal = getBeanWrapper().getPropertyValue(field);
		FieldError fe = new FieldError(this.objectName, field, newVal, false, errorCode, errorArgs, defaultMessage);
		this.errors.add(fe);
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
		List result = new ArrayList();
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			ObjectError fe = (ObjectError) it.next();
			if (!(fe instanceof FieldError)) {
				result.add(fe);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public ObjectError getGlobalError() {
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			ObjectError fe = (ObjectError) it.next();
			if (!(fe instanceof FieldError)) {
				return fe;
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
		List result = new ArrayList();
		field = fixedField(field);
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			ObjectError fe = (ObjectError) it.next();
			if (fe instanceof FieldError && field.equals(((FieldError) fe).getField())) {
				result.add(fe);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public FieldError getFieldError(String field) {
		field = fixedField(field);
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			ObjectError fe = (ObjectError) it.next();
			if (fe instanceof FieldError && field.equals(((FieldError) fe).getField())) {
				return (FieldError) fe;
			}
		}
		return null;
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

	public PropertyEditor getCustomEditor(String field) {
		field = fixedField(field);
		FieldError fe = getFieldError(field);
		return (fe == null ? getBeanWrapper().findCustomEditor(null, field) : null);
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
