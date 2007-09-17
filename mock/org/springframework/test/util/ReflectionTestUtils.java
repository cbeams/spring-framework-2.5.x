/*
 * Copyright 2007 the original author or authors.
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

package org.springframework.test.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * <p>
 * ReflectionTestUtils is a collection of reflection-based utility methods for
 * use in unit and integration testing scenarios.
 * </p>
 * <p>
 * There are often situations in which it would be beneficial to be able to set
 * a non-<code>public</code> field or invoke a non-<code>public</code>
 * setter method when testing code involving, for example:
 * </p>
 * <ul>
 * <li>ORM frameworks such as JPA and Hibernate which condone the usage of
 * <code>private</code> or <code>protected</code> field access as opposed to
 * <code>public</code> setter methods for properties in a domain entity.</li>
 * <li>Spring's support for annotations such as
 * {@link org.springframework.beans.factory.annotation.Autowired @Autowired} and
 * {@link javax.annotation.Resource @Resource} which provides dependency
 * injection for <code>private</code> or <code>protected</code> fields,
 * setter methods, and configuration methods.</li>
 * </ul>
 *
 * @author Sam Brannen
 * @since 2.5
 * @see ReflectionUtils
 */
public class ReflectionTestUtils {

	/** Class Logger. */
	private static final Log logger = LogFactory.getLog(ReflectionTestUtils.class);


	/**
	 * <p>
	 * Sets the {@link Field field} with the given <code>name</code> on the
	 * provided {@link Object target object} to the supplied <code>value</code>.
	 * </p>
	 * <p>
	 * This method traverses the class hierarchy in search of the desired field.
	 * In addition, an attempt will be made to make non-<code>public</code>
	 * fields <em>accessible</em>, thus allowing one to set
	 * <code>protected</code>, <code>private</code>, and
	 * <em>package-private</em> fields.
	 * </p>
	 *
	 * @param target The target object on which to set the field.
	 * @param name The name of the field to set.
	 * @param value The value to set; may be <code>null</code> unless the
	 *        field type is a primitive type.
	 * @param type The type of the field.
	 * @throws IllegalArgumentException if the target object or field type is
	 *         <code>null</code>; if the field name is <em>empty</em>; or
	 *         if the field could not be found on the target object.
	 * @throws Exception Allows all other exceptions to propagate.
	 * @see ReflectionUtils#findField(Class, String, Class)
	 * @see ReflectionUtils#makeAccessible(Field)
	 * @see ReflectionUtils#setField(Field, Object, Object)
	 */
	public static final void setField(final Object target, final String name, final Object value, final Class type)
			throws Exception {

		Assert.notNull(target, "The target object supplied to setField() can not be null.");
		Assert.hasText(name, "The field name supplied to setField() can not be empty.");
		Assert.notNull(type, "The field type supplied to setField() can not be null.");

		if (logger.isDebugEnabled()) {
			logger.debug("Setting field [" + name + "] on target [" + target + "] with value [" + value
					+ "] and target type [" + type + "].");
		}

		final Field field = ReflectionUtils.findField(target.getClass(), name, type);
		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + name + "] on target [" + target
					+ "] with type [" + type + "].");
		}

		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, target, value);
	}

	/**
	 * <p>
	 * Invokes the {@link Method setter method} with the given <code>name</code>
	 * on the supplied {@link Object target object} with the supplied
	 * <code>value</code>.
	 * </p>
	 * <p>
	 * This method traverses the class hierarchy in search of the desired
	 * method. In addition, an attempt will be made to make non-<code>public</code>
	 * methods <em>accessible</em>, thus allowing one to invoke
	 * <code>protected</code>, <code>private</code>, and
	 * <em>package-private</em> setter methods.
	 * </p>
	 * <p>
	 * In addition, this method supports JavaBean-style <em>property</em>
	 * names. For example, if you wish to set the <code>name</code> property
	 * on the target object, you may pass either &quot;name&quot; or
	 * &quot;setName&quot; as the method name.
	 * </p>
	 *
	 * @param target The target object on which to invoke the specified setter
	 *        method.
	 * @param name The name of the setter method to invoke or the corresponding
	 *        property name.
	 * @param value The value to provide to the setter method; may be
	 *        <code>null</code> unless the parameter type is a primitive type.
	 * @param type The formal parameter type declared by the setter method.
	 * @throws IllegalArgumentException if the target object or parameter type
	 *         is <code>null</code>; if the name is <em>empty</em>; or if
	 *         the method could not be found on the target object.
	 * @throws Exception Allows all other exceptions to propagate.
	 * @see ReflectionUtils#findMethod(Class, String, Class[])
	 * @see ReflectionUtils#makeAccessible(Method)
	 * @see ReflectionUtils#invokeMethod(Method, Object, Object[])
	 */
	public static final void invokeSetterMethod(final Object target, final String name, final Object value,
			final Class type) throws Exception {

		Assert.notNull(target, "The target object supplied to invokeSetterMethod() can not be null.");
		Assert.hasText(name, "The method name supplied to invokeSetterMethod() can not be empty.");
		Assert.notNull(type, "The parameter type supplied to invokeSetterMethod() can not be null.");

		String setterMethodName = name;
		if (!setterMethodName.startsWith("set")) {
			setterMethodName = "set" + StringUtils.capitalize(setterMethodName);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking setter method [" + setterMethodName + "] on target [" + target + "] with value ["
					+ value + "] and parameter type [" + type + "].");
		}

		Method method = ReflectionUtils.findMethod(target.getClass(), setterMethodName, new Class[] { type });
		if (method == null) {
			throw new IllegalArgumentException("Could not find setter method [" + setterMethodName + "] on target ["
					+ target + "] with parameter type [" + type + "].");
		}

		ReflectionUtils.makeAccessible(method);
		ReflectionUtils.invokeMethod(method, target, new Object[] { value });
	}

}
