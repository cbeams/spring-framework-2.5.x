package org.springframework.transaction.jta.dialect;

import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.jta.JtaDialect;

/**
 * Generic JtaDialect implementation that looks up the JTA TransactionManager
 * in JNDI. The location is configurable via "transactionManagerName".
 *
 * <p>Some well-known locations are:
 * <ul>
 * <li>"java:comp/UserTransaction" for Resin and Orion
 * <li>"java:/TransactionManager" for JBoss and JRun4
 * <li>"javax.transaction.TransactionManager" for BEA WebLogic
 * </ul>
 *
 * <p>JOTM (used in JOnAS) and IBM WebSphere are known to require
 * static accessor methods to obtain the JTA TransactionManager:
 * Therefore, they have their own JtaDialect implementations.
 *
 * <p>Does not do anything about the isolation level, as this needs access
 * to implementation-specific JTA classes rather than just knowledge about
 * an implementation-specific JNDI location of a standard JTA interface
 * like in the case of <code>javax.transaction.TransactionManager</code>.
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see #setTransactionManagerName
 * @see JotmJtaDialect
 * @see WebSphereJtaDialect
 */
public class JndiLookupJtaDialect implements JtaDialect, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private JndiTemplate jndiTemplate = new JndiTemplate();

	private String transactionManagerName;

	private TransactionManager transactionManager;

	/**
	 * Set the JndiTemplate to use for JNDI lookup.
	 * A default one is used if not set.
	 */
	public void setJndiTemplate(JndiTemplate jndiTemplate) {
		if (jndiTemplate == null) {
			throw new IllegalArgumentException("jndiTemplate must not be null");
		}
		this.jndiTemplate = jndiTemplate;
	}

	/**
	 * Set the JNDI name of the JTA TransactionManager.
	 * Some well-known locations are discussed in the class-level Javadoc.
	 */
	public void setTransactionManagerName(String transactionManagerName) {
		this.transactionManagerName = transactionManagerName;
	}

	public void afterPropertiesSet() throws NamingException {
		if (this.transactionManagerName == null) {
			throw new IllegalArgumentException("transactionManagerName is required");
		}
		this.transactionManager = (TransactionManager) this.jndiTemplate.lookup(this.transactionManagerName);
		if (logger.isInfoEnabled()) {
			logger.info("Using JTA TransactionManager [" + this.transactionManager +
			            "] from JNDI location [" + this.transactionManagerName + "]");
		}
	}

	/**
	 * This implementations looks up the JTA TransactionManager from the
	 * specified transactionManagerName location in JNDI.
	 * @see #setTransactionManagerName
	 */
	public TransactionManager getInternalTransactionManager() {
		return transactionManager;
	}

	/**
	 * This implementation throws an InvalidIsolationLevelException, not being able
	 * to do anything about a custom isolation level in this generic dialect.
	 */
	public void applyIsolationLevel(UserTransaction ut, int isolationLevel) throws InvalidIsolationLevelException {
		if (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException("JndiLookupJtaDialect does not support custom isolation levels");
		}
	}

}
