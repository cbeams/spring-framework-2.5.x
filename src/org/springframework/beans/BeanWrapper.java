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

package org.springframework.beans;

import java.beans.PropertyDescriptor;

/**
 * The central interface of Spring's low-level JavaBeans infrastructure.
 *
 * <p>Typically not used directly but rather implicitly via a
 * {@link org.springframework.beans.factory.BeanFactory} or a
 * {@link org.springframework.validation.DataBinder}.
 *
 * <p>Provides operations to analyze and manipulate standard Java Beans:
 * the ability to get and set property values (individually or in bulk),
 * get property descriptors, and query the readability/writability of properties.
 *
 * <p>This interface supports <b>nested properties</b> enabling the setting
 * of properties on subproperties to an unlimited depth.
 * A <code>BeanWrapper</code> instance can be used repeatedly, with its
 * {@link #setWrappedInstance(Object) target object} (the wrapped Java Bean
 * instance) changing as required.
 *
 * <p>A BeanWrapper's default for the "extractOldValueForEditor" setting
 * is "false", to avoid side effects caused by getter method invocations.
 * Turn this to "true" to expose present property values to custom editors.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @see #setExtractOldValueForEditor
 * @see PropertyAccessor
 * @see PropertyEditorRegistry
 * @see BeanWrapperImpl
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.validation.BeanPropertyBindingResult
 * @see org.springframework.validation.DataBinder#initBeanPropertyAccess()
 */
public interface BeanWrapper extends ConfigurablePropertyAccessor, TypeConverter {

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

}
