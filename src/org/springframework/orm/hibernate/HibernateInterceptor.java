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

package org.springframework.orm.hibernate;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.Session;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This interceptor binds a new Hibernate Session to the thread before a method
 * call, closing and removing it afterwards in case of any method outcome.
 * If there already was a pre-bound Session (e.g. from HibernateTransactionManager,
 * or from a surrounding Hibernate-intercepted method), the interceptor simply
 * takes part in it.
 *
 * <p>Application code must retrieve a Hibernate Session via SessionFactoryUtils'
 * getSession method, to be able to detect a thread-bound Session. It is preferable
 * to use getSession with allowCreate=false, as the code relies on the interceptor
 * to provide proper Session handling. Typically the code will look as follows:
 *
 * <pre>
 * public void doHibernateAction() {
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
 * preferably via delegating to SessionFactoryUtils' convertHibernateAccessException
 * that converts them to ones that are compatible with the org.springframework.dao
 * exception hierarchy (like HibernateTemplate does).
 *
 * <p>Unfortunately, this interceptor cannot convert checked HibernateExceptions
 * to unchecked dao ones automatically. The intercepted method would have to throw
 * HibernateException to be able to achieve this - thus the caller would still have
 * to catch or rethrow it, even if it will never be thrown if intercepted.
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
 * <p>Note: Spring's Hibernate support requires Hibernate 2.1 (as of Spring 1.0).
 *
 * @author Juergen Hoeller
 * @since 13.06.2003
 * @see SessionFactoryUtils#getSession
 * @see HibernateTransactionManager
 * @see HibernateTemplate
 */
public class HibernateInterceptor extends HibernateAccessor implements MethodInterceptor, BeanFactoryAware {

	private String entityInterceptorBeanName;

	/**
	 * Just needed for entityInterceptorBeanName.
	 * @see #setEntityInterceptorBeanName
	 */
	private BeanFactory beanFactory;


	/**
	 * Set the bean name of a Hibernate entity interceptor that allows to inspect
	 * and change property values before writing to and reading from the database.
	 * Will get applied to any new Session created by this transaction manager.
	 * <p>Requires the bean factory to be known, to be able to resolve the bean
	 * name to an interceptor instance on session creation. Typically used for
	 * prototype interceptors, i.e. a new interceptor instance per session.
	 * <p>Can also be used for shared interceptor instances, but it is recommended
	 * to set the interceptor reference directly in such a scenario.
	 * @param entityInterceptorBeanName the name of the entity interceptor in
	 * the bean factory
	 * @see #setBeanFactory
	 * @see #setEntityInterceptor
	 */
	public void setEntityInterceptorBeanName(String entityInterceptorBeanName) {
		this.entityInterceptorBeanName = entityInterceptorBeanName;
		setEntityInterceptor(null);
	}

	/**
	 * Return the current Hibernate entity interceptor, or null if none.
	 * Resolves an entity interceptor bean name via the bean factory,
	 * if necessary.
	 * @throws IllegalStateException if bean name specified but no bean factory set
	 * @throws org.springframework.beans.BeansException if bean name resolution via the bean factory failed
	 * @see #setEntityInterceptor
	 * @see #setEntityInterceptorBeanName
	 * @see #setBeanFactory
	 */
	public Interceptor getEntityInterceptor() throws IllegalStateException, BeansException {
		if (this.entityInterceptorBeanName != null) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("Cannot get entity interceptor via bean name if no bean factory set");
			}
			return (Interceptor) this.beanFactory.getBean(this.entityInterceptorBeanName, Interceptor.class);
		}
		else {
			return super.getEntityInterceptor();
		}
	}

	/**
	 * The bean factory just needs to be known for resolving entity interceptor
	 * bean names. It does not need to be set for any other mode of operation.
	 * @see #setEntityInterceptorBeanName
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}


	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		boolean existingTransaction = false;
		Session session = SessionFactoryUtils.getSession(
				getSessionFactory(), getEntityInterceptor(), getJdbcExceptionTranslator());
		if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
			logger.debug("Found thread-bound session for Hibernate interceptor");
			existingTransaction = true;
		}
		else {
			logger.debug("Using new session for Hibernate interceptor");
			if (getFlushMode() == FLUSH_NEVER) {
				session.setFlushMode(FlushMode.NEVER);
			}
			TransactionSynchronizationManager.bindResource(getSessionFactory(), new SessionHolder(session));
		}
		try {
			Object retVal = methodInvocation.proceed();
			flushIfNecessary(session, existingTransaction);
			return retVal;
		}
		catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
		finally {
			if (existingTransaction) {
				logger.debug("Not closing pre-bound Hibernate session after interceptor");
			}
			else {
				TransactionSynchronizationManager.unbindResource(getSessionFactory());
				SessionFactoryUtils.closeSessionIfNecessary(session, getSessionFactory());
			}
		}
	}

}
