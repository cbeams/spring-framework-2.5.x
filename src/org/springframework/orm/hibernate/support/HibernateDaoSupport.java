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

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate.HibernateTemplate;
import org.springframework.orm.hibernate.SessionFactoryUtils;

/**
 * Convenient super class for Hibernate data access objects.
 *
 * <p>Requires a SessionFactory to be set, providing a HibernateTemplate
 * based on it to subclasses. Can alternatively be initialized directly via
 * a HibernateTemplate, to reuse the latter's settings like SessionFactory,
 * exception translator, flush mode, etc.
 *
 * <p>This base class is mainly intended for HibernateTemplate usage
 * but can also be used when working with SessionFactoryUtils directly,
 * e.g. in combination with HibernateInterceptor-managed Sessions.
 * Convenience <code>getSession</code> and <code>closeSessionIfNecessary</code>
 * methods are provided for that usage style.
 * 
 * <p>This class will create its own HibernateTemplate if only a SessionFactory
 * is passed in. The allowCreate flag on that HibernateTemplate will be true by
 * default. A custom HibernateTemplate instance can be used through overriding
 * <code>createHibernateTemplate</code>.
 *
 * @author Juergen Hoeller
 * @since 28.07.2003
 * @see #setSessionFactory
 * @see #setHibernateTemplate
 * @see #createHibernateTemplate
 * @see #getSession
 * @see #closeSessionIfNecessary
 * @see org.springframework.orm.hibernate.HibernateTemplate
 * @see org.springframework.orm.hibernate.HibernateInterceptor
 */
public abstract class HibernateDaoSupport implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private HibernateTemplate hibernateTemplate;


	/**
	 * Set the Hibernate SessionFactory to be used by this DAO.
	 * Will automatically create a HibernateTemplate for the given SessionFactory.
	 * @see #createHibernateTemplate
	 * @see #setHibernateTemplate
	 */
	public final void setSessionFactory(SessionFactory sessionFactory) {
	  this.hibernateTemplate = createHibernateTemplate(sessionFactory);
	}

	/**
	 * Create a HibernateTemplate for the given SessionFactory.
	 * Only invoked if populating the DAO with a SessionFactory reference!
	 * <p>Can be overridden in subclasses to provide a HibernateTemplate instance
	 * with different configuration, or a custom HibernateTemplate subclass.
	 * @param sessionFactory the Hibernate SessionFactory to create a HibernateTemplate for
	 * @return the new HibernateTemplate instance
	 * @see #setSessionFactory
	 */
	protected HibernateTemplate createHibernateTemplate(SessionFactory sessionFactory) {
		return new HibernateTemplate(sessionFactory);
	}

	/**
	 * Return the Hibernate SessionFactory used by this DAO.
	 */
	public final SessionFactory getSessionFactory() {
		return (this.hibernateTemplate != null ? this.hibernateTemplate.getSessionFactory() : null);
	}

	/**
	 * Set the HibernateTemplate for this DAO explicitly,
	 * as an alternative to specifying a SessionFactory.
	 * @see #setSessionFactory
	 */
	public final void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	/**
	 * Return the HibernateTemplate for this DAO,
	 * pre-initialized with the SessionFactory or set explicitly.
	 */
	public final HibernateTemplate getHibernateTemplate() {
	  return hibernateTemplate;
	}

	public final void afterPropertiesSet() throws Exception {
		if (this.hibernateTemplate == null) {
			throw new IllegalArgumentException("sessionFactory or hibernateTemplate is required");
		}
		initDao();
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called after population of this instance's bean properties.
	 * @throws Exception if initialization fails
	 */
	protected void initDao() throws Exception {
	}


	/**
	 * Get a Hibernate Session, either from the current transaction or a new one.
	 * The latter is only allowed if the "allowCreate" setting of this bean's
	 * HibernateTemplate is true.
	 * <p><b>Note that this is not meant to be invoked from HibernateTemplate code
	 * but rather just in plain Hibernate code.</b> Either rely on a thread-bound
	 * Session (via HibernateInterceptor), or use it in combination with
	 * <code>closeSessionIfNecessary</code>.
	 * <p>In general, it is recommended to use HibernateTemplate, either with
	 * the provided convenience operations or with a custom HibernateCallback
	 * that provides you with a Session to work on. HibernateTemplate will care
	 * for all resource management and for proper exception conversion.
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see #closeSessionIfNecessary
	 * @see org.springframework.orm.hibernate.SessionFactoryUtils#getSession(SessionFactory, boolean)
	 * @see org.springframework.orm.hibernate.HibernateInterceptor
	 * @see org.springframework.orm.hibernate.HibernateTemplate
	 * @see org.springframework.orm.hibernate.HibernateCallback
	 */
	protected final Session getSession()
	    throws DataAccessResourceFailureException, IllegalStateException {
		return getSession(this.hibernateTemplate.isAllowCreate());
	}

	/**
	 * Get a Hibernate Session, either from the current transaction or
	 * a new one. The latter is only allowed if "allowCreate" is true.
	 * <p><b>Note that this is not meant to be invoked from HibernateTemplate code
	 * but rather just in plain Hibernate code.</b> Either rely on a thread-bound
	 * Session (via HibernateInterceptor), or use it in combination with
	 * <code>closeSessionIfNecessary</code>.
	 * <p>In general, it is recommended to use HibernateTemplate, either with
	 * the provided convenience operations or with a custom HibernateCallback
	 * that provides you with a Session to work on. HibernateTemplate will care
	 * for all resource management and for proper exception conversion.
	 * @param allowCreate if a new Session should be created if no thread-bound found
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see #closeSessionIfNecessary
	 * @see org.springframework.orm.hibernate.SessionFactoryUtils#getSession(SessionFactory, boolean)
	 * @see org.springframework.orm.hibernate.HibernateInterceptor
	 * @see org.springframework.orm.hibernate.HibernateTemplate
	 * @see org.springframework.orm.hibernate.HibernateCallback
	 */
	protected final Session getSession(boolean allowCreate)
	    throws DataAccessResourceFailureException, IllegalStateException {
		return (!allowCreate ?
		    SessionFactoryUtils.getSession(getSessionFactory(), false) :
				SessionFactoryUtils.getSession(
						getSessionFactory(),
						this.hibernateTemplate.getEntityInterceptor(),
						this.hibernateTemplate.getJdbcExceptionTranslator()));
	}

	/**
	 * Convert the given HibernateException to an appropriate exception from
	 * the org.springframework.dao hierarchy. Will automatically detect
	 * wrapped SQLExceptions and convert them accordingly.
	 * <p>Delegates to the <code>convertHibernateAccessException</code>
	 * method of this DAO's HibernateTemplate.
	 * <p>Typically used in plain Hibernate code, in combination with
	 * <code>getSession</code> and <code>closeSessionIfNecessary</code>.
	 * @param ex HibernateException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #setHibernateTemplate
	 * @see #getSession
	 * @see #closeSessionIfNecessary
	 * @see org.springframework.orm.hibernate.HibernateTemplate#convertHibernateAccessException
	 */
	protected final DataAccessException convertHibernateAccessException(HibernateException ex) {
		return this.hibernateTemplate.convertHibernateAccessException(ex);
	}

	/**
	 * Close the given Hibernate Session if necessary, created via this bean's
	 * SessionFactory, if it isn't bound to the thread.
	 * <p>Typically used in plain Hibernate code, in combination with
	 * <code>getSession</code> and <code>convertHibernateAccessException</code>.
	 * @param session Session to close
	 * @see org.springframework.orm.hibernate.SessionFactoryUtils#closeSessionIfNecessary
	 */
	protected final void closeSessionIfNecessary(Session session) {
		SessionFactoryUtils.closeSessionIfNecessary(session, getSessionFactory());
	}

}
