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

package org.springframework.beans.factory.config;

import java.lang.reflect.Field;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * FactoryBean which retrieves a static or non-static field value.
 * Typically used for retrieving public static final constants.
 * @author Juergen Hoeller
 * @since 31.07.2004
 * @see #setStaticField
 */
public class FieldRetrievingFactoryBean implements FactoryBean, InitializingBean {

	private Class targetClass;

	private Object targetObject;

	private String targetField;

	// the field we will retrieve
	private Field fieldObject;


	/**
	 * Set the target class on which the field is defined.
	 * Only necessary when the target field is static; else,
	 * a target object needs to be specified anyway.
	 * @see #setTargetObject
	 * @see #setTargetField
	 */
	public void setTargetClass(Class targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * Return the target class on which the field is defined.
	 */
	public Class getTargetClass() {
		return targetClass;
	}

	/**
	 * Set the target object on which the field is defined.
	 * Only necessary when the target field is not static;
	 * else, a target class is sufficient.
	 * @see #setTargetClass
	 * @see #setTargetField
	 */
	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
	}

	/**
	 * Return the target object on which the field is defined.
	 */
	public Object getTargetObject() {
		return targetObject;
	}

	/**
	 * Set the name of the field to be retrieved.
	 * Refers to either a static field or a non-static field,
	 * depending on a target object being set.
	 * @see #setTargetClass
	 * @see #setTargetObject
	 */
	public void setTargetField(String targetField) {
		this.targetField = targetField;
	}

	/**
	 * Return the name of the field to be retrieved.
	 */
	public String getTargetField() {
		return targetField;
	}

	/**
	 * Set a fully qualified static field name to retrieve,
	 * e.g. "example.MyExampleClass.MY_EXAMPLE_FIELD".
	 * Convenient alternative to specifying targetClass and targetField.
	 * @see #setTargetClass
	 * @see #setTargetField
	 */
	public void setStaticField(String staticField) throws ClassNotFoundException {
		int lastDotIndex = staticField.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == staticField.length()) {
			throw new IllegalArgumentException("staticField must be a fully qualified class plus method name: " +
																				 "e.g. 'example.MyExampleClass.MY_EXAMPLE_FIELD'");
		}
		String className = staticField.substring(0, lastDotIndex);
		String fieldName = staticField.substring(lastDotIndex + 1);
		setTargetClass(Class.forName(className, true, Thread.currentThread().getContextClassLoader()));
		setTargetField(fieldName);
	}


	public void afterPropertiesSet() throws NoSuchFieldException {
		if (this.targetClass == null && this.targetObject == null) {
			throw new IllegalArgumentException("Either targetClass or targetObject is required");
		}
		if (this.targetField == null) {
			throw new IllegalArgumentException("targetField is required");
		}

		// try to get the exact method first
		Class targetClass = (this.targetObject != null) ? this.targetObject.getClass() : this.targetClass;
		this.fieldObject = targetClass.getField(this.targetField);
	}


	public Object getObject() throws IllegalAccessException {
		if (this.targetObject != null){
			return this.fieldObject.get(this.targetObject);
		}
		else{
			return this.fieldObject.get(null);
		}
	}

	public Class getObjectType() {
		return this.fieldObject.getType();
	}

	public boolean isSingleton() {
		return true;
	}

}
