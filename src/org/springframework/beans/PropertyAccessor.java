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

import java.util.Map;

/**
 * Common interface for classes that can access bean properties.
 * Serves as base interface for BeanWrapper.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see BeanWrapper
 */
public interface PropertyAccessor {

	/**
	 * Path separator for nested properties.
	 * Follows normal Java conventions: getFoo().getBar() would be "foo.bar".
	 */
	String NESTED_PROPERTY_SEPARATOR = ".";
	char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

	/**
	 * Marker that indicates the start of a property key for an
	 * indexed or mapped property like "person.addresses[0]".
	 */
	String PROPERTY_KEY_PREFIX = "[";
	char PROPERTY_KEY_PREFIX_CHAR = '[';

	/**
	 * Marker that indicates the end of a property key for an
	 * indexed or mapped property like "person.addresses[0]".
	 */
	String PROPERTY_KEY_SUFFIX = "]";
	char PROPERTY_KEY_SUFFIX_CHAR = ']';


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
	 * if not determinable
	 */
	Class getPropertyType(String propertyName) throws BeansException;

	/**
	 * Get the value of a property.
	 * @param propertyName name of the property to get the value of
	 * @return the value of the property
	 * @throws FatalBeanException if there is no such property, if the property
	 * isn't readable, or if the property getter throws an exception.
	 */
	Object getPropertyValue(String propertyName) throws BeansException;

	/**
	 * Set a property value.
	 * @param propertyName name of the property to set value of
	 * @param value the new value
	 * @throws FatalBeanException if there is no such property, if the property
	 * isn't writable, or if the property setter throws an exception.
	 */
	void setPropertyValue(String propertyName, Object value) throws BeansException;

	/**
	 * Set a property value.
	 * @param pv object containing the new property value
	 * @throws FatalBeanException if there is no such property, if the property
	 * isn't writable, or if the property setter throws an exception.
	 */
	void setPropertyValue(PropertyValue pv) throws BeansException;

	/**
	 * Perform a bulk update from a Map.
	 * <p>Bulk updates from PropertyValues are more powerful: This method is
	 * provided for convenience. Behaviour will be identical to that of
	 * the setPropertyValues(PropertyValues) method.
	 * @param map Map to take properties from. Contains property value objects,
	 * keyed by property name
	 * @throws FatalBeanException if there is no such property, if the property
	 * isn't writable, or if the property setter throws an exception.
	 */
	void setPropertyValues(Map map) throws BeansException;

	/**
	 * The preferred way to perform a bulk update.
	 * <p>Note that performing a bulk update differs from performing a single update,
	 * in that an implementation of this class will continue to update properties
	 * if a <b>recoverable</b> error (such as a type mismatch, but <b>not</b> an
	 * invalid field name or the like) is encountered, throwing a
	 * PropertyAccessExceptionsException containing all the individual errors.
	 * This exception can be examined later to see all binding errors.
	 * Properties that were successfully updated stay changed.
	 * <p>Does not allow unknown fields or invalid fields.
	 * @param pvs PropertyValues to set on the target object
	 * @throws FatalBeanException if there is no such property, if the property
	 * isn't writable, or if the property setter throws an exception.
	 * @see #setPropertyValues(PropertyValues, boolean, boolean)
	 */
	void setPropertyValues(PropertyValues pvs) throws BeansException;

	/**
	 * Perform a bulk update with more control over behavior.
	 * <p>Note that performing a bulk update differs from performing a single update,
	 * in that an implementation of this class will continue to update properties
	 * if a <b>recoverable</b> error (such as a type mismatch, but <b>not</b> an
	 * invalid field name or the like) is encountered, throwing a
	 * PropertyAccessExceptionsException containing all the individual errors.
	 * This exception can be examined later to see all binding errors.
	 * Properties that were successfully updated stay changed.
	 * @param pvs PropertyValues to set on the target object
	 * @param ignoreUnknown should we ignore unknown properties (not found in the bean)
	 * @throws FatalBeanException if there is no such property, if the property
	 * isn't writable, or if the property setter throws an exception.
	 * @see #setPropertyValues(PropertyValues, boolean, boolean)
	 */
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown)
	    throws BeansException;

	/**
	 * Perform a bulk update with full control over behavior.
	 * <p>Note that performing a bulk update differs from performing a single update,
	 * in that an implementation of this class will continue to update properties
	 * if a <b>recoverable</b> error (such as a type mismatch, but <b>not</b> an
	 * invalid field name or the like) is encountered, throwing a
	 * PropertyAccessExceptionsException containing all the individual errors.
	 * This exception can be examined later to see all binding errors.
	 * Properties that were successfully updated stay changed.
	 * @param pvs PropertyValues to set on the target object
	 * @param ignoreUnknown should we ignore unknown properties (not found in the bean)
	 * @param ignoreInvalid should we ignore invalid properties (found but not accessible)
	 * @throws FatalBeanException if there is no such property, if the property
	 * isn't writable, or if the property setter throws an exception.
	 */
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
	    throws BeansException;

}
