package org.springframework.transaction.jta.dialect;

import java.lang.reflect.InvocationTargetException;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.jta.JtaDialect;

/**
 * JtaDialect implementation for IBM's WebSphere 4 and 5 application servers.
 *
 * <p>Uses WebSphere's static access methods to obtain the JTA
 * TransactionManager (different for WebSphere 4.x and 5.x).
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see com.ibm.ejs.jts.jta.JTSXA#getTransactionManager
 * @see com.ibm.ejs.jts.jta.TransactionManagerFactory#getTransactionManager
 */
public class WebSphereJtaDialect implements JtaDialect {

	private TransactionManager transactionManager;

	public WebSphereJtaDialect()
	    throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class clazz;
		try {
			// try WebSphere 5
			clazz = Class.forName("com.ibm.ejs.jts.jta.TransactionManagerFactory");
		}
		catch (ClassNotFoundException ex) {
			// try WebSphere 4
			clazz = Class.forName("com.ibm.ejs.jts.jta.JTSXA");
		}
		this.transactionManager = (TransactionManager) clazz.getMethod("getTransactionManager", null).invoke(null, null);
	}

	public TransactionManager getInternalTransactionManager() {
		return transactionManager;
	}

	/**
	 * This implementation throws an InvalidIsolationLevelException:
	 * If we'll find out how to set an isolation level with WebSphere JTA,
	 * we'll actually implement this.
	 */
	public void applyIsolationLevel(UserTransaction ut, int isolationLevel) throws InvalidIsolationLevelException {
		if (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException("WebSphereJtaDialect does not support custom isolation levels");
		}
	}

}
