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
import org.springframework.core.io.Resource;

/**
 * Exception thrown when a BeanFactory encounters an internal error, and
 * its definitions are invalid: for example, if an XML document containing
 * bean definitions isn't well-formed.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class BeanDefinitionStoreException extends FatalBeanException {

	public BeanDefinitionStoreException(String msg) {
		super(msg);
	}

	public BeanDefinitionStoreException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public BeanDefinitionStoreException(Resource documentLocation, String beanName, String msg) {
		this(documentLocation.getDescription(), beanName, msg, null);
	}

	public BeanDefinitionStoreException(Resource documentLocation, String beanName, String msg, Throwable ex) {
		this(documentLocation.getDescription(), beanName, msg, ex);
	}

	public BeanDefinitionStoreException(String resourceDescription, String beanName, String msg) {
		this(resourceDescription, beanName, msg, null);
	}

	public BeanDefinitionStoreException(String resourceDescription, String beanName, String msg, Throwable ex) {
		super("Error registering bean with name '" + beanName + "' defined in " + resourceDescription + ": " + msg, ex);
	}

}
