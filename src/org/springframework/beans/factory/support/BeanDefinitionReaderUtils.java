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

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;

/**
 * Common utility methods that are useful fo bean definition readers.
 * @author Juergen Hoeller
 * @since 09.07.2004
 * @see PropertiesBeanDefinitionReader
 * @see org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser
 */
public class BeanDefinitionReaderUtils {

	/**
	 * Create a new RootBeanDefinition or ChildBeanDefinition for the given
	 * class name, parent, constructor arguments, and property values.
	 * @param className the name of the bean class, if any
	 * @param parent the name of the parent bean, if any
	 * @param cargs the constructor arguments, if any
	 * @param pvs the property values, if any
	 * @param classLoader the ClassLoader to use for loading bean classes
	 * (can be null to just register bean classes by name)
	 * @return the bean definition
	 * @throws ClassNotFoundException if the bean class could not be loaded
	 */
	public static AbstractBeanDefinition createBeanDefinition(
	    String className, String parent, ConstructorArgumentValues cargs,
	    MutablePropertyValues pvs, ClassLoader classLoader)
	    throws ClassNotFoundException {

		Class beanClass = null;
		if (className != null && classLoader != null) {
			beanClass = Class.forName(className, true, classLoader);
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

}
