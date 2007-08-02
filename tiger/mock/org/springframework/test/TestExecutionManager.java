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

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * <p>
 * TestExecutionManager is the central entry point into the Spring testing
 * support API, which serves as a facade and encapsulates support for loading
 * and accessing {@link ApplicationContext application contexts}, dependency
 * injection of test classes, and {@link Transactional transactional} execution
 * of test methods.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.2
 */
public class TestExecutionManager<T> {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log LOG = LogFactory.getLog(TestExecutionManager.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private ConfigurableApplicationContext applicationContext;

	private final ContextConfigurationAttributes configurationAttributes;

	private final Class<T> testClass;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Constructs a new {@link TestExecutionManager} for the specified
	 * {@link Class testClass}.
	 * </p>
	 *
	 * @param testClass the Class object corresponding to the test class to be
	 *        managed.
	 * @throws Exception if an error occurs while processing the test class
	 */
	public TestExecutionManager(final Class<T> testClass) throws Exception {

		this.testClass = testClass;
		this.configurationAttributes = parseConfigurationAttributes(testClass);
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Builds and configures an {@link ApplicationContext} based on the supplied
	 * {@link ContextConfigurationAttributes}.
	 * </p>
	 *
	 * @param configAttributes the context configuration attributes to use to
	 *        determine how to build and configure an appropriate application
	 *        context.
	 * @throws Exception if an error occurs while building the application
	 *         context
	 */
	protected static ConfigurableApplicationContext buildApplicationContext(
			final ContextConfigurationAttributes configAttributes) throws Exception {

		Assert.notNull(configAttributes, "configAttributes can not be null.");

		final ConfigurableApplicationContext applicationContext = createContextLoader(configAttributes).loadContext();

		final ConfigurableBeanFactory beanFactory = applicationContext.getBeanFactory();
		final AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(beanFactory);
		beanFactory.addBeanPostProcessor(bpp);

		return applicationContext;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Creates a new {@link ContextLoader} of the concrete type specified in the
	 * supplied {@link ContextConfigurationAttributes}.
	 * </p>
	 *
	 * @see ContextLoader
	 * @see ContextConfigurationAttributes#getContextLoaderClass()
	 * @param configAttributes the context configuration attributes to use to
	 *        determine the type of ContextLoader to instantiate.
	 * @return a new ContextLoader
	 * @throws Exception
	 */
	protected static ContextLoader createContextLoader(final ContextConfigurationAttributes configAttributes)
			throws Exception {

		Assert.notNull(configAttributes, "ContextConfigurationAttributes can not be null.");
		final Class<? extends ContextLoader> contextLoaderClass = configAttributes.getContextLoaderClass();
		Assert.state(contextLoaderClass != null,
				"contextLoaderClass is null: ContextConfigurationAttributes have not been properly initialized.");

		@SuppressWarnings("unchecked")
		final Constructor<? extends ContextLoader> constructor = ClassUtils.getConstructorIfAvailable(
				contextLoaderClass, new Class[] { ContextConfigurationAttributes.class });
		Assert.state(constructor != null, "The configured ContextLoader class [" + contextLoaderClass
				+ "] must have a constructor which accepts an instance of"
				+ "ContextConfigurationAttributes as its lone argument.");

		return constructor.newInstance(configAttributes);
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Parses the {@link ContextConfigurationAttributes} from the specified
	 * {@link Class}.
	 * </p>
	 *
	 * @param clazz
	 * @return TODO Document return value!
	 * @throws IllegalArgumentException if any of the supplied arguments is
	 *         <code>null</code>.
	 */
	protected static ContextConfigurationAttributes parseConfigurationAttributes(final Class<?> clazz)
			throws IllegalArgumentException {

		Assert.notNull(clazz, "clazz can not be null.");
		final ContextConfigurationAttributes configAttributes = DefaultContextConfigurationAttributes.constructAttributes(clazz);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Parsed configuration attributes [" + configAttributes + "].");
		}
		return configAttributes;
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the {@link ApplicationContext} for the managed
	 * {@link #getTestClass() test class}.
	 * </p>
	 * <p>
	 * Currently performs lazy initialization but does not perform any caching!
	 * </p>
	 *
	 * @return Returns the applicationContext.
	 * @throws Exception
	 */
	public ConfigurableApplicationContext getApplicationContext() throws Exception {

		// TODO Extract ApplicationContext processing and caching into a new
		// class hierarchy. Cache context using configurationAttributes as key!

		// Lazy load for the time being...
		if (this.applicationContext == null) {
			this.applicationContext = buildApplicationContext(getConfigurationAttributes());
		}

		return this.applicationContext;
	}

	// ------------------------------------------------------------------------|

	/**
	 * @return Returns the configurationAttributes.
	 */
	public ContextConfigurationAttributes getConfigurationAttributes() {

		return this.configurationAttributes;
	}

	// ------------------------------------------------------------------------|

	/**
	 * @return Returns the testClass.
	 */
	public Class<T> getTestClass() {

		return this.testClass;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Injects dependencies into the supplied test instance.
	 * </p>
	 * <p>
	 * The default implementation autowires the test instance via the supplied
	 * application context, using the
	 * {@link ContextConfigurationAttributes#getAutowireMode() autowire mode}
	 * and
	 * {@link ContextConfigurationAttributes#isDependencyCheckEnabled() dependency check}
	 * attributes in the supplied configuration.
	 * </p>
	 * <p>
	 * Override this method if you need full control over how dependencies are
	 * injected into the test instance.
	 * </p>
	 *
	 * @param testInstance
	 * @param configurationAttributes
	 * @param context
	 * @throws Exception in case of dependency injection failure
	 */
	protected void injectDependencies(final T testInstance,
			final ContextConfigurationAttributes configurationAttributes, final ConfigurableApplicationContext context)
			throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Dependency injecting test instance [" + testInstance + "] based on configuration attributes ["
					+ configurationAttributes + "].");
		}
		context.getBeanFactory().autowireBeanProperties(testInstance,
				configurationAttributes.getAutowireMode().value(), configurationAttributes.isDependencyCheckEnabled());
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Prepares the supplied test instance (e.g., injecting dependencies, etc.)
	 * </p>
	 *
	 * @param testInstance
	 * @throws Exception
	 */
	public void prepareTestInstance(final T testInstance) throws Exception {

		injectDependencies(testInstance, getConfigurationAttributes(), getApplicationContext());
		if (!getApplicationContext().isActive()) {
			getApplicationContext().refresh();
		}
	}

	// ------------------------------------------------------------------------|

}
