/*
 * Copyright 2002-2007 the original author or authors.
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

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate.HibernateAccessor;
import org.springframework.orm.hibernate.SessionFactoryUtils;
import org.springframework.orm.hibernate.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

/**
 * Spring web request interceptor that binds a Hibernate Session to the thread for the
 * entire processing of the request. Intended for the "Open Session in View" pattern,
 * that is, to allow for lazy loading in web views despite the original transactions
 * already being completed.
 *
 * <p>This interceptor makes Hibernate Sessions available via the current thread,
 * which will be autodetected by transaction managers. It is suitable for service layer
 * transactions via {@link org.springframework.orm.hibernate.HibernateTransactionManager}
 * or {@link org.springframework.transaction.jta.JtaTransactionManager} as well
 * as for non-transactional execution (if configured appropriately).
 *
 * <p><b>NOTE</b>: This interceptor will by default <i>not</i> flush the Hibernate Session,
 * with the flush mode set to <code>FlushMode.NEVER</code>. It assumes to be used
 * in combination with service layer transactions that care for the flushing: The
 * active transaction manager will temporarily change the flush mode to
 * <code>FlushMode.AUTO</code> during a read-write transaction, with the flush
 * mode reset to <code>FlushMode.NEVER</code> at the end of each transaction.
 * If you intend to use this interceptor without transactions, consider changing
 * the default flush mode (through the "flushMode" property).
 *
 * <p>In contrast to {@link OpenSessionInViewFilter}, this interceptor is set up
 * in a Spring application context and can thus take advantage of bean wiring.
 * It inherits common Hibernate configuration properties from
 * {@link org.springframework.orm.hibernate.HibernateAccessor},
 * to be configured in a bean definition.
 *
 * <p><b>WARNING:</b> Applying this interceptor to existing logic can cause issues that
 * have not appeared before, through the use of a single Hibernate Session for the
 * processing of an entire request. In particular, the reassociation of persistent
 * objects with a Hibernate Session has to occur at the very beginning of request
 * processing, to avoid clashes will already loaded instances of the same objects.
 *
 * <p>Alternatively, turn this interceptor into deferred close mode, by specifying
 * "singleSession"="false": It will not use a single session per request then,
 * but rather let each data access operation or transaction use its own session
 * (like without Open Session in View). Each of those sessions will be registered
 * for deferred close, though, actually processed at request completion.
 *
 * <p>A single session per request allows for most efficient first-level caching,
 * but can cause side effects, for example on <code>saveOrUpdate</code> or when
 * continuing after a rolled-back transaction. The deferred close strategy is as safe
 * as no Open Session in View in that respect, while still allowing for lazy loading
 * in views (but not providing a first-level cache for the entire request).
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
public class OpenSessionInViewInterceptor extends HibernateAccessor implements WebRequestInterceptor {

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
	 * Set whether to use a single session for each request. Default is "true".
	 * <p>If set to false, each data access operation or transaction will use
	 * its own session (like without Open Session in View). Each of those
	 * sessions will be registered for deferred close, though, actually
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
	public void preHandle(WebRequest request) throws DataAccessException {
		if ((isSingleSession() && TransactionSynchronizationManager.hasResource(getSessionFactory())) ||
		    SessionFactoryUtils.isDeferredCloseActive(getSessionFactory())) {
			// Do not modify the Session: just mark the request accordingly.
			String participateAttributeName = getParticipateAttributeName();
			Integer count = (Integer) request.getAttribute(participateAttributeName, WebRequest.SCOPE_REQUEST);
			int newCount = (count != null) ? count.intValue() + 1 : 1;
			request.setAttribute(getParticipateAttributeName(), new Integer(newCount), WebRequest.SCOPE_REQUEST);
		}
		else {
			if (isSingleSession()) {
				// single session mode
				logger.debug("Opening single Hibernate Session in OpenSessionInViewInterceptor");
				Session session = SessionFactoryUtils.getSession(
						getSessionFactory(), getEntityInterceptor(), getJdbcExceptionTranslator());
				applyFlushMode(session, false);
				TransactionSynchronizationManager.bindResource(getSessionFactory(), new SessionHolder(session));
			}
			else {
				// deferred close mode
				SessionFactoryUtils.initDeferredClose(getSessionFactory());
			}
		}
	}

	/**
	 * Flush the Hibernate Session before view rendering, if necessary.
	 * Note that this just applies in single session mode!
	 * <p>The default is FLUSH_NEVER to avoid this extra flushing, assuming that
	 * service layer transactions have flushed their changes on commit.
	 * @see #setFlushMode
	 */
	public void postHandle(WebRequest request, ModelMap model) throws DataAccessException {
		if (isSingleSession()) {
			// Only potentially flush in single session mode.
			SessionHolder sessionHolder =
					(SessionHolder) TransactionSynchronizationManager.getResource(getSessionFactory());
			logger.debug("Flushing single Hibernate Session in OpenSessionInViewInterceptor");
			try {
				flushIfNecessary(sessionHolder.getSession(), false);
			}
			catch (HibernateException ex) {
				throw convertHibernateAccessException(ex);
			}
		}
	}

	/**
	 * Unbind the Hibernate Session from the thread and closes it (in single session
	 * mode), or process deferred close for all sessions that have been opened
	 * during the current request (in deferred close mode).
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 */
	public void afterCompletion(WebRequest request, Exception ex) throws DataAccessException {
		String participateAttributeName = getParticipateAttributeName();
		Integer count = (Integer) request.getAttribute(participateAttributeName, WebRequest.SCOPE_REQUEST);
		if (count != null) {
			// Do not modify the Session: just clear the marker.
			if (count.intValue() > 1) {
				request.setAttribute(participateAttributeName, new Integer(count.intValue() - 1), WebRequest.SCOPE_REQUEST);
			}
			else {
				request.removeAttribute(participateAttributeName, WebRequest.SCOPE_REQUEST);
			}
		}
		else {
			if (isSingleSession()) {
				// single session mode
				SessionHolder sessionHolder =
						(SessionHolder) TransactionSynchronizationManager.unbindResource(getSessionFactory());
				logger.debug("Closing single Hibernate Session in OpenSessionInViewInterceptor");
				SessionFactoryUtils.closeSession(sessionHolder.getSession());
			}
			else {
				// deferred close mode
				SessionFactoryUtils.processDeferredClose(getSessionFactory());
			}
		}
	}

	/**
	 * Return the name of the request attribute that identifies that a request is
	 * already intercepted. Default implementation takes the toString representation
	 * of the SessionFactory instance and appends ".PARTICIPATE".
	 * @see #PARTICIPATE_SUFFIX
	 */
	protected String getParticipateAttributeName() {
		return getSessionFactory().toString() + PARTICIPATE_SUFFIX;
	}

}
