package org.springframework.orm.hibernate;

import java.util.Properties;

import javax.transaction.TransactionManager;

import net.sf.hibernate.transaction.TransactionManagerLookup;

import org.springframework.transaction.jta.JtaDialect;

/**
 * Implementation of Hibernate's TransactionManagerLookup interface that
 * delegates to a Spring JtaDialect. The dialect will be determined by
 * LocalSessionFactoryBean's "jtaDialect" property.
 *
 * <p>The main advantage of this TransactionManagerLookup is that it avoids
 * double configuration of JTA specifics. A single JtaDialect bean can be
 * used for both JtaTransactionManager and LocalSessionFactoryBean, with no
 * JTA setup in Hibernate configuration.
 *
 * <p>Alternatively, use Hibernate's own TransactionManagerLookup implementations:
 * Spring's JtaTransactionManager only requires a JtaDialect for suspending and
 * resuming transactions, so you might not need to specify such a dialect at all.
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see LocalSessionFactoryBean#setJtaDialect
 */
public class JtaDialectTransactionManagerLookup implements TransactionManagerLookup {

	/**
	 * This will hold the JtaDialect to use for the currently configured
	 * Hibernate SessionFactory. It will be set just before initialization
	 * of the respective SessionFactory, and reset immediately afterwards.
	 */
	protected static ThreadLocal configTimeJtaDialectHolder = new ThreadLocal();

	private final TransactionManager transactionManager;

	public JtaDialectTransactionManagerLookup() {
		JtaDialect jtaDialect = (JtaDialect) configTimeJtaDialectHolder.get();
		// absolutely needs thread-bound DataSource to initialize
		if (jtaDialect == null) {
			throw new IllegalStateException("No JtaDialect found - jtaDialect property must be set on LocalSessionFactoryBean");
		}
		this.transactionManager = jtaDialect.getInternalTransactionManager();
	}

	public TransactionManager getTransactionManager(Properties props) {
		return transactionManager;
	}

	public String getUserTransactionName() {
		return null;
	}

}
