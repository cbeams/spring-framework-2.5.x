/*
 * Copyright 2002-2005 the original author or authors.
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

import javax.jdo.PersistenceManager;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This interceptor binds a new JDO PersistenceManager to the thread before a method
 * call, closing and removing it afterwards in case of any method outcome.
 * If there already is a pre-bound PersistenceManager (e.g. from JdoTransactionManager,
 * or from a surrounding JDO-intercepted method), the interceptor simply participates in it.
 *
 * <p>Application code must retrieve a JDO PersistenceManager via the
 * <code>PersistenceManagerFactoryUtils.getPersistenceManager</code> method,
 * to be able to detect a thread-bound PersistenceManager. It is preferable to use
 * <code>getPersistenceManager</code> with allowCreate=false, if the code relies on
 * the interceptor to provide proper PersistenceManager handling. Typically, the code
 * will look as follows:
 *
 * <pre>
 * public void doJdoAction() {
 *   PersistenceManager pm = PersistenceManagerFactoryUtils.getPersistenceManager(this.pmf, false);
 *   try {
 *     ...
 *   }
 *   catch (JDOException ex) {
 *     throw PersistenceManagerFactoryUtils.convertJdoAccessException(ex);
 *   }
 * }</pre>
 *
 * Note that the application must care about handling JDOExceptions itself,
 * preferably via delegating to the <code>PersistenceManagerFactoryUtils.convertJdoAccessException</code>
 * method that converts them to exceptions that are compatible with the
 * <code>org.springframework.dao</code> exception hierarchy (like JdoTemplate does).
 *
 * <p>This interceptor could convert unchecked JDOExceptions to unchecked dao ones
 * on-the-fly. The intercepted method wouldn't have to throw any special checked
 * exceptions to be able to achieve this. Nevertheless, such a mechanism would
 * effectively break the contract of the intercepted method (runtime exceptions
 * can be considered part of the contract too), therefore it isn't supported.
 *
 * <p>This class can be considered a declarative alternative to JdoTemplate's
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
 * @since 13.06.2003
 * @see PersistenceManagerFactoryUtils#getPersistenceManager
 * @see JdoTransactionManager
 * @see JdoTemplate
 */
public class JdoInterceptor extends JdoAccessor implements MethodInterceptor {

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		boolean existingTransaction = false;
		PersistenceManager pm = PersistenceManagerFactoryUtils.getPersistenceManager(getPersistenceManagerFactory(), true);
		if (TransactionSynchronizationManager.hasResource(getPersistenceManagerFactory())) {
			logger.debug("Found thread-bound PersistenceManager for JDO interceptor");
			existingTransaction = true;
		}
		else {
			logger.debug("Using new PersistenceManager for JDO interceptor");
			TransactionSynchronizationManager.bindResource(getPersistenceManagerFactory(), new PersistenceManagerHolder(pm));
		}
		try {
			Object retVal = methodInvocation.proceed();
			flushIfNecessary(pm, existingTransaction);
			return retVal;
		}
		finally {
			if (existingTransaction) {
				logger.debug("Not closing pre-bound JDO PersistenceManager after interceptor");
			}
			else {
				TransactionSynchronizationManager.unbindResource(getPersistenceManagerFactory());
				PersistenceManagerFactoryUtils.releasePersistenceManager(pm, getPersistenceManagerFactory());
			}
		}
	}

}
