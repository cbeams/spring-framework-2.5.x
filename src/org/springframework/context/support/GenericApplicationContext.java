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

package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Generic ApplicationContext implementation which does not assume a specific
 * bean definition format. Holds an internal DefaultListableBeanFactory,
 * and implements the BeanDefinitionRegistry interface to allow for applying
 * bean definition readers to it.
 *
 * <p>Typical usage is to register a variety of bean definitions via the
 * BeanDefinitionRegistry interface and then call <code>refresh</code> to initialize
 * those beans with application context semantics (handling ApplicationContextAware,
 * auto-detecting BeanFactoryPostProcessors, etc).
 *
 * <p>In contrast to other ApplicationContext implementations that create a new
 * internal BeanFactory instance for each refresh, the internal BeanFactory of
 * this context is available right from the start, to be able to register bean
 * definitions on it. <code>refresh</code> may only be called once.
 *
 * <p>Usage example:
 *
 * <pre>
 * GenericApplicationContext ctx = new GenericApplicationContext();
 * XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
 * xmlReader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml"));
 * PropertiesBeanDefinitionReader propReader = new PropertiesBeanDefinitionReader(ctx);
 * propReader.loadBeanDefinitions(new ClassPathResource("otherBeans.properties"));
 * ctx.refresh();
 *
 * MyBean myBean = (MyBean) ctx.getBean("myBean");
 * ...</pre>
 *
 * For the typical case of XML bean definitions, simply use ClassPathXmlApplicationContext
 * or FileSystemXmlApplicationContext, which are easier to set up - but less flexible,
 * as you can just use standard resource locations for XML bean definitions, rather than
 * mixing arbitrary bean definition formats.
 *
 * @author Juergen Hoeller
 * @since 1.1.2
 * @see #registerBeanDefinition
 * @see #refresh
 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see ClassPathXmlApplicationContext
 * @see FileSystemXmlApplicationContext
 */
public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {

	private final DefaultListableBeanFactory beanFactory;

	private ResourceLoader resourceLoader;

	private boolean refreshed = false;


	/**
	 * Create a new GenericApplicationContext.
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext() {
		this.beanFactory = new DefaultListableBeanFactory();
	}

	/**
	 * Create a new GenericApplicationContext with the given DefaultListableBeanFactory.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext(DefaultListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Create a new GenericApplicationContext with the given parent.
	 * @param parent the parent application context
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext(ApplicationContext parent) {
		super(parent);
		this.beanFactory = new DefaultListableBeanFactory(getInternalParentBeanFactory());
	}

	/**
	 * Create a new GenericApplicationContext with the given DefaultListableBeanFactory.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 * @param parent the parent application context
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext(DefaultListableBeanFactory beanFactory, ApplicationContext parent) {
		super(parent);
		this.beanFactory = beanFactory;
	}


	/**
	 * Set a ResourceLoader to use for this context. If set, the context
	 * will delegate all resource loading to the given ResourceLoader.
	 * If not set, default resource loading will apply.
	 * <p>The main reason to specify a custom ResourceLoader is to resolve
	 * resource paths (withour URL prefix) in a specific fashion.
	 * The default behavior is to resolve such paths as class path locations.
	 * To resolve resource paths as file system locations, specify a
	 * FileSystemResourceLoader here.
	 * @see #getResource
	 * @see org.springframework.core.io.DefaultResourceLoader
	 * @see org.springframework.core.io.FileSystemResourceLoader
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * This implementation delegates to this context's ResourceLoader if set,
	 * falling back to the default superclass behavior else.
	 * @see #setResourceLoader
	 */
	public Resource getResource(String location) {
		if (this.resourceLoader != null) {
			return this.resourceLoader.getResource(location);
		}
		return super.getResource(location);
	}


	//---------------------------------------------------------------------
	// Implementations of AbstractApplicationContext's template methods
	//---------------------------------------------------------------------

	/**
	 * Do nothing: We hold a single internal BeanFactory and rely on callers to
	 * register beans through our public methods respectively the BeanFactory's.
	 * @see #registerBeanDefinition
	 */
	protected void refreshBeanFactory() throws IllegalStateException {
		if (this.refreshed) {
			throw new IllegalStateException("Multiple refreshs not supported - just call 'refresh' once");
		}
		this.refreshed = true;
	}

	/**
	 * Return the single internal BeanFactory held by this context.
	 */
	public ConfigurableListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * Return the underlying bean factory of this context,
	 * available for registering bean definitions.
	 * <p><b>NOTE:</b> You need to call <code>refresh</code> to initialize the
	 * bean factory and its contained beans with application context semantics
	 * (auto-detecting BeanFactoryPostProcessors, etc)
	 * @see #refresh
	 */
	public DefaultListableBeanFactory getDefaultListableBeanFactory() {
		return this.beanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry
	//---------------------------------------------------------------------

	public BeanDefinition getBeanDefinition(String name) throws BeansException {
		return this.beanFactory.getBeanDefinition(name);
	}

	public void registerBeanDefinition(String name, BeanDefinition beanDefinition) throws BeansException {
		this.beanFactory.registerBeanDefinition(name, beanDefinition);
	}

	public void registerAlias(String name, String alias) throws BeansException {
		this.beanFactory.registerAlias(name, alias);
	}

}
