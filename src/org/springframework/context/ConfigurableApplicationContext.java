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

package org.springframework.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * SPI interface to be implemented by most if not all application contexts.
 * Provides means to configure an application context in addition to the
 * application context client methods in the ApplicationContext interface.
 *
 * <p>Configuration and lifecycle methods are encapsulated here to avoid
 * making them obvious to ApplicationContext client code.
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 */
public interface ConfigurableApplicationContext extends ApplicationContext {

	/**
	 * Set the parent of this application context.
	 * <p>Note that the parent shouldn't be changed: It should only be set outside
	 * a constructor if it isn't available when an object of this class is created,
	 * for example in case of WebApplicationContext setup.
	 * @param parent the parent context
	 * @see org.springframework.web.context.ConfigurableWebApplicationContext
	 */
	void setParent(ApplicationContext parent);

	/**
	 * Add a new BeanFactoryPostProcessor that will get applied to the internal
	 * bean factory of this application context on refresh, before any of the
	 * bean definitions get evaluated. To be invoked during context configuration.
	 * @param beanFactoryPostProcessor the factory processor to register
	 */
	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor);

	/**
	 * Load or refresh the persistent representation of the configuration,
	 * which might an XML file, properties file, or relational database schema.
	 * @throws org.springframework.context.ApplicationContextException if the config cannot be loaded
	 * @throws org.springframework.beans.BeansException if the bean factory could not be initialized
	 */
	void refresh() throws BeansException;

	/**
	 * Return the internal bean factory of this application context.
	 * Can be used to access specific functionality of the factory.
	 * <p>Note that this is just guaranteed to return a non-null instance
	 * <i>after</i> the context has been refreshed at least once.
	 * <p>Note: Do not use this to post-process the bean factory; singletons
	 * will already have been instantiated before. Use a BeanFactoryPostProcessor
	 * to intercept the bean factory setup process before beans get touched.
	 * @see #refresh
	 * @see #addBeanFactoryPostProcessor
	 */
	ConfigurableListableBeanFactory getBeanFactory();

	/**
	 * Close this application context, releasing all resources and locks that the
	 * implementation might hold. This includes disposing all cached singleton beans.
	 * <p>Note: Does <i>not</i> invoke close on a parent context.
	 * @throws org.springframework.context.ApplicationContextException if there were fatal errors
	 */
	void close() throws ApplicationContextException;

}
