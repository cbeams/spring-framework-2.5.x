package org.springframework.transaction.jta.dialect;

import java.lang.reflect.InvocationTargetException;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.jta.JtaDialect;

/**
 * JtaDialect implementation for ObjectWeb's <a href="http://jotm.objectweb.org/">JOTM</a>,
 * as used in the JOnAS application server.
 *
 * <p>Uses JOTM's static access method to obtain the JTA TransactionManager.
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see org.objectweb.jotm.Current#getTransactionManager
 */
public class JotmJtaDialect implements JtaDialect {

	private TransactionManager transactionManager;

	public JotmJtaDialect()
	    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class clazz = Class.forName("org.objectweb.jotm.Current");
		this.transactionManager = (TransactionManager) clazz.getMethod("getTransactionManager", null).invoke(null, null);
	}

	public TransactionManager getInternalTransactionManager() {
		return transactionManager;
	}

	/**
	 * This implementation throws an InvalidIsolationLevelException:
	 * If we'll find out how to set an isolation level with JOTM,
	 * we'll actually implement this.
	 */
	public void applyIsolationLevel(UserTransaction ut, int isolationLevel) throws InvalidIsolationLevelException {
		if (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException("JotmJtaDialect does not support custom isolation levels");
		}
	}

}
