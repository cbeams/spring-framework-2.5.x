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

package org.springframework.orm.hibernate.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate.HibernateAccessor;
import org.springframework.orm.hibernate.SessionFactoryUtils;
import org.springframework.orm.hibernate.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring web HandlerInterceptor that binds a Hibernate Session to the thread for the
 * whole processing of the request. Intended for the "Open Session in View" pattern,
 * i.e. to allow for lazy loading in web views despite the original transactions
 * already being completed.
 *
 * <p>This interceptor works similar to the AOP HibernateInterceptor: It just makes
 * Hibernate Sessions available via the thread. It is suitable for non-transactional
 * execution but also for middle tier transactions via HibernateTransactionManager
 * or JtaTransactionManager. In the latter case, Sessions pre-bound by this interceptor
 * will automatically be used for the transactions and flushed accordingly.
 *
 * <p>In contrast to OpenSessionInViewFilter, this interceptor is set up in a Spring
 * application context and can thus take advantage of bean wiring. It derives from
 * HibernateAccessor to inherit common Hibernate configuration properties.
 *
 * <p><b>WARNING:</b> Applying this interceptor to existing logic can cause issues that
 * have not appeared before, through the use of a single Hibernate Session for the
 * processing of an entire request. In particular, the reassociation of persistent
 * objects with a Hibernate Session has to occur at the very beginning of request
 * processing, to avoid clashes will already loaded instances of the same objects.
 *
 * <p><b>NOTE</b>: This interceptor will by default not flush the Hibernate session,
 * as it assumes to be used in combination with middle tier transactions that care for
 * the flushing, or HibernateAccessors with flushMode FLUSH_EAGER. If you want this
 * interceptor to flush after the handler has been invoked but before view rendering,
 * set the flushMode of this interceptor to FLUSH_AUTO in such a scenario.
 *
 * @author Juergen Hoeller
 * @since 06.12.2003
 * @see #setFlushMode
 * @see OpenSessionInViewFilter
 * @see org.springframework.orm.hibernate.HibernateInterceptor
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 */
public class OpenSessionInViewInterceptor extends HibernateAccessor implements HandlerInterceptor {

	/**
	 * Create a new OpenSessionInViewInterceptor,
	 * turning the default flushMode to FLUSH_NEVER.
	 * @see #setFlushMode
	 */
	public OpenSessionInViewInterceptor() {
		setFlushMode(FLUSH_NEVER);
	}

	/**
	 * Opens a new Hibernate Session according to the settings of this HibernateAccessor
	 * and binds in to the thread via TransactionSynchronizationManager.
	 * @see org.springframework.orm.hibernate.SessionFactoryUtils#getSession
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 */
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
													 Object handler) throws DataAccessException {
		logger.debug("Opening Hibernate Session in OpenSessionInViewInterceptor");
		Session session = SessionFactoryUtils.getSession(getSessionFactory(), getEntityInterceptor(),
																										 getJdbcExceptionTranslator());
		if (getFlushMode() == FLUSH_NEVER) {
			session.setFlushMode(FlushMode.NEVER);
		}
		TransactionSynchronizationManager.bindResource(getSessionFactory(), new SessionHolder(session));
		return true;
	}

	/**
	 * Flushes the Hibernate Session before view rendering, if necessary.
	 * Set the flushMode of this HibernateAccessor to FLUSH_NEVER to avoid this extra flushing.
	 * @see #setFlushMode
	 */
	public void postHandle(HttpServletRequest request, HttpServletResponse response,
												 Object handler, ModelAndView modelAndView) throws DataAccessException {
		logger.debug("Flushing Hibernate Session in OpenSessionInViewInterceptor");
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(getSessionFactory());
		try {
			flushIfNecessary(sessionHolder.getSession(), false);
		}
		catch (HibernateException ex) {
			throw convertHibernateAccessException(ex);
		}
	}

	/**
	 * Unbinds the Hibernate Session from the thread and closes it.
	 * @see org.springframework.orm.hibernate.SessionFactoryUtils#closeSessionIfNecessary
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 */
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
															Object handler, Exception ex) throws DataAccessException {
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(getSessionFactory());
		logger.debug("Closing Hibernate Session in OpenSessionInViewInterceptor");
		SessionFactoryUtils.closeSessionIfNecessary(sessionHolder.getSession(), getSessionFactory());
	}

}
