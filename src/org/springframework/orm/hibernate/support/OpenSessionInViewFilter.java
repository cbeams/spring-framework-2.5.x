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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;

import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate.SessionFactoryUtils;
import org.springframework.orm.hibernate.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet 2.3 Filter that binds a Hibernate Session to the thread for the whole
 * processing of the request. Intended for the "Open Session in View" pattern,
 * i.e. to allow for lazy loading in web views despite the original transactions
 * already being completed.
 *
 * <p>This filter works similar to the AOP HibernateInterceptor: It just makes
 * Hibernate Sessions available via the thread. It is suitable for non-transactional
 * execution but also for middle tier transactions via HibernateTransactionManager
 * or JtaTransactionManager. In the latter case, Sessions pre-bound by this filter
 * will automatically be used for the transactions and flushed accordingly.
 *
 * <p><b>WARNING:</b> Applying this filter to existing logic can cause issues that
 * have not appeared before, through the use of a single Hibernate Session for the
 * processing of an entire request. In particular, the reassociation of persistent
 * objects with a Hibernate Session has to occur at the very beginning of request
 * processing, to avoid clashes will already loaded instances of the same objects.
 *
 * <p>Looks up the SessionFactory in Spring's root web application context.
 * Supports a "sessionFactoryBeanName" filter init-param; the default bean name is
 * "sessionFactory". Looks up the SessionFactory on each request, to avoid
 * initialization order issues (if using ContextLoaderServlet, the root
 * application context will get initialized <i>after</i> this filter).
 *
 * <p><b>NOTE</b>: This filter will by default not flush the Hibernate session, as
 * it assumes to be used in combination with middle tier transactions that care for
 * the flushing, or HibernateAccessors with flushMode FLUSH_EAGER. If you want this
 * filter to flush after completed request processing, override closeSession and
 * invoke flush on the Session before closing it.
 *
 * @author Juergen Hoeller
 * @since 06.12.2003
 * @see OpenSessionInViewInterceptor
 * @see #closeSession
 * @see org.springframework.orm.hibernate.HibernateInterceptor
 * @see org.springframework.orm.hibernate.HibernateTransactionManager
 * @see org.springframework.orm.hibernate.SessionFactoryUtils#getSession
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public class OpenSessionInViewFilter extends OncePerRequestFilter {

	public static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "sessionFactory";

	private String sessionFactoryBeanName = DEFAULT_SESSION_FACTORY_BEAN_NAME;

	/**
	 * Set the bean name of the SessionFactory to fetch from Spring's
	 * root application context.
	 */
	public void setSessionFactoryBeanName(String sessionFactoryBeanName) {
		this.sessionFactoryBeanName = sessionFactoryBeanName;
	}

	/**
	 * Return the bean name of the SessionFactory to fetch from Spring's
	 * root application context.
	 */
	protected String getSessionFactoryBeanName() {
		return sessionFactoryBeanName;
	}

	/**
	 * This implementation appends the SessionFactory bean name to the class name,
	 * to be executed one per SessionFactory. Can be overridden in subclasses,
	 * e.g. when also overriding lookupSessionFactory.
	 * @see #lookupSessionFactory
	 */
	protected String getAlreadyFilteredAttributeName() {
		return getClass().getName() + "." + this.sessionFactoryBeanName;
	}

	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
																	FilterChain filterChain) throws ServletException, IOException {
		SessionFactory sessionFactory = lookupSessionFactory();
		logger.debug("Opening Hibernate Session in OpenSessionInViewFilter");
		Session session = getSession(sessionFactory);
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		try {
			filterChain.doFilter(request, response);
		}
		finally {
			TransactionSynchronizationManager.unbindResource(sessionFactory);
			logger.debug("Closing Hibernate Session in OpenSessionInViewFilter");
			closeSession(session, sessionFactory);
		}
	}

	/**
	 * Look up the SessionFactory that this filter should use.
	 * The default implementation looks for a bean with the specified name
	 * in Spring's root application context.
	 * @return the SessionFactory to use
	 * @see #getSessionFactoryBeanName
	 */
	protected SessionFactory lookupSessionFactory() {
		logger.info("Using SessionFactory '" + getSessionFactoryBeanName() + "' for OpenSessionInViewFilter");
		WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		return (SessionFactory) wac.getBean(getSessionFactoryBeanName());
	}

	/**
	 * Get a Session for the SessionFactory that this filter uses.
	 * The default implementation invokes SessionFactoryUtils.getSession,
	 * and sets the Session's flushMode to NEVER.
	 * <p>Can be overridden in subclasses for creating a Session with a custom
	 * entity interceptor or JDBC exception translator.
	 * @param sessionFactory the SessionFactory that this filter uses
	 * @return the Session to use
	 * @throws DataAccessResourceFailureException if the Session could not be created
	 * @see org.springframework.orm.hibernate.SessionFactoryUtils#getSession(SessionFactory, boolean)
	 */
	protected Session getSession(SessionFactory sessionFactory)
			throws DataAccessResourceFailureException {
		Session session = SessionFactoryUtils.getSession(sessionFactory, true);
		session.setFlushMode(FlushMode.NEVER);
		return session;
	}

	/**
	 * Close the given Session.
	 * The default implementation invokes SessionFactoryUtils.closeSessionIfNecessary.
	 * <p>Can be overridden in subclasses, e.g. for flushing the Session before
	 * closing it. See class-level javadoc for a discussion of flush handling.
	 * @param session the Session used for filtering
	 * @param sessionFactory the SessionFactory that this filter uses
	 * @throws DataAccessResourceFailureException if the Session could not be closed
	 */
	protected void closeSession(Session session, SessionFactory sessionFactory)
			throws CleanupFailureDataAccessException {
		SessionFactoryUtils.closeSessionIfNecessary(session, sessionFactory);
	}

}
