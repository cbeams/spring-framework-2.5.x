/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Callback interface used by the JdbcTemplate class.
 *
 * <p>This interface sets values on a PreparedStatement provided by the
 * JdbcTemplate class. Implementations are responsible for setting any
 * necessary parameters. SQL with placeholders will already have been supplied.
 *
 * <p>It's easier to use this interface than PreparedStatementCreator,
 * as the JdbcTemplate will create the prepared statement.
 *
 * <p>Implementations <i>do not</i> need to concern themselves with
 * SQLExceptions that may be thrown from operations they attempt.
 * The JdbcTemplate class will catch and handle SQLExceptions appropriately.
 *
 * @version $Id: PreparedStatementSetter.java,v 1.3 2004-01-04 23:43:42 jhoeller Exp $
 * @author Rod Johnson
 * @since March 2, 2003
 * @see JdbcTemplate#update(String, PreparedStatementSetter)
 */
public interface PreparedStatementSetter {

	/** 
	* Set values on the given PreparedStatement.
	* @param ps PreparedStatement we'll invoke setter methods on
	* @throws SQLException there is no need to catch SQLExceptions
	* that may be thrown in the implementation of this method.
	* The JdbcTemplate class will handle them.
	*/
	void setValues(PreparedStatement ps) throws SQLException;

}
