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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * Exception thrown when a BeanFactory is asked for a bean
 * instance name for which it cannot find a definition.
 * @author Rod Johnson
 */
public class NoSuchBeanDefinitionException extends BeansException {

	/** Name of the missing bean */
	private String beanName;

	/** Required bean type */
	private Class beanType;

	/**
	 * Create new <code>NoSuchBeanDefinitionException</code>.
	 * @param name the name of the missing bean
	 * @param message further, detailed message describing the problem
	 */
	public NoSuchBeanDefinitionException(String name, String message) {
		super("No bean named '" + name + "' is defined: " + message);
		this.beanName = name;
	}

	/**
	 * Create new <code>NoSuchBeanDefinitionException</code>.
	 * @param type required type of bean
	 * @param message further, detailed message describing the problem
	 */
	public NoSuchBeanDefinitionException(Class type, String message) {
		super("No unique bean of type [" + type.getName() + "] is defined: " + message);
		this.beanType = type;
	}

	/**
	 * Return the name of the missing bean,
	 * if it was a lookup by name that failed.
	 */
	public String getBeanName() {
		return beanName;
	}

	/**
	 * Return the required type of bean,
	 * if it was a lookup by type that failed.
	 */
	public Class getBeanType() {
		return beanType;
	}

}
