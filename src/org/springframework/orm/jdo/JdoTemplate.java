/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.orm.jdo;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helper class that simplifies JDO data access code, and converts
 * JDOExceptions into JdoUsage/JdoSystemException, compatible to the
 * org.springframework.dao exception hierarchy.
 *
 * <p>The central method is "execute", supporting JDO code implementing
 * the JdoCallback interface. It provides JDO PersistenceManager handling
 * such that neither theJdoCallback implementation nor the calling code
 * needs to explicitly care about retrieving/closing PersistenceManagers,
 * or handling JDO lifecycle exceptions.
 *
 * <p>Typically used to implement data access or business logic services that
 * use JDO within their implementation but are JDO-agnostic in their interface.
 * The latter resp. code calling the latter only have to deal with business
 * objects, query objects, and org.springframework.dao exceptions.
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a PersistenceManagerFactory reference, or get prepared in an
 * application context and given to services as bean reference.
 * Note: The PersistenceManagerFactory should always be configured as bean in
 * the application context, in the first case given to the service directly,
 * in the second case to the prepared template.
 *
 * <p>This class can be considered a programmatic alternative to
 * JdoInterceptor. The major advantage is its straightforwardness, the
 * major disadvantage that no checked application exceptions can get thrown
 * from within data access code. Respective checks and the actual throwing of
 * such exceptions can often be deferred to after callback execution, though.
 *
 * <p>Note that even if JdoTransactionManager is used for transaction
 * demarcation in higher-level services, all those services above the data
 * access layer don't need need to be JDO-aware. Setting such a special
 * PlatformTransactionManager is a configuration issue, without introducing
 * code dependencies.
 *
 * <p>LocalPersistenceManagerFactoryBean is the preferred way of obtaining a
 * reference to a specific PersistenceManagerFactory, at least in a non-EJB
 * environment. Registering a PersistenceManagerFactory with JNDI is only
 * advisable when using a JCA Connector, i.e. when the application server
 * cares for initialization. Else, portability is rather limited: Manual
 * JNDI binding isn't supported by some application servers (e.g. Tomcat).
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see JdoCallback
 * @see JdoTransactionManager
 */
public class JdoTemplate extends JdoAccessor {

	private boolean allowCreate = true;

	/**
	 * Create a new JdoTemplate instance.
	 */
	public JdoTemplate() {
	}

	/**
	 * Create a new JdoTemplate instance.
	 * @param pmf PersistenceManagerFactory to create PersistenceManagers
	 */
	public JdoTemplate(PersistenceManagerFactory pmf) {
		setPersistenceManagerFactory(pmf);
		afterPropertiesSet();
	}

	/**
	 * Set if a new PersistenceManager should be created if no thread-bound found.
	 * <p>JdoTemplate is aware of a respective PersistenceManager bound to the
	 * current thread, for example when using JdoTransactionManager.
	 * If allowCreate is true, a new PersistenceManager will be created if none
	 * found. If false, an IllegalStateException will get thrown in this case.
	 * @see PersistenceManagerFactoryUtils#getPersistenceManager
	 */
	public void setAllowCreate(boolean allowCreate) {
		this.allowCreate = allowCreate;
	}

	/**
	 * Return if a new Session should be created if no thread-bound found.
	 */
	public boolean isAllowCreate() {
		return allowCreate;
	}

	/**
	 * Execute the action specified by the given action object within a
	 * PersistenceManager. Application exceptions thrown by the action object
	 * get propagated to the caller (can only be unchecked). JDO exceptions
	 * are transformed into appropriate DAO ones. Allows for returning a
	 * result object, i.e. a domain object or a collection of domain objects.
	 * <p>Note: Callback code is not supposed to handle transactions itself!
	 * Use an appropriate transaction manager like JdoTransactionManager.
	 * @param action action object that specifies the JDO action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException in case of JDO errors
	 * @see JdoTransactionManager
	 * @see org.springframework.dao
	 * @see org.springframework.transaction
	 */
	public Object execute(JdoCallback action) throws DataAccessException {
		PersistenceManager pm = PersistenceManagerFactoryUtils.getPersistenceManager(getPersistenceManagerFactory(), this.allowCreate);
		boolean existingTransaction = TransactionSynchronizationManager.hasResource(getPersistenceManagerFactory());
		try {
			Object result = action.doInJdo(pm);
			flushIfNecessary(pm, existingTransaction);
			return result;
		}
		catch (JDOException ex) {
			throw convertJdoAccessException(ex);
		}
		catch (RuntimeException ex) {
			// callback code threw application exception
			throw ex;
		}
		finally {
			PersistenceManagerFactoryUtils.closePersistenceManagerIfNecessary(pm, getPersistenceManagerFactory());
		}
	}

}
