package org.springframework.orm.jdo;

import javax.jdo.PersistenceManager;

import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Holder wrapping a JDO PersistenceManager.
 *JdoTransactionManager binds instances of this class
 * to the thread, for a given PersistenceManagerFactory.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see JdoTransactionManager
 * @see PersistenceManagerFactoryUtils
 */
public class PersistenceManagerHolder extends ResourceHolderSupport {

	private PersistenceManager persistenceManager;

	private boolean rollbackOnly;

	public PersistenceManagerHolder(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

}
