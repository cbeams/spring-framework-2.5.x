/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jndi.AbstractJndiLocator;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
 
/**
 * Helper class that provides static methods to obtain connections from
 * JNDI and close connections if necessary. Has support for thread-bound
 * connections, e.g. for use with DataSourceTransactionManager.
 *
 * <p>Note: The getDataSourceFromJndi methods are targetted at applications
 * that do not use a BeanFactory resp. an ApplicationContext. With the latter,
 * it is preferable to preconfigure your beans or even JdbcTemplate instances
 * in the factory: JndiObjectFactoryBean can be used to fetch a DataSource
 * from JNDI and give the DataSource bean reference to other beans. Switching
 * to another DataSource is just a matter of configuration then: You can even
 * replace the definition of the FactoryBean with a non-JNDI DataSource!
 *
 * @version $Id: DataSourceUtils.java,v 1.6 2004-01-26 18:03:42 jhoeller Exp $
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DataSourceTransactionManager
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public abstract class DataSourceUtils {

	private static final Log logger = LogFactory.getLog(DataSourceUtils.class);

	/**
	 * Look up the specified DataSource in JNDI, assuming that the lookup
	 * occurs in a J2EE container, i.e. adding the prefix "java:comp/env/"
	 * to the JNDI name if it doesn't already contain it.
	 * <p>Use getDataSourceFromJndi(jndiName,false) in case of a custom JNDI name.
	 * @param jndiName jndiName of the DataSource
	 * @return the DataSource
	 * @throws CannotGetJdbcConnectionException if the data source cannot be located
	 * @see #getDataSourceFromJndi(String, boolean)
	 */
	public static DataSource getDataSourceFromJndi(String jndiName) throws CannotGetJdbcConnectionException {
		return getDataSourceFromJndi(jndiName, true);
	}

	/**
	 * Look up the specified DataSource in JNDI, explicitly specifying
	 * if the lookup occurs in a J2EE container.
	 * @param jndiName jndiName of the DataSource
	 * @param inContainer if the lookup occurs in a J2EE container, i.e. if the prefix
	 * "java:comp/env/" needs to be added if the JNDI name doesn't already contain it.
	 * @return the DataSource
	 * @throws CannotGetJdbcConnectionException if the data source cannot be located
	 */
	public static DataSource getDataSourceFromJndi(String jndiName, boolean inContainer) throws CannotGetJdbcConnectionException {
		if (jndiName == null || "".equals(jndiName)) {
			throw new IllegalArgumentException("jndiName must not be empty");
		}
		if (inContainer && !jndiName.startsWith(AbstractJndiLocator.CONTAINER_PREFIX)) {
			jndiName = AbstractJndiLocator.CONTAINER_PREFIX + jndiName;
		}
		try {
			// Perform JNDI lookup to obtain resource manager connection factory
			return (DataSource) new JndiTemplate().lookup(jndiName);
		}
		catch (NamingException ex) {
			throw new CannotGetJdbcConnectionException("Naming exception looking up JNDI data source [" + jndiName + "]", ex);
		}
	}

	/**
	 * Get a connection from the given DataSource. Changes any SQL exception into
	 * the Spring hierarchy of unchecked generic data access exceptions, simplifying
	 * calling code and making any exception that is thrown more meaningful.
	 * <p>Is aware of a respective connection bound to the current thread, for example
	 * when using DataSourceTransactionManager. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * @param ds DataSource to get connection from
	 * @return a JDBC connection from this DataSource
	 * @throws CannotGetJdbcConnectionException if the attempt to get a Connection failed
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 * @see DataSourceTransactionManager
	 */
	public static Connection getConnection(DataSource ds) throws CannotGetJdbcConnectionException {
		return getConnection(ds, true);
	}

	public static Connection getConnection(DataSource ds, boolean allowSynchronization)
	    throws CannotGetJdbcConnectionException {
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(ds);
		if (conHolder != null) {
			return conHolder.getConnection();
		}
		else {
			try {
				Connection con = ds.getConnection();
				if (allowSynchronization && TransactionSynchronizationManager.isSynchronizationActive()) {
					logger.debug("Registering transaction synchronization for JDBC connection");
					// use same Connection for further JDBC actions within the transaction
					// thread object will get removed by synchronization at transaction completion
					conHolder = new ConnectionHolder(con);
					TransactionSynchronizationManager.bindResource(ds, conHolder);
					TransactionSynchronizationManager.registerSynchronization(new ConnectionSynchronization(conHolder, ds));
				}
				return con;
			}
			catch (SQLException ex) {
				throw new CannotGetJdbcConnectionException(ex);
			}
		}
	}

	/**
	 * Apply the current transaction timeout, if any,
	 * to the given JDBC Statement object.
	 * @param stmt the JDBC Statement object
	 * @param ds DataSource that the connection came from
	 */
	public static void applyTransactionTimeout(Statement stmt, DataSource ds) throws SQLException {
		ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(ds);
		if (holder != null && holder.getDeadline() != null) {
			stmt.setQueryTimeout(holder.getTimeToLiveInSeconds());
		}
	}

	/**
	 * Close the given connection if necessary, i.e. if it is not bound to the thread
	 * and it is not created by a SmartDataSource returning shouldClose=false.
	 * @param con connection to close if necessary
	 * (if this is null, the call will be ignored)
	 * @param ds DataSource that the connection came from
	 * @throws CannotCloseJdbcConnectionException if the attempt to close the
	 * Connection failed
	 * @see SmartDataSource#shouldClose
	 */
	public static void closeConnectionIfNecessary(Connection con, DataSource ds) throws CannotCloseJdbcConnectionException {
		if (con == null || TransactionSynchronizationManager.hasResource(ds)) {
			return;
		}
		// leave the connection open only if the DataSource is our
		// special data source, and it wants the connection left open
		if (!(ds instanceof SmartDataSource) || ((SmartDataSource) ds).shouldClose(con)) {
			try {
				con.close();
			}
			catch (SQLException ex) {
				throw new CannotCloseJdbcConnectionException(ex);
			}
		}
	}

	/**
	 * Wrap the given connection with a proxy that delegates every method call to it
	 * but suppresses close calls. This is useful for allowing application code to
	 * handle a special framework connection just like an ordinary DataSource connection.
	 * @param source original connection
	 * @return the wrapped connection
	 * @see SingleConnectionDataSource
	 */
	static Connection getCloseSuppressingConnectionProxy(Connection source) {
		return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
		                                           new Class[] {Connection.class},
		                                           new CloseSuppressingInvocationHandler(source));
	}


	/**
	 * Invocation handler that suppresses close calls on JDBC connections.
	 * @see #getCloseSuppressingConnectionProxy
	 */
	private static class CloseSuppressingInvocationHandler implements InvocationHandler {

		private final Connection source;

		private CloseSuppressingInvocationHandler(Connection source) {
			this.source = source;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals("close")) {
				// Don't pass the call on
				return null;
			}
			try {
				return method.invoke(this.source, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}


	/**
	 * Callback for resource cleanup at the end of a non-native-JDBC transaction
	 * (e.g. when participating in a JTA transaction).
	 */
	private static class ConnectionSynchronization extends TransactionSynchronizationAdapter {

		private ConnectionHolder connectionHolder;

		private DataSource dataSource;

		private ConnectionSynchronization(ConnectionHolder connectionHolder, DataSource dataSource) {
			this.connectionHolder = connectionHolder;
			this.dataSource = dataSource;
		}

		public void suspend() {
			TransactionSynchronizationManager.unbindResource(this.dataSource);
		}

		public void resume() {
			TransactionSynchronizationManager.bindResource(this.dataSource, this.connectionHolder);
		}

		public void beforeCompletion() throws CannotCloseJdbcConnectionException {
			TransactionSynchronizationManager.unbindResource(this.dataSource);
			closeConnectionIfNecessary(this.connectionHolder.getConnection(), this.dataSource);
		}
	}

}
