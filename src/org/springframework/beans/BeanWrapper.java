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

package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;

/**
 * The central interface of Spring's low-level JavaBeans infrastructure;
 * the default implementation is BeanWrapperImpl. Typically not directly
 * used by application code but rather implicitly via a BeanFactory or
 * a DataBinder.
 *
 * <p>To be implemented by classes that can manipulate Java beans.
 * Implementing classes have the ability to get and set property values
 * (individually or in bulk), get property descriptors and query the
 * readability and writability of properties.
 *
 * <p>This interface supports <b>nested properties</b> enabling the setting
 * of properties on subproperties to an unlimited depth.
 *
 * <p>If a property update causes an exception, a PropertyVetoException will be
 * thrown. Bulk updates continue after exceptions are encountered, throwing an
 * exception wrapping <b>all</b> exceptions encountered during the update.
 *
 * <p>BeanWrapper implementations can be used repeatedly, with their "target"
 * or wrapped object changed.
 * 
 * @author Rod Johnson
 * @since 13 April 2001
 * @see BeanWrapperImpl
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.validation.DataBinder
 */
public interface BeanWrapper extends PropertyAccessor {

	/**
	 * Change the wrapped object. Implementations are required
	 * to allow the type of the wrapped object to change.
	 * @param obj wrapped object that we are manipulating
	 */
	void setWrappedInstance(Object obj);

	/**
	 * Return the bean wrapped by this object (cannot be null).
	 * @return the bean wrapped by this object
	 */
	Object getWrappedInstance();

	/**
	 * Convenience method to return the class of the wrapped object.
	 * @return the class of the wrapped object
	 */
	Class getWrappedClass();

	/**
	 * Register the given custom property editor for all properties
	 * of the given type.
	 * @param requiredType type of the property
	 * @param propertyEditor editor to register
	 */
	void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor);

	/**
	 * Register the given custom property editor for the given type and
	 * property, or for all properties of the given type.
	 * <p>If the property path denotes an array or Collection property,
	 * the editor will get applied either to the array/Collection itself
	 * (the PropertyEditor has to create an array or Collection value) or
	 * to each element (the PropertyEditor has to create the element type),
	 * depending on the specified required type.
	 * <p>Note: Only one single registered custom editor per property path
	 * is supported. In case of a Collection/array, do not register an editor
	 * for both the Collection/array and each element on the same property.
	 * @param requiredType type of the property (can be null if a property is
	 * given but should be specified in any case for consistency checking)
	 * @param propertyPath path of the property (name or nested path), or
	 * null if registering an editor for all properties of the given type
	 * @param propertyEditor editor to register
	 */
	void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor);

	/**
	 * Find a custom property editor for the given type and property.
	 * @param requiredType type of the property (can be null if a property is
	 * given but should be specified in any case for consistency checking)
	 * @param propertyPath path of the property (name or nested path), or
	 * null if looking for an editor for all properties of the given type
	 * @return the registered editor, or null if none
	 */
	PropertyEditor findCustomEditor(Class requiredType, String propertyPath);


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
	 * Determine the property type for a particular property, either checking
	 * the property descriptor or checking the value in case of an indexed or
	 * mapped element.
	 * @param propertyName property to check status for
	 * @return the property type for the particular property, or null if not
	 * determinable (can only happen with an indexed or mapped element)
	 * @throws InvalidPropertyException if there is no such property
	 */
	Class getPropertyType(String propertyName) throws BeansException;

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

}
