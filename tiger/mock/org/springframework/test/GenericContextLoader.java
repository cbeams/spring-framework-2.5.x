/*
 * Copyright 2007 the original author or authors.
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
package org.springframework.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * <p>
 * Concrete implementation of the {@link ContextLoader} strategy which loads a
 * {@link GenericApplicationContext} from the <em>locations</em> in the
 * supplied {@link ContextConfigurationAttributes configuration attributes}.
 * </p>
 *
 * @see #loadContext()
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.2
 */
public class GenericContextLoader implements ContextLoader {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	protected static final Log LOG = LogFactory.getLog(GenericContextLoader.class);

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CLASS VARIABLES ----------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final ContextConfigurationAttributes configAttributes;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	public GenericContextLoader(final ContextConfigurationAttributes configAttributes) {

		this.configAttributes = configAttributes;
	}

	// ------------------------------------------------------------------------|
	// --- CLASS METHODS ------------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Loads a Spring ApplicationContext from the <em>locations</em> defined
	 * in the supplied
	 * {@link ContextConfigurationAttributes configuration attributes}.
	 * </p>
	 * <p>
	 * The default implementation creates a standard GenericApplicationContext
	 * instance, populates it from the specified config locations through an
	 * {@link XmlBeanDefinitionReader}, calls {@link #customizeBeanFactory} to
	 * allow for customizing the context's DefaultListableBeanFactory, and
	 * finally registers a JVM shutdown hook for itself
	 * </p>
	 * <p>
	 * Note: the returned context will already have been
	 * {@link ConfigurableApplicationContext#refresh() refreshed}.
	 * </p>
	 *
	 * @see org.springframework.test.ContextLoader#loadContext()
	 * @see GenericApplicationContext
	 * @see XmlBeanDefinitionReader
	 * @see #customizeBeanFactory
	 * @return
	 */
	@Override
	public ConfigurableApplicationContext loadContext() throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Loading ApplicationContext for ContextConfigurationAttributes [" + this.configAttributes + "].");
		}

		final GenericApplicationContext context = new GenericApplicationContext();
		new XmlBeanDefinitionReader(context).loadBeanDefinitions(this.configAttributes.getLocations());
		customizeBeanFactory(context.getDefaultListableBeanFactory());
		context.refresh();
		context.registerShutdownHook();
		return context;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Customize the internal bean factory of the ApplicationContext created by
	 * this ContextLoader.
	 * </p>
	 * <p>
	 * The default implementation is empty. Can be overridden in subclasses to
	 * customize DefaultListableBeanFactory's standard settings.
	 * </p>
	 *
	 * @param beanFactory the newly created bean factory for this context
	 * @see #loadContextLocations
	 * @see #createApplicationContext
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowBeanDefinitionOverriding
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowEagerClassLoading
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowCircularReferences
	 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#setAllowRawInjectionDespiteWrapping
	 */
	protected void customizeBeanFactory(final DefaultListableBeanFactory beanFactory) {

		/* no-op */
	}

	// ------------------------------------------------------------------------|

}
