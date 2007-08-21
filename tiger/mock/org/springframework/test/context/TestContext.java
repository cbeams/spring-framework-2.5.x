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

import static org.springframework.core.annotation.AnnotationUtils.findAnnotationDeclaringClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.ContextConfiguration;
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
 * with bean factories of {@link ApplicationContext application contexts}
 * created for the test instance referenced by this test context. Test instances
 * are therefore automatically candidates for annotation-based dependency
 * injection using
 * {@link org.springframework.beans.factory.annotation.Autowired Autowired} and
 * {@link javax.annotation.Resource Resource}.
 * </p>
 * <p>
 * Note that all
 * {@link ContextConfigurationAttributes#getLocations() configuration locations}
 * are considered to be classpath resources.
 * </p>
 *
 * @param <T> The type of the test managed by this TestContext.
 * @author Sam Brannen
 * @version $Revision: 1.8 $
 * @since 2.1
 */
public class TestContext<T> {

	// ------------------------------------------------------------------------|
	// --- CONSTANTS ----------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/** Class Logger. */
	private static final Log														LOG	= LogFactory.getLog(TestContext.class);

	// ------------------------------------------------------------------------|
	// --- STATIC VARIABLES ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- STATIC INITIALIZATION ----------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- INSTANCE VARIABLES -------------------------------------------------|
	// ------------------------------------------------------------------------|

	private final ContextConfigurationAttributes									configurationAttributes;

	private final ContextCache<ContextConfigurationAttributes, ApplicationContext>	contextCache;

	private Throwable																testException;

	private final Class<T>															testClass;

	private Object																	testInstance;

	private Method																	testMethod;

	// ------------------------------------------------------------------------|
	// --- INSTANCE INITIALIZATION --------------------------------------------|
	// ------------------------------------------------------------------------|

	// ------------------------------------------------------------------------|
	// --- CONSTRUCTORS -------------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * Constructs a new test context for the supplied {@link Class test class}
	 * and {@link ContextCache context cache} and parses the
	 * {@link ContextConfigurationAttributes} configured for the test class via
	 * the
	 * {@link org.springframework.test.annotation.ContextConfiguration @ContextConfiguration}
	 * annotation.
	 *
	 * @param testClass The {@link Class} object corresponding to the test class
	 *        for which the test context should be constructed, not
	 *        <code>null</code>.
	 * @param contextCache The context cache from which the constructed test
	 *        context should retrieve application contexts, not
	 *        <code>null</code>.
	 */
	public TestContext(final Class<T> testClass,
			final ContextCache<ContextConfigurationAttributes, ApplicationContext> contextCache) {

		super();

		Assert.notNull(testClass, "The testClass can not be null.");
		Assert.notNull(contextCache, "The contextCache can not be null.");

		this.testClass = testClass;
		this.contextCache = contextCache;
		this.configurationAttributes = retrieveContextConfigurationAttributes(testClass);
	}

	// ------------------------------------------------------------------------|
	// --- STATIC METHODS -----------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Builds and configures a {@link ApplicationContext} based on the supplied
	 * {@link ContextConfigurationAttributes}.
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
	protected static ApplicationContext buildApplicationContext(final ContextConfigurationAttributes configAttributes)
			throws Exception {

		Assert.notNull(configAttributes, "configAttributes can not be null.");

		final ApplicationContext applicationContext = createContextLoader(configAttributes).loadContext();

		// TODO Remove cast to ConfigurableApplicationContext once we've pushed
		// the context configuration to the ContextLoader.
		final ConfigurableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();

		// TODO Use AnnotationConfigUtils.registerAnnotationConfigProcessors()
		// and move the BeanPostProcessor code to the ContextLoader.

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
				"loaderClass is null: ContextConfigurationAttributes have not been properly initialized.");

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
	 * Retrieves the {@link ContextConfigurationAttributes} for the supplied
	 * {@link Class} which must declare or inherit
	 * {@link ContextConfiguration @ContextConfiguration}.
	 *
	 * @param clazz The class for which the
	 *        {@link ContextConfigurationAttributes} should be constructed.
	 * @return a new ContextConfigurationAttributes instance.
	 * @throws IllegalArgumentException if the supplied class is
	 *         <code>null</code> or if a {@link ContextConfiguration}
	 *         annotation is not present for the supplied class.
	 */
	public static ContextConfigurationAttributes retrieveContextConfigurationAttributes(final Class<?> clazz) {

		Assert.notNull(clazz, "Can not retrieve ContextConfigurationAttributes for a NULL class.");
		final Class<ContextConfiguration> annotationType = ContextConfiguration.class;
		final Class<?> declaringClass = findAnnotationDeclaringClass(annotationType, clazz);
		final ContextConfiguration contextConfiguration = clazz.getAnnotation(annotationType);
		Assert.notNull(contextConfiguration, "@ContextConfiguration must be present for class [" + clazz + "].");
		Assert.notNull(declaringClass, "Could not find an 'annotation declaring class' for annotation type ["
				+ annotationType + "] and class [" + clazz + "].");

		if (LOG.isDebugEnabled()) {
			LOG.debug("ContextConfiguration: test class [" + clazz + "], annotation declaring class [" + declaringClass
					+ "].");
		}

		final ContextConfigurationAttributes configAttributes = new ContextConfigurationAttributes(
				contextConfiguration.loaderClass(), contextConfiguration.locations(), declaringClass,
				contextConfiguration.generateDefaultLocations(), contextConfiguration.resourceSuffix());
		if (LOG.isDebugEnabled()) {
			LOG.debug("Retrieved ContextConfigurationAttributes [" + configAttributes + "] for class [" + clazz + "].");
		}

		return configAttributes;
	}

	// ------------------------------------------------------------------------|
	// --- INSTANCE METHODS ---------------------------------------------------|
	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the {@link ApplicationContext application context} for this test
	 * context, possibly cached.
	 * </p>
	 *
	 * @return The application context.
	 * @throws Exception if an error occurs while retrieving the application
	 *         context.
	 */
	public ApplicationContext getApplicationContext() throws Exception {

		ApplicationContext context;
		final ContextCache<ContextConfigurationAttributes, ApplicationContext> cache = getContextCache();

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
	protected final ContextCache<ContextConfigurationAttributes, ApplicationContext> getContextCache() {

		return this.contextCache;
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
	 * Gets the exception that was thrown during execution of the
	 * {@link #getTestMethod() test method}.
	 * </p>
	 * <p>
	 * Note: this is a mutable property.
	 * </p>
	 *
	 * @see #updateState(Object, Method, Throwable)
	 * @return The exception that was thrown, or <code>null</code> if no
	 *         exception was thrown.
	 */
	public final Throwable getTestException() {

		return this.testException;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the current {@link Object test instance} for this test context.
	 * </p>
	 * <p>
	 * Note: this is a mutable property.
	 * </p>
	 *
	 * @see #updateState(Object, Method, Throwable)
	 * @return The current test instance; may be <code>null</code>.
	 */
	public final Object getTestInstance() {

		return this.testInstance;
	}

	// ------------------------------------------------------------------------|

	/**
	 * <p>
	 * Gets the current {@link Method test method} for this test context.
	 * </p>
	 * <p>
	 * Note: this is a mutable property.
	 * </p>
	 *
	 * @see #updateState(Object, Method, Throwable)
	 * @return The current test method; may be <code>null</code>.
	 */
	public final Method getTestMethod() {

		return this.testMethod;
	}

	// ------------------------------------------------------------------------|

	/**
	 * Call this method to signal that the
	 * {@link ApplicationContext application context} associated with this
	 * {@link TestContext} is <em>dirty</em> and should be reloaded. Do this
	 * if a test has modified the context (for example, by replacing a bean
	 * definition).
	 */
	public void markApplicationContextDirty() {

		getContextCache().setDirty(getConfigurationAttributes());
	}

	// ------------------------------------------------------------------------|

	/**
	 * Updates this test context to reflect the state of the currently executing
	 * test.
	 *
	 * @param testInstance The current test instance; may be <code>null</code>.
	 * @param testMethod The current test method; may be <code>null</code>.
	 * @param testException The exception that was thrown in the test method, or
	 *        <code>null</code> if no exception was thrown.
	 */
	public final void updateState(final Object testInstance, final Method testMethod, final Throwable testException) {

		synchronized (this) {
			this.testInstance = testInstance;
			this.testMethod = testMethod;
		}
	}

	// ------------------------------------------------------------------------|

	/**
	 * Provides a string representation of this test contexts's
	 * {@link #getTestClass() test class},
	 * {@link #getConfigurationAttributes() configuration attributes},
	 * {@link #getTestInstance() test instance},
	 * {@link #getTestMethod() test method}, and
	 * {@link #getTestException() test exception}.
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return new ToStringBuilder(this)

		.append("testClass", getTestClass())

		.append("configurationAttributes", getConfigurationAttributes())

		.append("testInstance", getTestInstance())

		.append("testMethod", getTestMethod())

		.append("testException", getTestException())

		.toString();
	}

	// ------------------------------------------------------------------------|

}
