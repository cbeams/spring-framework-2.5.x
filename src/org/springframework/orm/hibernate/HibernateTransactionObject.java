package org.springframework.orm.hibernate;

/**
 * Hibernate transaction object, representing a SessionHolder.
 * Used as transaction object by HibernateTransactionManager.
 *
 * <p>Instances of this class are the transaction objects that
 * HibernateTransactionManager returns. They nest the thread-bound
 * SessionHolder internally.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see HibernateTransactionManager
 * @see SessionHolder
 */
public class HibernateTransactionObject {

	private SessionHolder sessionHolder;

	private boolean newSessionHolder;

	private Integer previousIsolationLevel;

	/**
	 * Create HibernateTransactionObject for new SessionHolder.
	 */
	protected HibernateTransactionObject() {
	}

	/**
	 * Create HibernateTransactionObject for existing SessionHolder.
	 */
	protected HibernateTransactionObject(SessionHolder sessionHolder) {
		this.sessionHolder = sessionHolder;
		this.newSessionHolder = false;
	}

	/**
	 * Set new SessionHolder.
	 */
	protected void setSessionHolder(SessionHolder sessionHolder) {
		if (this.sessionHolder != null) {
			throw new IllegalStateException("Already initialized with an existing SessionHolder");
		}
		this.sessionHolder = sessionHolder;
		this.newSessionHolder = true;
	}

	public SessionHolder getSessionHolder() {
		return sessionHolder;
	}

	public boolean isNewSessionHolder() {
		return newSessionHolder;
	}

	public boolean hasTransaction() {
		return (sessionHolder != null && sessionHolder.getTransaction() != null);
	}

	protected void setPreviousIsolationLevel(Integer previousIsolationLevel) {
		this.previousIsolationLevel = previousIsolationLevel;
	}

	public Integer getPreviousIsolationLevel() {
		return previousIsolationLevel;
	}

}
