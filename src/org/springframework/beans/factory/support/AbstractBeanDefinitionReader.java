/*
 * Copyright 2002-2006 the original author or authors.
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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Abstract base class for bean definition readers which implement
 * the BeanDefinitionReader interface.
 *
 * <p>Provides common properties like the bean factory to work on
 * and the class loader to use for loading bean classes.
 *
 * @author Juergen Hoeller
 * @since 11.12.2003
 * @see BeanDefinitionReaderUtils
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {

	protected final Log logger = LogFactory.getLog(getClass());

	private final BeanDefinitionRegistry beanFactory;

	private ResourceLoader resourceLoader;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	/**
	 * Create a new AbstractBeanDefinitionReader for the given bean factory.
	 * <p>If the passed-in bean factory does not only implement the
	 * BeanDefinitionRegistry interface but also the ResourceLoader interface,
	 * it will be used as default ResourceLoader as well. This will usually
	 * be the case for ApplicationContext implementations.
	 * <p>If given a plain BeanDefinitionRegistry, the default ResourceLoader
	 * will be a PathMatchingResourcePatternResolver, also capable of resource
	 * pattern resolving through the ResourcePatternResolver interface.
	 * @param beanFactory the bean factory to work on
	 * @see #setResourceLoader
	 * @see org.springframework.context.ApplicationContext
	 * @see org.springframework.core.io.ResourceLoader
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 */
	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		Assert.notNull(beanFactory, "Bean factory must not be null");
		this.beanFactory = beanFactory;

		// Determine ResourceLoader to use.
		if (this.beanFactory instanceof ResourceLoader) {
			this.resourceLoader = (ResourceLoader) this.beanFactory;
		}
		else {
			this.resourceLoader = new PathMatchingResourcePatternResolver();
		}
	}

	public BeanDefinitionRegistry getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Set the ResourceLoader to use for resource locations.
	 * If specifying a ResourcePatternResolver, the bean definition reader
	 * will be capable of resolving resource patterns to Resource arrays.
	 * <p>Default is PathMatchingResourcePatternResolver, also capable of
	 * resource pattern resolving through the ResourcePatternResolver interface.
	 * <p>Setting this to <code>null</code> suggests that absolute resource loading
	 * is not available for this bean definition reader.
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	/**
	 * Set the ClassLoader to use for bean classes.
	 * Default is the context class loader of the thread that instantiated
	 * this bean definition reader.
	 * <p>Setting this to <code>null</code> suggests to not load bean classes but just
	 * register bean definitions with class names, for example when just registering
	 * beans in a registry but not actually instantiating them in a factory.
	 * @see java.lang.Thread#getContextClassLoader()
	 */
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	public ClassLoader getBeanClassLoader() {
		return beanClassLoader;
	}


	public int loadBeanDefinitions(Resource[] resources) throws BeansException {
		Assert.notNull(resources, "Resource array must not be null");
		int counter = 0;
		for (int i = 0; i < resources.length; i++) {
			counter += loadBeanDefinitions(resources[i]);
		}
		return counter;
	}

	public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
		ResourceLoader resourceLoader = getResourceLoader();
		if (resourceLoader == null) {
			throw new BeanDefinitionStoreException(
					"Cannot import bean definitions from location [" + location + "]: no ResourceLoader available");
		}

		if (resourceLoader instanceof ResourcePatternResolver) {
			// Resource pattern matching available.
			try {
				Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
				int loadCount = loadBeanDefinitions(resources);
				if (logger.isDebugEnabled()) {
					logger.debug("Loaded " + loadCount + " bean definitions from location pattern [" + location + "]");
				}
				return loadCount;
			}
			catch (IOException ex) {
				throw new BeanDefinitionStoreException(
						"Could not resolve bean definition resource pattern [" + location + "]", ex);
			}
		}
		else {
			// Can only load single resources by absolute URL.
			Resource resource = resourceLoader.getResource(location);
			int loadCount = loadBeanDefinitions(resource);
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + loadCount + " bean definitions from location [" + location + "]");
			}
			return loadCount;
		}
	}

	public int loadBeanDefinitions(String[] locations) throws BeansException {
		Assert.notNull(locations, "Location array must not be null");
		int counter = 0;
		for (int i = 0; i < locations.length; i++) {
			counter += loadBeanDefinitions(locations[i]);
		}
		return counter;
	}

}
