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

package org.springframework.beans.factory.access;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.util.Assert;

/**
 * One singleton to rule them all. Reads system properties, which
 * must contain the definition of a bootstrap bean factory using
 * the Properties syntax supported by PropertiesBeanDefinitionReader.
 *
 * <p>The name of the bootstrap factory must be "bootstrapBeanFactory".
 * Thus a typical definition might be:
 *
 * <pre>
 * bootstrapBeanFactory.class=com.mycompany.MyBeanFactory</pre>
 *
 * Use as follows:
 *
 * <pre>
 * BeanFactory bf = BeanFactoryBootstrap.getInstance().getBeanFactory();</pre>
 *
 * @author Rod Johnson
 * @since 02.12.2002
 * @deprecated since Spring 1.2.7: consider using a BeanFactoryLocator
 * or a custom bootstrap class instead
 * @see BeanFactoryLocator
 */
public class BeanFactoryBootstrap {
	
	public static final String BEAN_FACTORY_BEAN_NAME = "bootstrapBeanFactory";

	private static BeanFactoryBootstrap instance;
	
	private static BeansException startupException;
	
	private static void initializeSingleton() {
		try {
			instance = new BeanFactoryBootstrap();
		}
		catch (BeansException ex) {
			startupException = ex;
		}
	}
	
	// Do initialization when this class is loaded to avoid
	// potential concurrency issues or the need to synchronize later
	static {
		initializeSingleton();
	}

	/**
	 * Return the singleton instance of the bootstrap factory
	 * @return BeanFactoryBootstrap
	 * @throws org.springframework.beans.BeansException
	 */
	public static BeanFactoryBootstrap getInstance() throws BeansException {
		if (startupException != null) {
			throw startupException;
		}
		Assert.notNull(instance);
		return instance;
	}
	
	/**
	 * <b>For testing only. Cleans and reinitalizes the instance.
	 * Do not use in a production application!</b>
	 */
	protected static void reinitialize() {
		instance = null;
		startupException = null;
		initializeSingleton();
	}


	/** the singleton instance */
	private BeanFactory bootstrapFactory;
	
	/**
	 * Apply rules to load factory.
	 */
	private BeanFactoryBootstrap() throws BeansException {
		DefaultListableBeanFactory startupFactory = new DefaultListableBeanFactory();
		PropertiesBeanDefinitionReader propReader = new PropertiesBeanDefinitionReader(startupFactory);
		try {
			propReader.registerBeanDefinitions(System.getProperties());
			this.bootstrapFactory = (BeanFactory) startupFactory.getBean(BEAN_FACTORY_BEAN_NAME, BeanFactory.class);
		}
		catch (NoSuchBeanDefinitionException ex) {
			throw new BootstrapException(
					"No bean named '" + BEAN_FACTORY_BEAN_NAME + "' in system properties: [" + startupFactory + "]");
		}
		catch (BeansException ex) {
			throw new BootstrapException("Failed to bootstrap bean factory", ex);
		}
	}

	/**
	 * Return the BeanFactory managed by the Bootstrap.
	 */
	public BeanFactory getBeanFactory() {
		return bootstrapFactory;
	}
	
}
