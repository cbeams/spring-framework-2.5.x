/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * Static convenience methods for JavaBeans.
 * Provides e.g. methods for sorting lists of beans by any property.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: BeanUtils.java,v 1.9 2003-11-23 22:42:05 jhoeller Exp $
 */
public abstract class BeanUtils {

	/**
	 * Convenience method to instantiate a class using its no-arg constructor.
	 * As this method doesn't try to load classes by name, it should avoid class-loading issues.
	 * @param clazz class to instantiate
	 * @return the new instance
	 */
	public static Object instantiateClass(Class clazz) throws BeansException {
		try {
			//Object bean = Beans.instantiate(null, className);
			return clazz.newInstance();
		}
		catch (InstantiationException ex) {
			throw new FatalBeanException("Could not instantiate class [" + clazz.getName() + "]; Is it an interface or an abstract class? Does it have a no-arg constructor?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Could not instantiate class [" + clazz.getName() + "]; has class definition changed? Is there a public no-arg constructor?", ex);
		}
	}

	/**
	 * Convenience method to instantiate a class using the given constructor.
	 * As this method doesn't try to load classes by name, it should avoid class-loading issues.
	 * @param constructor constructor to instantiate
	 * @return the new instance
	 */
	public static Object instantiateClass(Constructor constructor, Object[] arguments) throws BeansException {
		try {
			return constructor.newInstance(arguments);
		}
		catch (IllegalArgumentException ex) {
			throw new FatalBeanException("Illegal arguments when trying to instantiate constructor: " + constructor, ex);
		}
		catch (InstantiationException ex) {
			throw new FatalBeanException("Could not instantiate class [" + constructor.getDeclaringClass().getName() + "]; is it an interface or an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Could not instantiate class [" + constructor.getDeclaringClass().getName() + "]; has class definition changed? Is there a public constructor?", ex);
		}
		catch (InvocationTargetException ex) {
			throw new FatalBeanException("Could not instantiate class [" + constructor.getDeclaringClass().getName() + "]; constructor threw exception", ex.getTargetException());
		}
	}

	/**
	 * Check if the given class represents a primitive array.
	 */
	public static boolean isPrimitiveArray(Class clazz) {
		return boolean[].class.equals(clazz) || byte[].class.equals(clazz) || char[].class.equals(clazz) ||
		    short[].class.equals(clazz) || int[].class.equals(clazz) || long[].class.equals(clazz) ||
		    float[].class.equals(clazz) || double[].class.equals(clazz);
	}

	/**
	 * Check if the given class represents a "simple" property,
	 * i.e. a primitive, a String, a Class, or a corresponding array.
	 */
	public static boolean isSimpleProperty(Class clazz) {
		return clazz.isPrimitive() || isPrimitiveArray(clazz) ||
		    clazz.equals(String.class) || clazz.equals(String[].class) ||
		    clazz.equals(Class.class) || clazz.equals(Class[].class);
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
			throw new IllegalArgumentException("Target must an instance of source");
		}
		List ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;
		BeanWrapper sourceBw = new BeanWrapperImpl(source);
		BeanWrapper targetBw = new BeanWrapperImpl(target);
		MutablePropertyValues values = new MutablePropertyValues();
		for (int i = 0; i < sourceBw.getPropertyDescriptors().length; i++) {
			PropertyDescriptor sourceDesc = sourceBw.getPropertyDescriptors()[i];
			String name = sourceDesc.getName();
			PropertyDescriptor targetDesc = targetBw.getPropertyDescriptor(name);
			if (targetDesc.getWriteMethod() != null &&
			    (ignoreProperties == null || (!ignoreList.contains(name)))) {
				values.addPropertyValue(new PropertyValue(name, sourceBw.getPropertyValue(name)));
			}
		}
		targetBw.setPropertyValues(values);
	}

}
