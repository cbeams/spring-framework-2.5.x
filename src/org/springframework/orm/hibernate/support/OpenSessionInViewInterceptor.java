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
 * entire processing of the request. Intended for the "Open Session in View" pattern,
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
 * <p>Alternatively, turn this interceptor into deferred close mode, by specifying
 * "singleSession"="false": It will not use a single session per request then,
 * but rather let each data access operation respectively transaction use its own
 * session (like without Open Session in View). Each of those sessions will be
 * registered for deferred close, though, actually processed at request completion.
 *
 * <p>A single session per request allows for most efficient first-level caching,
 * but can cause side effects, for example on saveOrUpdate or if continuing
 * after a rolled-back transaction. The deferred close strategy is as safe as
 * no Open Session in View in that respect, while still allowing for lazy loading
 * in views (but not providing a first-level cache for the entire request).
 *
 * <p><b>NOTE</b>: This interceptor will by default not flush the Hibernate session,
 * as it assumes to be used in combination with middle tier transactions that care for
 * the flushing, or HibernateAccessors with flushMode FLUSH_EAGER. If you want this
 * interceptor to flush after the handler has been invoked but before view rendering,
 * set the flushMode of this interceptor to FLUSH_AUTO in such a scenario. Note that
 * the flushMode of this interceptor will just apply in single session mode!
 *
 * @author Juergen Hoeller
 * @since 06.12.2003
 * @see #setSingleSession
 * @see #setFlushMode
 * @see OpenSessionInViewFilter
 * @see org.springframework.orm.hibernate.HibernateInterceptor
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 * @see org.springframework.orm.hibernate.SessionFactoryUtils#getSession
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public class OpenSessionInViewInterceptor extends HibernateAccessor implements HandlerInterceptor {

	/**
	 * Suffix that gets appended to the SessionFactory toString representation
	 * for the "participate in existing session handling" request attribute.
	 * @see #getParticipateAttributeName
	 */
	public static final String PARTICIPATE_SUFFIX = ".PARTICIPATE";


	private boolean singleSession = true;


	/**
	 * Create a new OpenSessionInViewInterceptor,
	 * turning the default flushMode to FLUSH_NEVER.
	 * @see #setFlushMode
	 */
	public OpenSessionInViewInterceptor() {
		setFlushMode(FLUSH_NEVER);
	}

	/**
	 * Set whether to use a single session for each request. Default is true.
	 * <p>If set to false, each data access operation respectively transaction
	 * will use its own session (like without Open Session in View). Each of
	 * those sessions will be registered for deferred close, though, actually
	 * processed at request completion.
	 * @see SessionFactoryUtils#initDeferredClose
	 * @see SessionFactoryUtils#processDeferredClose
	 */
	public void setSingleSession(boolean singleSession) {
		this.singleSession = singleSession;
	}

	/**
	 * Return whether to use a single session for each request.
	 */
	protected boolean isSingleSession() {
		return singleSession;
	}


	/**
	 * Open a new Hibernate Session according to the settings of this HibernateAccessor
	 * and binds in to the thread via TransactionSynchronizationManager.
	 * @see org.springframework.orm.hibernate.SessionFactoryUtils#getSession
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 */
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws DataAccessException {

		if ((isSingleSession() && TransactionSynchronizationManager.hasResource(getSessionFactory())) ||
		    SessionFactoryUtils.isDeferredCloseActive(getSessionFactory())) {
			// do not modify the Session: just mark the request accordingly
			String participateAttributeName = getParticipateAttributeName();
			Integer count = (Integer) request.getAttribute(participateAttributeName);
			int newCount = (count != null) ? count.intValue() + 1 : 1;
			request.setAttribute(getParticipateAttributeName(), new Integer(newCount));
		}

		else {
			if (isSingleSession()) {
				// single session mode
				logger.debug("Opening single Hibernate session in OpenSessionInViewInterceptor");
				Session session = SessionFactoryUtils.getSession(
						getSessionFactory(), getEntityInterceptor(), getJdbcExceptionTranslator());
				if (getFlushMode() == FLUSH_NEVER) {
					session.setFlushMode(FlushMode.NEVER);
				}
				TransactionSynchronizationManager.bindResource(getSessionFactory(), new SessionHolder(session));
			}
			else {
				// deferred close mode
				SessionFactoryUtils.initDeferredClose(getSessionFactory());
			}
		}

		return true;
	}

	/**
	 * Flush the Hibernate Session before view rendering, if necessary.
	 * Note that this just applies in single session mode!
	 * <p>The default is FLUSH_NEVER to avoid this extra flushing, assuming that
	 * middle tier transactions have flushed their changes on commit.
	 * @see #setFlushMode
	 */
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
	    ModelAndView modelAndView) throws DataAccessException {
		if (isSingleSession()) {
			// only potentially flush in single session mode
			SessionHolder sessionHolder =
					(SessionHolder) TransactionSynchronizationManager.getResource(getSessionFactory());
			logger.debug("Flushing single Hibernate session in OpenSessionInViewInterceptor");
			try {
				flushIfNecessary(sessionHolder.getSession(), false);
			}
			catch (HibernateException ex) {
				throw convertHibernateAccessException(ex);
			}
		}
	}

	/**
	 * Unbind the Hibernate Session from the thread and closes it (in single session mode),
	 * respectively process deferred close for all sessions that have been opened during
	 * the current request (in deferred close mode).
	 * @see org.springframework.orm.hibernate.SessionFactoryUtils#closeSessionIfNecessary
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 */
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
	    Exception ex) throws DataAccessException {

		String participateAttributeName = getParticipateAttributeName();
		Integer count = (Integer) request.getAttribute(participateAttributeName);
		if (count != null) {
			// do not modify the Session: just clear the marker
			if (count.intValue() > 1) {
				request.setAttribute(participateAttributeName, new Integer(count.intValue() - 1));
			}
			else {
				request.removeAttribute(participateAttributeName);
			}
		}

		else {
			if (isSingleSession()) {
				// single session mode
				SessionHolder sessionHolder =
						(SessionHolder) TransactionSynchronizationManager.unbindResource(getSessionFactory());
				logger.debug("Closing single Hibernate session in OpenSessionInViewInterceptor");
				SessionFactoryUtils.closeSessionIfNecessary(sessionHolder.getSession(), getSessionFactory());
			}
			else {
				// deferred close mode
				SessionFactoryUtils.processDeferredClose(getSessionFactory());
			}
		}
	}

	/**
	 * Return the name of the request attribute that identifies that a request is
	 * already filtered. Default implementation takes the toString representation
	 * of the SessionFactory instance and appends ".FILTERED".
	 * @see #PARTICIPATE_SUFFIX
	 */
	protected String getParticipateAttributeName() {
		return getSessionFactory().toString() + PARTICIPATE_SUFFIX;
	}

}
