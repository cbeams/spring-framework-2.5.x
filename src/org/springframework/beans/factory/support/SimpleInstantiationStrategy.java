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

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;

/**
 * Simple object instantiation strategy for use in BeanFactories.
 *
 * <p>Does not support Method Injection, although it provides hooks for subclasses
 * to override to add Method Injection  support, for example by overriding methods.
 * 
 * @author Rod Johnson
 * @since 1.1
 */
public class SimpleInstantiationStrategy implements InstantiationStrategy {
	
	protected final Log logger = LogFactory.getLog(getClass());

	public Object instantiate(
			RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) {
		// don't override the class with CGLIB if no overrides
		if (beanDefinition.getMethodOverrides().isEmpty()) {
			return BeanUtils.instantiateClass(beanDefinition.getBeanClass());
		}
		else {
			// must generate CGLIB subclass
			return instantiateWithMethodInjection(beanDefinition, beanName, owner);
		}
	}
	
	/**
	 * Subclasses can override this method, which is implemented to throw
	 * UnsupportedOperationException, if they can instantiate an object with
	 * the Method Injection specified in the given RootBeanDefinition.
	 * Instantiation should use a no-arg constructor.
	 */
	protected Object instantiateWithMethodInjection(
			RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) {
		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}

	public Object instantiate(
			RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
			Constructor ctor, Object[] args) {
		if (beanDefinition.getMethodOverrides().isEmpty()) {
			return BeanUtils.instantiateClass(ctor, args);
		}
		else {
			return instantiateWithMethodInjection(beanDefinition, beanName, owner, ctor, args);
		}
	}
	
	/**
	 * Subclasses can override this method, which is implemented to throw
	 * UnsupportedOperationException, if they can instantiate an object with
	 * the Method Injection specified in the given RootBeanDefinition.
	 * Instantiation should use the given constructor and parameters.
	 */
	protected Object instantiateWithMethodInjection(
			RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
			Constructor ctor, Object[] args) {
		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}

	public Object instantiate(
			RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
			Method factoryMethod, Object[] args) {

		Object target = null;
		if (beanDefinition.getFactoryBeanName() != null) {
			target = owner.getBean(beanDefinition.getFactoryBeanName());
		}
		
		try {
			// It's a static method if the target is null.
			return factoryMethod.invoke(target, args);
		}
		catch (IllegalArgumentException ex) {
			throw new BeanDefinitionStoreException("Illegal arguments to factory method " + factoryMethod + "; " +
					"args=" + StringUtils.arrayToCommaDelimitedString(args));
		}
		catch (IllegalAccessException ex) {
			throw new BeanDefinitionStoreException(
					"Cannot access factory method " + factoryMethod + "; is it public?");
		}
		catch (InvocationTargetException ex) {
			String msg = "Factory method " + factoryMethod + " threw exception";
			// We want to log this one, as it may be a config error:
			// the method may match, but may have been given incorrect arguments.
			logger.warn(msg, ex.getTargetException());
			throw new BeanDefinitionStoreException(msg, ex.getTargetException());
		}
	}
	
}
