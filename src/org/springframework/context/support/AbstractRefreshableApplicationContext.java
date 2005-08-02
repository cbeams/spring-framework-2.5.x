/*
 * Copyright 2002-2005 the original author or authors.
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

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

/**
 * Base class for ApplicationContext implementations that are supposed to support
 * multiple refreshs, creating a new internal bean factory instance every time.
 * Typically (but not necessarily), such a context will be driven by a set of
 * config locations to load bean definitions from.
 *
 * <p>The only method to be implemented by subclasses is <code>loadBeanDefinitions</code>,
 * which gets invoked on each refresh. A concrete implementation is supposed to load
 * bean definitions into the given DefaultListableBeanFactory, typically delegating
 * to one or more specific bean definition readers.
 *
 * <p><b>Note that there is a similar base class for WebApplicationContexts.</b>
 * AbstractRefreshableWebApplicationContext provides the same subclassing strategy,
 * but additionally pre-implements all context functionality for web environments.
 * There is also a pre-defined way to receive config locations for a web context.
 *
 * <p>Concrete standalone subclasses of this base class, reading in a specific bean
 * definition format, are ClassPathXmlApplicationContext and FileSystemXmlApplicationContext,
 * which both derive from the common AbstractXmlApplicationContext base class.
 *
 * @author Juergen Hoeller
 * @since 1.1.3
 * @see #loadBeanDefinitions
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.beans.factory.support.PropertiesBeanDefinitionReader
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext
 * @see AbstractXmlApplicationContext
 * @see ClassPathXmlApplicationContext
 * @see FileSystemXmlApplicationContext
 */
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {

	/** Bean factory for this context */
	private DefaultListableBeanFactory beanFactory;


	/**
	 * Create a new AbstractRefreshableApplicationContext with no parent.
	 */
	public AbstractRefreshableApplicationContext() {
	}

	/**
	 * Create a new AbstractRefreshableApplicationContext with the given parent context.
	 * @param parent the parent context
	 */
	public AbstractRefreshableApplicationContext(ApplicationContext parent) {
		super(parent);
	}


	protected final void refreshBeanFactory() throws BeansException {
		// Shut down previous bean factory, if any.
		if (this.beanFactory != null) {
			this.beanFactory.destroySingletons();
			this.beanFactory = null;
		}

		// Initialize fresh bean factory.
		try {
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			loadBeanDefinitions(beanFactory);
			this.beanFactory = beanFactory;
			if (logger.isInfoEnabled()) {
				logger.info("Bean factory for application context [" + getDisplayName() + "]: " + beanFactory);
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException(
					"I/O error parsing XML document for application context [" + getDisplayName() + "]", ex);
		}
	}

	public final ConfigurableListableBeanFactory getBeanFactory() {
		if (this.beanFactory == null) {
			throw new IllegalStateException("BeanFactory not initialized - " +
					"call 'refresh' before accessing beans via the context: " + this);

		}
		return this.beanFactory;
	}

	/**
	 * Create the bean factory for this context.
	 * <p>Default implementation creates a DefaultListableBeanFactory with the
	 * internal bean factory of this context's parent as parent bean factory.
	 * <p>Can be overridden in subclasses.
	 * @return the bean factory for this context
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
	 * @see #getInternalParentBeanFactory
	 */
	protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory(getInternalParentBeanFactory());
	}

	/**
	 * Load bean definitions into the given bean factory, typically through
	 * delegating to one or more bean definition readers.
	 * @param beanFactory the bean factory to load bean definitions into
	 * @throws IOException if loading of bean definition files failed
	 * @throws BeansException if parsing of the bean definitions failed
	 * @see org.springframework.beans.factory.support.PropertiesBeanDefinitionReader
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 */
	protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory)
			throws IOException, BeansException;

}
