package org.springframework.orm.hibernate;

import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Session holder, wrapping a Hibernate Session and a Hibernate Transaction.
 * HibernateTransactionManager binds instances of this class
 * to the thread, for a given SessionFactory.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 06.05.2003
 * @see HibernateTransactionManager
 * @see HibernateTransactionObject
 * @see SessionFactoryUtils
 */
public class SessionHolder extends ResourceHolderSupport {

	private final Session session;

	private Transaction transaction;

	public SessionHolder(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

}
