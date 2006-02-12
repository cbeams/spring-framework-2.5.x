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

import java.beans.PropertyEditor;
import java.io.Serializable;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;

/**
 * Default implementation of the Errors interface, supporting
 * registration and evaluation of binding errors on JavaBean objects.
 *
 * <p>Normally, application code will work with the Errors interface
 * or the BindingResult interface. A DataBinder returns its BindingResult
 * via <code>getBindingResult()</code>.
 *
 * <p>Supports exporting a model, suitable for example for web MVC.
 * Thus, it is sometimes used as parameter type instead of the Errors interface
 * itself - if extracting the model makes sense in the particular context.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see DataBinder#getBindingResult()
 */
public class BeanBindingResult extends AbstractBindingResult implements Serializable {

	private final Object target;

	private transient BeanWrapper beanWrapper;


	/**
	 * Create a new BeanBindingResult instance.
	 * @param target the target bean to bind onto
	 * @param objectName the name of the target object
	 */
	public BeanBindingResult(Object target, String objectName) {
		super(objectName);
		Assert.notNull(target, "Target bean must not be null");
		this.target = target;
	}

	/**
	 * Return the BeanWrapper that this instance uses.
	 * Creates a new one if none existed before.
	 */
	protected BeanWrapper getBeanWrapper() {
		if (this.beanWrapper == null) {
			this.beanWrapper = new BeanWrapperImpl(this.target);
		}
		return this.beanWrapper;
	}

	/**
	 * Retrieve the custom PropertyEditor for the given field, if any.
	 * @param field the field name
	 * @return the custom PropertyEditor, or <code>null</code>
	 */
	public PropertyEditor getCustomEditor(String field) {
		String fixedField = fixedField(field);
		Class type = getBeanWrapper().getPropertyType(fixedField);
		return getBeanWrapper().findCustomEditor(type, fixedField);
	}


	public Object getTarget() {
		return this.target;
	}

	protected String canonicalFieldName(String field) {
		return BeanUtils.canonicalName(field);
	}

	protected Class getFieldType(String field) {
		return getBeanWrapper().getPropertyType(field);
	}

	protected Object getActualFieldValue(String field) {
		return getBeanWrapper().getPropertyValue(field);
	}

	protected Object formatFieldValue(String field, Object value) {
		PropertyEditor customEditor = getCustomEditor(field);
		if (customEditor != null) {
			customEditor.setValue(value);
			String textValue = customEditor.getAsText();
			// If the PropertyEditor returned null, there is no appropriate
			// text representation for this value: only use it if non-null.
			if (textValue != null) {
				return textValue;
			}
		}
		return value;
	}

}
