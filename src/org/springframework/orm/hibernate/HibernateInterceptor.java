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

package org.springframework.orm.hibernate;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This interceptor binds a new Hibernate Session to the thread before a method
 * call, closing and removing it afterwards in case of any method outcome.
 * If there already is a pre-bound Session (e.g. from HibernateTransactionManager,
 * or from a surrounding Hibernate-intercepted method), the interceptor simply
 * participates in it.
 *
 * <p>Application code must retrieve a Hibernate Session via the
 * <code>SessionFactoryUtils.getSession</code> method, to be able to detect a
 * thread-bound Session. It is preferable to use <code>getSession</code> with
 * allowCreate=false, if the code relies on the interceptor to provide proper
 * Session handling. Typically, the code will look like as follows:
 *
 * <pre>
 * public void doSomeDataAccessAction() {
 *   Session session = SessionFactoryUtils.getSession(this.sessionFactory, false);
 *   try {
 *     ...
 *   }
 *   catch (HibernateException ex) {
 *     throw SessionFactoryUtils.convertHibernateAccessException(ex);
 *   }
 * }</pre>
 *
 * Note that the application must care about handling HibernateExceptions itself,
 * preferably via delegating to the <code>SessionFactoryUtils.convertHibernateAccessException</code>
 * method that converts them to exceptions that are compatible with the
 * <code>org.springframework.dao</code> exception hierarchy (like HibernateTemplate does).
 *
 * <p>Unfortunately, this interceptor cannot convert checked HibernateExceptions
 * to unchecked dao ones transparently. The intercepted method would have to declare
 * the checked HibernateException - thus the caller would still have to catch or
 * rethrow it, even if it will never be thrown if intercepted. Any such exception
 * will nevertheless get converted by default.
 *
 * <p>This class can be considered a declarative alternative to HibernateTemplate's
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
 * <p>Note: Spring's Hibernate support in this package requires Hibernate 2.1.
 * Dedicated Hibernate3 support can be found in a separate package:
 * <code>org.springframework.orm.hibernate3</code>.
 *
 * @author Juergen Hoeller
 * @since 13.06.2003
 * @see SessionFactoryUtils#getSession
 * @see HibernateTransactionManager
 * @see HibernateTemplate
 */
public class HibernateInterceptor extends HibernateAccessor implements MethodInterceptor {

	private boolean exceptionConversionEnabled = true;


	/**
	 * Set whether to convert any HibernateException raised to a Spring DataAccessException,
	 * compatible with the <code>org.springframework.dao</code> exception hierarchy.
	 * <p>Default is "true". Turn this flag off to let the caller receive raw exceptions
	 * as-is, without any wrapping. Note that this means that the DAO methods will have
	 * to declare the checked HibernateException, and callers will be forced to handle it.
	 * @see org.springframework.dao.DataAccessException
	 */
	public void setExceptionConversionEnabled(boolean exceptionConversionEnabled) {
		this.exceptionConversionEnabled = exceptionConversionEnabled;
	}


	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		Session session = getSession();
		boolean existingTransaction = SessionFactoryUtils.isSessionTransactional(session, getSessionFactory());

		if (existingTransaction) {
			logger.debug("Found thread-bound Session for HibernateInterceptor");
		}
		else {
			TransactionSynchronizationManager.bindResource(getSessionFactory(), new SessionHolder(session));
		}

		FlushMode previousFlushMode = null;
		try {
			previousFlushMode = applyFlushMode(session, existingTransaction);
			Object retVal = methodInvocation.proceed();
			flushIfNecessary(session, existingTransaction);
			return retVal;
		}
		catch (HibernateException ex) {
			if (this.exceptionConversionEnabled) {
				throw convertHibernateAccessException(ex);
			}
			else {
				throw ex;
			}
		}
		finally {
			if (existingTransaction) {
				logger.debug("Not closing pre-bound Hibernate Session after HibernateInterceptor");
				if (previousFlushMode != null) {
					session.setFlushMode(previousFlushMode);
				}
			}
			else {
				TransactionSynchronizationManager.unbindResource(getSessionFactory());
				SessionFactoryUtils.closeSessionOrRegisterDeferredClose(session, getSessionFactory());
			}
		}
	}

	/**
	 * Return a Session for use by this interceptor.
	 * @see SessionFactoryUtils#getSession
	 */
	protected Session getSession() {
		return SessionFactoryUtils.getSession(
				getSessionFactory(), getEntityInterceptor(), getJdbcExceptionTranslator());
	}

}
