/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans;

import java.beans.PropertyDescriptor;

/**
 * The central interface of Spring's low-level JavaBeans infrastructure.
 * Extends the {@link PropertyAccessor} and {@link PropertyEditorRegistry} interfaces.
 *
 * <p>Typically not used directly but rather implicitly via a
 * {@link org.springframework.beans.factory.BeanFactory} or a
 * {@link org.springframework.validation.DataBinder}.
 *
 * <p>Provides operations to analyze and manipulate standard JavaBeans:
 * the ability to get and set property values (individually or in bulk),
 * get property descriptors, and query the readability/writability of properties.
 *
 * <p>This interface supports <b>nested properties</b> enabling the setting
 * of properties on subproperties to an unlimited depth.
 * A <code>BeanWrapper</code> instance can be used repeatedly, with its
 * {@link #setWrappedInstance(Object) target object} (the wrapped JavaBean
 * instance) changing as required.
 *
 * <p>A BeanWrapper instance can be used repeatedly, with its target object
 * (the wrapped Java Bean instance) changing.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @see PropertyAccessor
 * @see PropertyEditorRegistry
 * @see BeanWrapperImpl
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.validation.DataBinder
 */
public interface BeanWrapper extends PropertyAccessor, PropertyEditorRegistry {

	/**
	 * Change the wrapped object. Implementations are required
	 * to allow the type of the wrapped object to change.
	 * @param obj the wrapped object that we are manipulating
	 */
	void setWrappedInstance(Object obj);

	/**
	 * Return the bean instance wrapped by this object, if any.
	 * @return the bean instance, or <code>null</code> if none set
	 */
	Object getWrappedInstance();

	/**
	 * Return the class of the wrapped object.
	 * @return the class of the wrapped bean instance,
	 * or <code>null</code> if no wrapped object has been set
	 */
	Class getWrappedClass();

	/**
	 * Set whether to extract the old property value when applying a
	 * property editor to a new value for a property.
	 * <p>Default is "false", avoiding side effects caused by getters.
	 * Turn this to "true" to expose previous property values to custom editors.
	 */
	void setExtractOldValueForEditor(boolean extractOldValueForEditor);

	/**
	 * Return whether to extract the old property value when applying a
	 * property editor to a new value for a property.
	 */
	boolean isExtractOldValueForEditor();


	/**
	 * Obtain the PropertyDescriptors for the wrapped object
	 * (as determined by standard JavaBeans introspection).
	 * @return the PropertyDescriptors for the wrapped object
	 * @throws BeansException if case of introspection failure
	 */
	PropertyDescriptor[] getPropertyDescriptors() throws BeansException;

	/**
	 * Obtain the property descriptor for a particular property
	 * of the wrapped object.
	 * @param propertyName the property to obtain the descriptor for
	 * @return the property descriptor for the particular property
	 * @throws InvalidPropertyException if there is no such property
	 * @throws BeansException if case of introspection failure
	 */
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException;

	/**
	 * Determine the property type for the specified property,
	 * either checking the property descriptor or checking the value
	 * in case of an indexed or mapped element.
	 * @param propertyName property to check status for
	 * @return the property type for the particular property,
	 * or <code>null</code> if not determinable
	 * @throws InvalidPropertyException if there is no such property or
	 * if the property isn't readable
	 */
	Class getPropertyType(String propertyName) throws BeansException;

	/**
	 * Determine whether the specified property is readable.
	 * <p>Returns <code>false</code> if the property doesn't exist.
	 * @param propertyName property to check status for
	 * @return whether the property is readable
	 */
	boolean isReadableProperty(String propertyName);

	/**
	 * Determine whether the specified property is writable.
	 * <p>Returns <code>false</code> if the property doesn't exist.
	 * @param propertyName property to check status for
	 * @return whether the property is writable
	 */
	boolean isWritableProperty(String propertyName);

}
