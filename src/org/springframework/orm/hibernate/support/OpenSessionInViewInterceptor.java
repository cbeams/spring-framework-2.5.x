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
 * execution but also for middle tier transactions via HibernateTransactionManager.
 * The latter will automatically use Sessions pre-bound by this filter.
 *
 * <p>In contrast to OpenSessionInViewFilter, this interceptor is set up in a Spring
 * application context and can thus take advantage of bean wiring. It derives from
 * HibernateAccessor to inherit Hibernate configuration properties.
 *
 * <p>Note: This interceptor will by default not flush the Hibernate session, as it
 * assumes to be used in combination with middle tier transactions that care for
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
	 * turning the default flushMode to FLUSH_NEVER
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
