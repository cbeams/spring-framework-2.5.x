package org.springframework.orm.jdo.support;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jdo.JdoTemplate;
import org.springframework.orm.jdo.PersistenceManagerFactoryUtils;

/**
 * Convenient super class for JDO data access objects.
 * Requires a PersistenceManagerFactory to be set,
 * providing a JdoTemplate based on it to subclasses.
 *
 * <p>This base class is mainly intended for JdoeTemplate usage but can
 * also be used when working with PersistenceManagerFactoryUtils directly,
 * e.g. in combination with JdoInterceptor-managed PersistenceManagers.
 *
 * @author Juergen Hoeller
 * @since 28.07.2003
 * @see #setPersistenceManagerFactory
 * @see org.springframework.orm.jdo.JdoTemplate
 * @see org.springframework.orm.jdo.JdoInterceptor
 */
public abstract class JdoDaoSupport implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private JdoTemplate jdoTemplate;

	/**
	 * Set the JDO PersistenceManagerFactory to be used by this DAO.
	 */
	public final void setPersistenceManagerFactory(PersistenceManagerFactory persistenceManagerFactory) {
	  this.jdoTemplate = new JdoTemplate(persistenceManagerFactory);
	}

	/**
	 * Return the JDO PersistenceManagerFactory used by this DAO.
	 */
	protected final PersistenceManagerFactory getPersistenceManagerFactory() {
		return jdoTemplate.getPersistenceManagerFactory();
	}

	/**
	 * Set the JdoTemplate for this DAO explicitly,
	 * as an alternative to specifying a PersistenceManagerFactory.
	 */
	public final void setJdoTemplate(JdoTemplate jdoTemplate) {
		this.jdoTemplate = jdoTemplate;
	}

	/**
	 * Return the JdoTemplate for this DAO, pre-initialized
	 * with the PersistenceManagerFactory or set explicitly.
	 */
	protected final JdoTemplate getJdoTemplate() {
	  return jdoTemplate;
	}

	public final void afterPropertiesSet() throws Exception {
		if (this.jdoTemplate == null) {
			throw new IllegalArgumentException("persistenceManagerFactory or jdoTemplate is required");
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
	 * Get a JDO PersistenceManager, either from the current transaction or
	 * a new one. The latter is only allowed if the "allowCreate" setting
	 * of this bean's JdoTemplate is true.
	 * @return the JDO PersistenceManager
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see org.springframework.orm.jdo.PersistenceManagerFactoryUtils#getPersistenceManager
	 */
	protected final PersistenceManager getPersistenceManager() {
		return getPersistenceManager(getJdoTemplate().isAllowCreate());
	}

	/**
	 * Get a JDO PersistenceManager, either from the current transaction or
	 * a new one. The latter is only allowed if "allowCreate" is true.
	 * @param allowCreate if a new PersistenceManager should be created if no thread-bound found
	 * @return the JDO PersistenceManager
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see org.springframework.orm.jdo.PersistenceManagerFactoryUtils#getPersistenceManager
	 */
	protected final PersistenceManager getPersistenceManager(boolean allowCreate)
	    throws DataAccessResourceFailureException {
		return PersistenceManagerFactoryUtils.getPersistenceManager(getPersistenceManagerFactory(), allowCreate);
	}

	/**
	 * Close the given JDO PersistenceManager if necessary, created via this bean's
	 * PersistenceManagerFactory, if it isn't bound to the thread.
	 * @param pm PersistenceManager to close
	 * @throws DataAccessResourceFailureException if the PersistenceManager couldn't be closed
	 * @see org.springframework.orm.jdo.PersistenceManagerFactoryUtils#closePersistenceManagerIfNecessary
	 */
	protected final void closeSessionIfNecessary(PersistenceManager pm)
	    throws CleanupFailureDataAccessException {
		PersistenceManagerFactoryUtils.closePersistenceManagerIfNecessary(pm, getPersistenceManagerFactory());
	}

}
