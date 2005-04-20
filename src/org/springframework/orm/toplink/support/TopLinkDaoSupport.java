/*
@license@
  */ 

package org.springframework.orm.toplink.support;

import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.toplink.SessionFactory;
import org.springframework.orm.toplink.SessionFactoryUtils;
import org.springframework.orm.toplink.TopLinkTemplate;

/**
 * Convenient super class for Toplink data access objects.
 *
 * <p>Requires a SessionFactory to be set, providing a ToplinkTemplate
 * based on it to subclasses. Can alternatively be initialized directly via a
 * ToplinkTemplate, to reuse the latter's settings like Session,
 * exception translator, etc.
 *
 * <p>This base class is mainly intended for ToplinkTemplate usage
 * but can also be used when working with ToplinkSessionFactoryUtils directly,
 * e.g. in combination with ToplinkInterceptor-managed Sessions.
 * Convenience getSession and closeSessionIfNecessary methods are provided
 * for that usage.
 *
 */
public abstract class TopLinkDaoSupport implements InitializingBean
{
	protected final Log logger = LogFactory.getLog(getClass());
	private TopLinkTemplate toplinkTemplate;

	public final void setSessionFactory(SessionFactory sessionFactory)
	{
	    this.toplinkTemplate = new TopLinkTemplate(sessionFactory);
	}
	
	public final SessionFactory getSessionFactory()
	{
	    return this.toplinkTemplate.getSessionFactory();
	}

    /**
     * @return Returns the toplinkTemplate.
     */
    public TopLinkTemplate getTopLinkTemplate()
    {
        return toplinkTemplate;
    }

    /**
     * @param toplinkTemplate The toplinkTemplate to set.
     */
    public void setToplinkTemplate(TopLinkTemplate toplinkTemplate)
    {
        this.toplinkTemplate = toplinkTemplate;
    }

	public final void afterPropertiesSet() throws Exception
	{
		if (this.toplinkTemplate == null)
		{
			throw new IllegalArgumentException(
				"sessionFactory or toplinkTemplate is required");
		}
		initDao();
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called after population of this instance's bean properties.
	 * @throws Exception if initialization fails
	 */
	protected void initDao() throws Exception
	{
	}

	/**
	 * Get a Toplink Session, either from the current transaction or
	 * a new one. The latter is only allowed if the "allowCreate" setting
	 * of this bean's TopLinkTemplate is true.
	 * @return the Toplink Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see org.springframework.orm.toplink.TopLinkTemplate
	 */
	protected final Session getSession()
	    throws DataAccessResourceFailureException, IllegalStateException
	{
		return getSession(this.toplinkTemplate.isAllowCreate());
	}

	/**
	 * Get a Toplink Session, either from the current transaction or
	 * a new one. The latter is only allowed if "allowCreate" is true.
	 * @param allowCreate if a new Session should be created if no thread-bound found
	 * @return the Toplink Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see org.springframework.orm.toplink.SessionFactoryUtils#getSession(SessionFactory, boolean)
	 */
	protected final Session getSession(boolean allowCreate)
	    throws DataAccessResourceFailureException, IllegalStateException
	{
		return SessionFactoryUtils.getSession(this.getSessionFactory(),allowCreate);
	}

	/**
	 * Convert the given ToplinkException to an appropriate exception from
	 * the org.springframework.dao hierarchy. Will automatically detect
	 * wrapped SQLExceptions and convert them accordingly.
	 * <p>Delegates to the convertHibernateAccessException method of this
	 * DAO's ToplinkTemplate.
	 * @param ex HibernateException that occured
	 * @return the corresponding DataAccessException instance
	 * @see #setHibernateTemplate
	 * @see org.springframework.orm.toplink.TopLinkTemplate#convertTopLinkAccessException
	 */
	protected final DataAccessException convertToplinkAccessException(TopLinkException ex)
	{
		return this.toplinkTemplate.convertToplinkAccessException(ex);
	}

	/**
	 * Close the given Hibernate Session if necessary, created via this bean's
	 * SessionFactory, if it isn't bound to the thread.
	 * @param session Session to close
	 * @throws DataAccessResourceFailureException if the Session couldn't be closed
	 * @see org.springframework.orm.toplink.SessionFactoryUtils#closeSessionIfNecessary
	 */
	protected final void closeSessionIfNecessary(Session session)
	    throws CleanupFailureDataAccessException
	{
		SessionFactoryUtils.closeSessionIfNecessary(session, getSessionFactory());
	}
}
