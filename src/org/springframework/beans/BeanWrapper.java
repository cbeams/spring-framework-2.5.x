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

package org.springframework.beans;

import java.beans.PropertyDescriptor;

/**
 * The central interface of Spring's low-level JavaBeans infrastructure.
 * Extends the PropertyAccessor and PropertyEditorRegistry interfaces.
 *
 * <p>The default implementation is BeanWrapperImpl. Typically not used
 * directly but rather implicitly via a BeanFactory or a DataBinder.
 *
 * <p>Provides operations to analyze and manipulate standard Java Beans:
 * the ability to get and set property values (individually or in bulk),
 * get property descriptors, query the readability/writability of properties.
 *
 * <p>This interface supports <b>nested properties</b> enabling the setting
 * of properties on subproperties to an unlimited depth.
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
	 * @param obj wrapped object that we are manipulating
	 */
	void setWrappedInstance(Object obj);

	/**
	 * Return the bean wrapped by this object (cannot be <code>null</code>).
	 * @return the bean wrapped by this object
	 */
	Object getWrappedInstance();

	/**
	 * Convenience method to return the class of the wrapped object.
	 * @return the class of the wrapped object
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
	 * Get the PropertyDescriptors identified on this object
	 * (standard JavaBeans introspection).
	 * @return the PropertyDescriptors identified on this object
	 */
	PropertyDescriptor[] getPropertyDescriptors() throws BeansException;

	/**
	 * Get the property descriptor for a particular property.
	 * @param propertyName property to check status for
	 * @return the property descriptor for the particular property
	 * @throws InvalidPropertyException if there is no such property
	 */
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException;

	/**
	 * Return whether this property is readable.
	 * Returns false if the property doesn't exist.
	 * @param propertyName property to check status for
	 * @return whether this property is readable
	 */
	boolean isReadableProperty(String propertyName) throws BeansException;

	/**
	 * Return whether this property is writable.
	 * Returns false if the property doesn't exist.
	 * @param propertyName property to check status for
	 * @return whether this property is writable
	 */
	boolean isWritableProperty(String propertyName) throws BeansException;

	/**
	 * Determine the property type for a particular property, either checking
	 * the property descriptor or checking the value in case of an indexed or
	 * mapped element.
	 * @param propertyName property to check status for
	 * @return the property type for the particular property, or <code>null</code>
	 * if not determinable (can only happen with an indexed or mapped element)
	 * @throws InvalidPropertyException if there is no such property
	 */
	Class getPropertyType(String propertyName) throws BeansException;

}
