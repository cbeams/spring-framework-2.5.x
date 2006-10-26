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

package org.springframework.orm.jpa;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * Abstract base class for a FactoryBean that creates a local JPA
 * <code>EntityManagerFactory</code> instance with a Spring application context.
 * 
 * <p>Encapsulates the common functionality between the different JPA bootstrap contracts
 * (standalone as well as container).
 * 
 * <p>Implements support for standard JPA configuration as well as Spring's
 * <code>JpaVendorAdapter</code> abstraction, and controls the <code>EntityManagerFactory's</code>
 * lifecycle.
 * 
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 * @see LocalEntityManagerFactoryBean
 * @see LocalContainerEntityManagerFactoryBean
 */
public abstract class AbstractEntityManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean,
		EntityManagerFactoryInfo, PersistenceExceptionTranslator {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private PersistenceProvider persistenceProvider;

	private String persistenceUnitName;

	private JpaVendorAdapter jpaVendorAdapter;

	private final Map jpaPropertyMap = new HashMap();

	private Class entityManagerInterface;

	private JpaDialect jpaDialect;

	/** Raw EntityManagerFactory as returned by the PersistenceProvider */
	public EntityManagerFactory nativeEntityManagerFactory;

	private EntityManagerFactory entityManagerFactory;


	/**
	 * Set the PersistenceProvider implementation class to use for creating the
	 * EntityManagerFactory. If not specified, the persistence provider will be
	 * taken from the JpaVendorAdapter (if any) or retrieved through scanning
	 * (as far as possible).
	 * @see JpaVendorAdapter#getPersistenceProvider()
	 * @see javax.persistence.spi.PersistenceProvider
	 * @see javax.persistence.Persistence
	 */
	public void setPersistenceProviderClass(Class persistenceProviderClass) {
		Assert.notNull(persistenceProviderClass, "persistenceProviderClass must not be null");
		Assert.isTrue(PersistenceProvider.class.isAssignableFrom(persistenceProviderClass),
				"persistenceProviderClass must implement [javax.persistence.spi.PersistenceProvider]");
		this.persistenceProvider = (PersistenceProvider) BeanUtils.instantiateClass(persistenceProviderClass);
	}

	/**
	 * Set the PersistenceProvider instance to use for creating the
	 * EntityManagerFactory. If not specified, the persistence provider will be
	 * taken from the JpaVendorAdapter (if any) or retrieved through scanning
	 * (as far as possible).
	 * @see JpaVendorAdapter#getPersistenceProvider()
	 * @see javax.persistence.spi.PersistenceProvider
	 * @see javax.persistence.Persistence
	 */
	public void setPersistenceProvider(PersistenceProvider persistenceProvider) {
		this.persistenceProvider = persistenceProvider;
	}

	public PersistenceProvider getPersistenceProvider() {
		return persistenceProvider;
	}

	/**
	 * Specify the name of the EntityManagerFactory configuration.
	 * <p>Default is none, indicating the default EntityManagerFactory
	 * configuration. The persistence provider will throw an exception if
	 * ambiguous EntityManager configurations are found.
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String)
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String,
	 *      java.util.Map)
	 */
	public void setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	/**
	 * Specify the JpaVendorAdapter implementation for the desired JPA provider,
	 * if any. This will initialize appropriate defaults for the given provider,
	 * such as persistence provider class and JpaDialect, unless locally
	 * overridden in this FactoryBean.
	 */
	public void setJpaVendorAdapter(JpaVendorAdapter jpaVendorAdapter) {
		this.jpaVendorAdapter = jpaVendorAdapter;
	}

	/**
	 * Specify JPA properties, to be passed into
	 * <code>Persistence.createEntityManagerFactory</code> (if any).
	 * <p>Can be populated with a String "value" (parsed via PropertiesEditor) or a
	 * "props" element in XML bean definitions.
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String,
	 *      java.util.Map)
	 */
	public void setJpaProperties(Properties jpaProperties) {
		CollectionUtils.mergePropertiesIntoMap(jpaProperties, this.jpaPropertyMap);
	}

	/**
	 * Specify JPA properties as a Map, to be passed into
	 * <code>Persistence.createEntityManagerFactory</code> (if any).
	 * <p>Can be populated with a "map" or "props" element in XML bean definitions.
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String,
	 *      java.util.Map)
	 */
	public void setJpaPropertyMap(Map jpaProperties) {
		if (jpaProperties != null) {
			this.jpaPropertyMap.putAll(jpaProperties);
		}
	}

	/**
	 * Allow Map access to the JPA properties to be passed to the persistence
	 * provider, with the option to add or override specific entries.
	 * <p>Useful for specifying entries directly, for example via
	 * "jpaPropertyMap[myKey]".
	 */
	public Map getJpaPropertyMap() {
		return jpaPropertyMap;
	}

	/**
	 * Specify the (potentially vendor-specific) EntityManager interface that
	 * this factory's EntityManagers are supposed to implement.
	 * <p>The default will be taken from the specific JpaVendorAdapter, if any, or
	 * set to the standard <code>javax.persistence.EntityManager</code>
	 * interface else.
	 * @see EntityManagerFactoryInfo#getEntityManagerInterface()
	 */
	public void setEntityManagerInterface(Class entityManagerInterface) {
		this.entityManagerInterface = entityManagerInterface;
	}

	public Class getEntityManagerInterface() {
		return this.entityManagerInterface;
	}

	/**
	 * Specify the vendor-specific JpaDialect implementation to associate with
	 * this EntityManagerFactory. This will be exposed through the
	 * EntityManagerFactoryInfo interface, to be picked up as default dialect by
	 * accessors that intend to use JpaDialect functionality.
	 * @see EntityManagerFactoryInfo#getJpaDialect()
	 */
	public void setJpaDialect(JpaDialect jpaDialect) {
		this.jpaDialect = jpaDialect;
	}

	public JpaDialect getJpaDialect() {
		return jpaDialect;
	}


	public final void afterPropertiesSet() throws PersistenceException {
		if (this.jpaVendorAdapter != null) {
			if (this.persistenceProvider == null) {
				this.persistenceProvider = this.jpaVendorAdapter.getPersistenceProvider();
			}
			Map vendorPropertyMap = this.jpaVendorAdapter.getJpaPropertyMap();
			if (vendorPropertyMap != null) {
				for (Iterator it = vendorPropertyMap.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					if (!this.jpaPropertyMap.containsKey(entry.getKey())) {
						this.jpaPropertyMap.put(entry.getKey(), entry.getValue());
					}
				}
			}
			if (this.entityManagerInterface == null) {
				this.entityManagerInterface = this.jpaVendorAdapter.getEntityManagerInterface();
			}
			if (this.jpaDialect == null) {
				this.jpaDialect = this.jpaVendorAdapter.getJpaDialect();
			}
		}
		else {
			if (this.entityManagerInterface == null) {
				this.entityManagerInterface = EntityManager.class;
			}
		}

		this.nativeEntityManagerFactory = createNativeEntityManagerFactory();
		if (this.jpaVendorAdapter != null) {
			this.jpaVendorAdapter.postProcessEntityManagerFactory(this.nativeEntityManagerFactory);
		}

		// Wrap the EntityManagerFactory in a factory implementing all its
		// interfaces.
		// This allows interception of createEntityManager methods to return an
		// application-managed EntityManager proxy that automatically joins
		// existing transactions.
		this.entityManagerFactory = createEntityManagerFactoryProxy(this.nativeEntityManagerFactory);
	}

	/**
	 * Subclasses must implement this method to create the EntityManagerFactory
	 * that will be returned by the getObject() method
	 * @return EntityManagerFactory instance returned by this FactoryBean
	 * @throws PersistenceException if the EntityManager cannot be created
	 */
	protected abstract EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException;

	/**
	 * Create a proxy of the given EntityManagerFactory. We do this to be able
	 * to return transaction-aware proxies for application-managed
	 * EntityManagers, and to introduce the NamedEntityManagerFactory interface
	 * @param emf EntityManagerFactory as returned by the persistence provider
	 * @return proxy entity manager
	 */
	protected EntityManagerFactory createEntityManagerFactoryProxy(EntityManagerFactory emf) {
		// Automatically implement all interfaces implemented by the EntityManagerFactory
		Class[] ifcs = ClassUtils.getAllInterfaces(emf);
		ifcs = (Class[]) ObjectUtils.addObjectToArray(ifcs, EntityManagerFactoryInfo.class);
		EntityManagerFactoryPlusOperations plusOperations = null;
		if (getJpaDialect() != null && getJpaDialect().supportsEntityManagerFactoryPlusOperations()) {
			plusOperations = getJpaDialect().getEntityManagerFactoryPlusOperations(emf);
			ifcs = (Class[]) ObjectUtils.addObjectToArray(ifcs, EntityManagerFactoryPlusOperations.class);
		}
		return (EntityManagerFactory) Proxy.newProxyInstance(getClass().getClassLoader(), ifcs,
				new ManagedEntityManagerFactoryInvocationHandler(emf, this, plusOperations));
	}

	/**
	 * Implementation of the PersistenceExceptionTranslator interface, as
	 * autodetected by Spring's PersistenceExceptionTranslationPostProcessor.
	 * <p>Uses the dialect's conversion if possible; otherwise falls back to
	 * standard JPA exception conversion.
	 * @see org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
	 * @see JpaDialect#translateExceptionIfPossible
	 * @see EntityManagerFactoryUtils#convertJpaAccessExceptionIfPossible
	 */
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		return (this.jpaDialect != null ? this.jpaDialect.translateExceptionIfPossible(ex)
				: EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible(ex));
	}

	public EntityManagerFactory getNativeEntityManagerFactory() {
		return this.nativeEntityManagerFactory;
	}

	public PersistenceUnitInfo getPersistenceUnitInfo() {
		return null;
	}

	public DataSource getDataSource() {
		return null;
	}

	/**
	 * Return the singleton EntityManagerFactory.
	 */
	public EntityManagerFactory getObject() {
		return this.entityManagerFactory;
	}

	public Class getObjectType() {
		return (this.entityManagerFactory != null ? this.entityManagerFactory.getClass() : EntityManagerFactory.class);
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Close the EntityManagerFactory on bean factory shutdown.
	 */
	public void destroy() {
		logger.info("Closing JPA EntityManagerFactory for <" + persistenceUnitName + ">");
		this.entityManagerFactory.close();
	}


	/**
	 * Dynamic proxy invocation handler proxying an EntityManagerFactory to
	 * return a proxy EntityManager if necessary from createEntityManager()
	 * methods.
	 */
	private static class ManagedEntityManagerFactoryInvocationHandler implements InvocationHandler {

		private final EntityManagerFactory targetEntityManagerFactory;

		private final EntityManagerFactoryInfo entityManagerFactoryInfo;

		private final EntityManagerFactoryPlusOperations entityManagerFactoryPlusOperations;

		private final JpaDialect jpaDialect;

		public ManagedEntityManagerFactoryInvocationHandler(EntityManagerFactory targetEmf,
				EntityManagerFactoryInfo emfInfo, EntityManagerFactoryPlusOperations entityManagerFactoryPlusOperations) {

			this.targetEntityManagerFactory = targetEmf;
			this.entityManagerFactoryInfo = emfInfo;
			this.entityManagerFactoryPlusOperations = entityManagerFactoryPlusOperations;
			this.jpaDialect = emfInfo.getJpaDialect();
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				if (method.getDeclaringClass().isAssignableFrom(EntityManagerFactoryInfo.class)) {
					return method.invoke(this.entityManagerFactoryInfo, args);
				}
				if (method.getDeclaringClass().equals(EntityManagerFactoryPlusOperations.class)) {
					return method.invoke(this.entityManagerFactoryPlusOperations, args);
				}
				Object retVal = method.invoke(this.targetEntityManagerFactory, args);
				if (retVal instanceof EntityManager) {
					EntityManager rawEntityManager = (EntityManager) retVal;
					EntityManagerPlusOperations plusOperations = null;
					if (this.jpaDialect != null && this.jpaDialect.supportsEntityManagerPlusOperations()) {
						plusOperations = this.jpaDialect.getEntityManagerPlusOperations(rawEntityManager);
					}
					retVal = ExtendedEntityManagerCreator.createApplicationManagedEntityManager(rawEntityManager,
							plusOperations);
				}
				return retVal;
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
