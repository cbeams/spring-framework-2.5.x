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

package org.springframework.beans.factory.serviceloader;

import java.util.ServiceLoader;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;

/**
 * Abstract base class for FactoryBeans operating on the
 * JDK 1.6 {@link java.util.ServiceLoader} facility.
 *
 * @author Juergen Hoeller
 * @since 2.1
 * @see java.util.ServiceLoader
 */
public abstract class AbstractServiceLoaderBasedFactoryBean extends AbstractFactoryBean
		implements BeanClassLoaderAware {

	private Class serviceClass;

	private ClassLoader beanClassLoader;


	/**
	 * Specify the desired service class.
	 */
	public void setServiceClass(Class serviceClass) {
		this.serviceClass = serviceClass;
	}

	/**
	 * Return the desired service class.
	 */
	public Class getServiceClass() {
		return this.serviceClass;
	}

	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}


	/**
	 * Delegates to {@link #getObjectToExpose(java.util.ServiceLoader)}.
	 * @return the object to expose
	 */
	protected Object createInstance() {
		Assert.notNull(getServiceClass(), "Property 'serviceClass' is required");
		return getObjectToExpose(ServiceLoader.load(getServiceClass(), this.beanClassLoader));
	}

	/**
	 * Determine the actual object to expose for the given ServiceLoader.
	 * <p>Left to concrete subclasses.
	 * @param serviceLoader the ServiceLoader for the configured service class
	 * @return the object to expose
	 */
	protected abstract Object getObjectToExpose(ServiceLoader serviceLoader);

}
