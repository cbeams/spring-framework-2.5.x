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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Static convenience methods for JavaBeans, for instantiating beans,
 * copying bean properties, etc.
 *
 * <p>Mainly for use within the framework, but to some degree also
 * useful for application classes.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class BeanUtils {

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
		try {
			return instantiateClass(clazz.getDeclaredConstructor(null), null);
		}
		catch (NoSuchMethodException ex) {
			throw new FatalBeanException("Could not instantiate class [" + clazz.getName() +
					"]: no default constructor found", ex);
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
		try {
			if (!Modifier.isPublic(ctor.getModifiers()) ||
					!Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) {
				ctor.setAccessible(true);
			}
			return ctor.newInstance(args);
		}
		catch (InstantiationException ex) {
			throw new FatalBeanException("Could not instantiate class [" + ctor.getDeclaringClass().getName() +
					"]: Is it an interface or an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Could not instantiate class [" + ctor.getDeclaringClass().getName() +
					"]: Has the class definition changed? Is the constructor accessible?", ex);
		}
		catch (IllegalArgumentException ex) {
			throw new FatalBeanException("Could not instantiate class [" + ctor.getDeclaringClass().getName() +
					"]: illegal args for constructor", ex);
		}
		catch (InvocationTargetException ex) {
			throw new FatalBeanException("Could not instantiate class [" + ctor.getDeclaringClass().getName() +
					"]; constructor threw exception", ex.getTargetException());
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
	 * @return the method object, or null if not found
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
	 * @return the method object, or null if not found
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
	 * @return the method object, or null if not found
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
	 * @return the method object, or null if not found
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
	 * Determine if the given type is assignable from the given value,
	 * assuming setting by reflection. Considers primitive wrapper classes
	 * as assignable to the corresponding primitive types.
	 * <p>For example used in a bean factory's constructor resolution.
	 * @param type the target type
	 * @param value the value that should be assigned to the type
	 * @return if the type is assignable from the value
	 */
	public static boolean isAssignable(Class type, Object value) {
		return (value != null && isAssignable(type, value.getClass()) ||
				(value == null) && !type.isPrimitive());
	}

	/**
	 * Determine if the given target type is assignable from the given value
	 * type, assuming setting by reflection. Considers primitive wrapper
	 * classes as assignable to the corresponding primitive types.
	 * <p>For example used in BeanWrapperImpl's custom editor matrching.
	 * @param targetType the target type
	 * @param valueType the value type that should be assigned to the target type
	 * @return if the target type is assignable from the value type
	 */
	public static boolean isAssignable(Class targetType, Class valueType) {
		return (targetType.isAssignableFrom(valueType)) ||
				(targetType.equals(boolean.class) && valueType.equals(Boolean.class)) ||
		    (targetType.equals(byte.class) && valueType.equals(Byte.class)) ||
		    (targetType.equals(char.class) && valueType.equals(Character.class)) ||
		    (targetType.equals(short.class) && valueType.equals(Short.class)) ||
		    (targetType.equals(int.class) && valueType.equals(Integer.class)) ||
		    (targetType.equals(long.class) && valueType.equals(Long.class)) ||
		    (targetType.equals(float.class) && valueType.equals(Float.class)) ||
		    (targetType.equals(double.class) && valueType.equals(Double.class));
	}

	/**
	 * Check if the given class represents a "simple" property,
	 * i.e. a primitive, a String, a Class, or a corresponding array.
	 * Used to determine properties to check for a "simple" dependency-check.
	 * @see org.springframework.beans.factory.support.RootBeanDefinition#DEPENDENCY_CHECK_SIMPLE
	 * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#dependencyCheck
	 */
	public static boolean isSimpleProperty(Class clazz) {
		return clazz.isPrimitive() || isPrimitiveArray(clazz) || isPrimitiveWrapperArray(clazz) ||
		    clazz.equals(String.class) || clazz.equals(String[].class) ||
		    clazz.equals(Class.class) || clazz.equals(Class[].class);
	}

	/**
	 * Check if the given class represents a primitive array,
	 * i.e. boolean, byte, char, short, int, long, float, or double.
	 */
	public static boolean isPrimitiveArray(Class clazz) {
		return boolean[].class.equals(clazz) || byte[].class.equals(clazz) || char[].class.equals(clazz) ||
		    short[].class.equals(clazz) || int[].class.equals(clazz) || long[].class.equals(clazz) ||
		    float[].class.equals(clazz) || double[].class.equals(clazz);
	}

	/**
	 * Check if the given class represents an array of primitive wrappers,
	 * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
	 */
	public static boolean isPrimitiveWrapperArray(Class clazz) {
		return Boolean[].class.equals(clazz) || Byte[].class.equals(clazz) || Character[].class.equals(clazz) ||
		    Short[].class.equals(clazz) || Integer[].class.equals(clazz) || Long[].class.equals(clazz) ||
		    Float[].class.equals(clazz) || Double[].class.equals(clazz);
	}

	/**
	 * Copy the property values of the given source bean into the target bean.
	 * @param source source bean
	 * @param target target bean
	 * @throws IllegalArgumentException if the classes of source and target do not match
	 */
	public static void copyProperties(Object source, Object target)
	    throws IllegalArgumentException, BeansException {
		copyProperties(source, target, null);
	}

	/**
	 * Copy the property values of the given source bean into the given target bean,
	 * ignoring the given ignoreProperties.
	 * @param source source bean
	 * @param target target bean
	 * @param ignoreProperties array of property names to ignore
	 * @throws IllegalArgumentException if the classes of source and target do not match
	 */
	public static void copyProperties(Object source, Object target, String[] ignoreProperties)
	    throws IllegalArgumentException, BeansException {
		if (source == null || target == null || !source.getClass().isInstance(target)) {
			throw new IllegalArgumentException("Target must be an instance of source");
		}
		List ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;
		BeanWrapper sourceBw = new BeanWrapperImpl(source);
		BeanWrapper targetBw = new BeanWrapperImpl(target);
		MutablePropertyValues values = new MutablePropertyValues();
		for (int i = 0; i < sourceBw.getPropertyDescriptors().length; i++) {
			PropertyDescriptor sourceDesc = sourceBw.getPropertyDescriptors()[i];
			String name = sourceDesc.getName();
			PropertyDescriptor targetDesc = targetBw.getPropertyDescriptor(name);
			if (targetDesc.getWriteMethod() != null && targetDesc.getReadMethod() != null &&
			    (ignoreProperties == null || (!ignoreList.contains(name)))) {
				values.addPropertyValue(new PropertyValue(name, sourceBw.getPropertyValue(name)));
			}
		}
		targetBw.setPropertyValues(values);
	}

}
