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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.DefaultContextConfigurationAttributes;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * <p>
 * TestContext encapsulates the context in which a test is executed, agnostic of
 * the actual testing framework in use.
 * </p>
 * <p>
 * {@link AutowiredAnnotationBeanPostProcessor} and
 * {@link CommonAnnotationBeanPostProcessor} will be automatically registered
 * with bean factories of
 * {@link ConfigurableApplicationContext application contexts} created for the
 * test instance referenced by this test context. Test instances are therefore
 * automatically candidates for annotation-based dependency injection using
 * {@link org.springframework.beans.factory.annotation.Autowired Autowired} and
 * {@link javax.annotation.Resource Resource}.
 * </p>
 *
 * @author Sam Brannen
 * @version $Revision: 1.3 $
 * @since 2.1
 */
public class TestContext<T> {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log LOG = LogFactory.getLog(TestContext.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final ContextConfigurationAttributes configurationAttributes;

	private final ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> contextCache;

	private Throwable exception;

	private final Class<T> testClass;

	private Object testInstance;

	private Method testMethod;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new test context for the supplied {@link Class class} and
	 * {@link ContextCache context cache}.
	 *
	 * @param testClass The {@link Class} object corresponding to the test class
	 *        for which the test context should be constructed, not
	 *        <code>null</code>.
	 * @param contextCache The context cache from which the constructed test
	 *        context should retrieve application contexts, not
	 *        <code>null</code>.
	 */
	public TestContext(final Class<T> testClass,
			final ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> contextCache) {

		super();

		Assert.notNull(testClass, "The testClass can not be null.");
		Assert.notNull(contextCache, "The contextCache can not be null.");

		this.testClass = testClass;
		this.contextCache = contextCache;
		this.configurationAttributes = retrieveConfigurationAttributes(testClass);
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
	 * Retrieves the {@link ContextConfigurationAttributes} for the specified
	 * {@link Class class}.
	 * </p>
	 *
	 * @param clazz The Class object corresponding to the test class for which
	 *        the configuration attributes should be retrieved.
	 * @return a new ContextConfigurationAttributes instance for the specified
	 *         class.
	 * @throws IllegalArgumentException if the supplied class is
	 *         <code>null</code>.
	 */
	protected static ContextConfigurationAttributes retrieveConfigurationAttributes(final Class<?> clazz)
			throws IllegalArgumentException {

		Assert.notNull(clazz, "Can not retrieve context configuration attributes for a NULL class.");
		final ContextConfigurationAttributes configAttributes = DefaultContextConfigurationAttributes.constructAttributes(clazz);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Retrieved configuration attributes [" + configAttributes + "].");
		}
		return configAttributes;
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the {@link ConfigurableApplicationContext application context} for
	 * this test context, possibly cached.
	 * </p>
	 *
	 * @return The application context.
	 * @throws Exception if an error occurs while retrieving the application
	 *         context.
	 */
	public ConfigurableApplicationContext getApplicationContext() throws Exception {

		ConfigurableApplicationContext context;
		final ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> cache = getContextCache();

		synchronized (cache) {
			context = cache.get(getConfigurationAttributes());

			if (context == null) {
				context = buildApplicationContext(getConfigurationAttributes());
				cache.put(getConfigurationAttributes(), context);
			}
		}

		return context;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the {@link ContextConfigurationAttributes configuration attributes}
	 * for this test context.
	 * </p>
	 *
	 * @return The configuration attributes, never <code>null</code>.
	 */
	public final ContextConfigurationAttributes getConfigurationAttributes() {

		return this.configurationAttributes;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the {@link ContextCache context cache} for this test context.
	 * </p>
	 *
	 * @return The context cache, never <code>null</code>.
	 */
	protected final ContextCache<ContextConfigurationAttributes, ConfigurableApplicationContext> getContextCache() {

		return this.contextCache;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Gets the exception that was thrown during execution of the last test
	 * method.
	 *
	 * @return the exception that was thrown, or <code>null</code> if none.
	 */
	public final Throwable getException() {

		return this.exception;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the {@link Class test class} for this test context.
	 * </p>
	 *
	 * @return The test class, never <code>null</code>.
	 */
	public final Class<T> getTestClass() {

		return this.testClass;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the current {@link Object test instance} for this test context. Note
	 * that the test instance is a mutable property of a test context.
	 * </p>
	 *
	 * @return The current test instance; may be <code>null</code>.
	 */
	public final Object getTestInstance() {

		return this.testInstance;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the current {@link Method test method} for this test context. Note
	 * that the test method is a mutable property of a test context.
	 * </p>
	 *
	 * @return The current test method; may be <code>null</code>.
	 */
	public final Method getTestMethod() {

		return this.testMethod;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Call this method to signal that the
	 * {@link ConfigurableApplicationContext application context} associated
	 * with this {@link TestContext} is <em>dirty</em> and should be reloaded.
	 * Do this if a test has modified the context (for example, by replacing a
	 * bean definition).
	 */
	public void markApplicationContextDirty() {

		getContextCache().setDirty(getConfigurationAttributes());
	}

	// ------------------------------------------------------------------------|

	/**
	 * Sets the exception thrown during the last test method execution.
	 *
	 * @param exception The exception that was thrown during execution of the
	 *        test method, or <code>null</code> if none was thrown.
	 */
	public final void setException(final Throwable exception) {

		this.exception = exception;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Sets the current {@link Object test instance} and
	 * {@link Method test method} to associate with this test context.
	 *
	 * @param testInstance The current test instance; may be <code>null</code>.
	 * @param testMethod The current test method; may be <code>null</code>.
	 */
	public final synchronized void setTestInstanceAndMethod(final Object testInstance, final Method testMethod) {

		this.testInstance = testInstance;
		this.testMethod = testMethod;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Provides a string representation of this test contexts's
	 * {@link #getTestClass() test class},
	 * {@link #getConfigurationAttributes() configuration attributes},
	 * {@link #getTestInstance() test instance}, and
	 * {@link #getTestMethod() test method}.
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return new ToStringBuilder(this)

		.append("testClass", this.testClass)

		.append("configurationAttributes", this.configurationAttributes)

		.append("testInstance", this.testInstance)

		.append("testMethod", this.testMethod)

		.toString();
	}

	// ------------------------------------------------------------------------|

}
