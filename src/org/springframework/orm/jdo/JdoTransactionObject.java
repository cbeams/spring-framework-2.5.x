package org.springframework.orm.jdo;

/**
 * JDO transaction object, representing a PersistenceManagerHolder.
 * Used as transaction object by JdoTransactionManager.
 *
 * <p>Instances of this class are the transaction objects that
 * JdoTransactionManager returns. They nest the thread-bound
 * PersistenceManagerHolder internally.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 13.06.2003
 */
public class JdoTransactionObject {

	private PersistenceManagerHolder persistenceManagerHolder;

	private boolean newPersistenceManagerHolder;

	/**
	 * Create JdoTransactionObject for new PersistenceManagerHolder.
	 */
	public JdoTransactionObject() {
	}

	/**
	 * Create JdoTransactionObject for existing PersistenceManagerHolder.
	 */
	protected JdoTransactionObject(PersistenceManagerHolder persistenceManagerHolder) {
		this.persistenceManagerHolder = persistenceManagerHolder;
		this.newPersistenceManagerHolder = false;
	}

	/**
	 * Set new PersistenceManagerHolder.
	 */
	protected void setPersistenceManagerHolder(PersistenceManagerHolder persistenceManagerHolder) {
		if (this.persistenceManagerHolder != null) {
			throw new IllegalStateException("Already initialized with an existing PersistenceManagerHolder");
		}
		this.persistenceManagerHolder = persistenceManagerHolder;
		this.newPersistenceManagerHolder = true;
	}

	public PersistenceManagerHolder getPersistenceManagerHolder() {
		return persistenceManagerHolder;
	}

	public boolean isNewPersistenceManagerHolder() {
		return newPersistenceManagerHolder;
	}

	public boolean hasTransaction() {
		return (persistenceManagerHolder != null && persistenceManagerHolder.getPersistenceManager() != null &&
		    persistenceManagerHolder.getPersistenceManager().currentTransaction().isActive());
	}

}
