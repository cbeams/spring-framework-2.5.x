/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * One of the three central callback interfaces used by the JdbcTemplate class.
 * This interface creates a CallableStatement given a connection, provided
 * by the JdbcTemplate class. Implementations are responsible for providing
 * SQL and any necessary parameters.
 *
 * <p>Implementations <i>do not</i> need to concern themselves with
 * SQLExceptions that may be thrown from operations they attempt.
 * The JdbcTemplate class will catch and handle SQLExceptions appropriately.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 */
public interface CallableStatementCreator {

	/** 
	 * Create a callable statement in this connection. Allows implementations to use
	 * CallableStatements. 
	 * @param con Connection to use to create statement
	 * @return a callable statement
	 * @throws SQLException there is no need to catch SQLExceptions
	 * that may be thrown in the implementation of this method.
	 * The JdbcTemplate class will handle them.
	 */
	CallableStatement createCallableStatement(Connection con) throws SQLException;

}
