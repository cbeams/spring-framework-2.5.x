package org.springframework.transaction.jta;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.springframework.transaction.TransactionException;

/**
 * Strategy that encapsulates certain functionality that standard JTA 1.0 does
 * not offer despite being relevant for sophisticated transaction demarcation.
 * Optionally used by JtaTransactionManager, if specified.
 *
 * <p>Provides a method to retrieve the internal TransactionManager used by a
 * JTA implementation, implementing the <code>javax.transaction.TransactionManager</code>
 * interface. This TransactionManager is necessary for suspending and resuming
 * transactions (as needed by propagation REQUIRES_NEW and NOT_SUPPORTED).
 * Additionally, defines a hook to apply a given isolation level to a JTA transaction.
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see JtaTransactionManager#setJtaDialect
 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_REQUIRES_NEW
 * @see org.springframework.transaction.TransactionDefinition#PROPAGATION_NOT_SUPPORTED
 */
public interface JtaDialect {

	/**
	 * Retrieve the internal <code>javax.transaction.TransactionManager</code>
	 * used by the JTA implementation. There should be a way to achieve this
	 * with every JTA implementation: either by looking up from a special JNDI
	 * location, or by invoking a special static accessor method.
	 * <p>Used by JtaTransactionManager's doSuspend and doResume implementations.
	 * If you do not rely on transaction suspension, i.e. do not use propagation
	 * REQUIRES_NEW or NOT_SUPPORTED, you do not need this mechanism and thus
	 * probably no JtaDialect in the first place.
	 * <p>The internal TransactionManager can also be used to register custom
	 * synchronizations with the JTA transaction itself instead of Spring's
	 * transaction manager. LocalSessionFactoryBean supports to plug a Spring
	 * JtaDialect into Hibernate's TransactionManagerLookup mechanism, used
	 * for registering cache synchronization.
	 * @return the <code>javax.transaction.TransactionManager</code>
	 * @throws TransactionException if the TransactionManager could not be retrieved
	 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#setJtaDialect
	 */
	TransactionManager getInternalTransactionManager() throws TransactionException;

	/**
	 * Initialize the given UserTransaction with the given isolation level.
	 * Many JTA implementations might not support this at all: In this case,
	 * it is recommended to return gracefully (most JDBC drivers also silently
	 * fall back to the default if they do not support a given isolation level).
	 * @param ut UserTransaction instance representing the JTA transaction
	 * @param isolationLevel the isolation level to set
	 */
	void applyIsolationLevel(UserTransaction ut, int isolationLevel) throws TransactionException;

}
