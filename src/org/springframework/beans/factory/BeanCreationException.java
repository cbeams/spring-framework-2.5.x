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

import org.springframework.beans.FatalBeanException;

/**
 * Exception thrown when a BeanFactory encounters an error when
 * attempting to create a bean from a bean definition.
 * @author Juergen Hoeller
 */
public class BeanCreationException extends FatalBeanException {

	public BeanCreationException(String msg) {
		super(msg);
	}

	public BeanCreationException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public BeanCreationException(String beanName, String msg) {
		this(beanName, msg, (Throwable) null);
	}

	public BeanCreationException(String beanName, String msg, Throwable ex) {
		super("Error creating bean with name '" + beanName + "': " + msg, ex);
	}

	public BeanCreationException(String resourceDescription, String beanName, String msg) {
		this(resourceDescription, beanName, msg, null);
	}

	public BeanCreationException(String resourceDescription, String beanName, String msg, Throwable ex) {
		super("Error creating bean with name '" + beanName + "' defined in " + resourceDescription + ": " + msg, ex);
	}

}
