/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;

/**
 * Default object instantiation strategy for use
 * in BeanFactories. Does not support Method Injection, although
 * it provides hooks for subclasses to override to add Method Injection 
 * support, for example by overriding methods.
 * 
 * @author Rod Johnson
 * @version $Id: DefaultInstantiationStrategy.java,v 1.1 2004-06-23 21:11:18 johnsonr Exp $
 */
public class DefaultInstantiationStrategy implements InstantiationStrategy {

	public final Object instantiate(RootBeanDefinition rbd, BeanFactory owner) {
		// Don't override the class with CGLIB if no overrides
		if (rbd.getMethodOverrides().isEmpty()) {
			return BeanUtils.instantiateClass(rbd.getBeanClass());
		}
		else {
			// Must generate CGLIB subclass
			return instantiateWithMethodInjection(rbd, owner);
		}
	}
	
	/**
	 * Subclasses can override this method, which is implemented to throw UnsupportedOperationException,
	 * if they can instantiate an object with the Method Injection specified in the given
	 * RootBeanDefinition. Instantiation should use a no-arg constructor.
	 */
	protected Object instantiateWithMethodInjection(RootBeanDefinition rbd, BeanFactory owner) {
		throw new UnsupportedOperationException("Method Injection not suppored in DefaultInstantiationStrategy");
	}
	

	public final Object instantiate(RootBeanDefinition rbd, BeanFactory owner, Constructor ctor, Object[] args) {
		if (rbd.getMethodOverrides().isEmpty()) {
			return BeanUtils.instantiateClass(ctor, args);
		}
		else {
			return instantiateWithMethodInjection(rbd, owner, ctor, args);
		}
	}
	
	/**
	 * Subclasses can override this method, which is implemented to throw UnsupportedOperationException,
	 * if they can instantiate an object with the Method Injection specified in the given
	 * RootBeanDefinition. Instantiation should use the given constructor and parameters.
	 */
	protected Object instantiateWithMethodInjection(RootBeanDefinition rbd, BeanFactory owner, Constructor ctor, Object[] args) {
		throw new UnsupportedOperationException("Method Injection not suppored in DefaultInstantiationStrategy");
	}
	
	public final Object instantiate(RootBeanDefinition rbd, BeanFactory owner, Method factoryMethod, Object[] args) {
		try {
			// Must be a static method
			return factoryMethod.invoke(null, args);
		}
		catch (IllegalArgumentException ex) {
			throw new BeanDefinitionStoreException("Illegal arguments to factory method " + factoryMethod + "; " +
					"args=" + StringUtils.arrayToCommaDelimitedString(args));
		}
		catch (IllegalAccessException ex) {
			throw new BeanDefinitionStoreException("Cannot access factory method " + factoryMethod + "; is it public?");
		}
		catch (InvocationTargetException ex) {
			throw new BeanDefinitionStoreException("Factory method " + factoryMethod + " threw exception", ex.getTargetException());
		}
	}
	
}
