/*
 * Copyright 2002-2007 the original author or authors.
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

import org.springframework.beans.FatalBeanException;

/**
 * Exception thrown when a BeanFactory encounters an error when
 * attempting to create a bean from a bean definition.
 *
 * @author Juergen Hoeller
 */
public class BeanCreationException extends FatalBeanException {

	private String beanName;

	private String resourceDescription;


	/**
	 * Create a new BeanCreationException.
	 * @param msg the detail message
	 */
	public BeanCreationException(String msg) {
		super(msg);
	}

	/**
	 * Create a new BeanCreationException.
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanCreationException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * Create a new BeanCreationException.
	 * @param beanName the name of the bean requested
	 * @param msg the detail message
	 */
	public BeanCreationException(String beanName, String msg) {
		this(beanName, msg, (Throwable) null);
	}

	/**
	 * Create a new BeanCreationException.
	 * @param beanName the name of the bean requested
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanCreationException(String beanName, String msg, Throwable cause) {
		super("Error creating bean with name '" + beanName + "': " + msg, cause);
		this.beanName = beanName;
	}

	/**
	 * Create a new BeanCreationException.
	 * @param resourceDescription description of the resource
	 * that the bean definition came from
	 * @param beanName the name of the bean requested
	 * @param msg the detail message
	 */
	public BeanCreationException(String resourceDescription, String beanName, String msg) {
		this(resourceDescription, beanName, msg, null);
	}

	/**
	 * Create a new BeanCreationException.
	 * @param resourceDescription description of the resource
	 * that the bean definition came from
	 * @param beanName the name of the bean requested
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	public BeanCreationException(String resourceDescription, String beanName, String msg, Throwable cause) {
		super("Error creating bean with name '" + beanName + "'" +
				(resourceDescription != null ? " defined in " + resourceDescription : "") +
				": " + msg, cause);
		this.resourceDescription = resourceDescription;
		this.beanName = beanName;
	}


	/**
	 * Return the name of the bean requested, if any.
	 */
	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * Return the description of the resource that the bean
	 * definition came from, if any.
	 */
	public String getResourceDescription() {
		return this.resourceDescription;
	}

}
