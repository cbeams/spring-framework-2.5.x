package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Static convenience methods for JavaBeans.
 * Provides e.g. methods for sorting lists of beans by any property.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class BeanUtils {

	/**
	 * Convenience method to instantiate a class using its
	 * no arg constructor. As this method doesn't try to load
	 * classes by name, it should avoid class-loading issues.
	 * @param clazz class to instantiate.
	 * @return the new instance
	 */
	public static Object instantiateClass(Class clazz) throws BeansException {
		try {
			//Object bean = Beans.instantiate(null, className);
			return clazz.newInstance();
		}
		catch (InstantiationException ex) {
			throw new FatalBeanException("Cannot instantiate [" + clazz + "]; is it an interface or an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Cannot instantiate [" + clazz + "]; has class definition changed? Is there a public constructor?", ex);
		}
	}

	/**
	 * Return all interfaces that the given object implements,
	 * including ones implemented by superclasses.
	 * @param object the object to analyse for interfaces
	 * @return all interfaces that the given object implements
	 */
	public static Class[] getAllInterfaces(Object object) {
		List interfaces = new ArrayList();
		Class clazz = object.getClass();
		while (clazz != null) {
			for (int i = 0; i < clazz.getInterfaces().length; i++) {
				Class ifc = clazz.getInterfaces()[i];
				interfaces.add(ifc);
			}
			clazz = clazz.getSuperclass();
		}
		return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
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
