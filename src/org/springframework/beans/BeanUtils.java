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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Static convenience methods for JavaBeans: for instantiating beans,
 * checking bean property types, copying bean properties, etc.
 *
 * <p>Mainly for use within the framework, but to some degree also
 * useful for application classes.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 */
public abstract class BeanUtils {

	/**
	 * Map with primitive wrapper type as key and corresponding primitive
	 * type as value, for example: Integer.class -> int.class
	 */
	private static final Map primitiveWrapperTypeMap = new HashMap(8);

	static {
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);
	}


	/**
	 * Determine if the given type is assignable from the given value,
	 * assuming setting by reflection. Considers primitive wrapper classes
	 * as assignable to the corresponding primitive types.
	 * <p>For example used in a bean factory's constructor resolution.
	 * @param type the target type
	 * @param value the value that should be assigned to the type
	 * @return if the type is assignable from the value
	 */
	public static boolean isAssignable(Class type, Object value) {
		Assert.notNull(type, "type must not be null");
		return (value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive());
	}

	/**
	 * Determine if the given target type is assignable from the given value
	 * type, assuming setting by reflection. Considers primitive wrapper
	 * classes as assignable to the corresponding primitive types.
	 * <p>For example used in BeanWrapperImpl's custom editor matching.
	 * @param targetType the target type
	 * @param valueType the value type that should be assigned to the target type
	 * @return if the target type is assignable from the value type
	 */
	public static boolean isAssignable(Class targetType, Class valueType) {
		Assert.notNull(targetType, "targetType must not be null");
		Assert.notNull(valueType, "valueType must not be null");
		return (targetType.isAssignableFrom(valueType) ||
				targetType.equals(primitiveWrapperTypeMap.get(valueType)));
	}

	/**
	 * Check if the given class represents a "simple" property,
	 * i.e. a primitive, a String, a Class, or a corresponding array.
	 * Used to determine properties to check for a "simple" dependency-check.
	 * @see org.springframework.beans.factory.support.RootBeanDefinition#DEPENDENCY_CHECK_SIMPLE
	 * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#checkDependencies
	 */
	public static boolean isSimpleProperty(Class clazz) {
		Assert.notNull(clazz, "clazz must not be null");
		return clazz.isPrimitive() || isPrimitiveArray(clazz) ||
				isPrimitiveWrapper(clazz) || isPrimitiveWrapperArray(clazz) ||
				clazz.equals(String.class) || clazz.equals(String[].class) ||
				clazz.equals(Class.class) || clazz.equals(Class[].class);
	}

	/**
	 * Check if the given class represents an array of primitives,
	 * i.e. boolean, byte, char, short, int, long, float, or double.
	 */
	public static boolean isPrimitiveArray(Class clazz) {
		return (clazz.isArray() && clazz.getComponentType().isPrimitive());
	}

	/**
	 * Check if the given class represents a primitive wrapper,
	 * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
	 */
	public static boolean isPrimitiveWrapper(Class clazz) {
		return primitiveWrapperTypeMap.containsKey(clazz);
	}

	/**
	 * Check if the given class represents an array of primitive wrappers,
	 * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
	 */
	public static boolean isPrimitiveWrapperArray(Class clazz) {
		return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
	}


	/**
	 * Convenience method to instantiate a class using its no-arg constructor.
	 * As this method doesn't try to load classes by name, it should avoid
	 * class-loading issues.
	 * <p>Note that this method tries to set the constructor accessible
	 * if given a non-accessible (i.e. non-public) constructor.
	 * @param clazz class to instantiate
	 * @return the new instance
	 */
	public static Object instantiateClass(Class clazz) throws BeansException {
		Assert.notNull(clazz, "clazz must not be null");
		if (clazz.isInterface()) {
			throw new FatalBeanException(
					"Class [" + clazz.getName() + "] cannot be instantiated: it is an interface");
		}
		try {
			return instantiateClass(clazz.getDeclaredConstructor((Class[]) null), null);
		}
		catch (NoSuchMethodException ex) {
			throw new BeanInstantiationException(clazz, "No default constructor found", ex);
		}
	}

	/**
	 * Convenience method to instantiate a class using the given constructor.
	 * As this method doesn't try to load classes by name, it should avoid
	 * class-loading issues.
	 * <p>Note that this method tries to set the constructor accessible
	 * if given a non-accessible (i.e. non-public) constructor.
	 * @param ctor constructor to instantiate
	 * @return the new instance
	 */
	public static Object instantiateClass(Constructor ctor, Object[] args) throws BeansException {
		Assert.notNull(ctor, "Constructor must not be null");
		try {
			if (!Modifier.isPublic(ctor.getModifiers()) ||
					!Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) {
				ctor.setAccessible(true);
			}
			return ctor.newInstance(args);
		}
		catch (InstantiationException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(),
					"Is it an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(),
					"Has the class definition changed? Is the constructor accessible?", ex);
		}
		catch (IllegalArgumentException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(),
					"Illegal arguments for constructor", ex);
		}
		catch (InvocationTargetException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(),
					"Constructor threw exception", ex.getTargetException());
		}
	}

	/**
	 * Find a method with the given method name and the given parameter types,
	 * declared on the given class or one of its superclasses. Prefers public methods,
	 * but will return a protected, package access, or private method too.
	 * <p>Checks <code>Class.getMethod</code> first, falling back to
	 * <code>findDeclaredMethod</code>. This allows to find public methods
	 * without issues even in environments with restricted Java security settings.
	 * @param clazz the class to check
	 * @param methodName the name of the method to find
	 * @param paramTypes the parameter types of the method to find
	 * @return the Method object, or <code>null</code> if not found
	 * @see java.lang.Class#getMethod
	 * @see #findDeclaredMethod
	 */
	public static Method findMethod(Class clazz, String methodName, Class[] paramTypes) {
		try {
			return clazz.getMethod(methodName, paramTypes);
		}
		catch (NoSuchMethodException ex) {
			return findDeclaredMethod(clazz, methodName, paramTypes);
		}
	}

	/**
	 * Find a method with the given method name and the given parameter types,
	 * declared on the given class or one of its superclasses. Will return a public,
	 * protected, package access, or private method.
	 * <p>Checks <code>Class.getDeclaredMethod</code>, cascading upwards to all superclasses.
	 * @param clazz the class to check
	 * @param methodName the name of the method to find
	 * @param paramTypes the parameter types of the method to find
	 * @return the Method object, or <code>null</code> if not found
	 * @see java.lang.Class#getDeclaredMethod
	 */
	public static Method findDeclaredMethod(Class clazz, String methodName, Class[] paramTypes) {
		try {
			return clazz.getDeclaredMethod(methodName, paramTypes);
		}
		catch (NoSuchMethodException ex) {
			if (clazz.getSuperclass() != null) {
				return findDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
			}
			return null;
		}
	}

	/**
	 * Find a method with the given method name and minimal parameters (best case: none),
	 * declared on the given class or one of its superclasses. Prefers public methods,
	 * but will return a protected, package access, or private method too.
	 * <p>Checks <code>Class.getMethods</code> first, falling back to
	 * <code>findDeclaredMethodWithMinimalParameters</code>. This allows to find public
	 * methods without issues even in environments with restricted Java security settings.
	 * @param clazz the class to check
	 * @param methodName the name of the method to find
	 * @return the Method object, or <code>null</code> if not found
	 * @see java.lang.Class#getMethods
	 * @see #findDeclaredMethodWithMinimalParameters
	 */
	public static Method findMethodWithMinimalParameters(Class clazz, String methodName) {
		Method[] methods = clazz.getMethods();
		Method targetMethod = null;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName)) {
				if (targetMethod == null ||
						methods[i].getParameterTypes().length < targetMethod.getParameterTypes().length) {
					targetMethod = methods[i];
				}
			}
		}
		if (targetMethod != null) {
			return targetMethod;
		}
		else {
			return findDeclaredMethodWithMinimalParameters(clazz, methodName);
		}
	}

	/**
	 * Find a method with the given method name and minimal parameters (best case: none),
	 * declared on the given class or one of its superclasses. Will return a public,
	 * protected, package access, or private method.
	 * <p>Checks <code>Class.getDeclaredMethods</code>, cascading upwards to all superclasses.
	 * @param clazz the class to check
	 * @param methodName the name of the method to find
	 * @return the method object, or <code>null</code> if not found
	 * @see java.lang.Class#getDeclaredMethods
	 */
	public static Method findDeclaredMethodWithMinimalParameters(Class clazz, String methodName) {
		Method[] methods = clazz.getDeclaredMethods();
		Method targetMethod = null;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(methodName)) {
				if (targetMethod == null ||
						methods[i].getParameterTypes().length < targetMethod.getParameterTypes().length) {
					targetMethod = methods[i];
				}
			}
		}
		if (targetMethod != null) {
			return targetMethod;
		}
		else {
			if (clazz.getSuperclass() != null) {
				return findDeclaredMethodWithMinimalParameters(clazz.getSuperclass(), methodName);
			}
			return null;
		}
	}


	/**
	 * Retrieve the JavaBeans <code>PropertyDescriptor</code>s of a given class.
	 * @param clazz the Class to retrieve the PropertyDescriptors for
	 * @return an array of <code>PropertyDescriptors</code> for the given class
	 * @throws BeansException if PropertyDescriptor look fails
	 */
	public static PropertyDescriptor[] getPropertyDescriptors(Class clazz) throws BeansException {
		CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
		return cr.getBeanInfo().getPropertyDescriptors();
	}

	/**
	 * Retrieve the JavaBeans <code>PropertyDescriptors</code> for the given property.
	 * @param clazz the Class to retrieve the PropertyDescriptor for
	 * @param propertyName the name of the property
	 * @return the corresponding PropertyDescriptor, or <code>null</code> if none
	 * @throws BeansException if PropertyDescriptor lookup fails
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class clazz, String propertyName)
			throws BeansException {

		CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
		return cr.getPropertyDescriptor(propertyName);
	}

	/**
	 * Find a JavaBeans <code>PropertyDescriptor</code> for the given method,
	 * with the method either being the read method or the write method for
	 * that bean property.
	 * @param method the method to find a corresponding PropertyDescriptor for
	 * @return the corresponding PropertyDescriptor, or <code>null</code> if none
	 * @throws BeansException if PropertyDescriptor lookup fails
	 */
	public static PropertyDescriptor findPropertyForMethod(Method method) throws BeansException {
		Assert.notNull(method, "method must not be null");
		PropertyDescriptor[] pds = getPropertyDescriptors(method.getDeclaringClass());
		for (int i = 0; i < pds.length; i++) {
			if (method.equals(pds[i].getReadMethod()) || method.equals(pds[i].getWriteMethod())) {
				return pds[i];
			}
		}
		return null;
	}

	/**
	 * Determine the canonical name for the given property path.
	 * Removes surrounding quotes from map keys:<br>
	 * <code>map['key']</code> -> <code>map[key]</code><br>
	 * <code>map["key"]</code> -> <code>map[key]</code>
	 * @param propertyName the bean property path
	 * @return the canonical representation of the property path
	 */
	public static String canonicalName(String propertyName) {
		if (propertyName == null) {
			return "";
		}

		// The following code does not use JDK 1.4's StringBuffer.indexOf(String)
		// method to retain JDK 1.3 compatibility. The slight loss in performance
		// is not really relevant, as this code will typically just run on startup.

		StringBuffer buf = new StringBuffer(propertyName);
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = buf.toString().indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = buf.toString().indexOf(
						PropertyAccessor.PROPERTY_KEY_SUFFIX, keyStart + PropertyAccessor.PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					String key = buf.substring(keyStart + PropertyAccessor.PROPERTY_KEY_PREFIX.length(), keyEnd);
					if ((key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) {
						buf.delete(keyStart + 1, keyStart + 2);
						buf.delete(keyEnd - 2, keyEnd - 1);
						keyEnd = keyEnd - 2;
					}
					searchIndex = keyEnd + PropertyAccessor.PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		return buf.toString();
	}


	/**
	 * Copy the property values of the given source bean into the target bean.
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>This is just a convenience method. For more complex transfer needs,
	 * consider using a full BeanWrapper.
	 * @param source the source bean
	 * @param target the target bean
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	public static void copyProperties(Object source, Object target) throws BeansException {
		copyProperties(source, target, null, null);
	}

	/**
	 * Copy the property values of the given source bean into the given target bean,
	 * only setting properties defined in the given "editable" class (or interface).
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>This is just a convenience method. For more complex transfer needs,
	 * consider using a full BeanWrapper.
	 * @param source the source bean
	 * @param target the target bean
	 * @param editable the class (or interface) to restrict property setting to
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	public static void copyProperties(Object source, Object target, Class editable)
			throws BeansException {

		copyProperties(source, target, editable, null);
	}

	/**
	 * Copy the property values of the given source bean into the given target bean,
	 * ignoring the given "ignoreProperties".
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>This is just a convenience method. For more complex transfer needs,
	 * consider using a full BeanWrapper.
	 * @param source the source bean
	 * @param target the target bean
	 * @param ignoreProperties array of property names to ignore
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	public static void copyProperties(Object source, Object target, String[] ignoreProperties)
			throws BeansException {

		copyProperties(source, target, null, ignoreProperties);
	}

	/**
	 * Copy the property values of the given source bean into the given target bean.
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * @param source the source bean
	 * @param target the target bean
	 * @param editable the class (or interface) to restrict property setting to
	 * @param ignoreProperties array of property names to ignore
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	private static void copyProperties(Object source, Object target, Class editable, String[] ignoreProperties)
			throws BeansException {

		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");

		Class actualEditable = target.getClass();
		if (editable != null) {
			if (!editable.isInstance(target)) {
				throw new IllegalArgumentException("Target class [" + target.getClass().getName() +
						"] not assignable to Editable class [" + editable.getName() + "]");
			}
			actualEditable = editable;
		}
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		List ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;

		for (int i = 0; i < targetPds.length; i++) {
			PropertyDescriptor targetPd = targetPds[i];
			if (targetPd.getWriteMethod() != null &&
					(ignoreProperties == null || (!ignoreList.contains(targetPd.getName())))) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					try {
						Method readMethod = sourcePd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
							readMethod.setAccessible(true);
						}
						Object value = readMethod.invoke(source, new Object[0]);
						Method writeMethod = targetPd.getWriteMethod();
						if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
							writeMethod.setAccessible(true);
						}
						writeMethod.invoke(target, new Object[] {value});
					}
					catch (Exception ex) {
						throw new FatalBeanException("Could not copy properties from source to target", ex);
					}
				}
			}
		}
	}

}
