/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * One of the two central callback interfaces used by the JdbcTemplate class.
 * This interface creates a PreparedStatement given a connection, provided
 * by the JdbcTemplate class. Implementations are responsible for providing
 * SQL and any necessary parameters.
 *
 * <p>Implementations <i>do not</i> need to concern themselves with
 * SQLExceptions that may be thrown from operations they attempt.
 * The JdbcTemplate class will catch and handle SQLExceptions appropriately.
 *
 * @author Rod Johnson
 * @version $Id: PreparedStatementCreator.java,v 1.4 2003-12-05 17:03:13 jhoeller Exp $
 */
public interface PreparedStatementCreator {

	/** 
	 * Create a statement in this connection. Allows implementations to use
	 * PreparedStatements. Only invoked if no SQL is passed into the
	 * ResultSetHandler. The ResultSetHandler will close this statement.
	 * @param con Connection to use to create statement
	 * @return a prepared statement
	 * @throws SQLException there is no need to catch SQLExceptions
	 * that may be thrown in the implementation of this method.
	 * The JdbcTemplate class will handle them.
	 */
	PreparedStatement createPreparedStatement(Connection con) throws SQLException;

}
