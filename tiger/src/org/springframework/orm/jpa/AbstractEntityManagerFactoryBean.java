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
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

/**
 * Abstract support for FactoryBeans that create a local JPA 
 * EntityManagerFactory instance.
 *
 * <p>Behaves like a EntityManagerFactory instance when used as bean
 * reference, e.g. for JpaTemplate's "entityManagerFactory" property.
 * Note that switching to a JndiObjectFactoryBean or a bean-style
 * EntityManagerFactory instance is just a matter of configuration!
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see JpaTemplate#setEntityManagerFactory
 * @see JpaTransactionManager#setEntityManagerFactory
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public abstract class AbstractEntityManagerFactoryBean
		implements FactoryBean, InitializingBean, DisposableBean, EntityManagerFactoryInfo {

	protected final Log logger = LogFactory.getLog(getClass());

	private EntityManagerFactory entityManagerFactory;

	private Class persistenceProviderClass;

	private String persistenceUnitName;

	private Map jpaPropertyMap;

	/**
	 * EntityManagerFactory directly returned by the PersistenceProvider
	 */
	public EntityManagerFactory nativeEntityManagerFactory;


	/**
	 * Set the PersistenceProvider implementation class to use for creating
	 * the EntityManagerFactory. If not specified (which is the default),
	 * the <code>Persistence</code> class will be used to create the
	 * EntityManagerFactory, relying on JPA's autodetection mechanism.
	 * @see javax.persistence.spi.PersistenceProvider
	 * @see javax.persistence.Persistence
	 */
	public void setPersistenceProviderClass(Class persistenceProviderClass) {
		if (persistenceProviderClass != null &&
				!PersistenceProvider.class.isAssignableFrom(persistenceProviderClass)) {
			throw new IllegalArgumentException(
					"serviceFactoryClass must implement [javax.persistence.spi.PersistenceProvider]");
		}
		this.persistenceProviderClass = persistenceProviderClass;
	}

	protected Class getPersistenceProviderClass() {
		return persistenceProviderClass;
	}

	/**
	 * Set the name of the EntityManager configuration for the factory.
	 * <p>Default is none, indicating the default EntityManager configuration.
	 * The persistence provider will throw an exception if ambiguous
	 * EntityManager configurations are found.
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String)
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String, java.util.Map)
	 */
	public void setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}
	
	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	/**
	 * Set JPA properties, to be passed into
	 * <code>Persistence.createEntityManagerFactory</code> (if any).
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String, java.util.Map)
	 */
	public void setJpaProperties(Properties jpaProperties) {
		this.jpaPropertyMap = jpaProperties;
	}
	
	public void setJpaPropertyMap(Map map) {
		this.jpaPropertyMap = map;
	}
	
	public Map getJpaPropertyMap() {
		return jpaPropertyMap;
	}


	public final void afterPropertiesSet() throws PersistenceException {
		this.nativeEntityManagerFactory = createNativeEntityManagerFactory();

		// Wrap the EntityManagerFactory in a factory implementing all its interfaces
		// This allows interception of createEntityManager methods to return an
		// application-managed EntityManager proxy that automatically joins existing
		// transactions.
		this.entityManagerFactory = createEntityManagerFactoryProxy(nativeEntityManagerFactory);
	}

	/**
	 * Subclasses must implement this method to create the EntityManagerFactory that
	 * will be returned by the getObject() method
	 * @return EntityManagerFactory instance returned by this FactoryBean
	 * @throws PersistenceException if the EntityManager cannot be created
	 */
	protected abstract EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException;

	/**
	 * Create a proxy of the given EntityManagerFactory. We do this to be able to
	 * return transaction-aware proxies for application-managed EntityManagers,
	 * and to introduce the NamedEntityManagerFactory interface
	 * @param emf EntityManagerFactory return by the persistence provider
	 * @return proxy entity manager
	 */
	protected EntityManagerFactory createEntityManagerFactoryProxy(EntityManagerFactory emf) {
		// Automatically implement all interfaces implemented by the EntityManagerFactory.
		Set ifcs = ClassUtils.getAllInterfacesAsSet(emf);
		ifcs.add(EntityManagerFactoryInfo.class);
		return (EntityManagerFactory) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				(Class[]) ifcs.toArray(new Class[ifcs.size()]),
				new ManagedEntityManagerFactoryInvocationHandler(emf));
	}


	public EntityManagerFactory getNativeEntityManagerFactory() {
		return this.nativeEntityManagerFactory;
	}

	public PersistenceUnitInfo getPersistenceUnitInfo() {
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
		logger.info("Closing JPA EntityManagerFactory");
		this.entityManagerFactory.close();
	}


	/**
	 * Dynamic proxy invocation handler proxying an EntityManagerFactory to return a proxy
	 * EntityManager if necessary from createEntityManager() methods.
	 */
	private class ManagedEntityManagerFactoryInvocationHandler implements InvocationHandler {
		
		private final EntityManagerFactory targetEntityManagerFactory;
		
		public ManagedEntityManagerFactoryInvocationHandler(
				EntityManagerFactory targetEntityManagerFactory) {

			this.targetEntityManagerFactory = targetEntityManagerFactory;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				if (method.getDeclaringClass().equals(EntityManagerFactoryInfo.class)) {
					return method.invoke(AbstractEntityManagerFactoryBean.this, args);
				}
				Object retVal = method.invoke(targetEntityManagerFactory, args);
				if (retVal instanceof EntityManager) {
					retVal = ExtendedEntityManagerCreator.createApplicationManagedEntityManager(
							(EntityManager) retVal);
				}
				return retVal;
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
