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

/**
 * Abstract base class for bean definition readers.
 * Provides common properties like the bean factory to work on
 * and the class loader to use for loading bean classes. 
 * @author Juergen Hoeller
 * @since 11.12.2003
 */
public abstract class AbstractBeanDefinitionReader {

	private BeanDefinitionRegistry beanFactory;

	private ClassLoader beanClassLoader = Thread.currentThread().getContextClassLoader();

	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Return the BeanFactory that this reader works on.
	 */
	public BeanDefinitionRegistry getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Set the class loader to use for bean classes.
	 * Default is the thread context class loader.
	 * <p>Setting this to null suggests to not load bean classes but just register
	 * bean definitions with class names, for example when just registering beans
	 * in a registry but not actually instantiating them in a factory.
	 * @see java.lang.Thread#getContextClassLoader
	 */
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	/**
	 * Return the class loader for bean classes.
	 */
	public ClassLoader getBeanClassLoader() {
		return beanClassLoader;
	}

}
