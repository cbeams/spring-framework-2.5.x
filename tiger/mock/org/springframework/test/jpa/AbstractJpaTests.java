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

package org.springframework.test.jpa;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.instrument.classloading.AbstractLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.orm.jpa.ContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.ExtendedEntityManagerCreator;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.test.annotation.AbstractAnnotationAwareTransactionalTests;
import org.springframework.test.instrument.classloading.ShadowingClassLoader;
import org.springframework.util.StringUtils;

/**
 * Convenient support class for JPA-related tests.
 * Offers the same contract as AbstractTransactionalDataSourceSpringContextTests
 * and equally good performance, even when performing the instrumentation
 * required by the JPA specification.
 * <p/>
 * Exposes an EntityManagerFactory and a shared EntityManager.
 * Requires EntityManagerFactory to be injected, plus DataSource and
 * JpaTransactionManager from superclass.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AbstractJpaTests extends AbstractAnnotationAwareTransactionalTests {

	private static final String DEFAULT_ORM_XML_LOCATION = "META-INF/orm.xml";
	
	/**
	 * Map from String defining unique combination of config locations, to ApplicationContext.
	 * Values are intentionally not strongly typed, to avoid potential class cast exceptions
	 * through use between different class loaders.
	 */
	private static Map<String, Object> contextCache = new HashMap<String, Object>();

	private static Map<String, ClassLoader> classLoaderCache = new HashMap<String, ClassLoader>();

	protected EntityManagerFactory entityManagerFactory;

	private boolean shadowed;

	/**
	 * Subclasses can use this in test cases.
	 * It will participate in any current transaction.
	 */
	protected EntityManager sharedEntityManager;


	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
		this.sharedEntityManager = SharedEntityManagerCreator.createSharedEntityManager(
						this.entityManagerFactory, EntityManager.class);
	}

	/**
	 * Create an EntityManager that will always automatically enlist itself in current
	 * transactions, in contrast to an EntityManager returned by
	 * <code>EntityManagerFactory.createEntityManager()</code>
	 * (which requires an explicit <code>joinTransaction()</code> call).
	 */
	protected EntityManager createContainerManagedEntityManager() {
		return ExtendedEntityManagerCreator.createContainerManagedEntityManager(this.entityManagerFactory);
	}

	@Override
	public void runBare() throws Throwable {
		String combinationOfContextLocationsForThisTestClass = StringUtils.arrayToCommaDelimitedString(getConfigLocations()); 			
		ClassLoader classLoaderForThisTestClass = getClass().getClassLoader();
		if (this.shadowed) {
			Thread.currentThread().setContextClassLoader(classLoaderForThisTestClass);
			super.runBare();
		}
		else {
			ShadowingClassLoader shadowingClassLoader = (ShadowingClassLoader) classLoaderCache.get(combinationOfContextLocationsForThisTestClass);

			if (shadowingClassLoader == null) {
				shadowingClassLoader = (ShadowingClassLoader) createShadowingClassLoader(classLoaderForThisTestClass);
				classLoaderCache.put(combinationOfContextLocationsForThisTestClass, shadowingClassLoader);
			}
			try {
				Thread.currentThread().setContextClassLoader(shadowingClassLoader);
				String[] configLocations = getConfigLocations();

				// Do not strongly type, to avoid ClassCastException
				Object cachedContext = contextCache.get(combinationOfContextLocationsForThisTestClass);

				if (cachedContext == null) {

					// create load time weaver
					Class shadowingLoadTimeWeaverClass = shadowingClassLoader.loadClass(ShadowingLoadTimeWeaver.class.getName());
					Constructor constructor = shadowingLoadTimeWeaverClass.getConstructor(ClassLoader.class);
					constructor.setAccessible(true);
					Object ltw = constructor.newInstance(shadowingClassLoader);

					// create the bean factory
					Class beanFactoryClass = shadowingClassLoader.loadClass(DefaultListableBeanFactory.class.getName());
					Object beanFactory = BeanUtils.instantiateClass(beanFactoryClass);

					// create the BeanDefinitionReader
					Class beanDefinitionReaderClass = shadowingClassLoader.loadClass(XmlBeanDefinitionReader.class.getName());
					Class beanDefinitionRegistryClass = shadowingClassLoader.loadClass(BeanDefinitionRegistry.class.getName());
					Object reader = beanDefinitionReaderClass.getConstructor(beanDefinitionRegistryClass).newInstance(beanFactory);

					// load the bean definitions into the bean factory
					Method loadBeanDefinitions = beanDefinitionReaderClass.getMethod("loadBeanDefinitions", String[].class);
					loadBeanDefinitions.invoke(reader, new Object[]{configLocations});

					// create BeanPostProcessor
					Class loadTimeWeaverInjectingBeanPostProcessorClass = shadowingClassLoader.loadClass(LoadTimeWeaverInjectingBeanPostProcessor.class.getName());
					Class loadTimeWeaverClass = shadowingClassLoader.loadClass(LoadTimeWeaver.class.getName());
					Constructor bppConstructor = loadTimeWeaverInjectingBeanPostProcessorClass.getConstructor(loadTimeWeaverClass);
					bppConstructor.setAccessible(true);
					Object beanPostProcessor = bppConstructor.newInstance(ltw);

					// add BeanPostProcessor
					Class beanPostProcessorClass = shadowingClassLoader.loadClass(BeanPostProcessor.class.getName());
					Method addBeanPostProcessor = beanFactoryClass.getMethod("addBeanPostProcessor", beanPostProcessorClass);
					addBeanPostProcessor.invoke(beanFactory, beanPostProcessor);

					// create the GenericApplicationContext
					Class genericApplicationContextClass = shadowingClassLoader.loadClass(GenericApplicationContext.class.getName());
					Class defaultListableBeanFactoryClass = shadowingClassLoader.loadClass(DefaultListableBeanFactory.class.getName());
					cachedContext = genericApplicationContextClass.getConstructor(defaultListableBeanFactoryClass).newInstance(beanFactory);
					contextCache.put(combinationOfContextLocationsForThisTestClass, cachedContext);

					// refresh
					genericApplicationContextClass.getMethod("refresh").invoke(cachedContext);

				}
				// create the shadowed test
				Class shadowedTestClass = shadowingClassLoader.loadClass(getClass().getName());
				Object testCase = BeanUtils.instantiateClass(shadowedTestClass);

				/* shadowed = true */
				Class thisShadowedClass = shadowingClassLoader.loadClass(AbstractJpaTests.class.getName());
				Field shadowed = thisShadowedClass.getDeclaredField("shadowed");
				shadowed.setAccessible(true);
				shadowed.set(testCase, true);

				/* AbstractSpringContextTests.addContext(Object, ApplicationContext) */
				Class applicationContextClass = shadowingClassLoader.loadClass(ConfigurableApplicationContext.class.getName());
				Method addContextMethod = shadowedTestClass.getMethod("addContext", Object.class, applicationContextClass);
				addContextMethod.invoke(testCase, configLocations, cachedContext);

				/* TestCase.setName(String) */
				Method setNameMethod = shadowedTestClass.getMethod("setName", String.class);
				setNameMethod.invoke(testCase, getName());

				/* TestCase.runBare() */
				Method testMethod = shadowedTestClass.getMethod("runBare");
				testMethod.invoke(testCase, (Object[]) null);
			}
			finally {
				Thread.currentThread().setContextClassLoader(classLoaderForThisTestClass);
			}
		}
	}

	/**
	 * NB: This method must <b>not</b> have a return type of ShadowingClassLoader as that would cause that
	 * class to be loaded eagerly when this test case loads, creating verify errors at runtime.
	 */
	private Object createShadowingClassLoader(ClassLoader classLoader) {
		return new OrmXmlOverridingShadowingClassLoader(classLoader, getSpringResourceStringForOrmXml());		
	}
	
	/**
	 * Subclasses can override this to return the real location path for
	 * orm.xml or null if they do not wish to find any orm.xml
	 * @return orm.xml path or null to hide any such file
	 */
	protected String getSpringResourceStringForOrmXml() {
		return DEFAULT_ORM_XML_LOCATION;
	}


	private static class LoadTimeWeaverInjectingBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

		private final LoadTimeWeaver ltw;

		public LoadTimeWeaverInjectingBeanPostProcessor(LoadTimeWeaver ltw) {
			this.ltw = ltw;
		}

		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			if (bean instanceof ContainerEntityManagerFactoryBean) {
				((ContainerEntityManagerFactoryBean) bean).setLoadTimeWeaver(ltw);
			}
			return bean;
		}
	}

	private static class ShadowingLoadTimeWeaver extends AbstractLoadTimeWeaver {

		private final ClassLoader shadowingClassLoader;

		private final Class shadowingClassLoaderClass;

		public ShadowingLoadTimeWeaver(ClassLoader shadowingClassLoader) {
			this.shadowingClassLoader = shadowingClassLoader;
			this.shadowingClassLoaderClass = shadowingClassLoader.getClass();
		}

		public ClassLoader getInstrumentableClassLoader() {
			return (ClassLoader) shadowingClassLoader;
		}

		public void addClassFileTransformer(ClassFileTransformer classFileTransformer) {
			try {
				Method addClassFileTransformer = shadowingClassLoaderClass.getMethod("addClassFileTransformer", ClassFileTransformer.class);
				addClassFileTransformer.setAccessible(true);
				addClassFileTransformer.invoke(shadowingClassLoader, classFileTransformer);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
