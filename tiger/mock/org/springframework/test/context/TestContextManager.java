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
package org.springframework.test.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * <p>
 * TestContextManager is the central entry point into the Spring testing support
 * API, which serves as a facade and encapsulates support for loading and
 * accessing {@link ConfigurableApplicationContext application contexts},
 * dependency injection of test classes, and {@link Transactional transactional}
 * execution of test methods.
 * </p>
 * <p>
 * {@link AutowiredAnnotationBeanPostProcessor} and
 * {@link CommonAnnotationBeanPostProcessor} will be automatically registered
 * with the bean factories of contexts managed by this test context manager.
 * Managed test instances are therefore automatically candidates for
 * annotation-based dependency injection using
 * {@link org.springframework.beans.factory.annotation.Autowired Autowired} and
 * {@link javax.annotation.Resource Resource}.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.2 $
 * @since 2.1
 */
public class TestContextManager<T> {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log LOG = LogFactory.getLog(TestContextManager.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Cache of Spring application contexts. This needs to be static, as tests
	 * may be destroyed and recreated between running individual test methods,
	 * for example with JUnit.
	 */
	private static final ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> contextCache = new MapBackedContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext>();

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

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
	 * Constructs a new {@link TestContextManager} for the specified
	 * {@link Class testClass}.
	 * </p>
	 *
	 * @param testClass the Class object corresponding to the test class to be
	 *        managed.
	 * @throws Exception if an error occurs while processing the test class
	 */
	public TestContextManager(final Class<T> testClass) throws Exception {

		this.testClass = testClass;
		this.configurationAttributes = parseConfigurationAttributes(testClass);
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Builds and configures a {@link ConfigurableApplicationContext} based on
	 * the supplied {@link ContextConfigurationAttributes}.
	 * </p>
	 * <p>
	 * {@link AutowiredAnnotationBeanPostProcessor} and
	 * {@link CommonAnnotationBeanPostProcessor} will be automatically
	 * registered with the context's bean factory.
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

		final AutowiredAnnotationBeanPostProcessor autowiredAnnotationBpp = new AutowiredAnnotationBeanPostProcessor();
		final CommonAnnotationBeanPostProcessor commonAnnotationBpp = new CommonAnnotationBeanPostProcessor();

		autowiredAnnotationBpp.setBeanFactory(beanFactory);
		commonAnnotationBpp.setBeanFactory(beanFactory);

		beanFactory.addBeanPostProcessor(autowiredAnnotationBpp);
		beanFactory.addBeanPostProcessor(commonAnnotationBpp);

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
	 * @see ContextConfigurationAttributes#getLoaderClass()
	 * @param configAttributes the context configuration attributes to use to
	 *        determine the type of ContextLoader to instantiate.
	 * @return a new ContextLoader
	 * @throws Exception if an error occurs while creating the context loader.
	 */
	protected static ContextLoader createContextLoader(final ContextConfigurationAttributes configAttributes)
			throws Exception {

		Assert.notNull(configAttributes, "ContextConfigurationAttributes can not be null.");
		final Class<? extends ContextLoader> contextLoaderClass = configAttributes.getLoaderClass();
		Assert.state(contextLoaderClass != null,
				"contextLoaderClass is null: ContextConfigurationAttributes have not been properly initialized.");

		@SuppressWarnings("unchecked")
		final Constructor<? extends ContextLoader> constructor = ClassUtils.getConstructorIfAvailable(
				contextLoaderClass, new Class[] { ContextConfigurationAttributes.class });
		Assert.state(constructor != null, "The configured ContextLoader class [" + contextLoaderClass
				+ "] must have a constructor which accepts an instance of "
				+ "ContextConfigurationAttributes as its only argument.");

		return constructor.newInstance(configAttributes);
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Parses the {@link ContextConfigurationAttributes} from the specified
	 * {@link Class}.
	 * </p>
	 *
	 * @param clazz the Class object corresponding to the test class from which
	 *        the configuration attributes should be parsed.
	 * @return a new ContextConfigurationAttributes instance for the specified
	 *         class.
	 * @throws IllegalArgumentException if any of the supplied arguments is
	 *         <code>null</code>.
	 */
	protected static ContextConfigurationAttributes parseConfigurationAttributes(final Class<?> clazz)
			throws IllegalArgumentException {

		Assert.notNull(clazz, "Can not parse context configuration attributes for a NULL class.");
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
	 * Gets the {@link ConfigurableApplicationContext application context} for
	 * the managed {@link #getTestClass() test class}, possibly cached.
	 * </p>
	 *
	 * @return The application context.
	 * @throws Exception if an error occurs while retrieving the application
	 *         context.
	 */
	public synchronized ConfigurableApplicationContext getApplicationContext() throws Exception {

		ConfigurableApplicationContext context = getContextCache().get(getConfigurationAttributes());

		if (context == null) {
			context = buildApplicationContext(getConfigurationAttributes());
			getContextCache().put(getConfigurationAttributes(), context);
		}

		return context;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the {@link ContextConfigurationAttributes configuration attributes}
	 * for the managed {@link #getTestClass() test class}.
	 * </p>
	 *
	 * @return The configuration attributes.
	 */
	public ContextConfigurationAttributes getConfigurationAttributes() {

		return this.configurationAttributes;
	}

	// ------------------------------------------------------------------------|

	/**
	 * @return the context cache.
	 */
	protected ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> getContextCache() {

		return TestContextManager.contextCache;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the managed {@link Class test class}.
	 * </p>
	 *
	 * @return The test class.
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
	 * {@link ContextConfigurationAttributes#isCheckDependencies() dependency check}
	 * attributes in the supplied configuration.
	 * </p>
	 * <p>
	 * Override this method if you need full control over how dependencies are
	 * injected into the test instance.
	 * </p>
	 *
	 * @param testInstance The object into which dependencies should be
	 *        injected.
	 * @param context The application context from which dependencies should be
	 *        retrieved.
	 * @throws Exception in case of dependency injection failure
	 */
	protected void injectDependencies(final T testInstance, final ConfigurableApplicationContext context)
			throws Exception {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Dependency injecting test instance [" + testInstance + "] based on configuration attributes ["
					+ getConfigurationAttributes() + "].");
		}
		final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.autowireBeanProperties(testInstance, getConfigurationAttributes().getAutowireMode().value(),
				getConfigurationAttributes().isCheckDependencies());
		beanFactory.initializeBean(testInstance, null);
	}

	// ------------------------------------------------------------------------|

	/**
	 * Call this method to signal that the
	 * {@link ConfigurableApplicationContext application context} associated
	 * with this {@link TestContextManager} is <em>dirty</em> and should be
	 * reloaded. Do this if a test has modified the context (for example, by
	 * replacing a bean definition).
	 */
	public void markApplicationContextDirty() {

		getContextCache().setDirty(getConfigurationAttributes());
	}

	// ------------------------------------------------------------------------|

	/**
	 * Hook method for preparing a test just <em>before</em> the execution of
	 * the supplied test method, for example setting up test fixtures,
	 * transaction handling, etc.
	 *
	 * @param method the test method which is about to be executed.
	 */
	public void beforeTestMethodExecution(final Method method) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("beforeTestMethodExecution(): method [" + method + "].");
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Hook method for post-processing a test just <em>after</em> the
	 * execution of the supplied test method, for example tearing down test
	 * fixtures, transaction handling, etc.
	 *
	 * @param method the test method which has just been executed.
	 */
	public void afterTestMethodExecution(final Method method) {

		final boolean dirtiesContext = (AnnotationUtils.findAnnotation(method, DirtiesContext.class) != null);

		if (LOG.isDebugEnabled()) {
			LOG.debug("afterTestMethodExecution(): method [" + method + "] dirtiesContext [" + dirtiesContext + "].");
		}

		if (dirtiesContext) {
			getContextCache().setDirty(getConfigurationAttributes());
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Prepares the supplied test instance (e.g., injecting dependencies, etc.).
	 * </p>
	 *
	 * @param testInstance The test object to prepare.
	 * @throws Exception if an error occurs while preparing the test instance.
	 */
	public synchronized void prepareTestInstance(final T testInstance) throws Exception {

		final ConfigurableApplicationContext context = getApplicationContext();
		injectDependencies(testInstance, context);
		if (!context.isActive()) {
			context.refresh();
		}
	}

	// ------------------------------------------------------------------------|

}
