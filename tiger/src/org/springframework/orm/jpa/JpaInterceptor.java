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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This interceptor binds a new JPA EntityManager to the thread before a method
 * call, closing and removing it afterwards in case of any method outcome.
 * If there already is a pre-bound EntityManager (e.g. from JpaTransactionManager,
 * or from a surrounding JPA-intercepted method), the interceptor simply participates in it.
 *
 * <p>Application code must retrieve a JPA EntityManager via the
 * <code>EntityManagerFactoryUtils.doGetEntityManager</code> method,
 * to be able to detect a thread-bound EntityManager. It is preferable to use
 * <code>doGetEntityManager</code> with allowCreate=false, if the code relies on
 * the interceptor to provide proper EntityManager handling. Typically, the code
 * will look as follows:
 *
 * <pre>
 * public void doJpaAction() {
 *   EntityManager em = EntityManagerFactoryUtils.doGetEntityManager(this.emf, false);
 *   try {
 *     ...
 *   }
 *   catch (PersistenceException ex) {
 *     throw EntityManagerFactoryUtils.convertJpaAccessException(ex);
 *   }
 * }</pre>
 *
 * Note that the application must care about handling PersistenceExceptions itself,
 * preferably via delegating to the <code>EntityManagerFactoryUtils.convertJpaAccessException</code>
 * method that converts them to exceptions that are compatible with the
 * <code>org.springframework.dao</code> exception hierarchy (like JpaTemplate does).
 *
 * <p>This interceptor could convert unchecked PersistenceExceptions to unchecked dao ones
 * on-the-fly. The intercepted method wouldn't have to throw any special checked
 * exceptions to be able to achieve this. Nevertheless, such a mechanism would
 * effectively break the contract of the intercepted method (runtime exceptions
 * can be considered part of the contract too), therefore it isn't supported.
 *
 * <p>This class can be considered a declarative alternative to JpaTemplate's
 * callback approach. The advantages are:
 * <ul>
 * <li>no anonymous classes necessary for callback implementations;
 * <li>the possibility to throw any application exceptions from within data access code.
 * </ul>
 *
 * <p>The drawbacks are:
 * <ul>
 * <li>the dependency on interceptor configuration;
 * <li>the delegating try/catch blocks.
 * </ul>
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public class JpaInterceptor extends JpaAccessor implements MethodInterceptor {

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		EntityManager em = null;
		boolean isNewEm = false;
		try {
			try {
				em = EntityManagerFactoryUtils.getEntityManager(getEntityManagerFactory());
			}
			catch (IllegalStateException ex) {
				logger.debug("Creating new EntityManager for JPA interceptor");
				em = getEntityManagerFactory().createEntityManager();
				isNewEm = true;
				TransactionSynchronizationManager.bindResource(getEntityManagerFactory(), new EntityManagerHolder(em));
			}
		}
		catch (PersistenceException ex) {
			throw new DataAccessResourceFailureException("Could not open JPA EntityManager", ex);
		}

		try {
			Object retVal = methodInvocation.proceed();
			flushIfNecessary(em, !isNewEm);
			return retVal;
		}
		finally {
			if (isNewEm) {
				TransactionSynchronizationManager.unbindResource(getEntityManagerFactory());
				logger.debug("Closing new EntityManager for JPA interceptor");
				em.close();
			}
		}
	}

}
