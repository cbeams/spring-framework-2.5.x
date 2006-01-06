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
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

/**
 * FactoryBean that creates a local JPA EntityManagerFactory instance.
 * Behaves like a EntityManagerFactory instance when used as bean
 * reference, e.g. for JpaTemplate's "entityManagerFactory" property.
 * Note that switching to a JndiObjectFactoryBean or a bean-style
 * EntityManagerFactory instance is just a matter of configuration!
 *
 * <p>The typical usage will be to register this as singleton factory
 * (for a certain underlying data source) in an application context,
 * and give bean references to application services that need it.
 *
 * <p>Configuration settings are usually read in from a <code>persistence.xml</code>
 * config file, residing in the class path - according to the JPA spec's
 * bootstrap mechanism. See the Java Persistence API specification for details.
 *
 * <p>This EntityManager handling strategy is most appropriate for
 * applications that solely use JPA for data access. If you want to set up
 * your persistence provider for global transactions, you will need to
 * deploy it into a full J2EE application server and access the deployed
 * EntityManagerFactory through JNDI (-> JndiObjectFactoryBean).
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see JpaTemplate#setEntityManagerFactory
 * @see JpaTransactionManager#setEntityManagerFactory
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see javax.persistence.EntityManagerFactory#close
 */
public class LocalEntityManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private EntityManagerFactory targetEntityManagerFactory;

	private Class persistenceProviderClass;

	private String entityManagerName;

	private EntityManagerFactory entityManagerFactory;

	private Properties jpaProperties;

	private boolean exposeTransactionAwareEntityManagerFactory = true;


	/**
	 * Specify a target EntityManagerFactory to delegate to.
	 * <p>This property will override any local factory settings, not
	 * letting this FactoryBean create its own EntityManagerFactory.
	 * This is mainly useful to create a Spring transaction-aware
	 * EntityManagerFactory proxy for an existing EntityManagerFactory
	 * as fetched from an application server's JNDI environment.
	 * @see #setExposeTransactionAwareEntityManagerFactory
	 */
	public void setTargetEntityManagerFactory(EntityManagerFactory targetEntityManagerFactory) {
		this.targetEntityManagerFactory = targetEntityManagerFactory;
	}

	/**
	 * Set the PersistenceProvider implementation class to use for creating
	 * the EntityManagerFactory. If not specified (which is the default),
	 * the <code>Persistence</code> class will be used to create the
	 * EntityManagerFactory, relying on JPA's autodetection mechanism.
	 * @see javax.persistence.spi.PersistenceProvider
	 */
	public void setPersistenceProviderClass(Class persistenceProviderClass) {
		if (persistenceProviderClass != null &&
				!PersistenceProvider.class.isAssignableFrom(persistenceProviderClass)) {
			throw new IllegalArgumentException(
					"serviceFactoryClass must implement [javax.persistence.spi.PersistenceProvider]");
		}
		this.persistenceProviderClass = persistenceProviderClass;
	}

	/**
	 * Set the name of the EntityManager configuration for the factory.
	 * <p>Default is none, indicating the default EntityManager configuration.
	 * The persistence provider will throw an exception if ambiguous
	 * EntityManager configurations are found.
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String)
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String, java.util.Map)
	 */
	public void setEntityManagerName(String entityManagerName) {
		this.entityManagerName = entityManagerName;
	}

	/**
	 * Set JPA properties, to be passed into
	 * <code>Persistence.createEntityManagerFactory</code> (if any).
	 * @see javax.persistence.Persistence#createEntityManagerFactory(String, java.util.Map)
	 */
	public void setJpaProperties(Properties jpaProperties) {
		this.jpaProperties = jpaProperties;
	}

	/**
	 * Set whether to expose a transaction-aware proxy for the EntityManagerFactory,
	 * returning the EntityManager that's associated with the current Spring-managed
	 * transaction on <code>doGetEntityManager()</code>, if any.
	 * <p>Default is "true", letting data access code work with the plain
	 * JPA EntityManagerFactory and its <code>doGetEntityManager()</code> method,
	 * while still being able to participate in current Spring-managed transactions:
	 * with any transaction management strategy, either local or JTA / EJB CMT,
	 * and any transaction synchronization mechanism, either Spring or JTA.
	 * Furthermore, <code>doGetEntityManager()</code> will also seamlessly work with a
	 * request-scoped EntityManager managed by OpenEntityManagerInViewFilter/Interceptor.
	 * <p>Turn this flag off to expose the plain JPA EntityManagerFactory with
	 * JPA's default <code>doGetEntityManager()</code> behavior, which usually only
	 * supports plain JTA synchronization through the JTA TransactionManager.
	 * @see javax.persistence.EntityManagerFactory#getEntityManager()
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 * @see JpaTransactionManager
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewFilter
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor
	 */
	public void setExposeTransactionAwareEntityManagerFactory(boolean exposeTransactionAwareEntityManagerFactory) {
		this.exposeTransactionAwareEntityManagerFactory = exposeTransactionAwareEntityManagerFactory;
	}


	/**
	 * Initialize the EntityManagerFactory for the given configuration.
	 * @throws javax.persistence.PersistenceException in case of JPA initialization errors
	 */
	public final void afterPropertiesSet() throws PersistenceException {
		EntityManagerFactory emf = null;

		if (this.targetEntityManagerFactory != null) {
			// Use specified target EntityManagerFactory.
			emf = this.targetEntityManagerFactory;
		}
		else if (this.persistenceProviderClass != null) {
			// Create EntityManagerFactory directly through PersistenceProvider.
			PersistenceProvider pp =
					(PersistenceProvider) BeanUtils.instantiateClass(this.persistenceProviderClass);
			emf = pp.createEntityManagerFactory(this.entityManagerName, this.jpaProperties);
			if (emf == null) {
				throw new IllegalStateException(
						"PersistenceProvider [" + this.persistenceProviderClass.getName() +
						"] did not return an EntityManagerFactory for name '" + this.entityManagerName + "'");
			}
		}
		else {
			// Let JPA perform its PersistenceProvider autodetection.
			emf = Persistence.createEntityManagerFactory(this.entityManagerName, this.jpaProperties);
		}

		// Wrap EntityManagerFactory with transaction-aware proxy, if demanded.
		if (this.exposeTransactionAwareEntityManagerFactory) {
			this.entityManagerFactory = getTransactionAwareEntityManagerFactory(emf);
		}
		else {
			this.entityManagerFactory = emf;
		}
	}

	/**
	 * Wrap the given EntityManagerFactory with a proxy that delegates every method call
	 * to it but delegates <code>doGetEntityManager</code> calls to EntityManagerFactoryUtils,
	 * for participating in Spring-managed transactions.
	 * @param target the original EntityManagerFactory to wrap
	 * @return the wrapped EntityManagerFactory
	 * @see javax.persistence.EntityManagerFactory#getEntityManager()
	 * @see EntityManagerFactoryUtils#doGetEntityManager(javax.persistence.EntityManagerFactory)
	 */
	protected EntityManagerFactory getTransactionAwareEntityManagerFactory(EntityManagerFactory target) {
		Class[] ifcs = ClassUtils.getAllInterfaces(target);
		return (EntityManagerFactory) Proxy.newProxyInstance(
				getClass().getClassLoader(), ifcs, new TransactionAwareInvocationHandler(target));
	}


	/**
	 * Return the singleton EntityManagerFactory.
	 */
	public Object getObject() {
		return this.entityManagerFactory;
	}

	public Class getObjectType() {
		return (this.entityManagerFactory != null) ?
		    this.entityManagerFactory.getClass() : EntityManagerFactory.class;
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
	 * Invocation handler that delegates <code>doGetEntityManager()</code> calls
	 * to EntityManagerFactoryUtils, for being aware of thread-bound transactions.
	 */
	private static class TransactionAwareInvocationHandler implements InvocationHandler {

		private final EntityManagerFactory target;

		public TransactionAwareInvocationHandler(EntityManagerFactory target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on EntityManagerFactory interface coming in...

			if (method.getName().equals("getEntityManager")) {
				// Handle getEntityManager method: return transactional EntityManager, if any.
				try {
					return EntityManagerFactoryUtils.doGetEntityManager((EntityManagerFactory) proxy);
				}
				catch (IllegalStateException springEx) {
					// Not within a Spring-managed transaction with Spring synchronization:
					// delegate to persistence provider's own synchronization mechanism.
					// Will throw IllegalStateException if not supported by persistence provider.
					try {
						return this.target.getEntityManager();
					}
					catch (IllegalStateException jpaEx) {
						throw new IllegalStateException(
								"Could not obtain transactional EntityManager: neither from Spring [" +
								springEx.getMessage() + "] nor from JPA provider [" + jpaEx.getMessage() + "]");
					}
				}
			}
			else if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of SessionFactory proxy.
				return new Integer(hashCode());
			}

			// Invoke method on target EntityManagerFactory.
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
