package org.springframework.orm.ibatis;

import java.sql.Connection;
import java.sql.SQLException;

import com.ibatis.db.sqlmap.MappedStatement;

/**
 * Callback interface for data access code that works on an iBATIS Database Layer
 * MappedStatement. To be used with SqlMapTemplate's execute method, assumably
 * often as anonymous classes within a method implementation.
 *
 * <p>NOTE: The SqlMap/MappedStatement API is the one to use with iBATIS SQL Maps 1.x.
 * The SqlMapClient/SqlMapSession is only available with SQL Maps 2.
 *
 * @author Juergen Hoeller
 * @since 28.11.2003
 */
public interface SqlMapCallback {

	/**
	 * Gets called by SqlMapTemplate.execute with an active JDBC Connection.
	 * Does not need to care about the lifecycle of the Connection or handling transactions.
	 *
	 * <p>If called without a thread-bound JDBC transaction (initiated by
	 * DataSourceTransactionManager), the code will simply get executed on the
	 * underlying JDBC connection with its transactional semantics.
	 * If SqlMapTemplate is configured to use a JTA-aware DataSource,
	 * the JDBC connection and thus the callback code will be transactional
	 * if a JTA transaction is active.
	 *
	 * <p>Allows for returning a result object created within the callback, i.e.
	 * a domain object or a collection of domain objects. A thrown RuntimeException
	 * is treated as application exception, it gets propagated to the caller of the
	 * template.
	 *
	 * @param stmt the iBATIS Database Layer mapped statement
	 * @param con the JDBC Connection to work on
	 * @return a result object, or null if none
	 * @throws SQLException if thrown by MappedStatement methods
	 */
	Object doInMappedStatement(MappedStatement stmt, Connection con) throws SQLException;

}
