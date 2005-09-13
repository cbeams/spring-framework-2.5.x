/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Utility methods that are useful for bean definition readers implementations.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1
 * @see PropertiesBeanDefinitionReader
 * @see org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser
 */
public class BeanDefinitionReaderUtils {

	/**
	 * Separator for generated bean names. If a class name or parent name is not
	 * unique, "#1", "#2" etc will be appended, until the name becomes unique.
	 */
	public static final String GENERATED_BEAN_NAME_SEPARATOR = "#";


	/**
	 * Create a new RootBeanDefinition or ChildBeanDefinition for the given
	 * class name, parent, constructor arguments, and property values.
	 * @param className the name of the bean class, if any
	 * @param parent the name of the parent bean, if any
	 * @param cargs the constructor arguments, if any
	 * @param pvs the property values, if any
	 * @param classLoader the ClassLoader to use for loading bean classes
	 * (can be <code>null</code> to just register bean classes by name)
	 * @return the bean definition
	 * @throws ClassNotFoundException if the bean class could not be loaded
	 */
	public static AbstractBeanDefinition createBeanDefinition(
			String className, String parent, ConstructorArgumentValues cargs,
			MutablePropertyValues pvs, ClassLoader classLoader)
			throws ClassNotFoundException {

		Class beanClass = null;
		if (className != null && classLoader != null) {
			beanClass = ClassUtils.forName(className, classLoader);
		}

		if (parent == null) {
			if (beanClass != null) {
				return new RootBeanDefinition(beanClass, cargs, pvs);
			}
			else {
				return new RootBeanDefinition(className, cargs, pvs);
			}
		}
		else {
			if (beanClass != null) {
				return new ChildBeanDefinition(parent, beanClass, cargs, pvs);
			}
			else {
				return new ChildBeanDefinition(parent, className, cargs, pvs);
			}
		}
	}

	/**
	 * Generate a bean name for the given bean definition, unique within the
	 * given bean factory.
	 * @param beanDefinition the bean definition to generate a bean name for
	 * @param beanFactory the bean factory that the definition is going to be
	 * registered with (to check for existing bean names)
	 * @param isInnerBean whether the given bean definition will be registered
	 * as inner bean or as top-level bean (allowing for special name generation
	 * for inner beans vs. top-level beans)
	 * @return the bean name to use
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 * for the given bean definition
	 */
	public static String generateBeanName(
			AbstractBeanDefinition beanDefinition, BeanDefinitionRegistry beanFactory, boolean isInnerBean)
			throws BeanDefinitionStoreException {

		String generatedId = beanDefinition.getBeanClassName();
		if (generatedId == null) {
			if (beanDefinition instanceof ChildBeanDefinition) {
				generatedId = ((ChildBeanDefinition) beanDefinition).getParentName() + "$child";
			}
			else if (beanDefinition.getFactoryBeanName() != null) {
				generatedId = beanDefinition.getFactoryBeanName() + "$created";
			}
		}
		if (!StringUtils.hasText(generatedId)) {
			throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), "",
					"Unnamed bean definition specifies neither 'class' nor 'parent' nor 'factory-bean'" +
					" - can't generate bean name");
		}

		String id = generatedId;
		if (isInnerBean) {
			// Inner bean: generate identity hashcode suffix.
			id = generatedId + GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(beanDefinition);
		}
		else {
			// Top-level bean: use plain class name. If not already unique,
			// add counter - increasing the counter until the name is unique.
			int counter = 0;
			while (beanFactory.containsBeanDefinition(id)) {
				counter++;
				id = generatedId + GENERATED_BEAN_NAME_SEPARATOR + counter;
			}
		}
		return id;
	}

	/**
	 * Register the given bean definition with the given bean factory.
	 * @param bdHolder the bean definition including name and aliases
	 * @param beanFactory the bean factory to register with
	 * @throws BeansException if registration failed
	 */
	public static void registerBeanDefinition(
			BeanDefinitionHolder bdHolder, BeanDefinitionRegistry beanFactory) throws BeansException {

		// Register bean definition under primary name.
		beanFactory.registerBeanDefinition(bdHolder.getBeanName(), bdHolder.getBeanDefinition());

		// Register aliases for bean name, if any.
		if (bdHolder.getAliases() != null) {
			for (int i = 0; i < bdHolder.getAliases().length; i++) {
				beanFactory.registerAlias(bdHolder.getBeanName(), bdHolder.getAliases()[i]);
			}
		}
	}

}
