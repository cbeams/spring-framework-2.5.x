/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

/**
 * Interface implemented by JdbcTemplate.
 * Not often used, but a useful option to enhance testability,
 * as it can easily be mocked or stubbed.
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @author Rod Johnson
 * @version $Id: IJdbcTemplate.java,v 1.1 2003-11-20 09:01:42 johnsonr Exp $
 */
public interface IJdbcTemplate {

	/**
	 * Execute a query given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded query method
	 * with the PREPARE_STATEMENT constant as PreparedStatementSetter argument.
	 * <p>In most cases the query() method should be preferred to the parallel
	 * doWithResultSetXXXX() method. The doWithResultSetXXXX() methods are
	 * included to allow full control over the extraction of data from ResultSets
	 * and to facilitate integration with third-party software.
	 * @param sql SQL query to execute
	 * @param callbackHandler object that will extract results
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #query(String, PreparedStatementSetter, RowCallbackHandler)
	 */
	void query(String sql, RowCallbackHandler callbackHandler) throws DataAccessException;
	
	/**
	 * Execute a query given static SQL.
	 * Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded query method
	 * with a NOP PreparedStatement setter as a parameter.
	 * @param sql SQL query to execute
	 * @param rse object that will extract all rows of results
	 * @throws DataAccessException if there is any problem executing
	 * the query
	 */
	void doWithResultSetFromStaticQuery(String sql, ResultSetExtractor rse) throws DataAccessException;
	
	/**
	 * Query using a prepared statement.
	 * @param psc Callback handler that can create a PreparedStatement
	 * given a Connection
	 * @param callbackHandler object that will extract results,
	 * one row at a time
	 * @throws DataAccessException if there is any problem
	 */
	void query(PreparedStatementCreator psc, RowCallbackHandler callbackHandler) throws DataAccessException;
	
	/**
	 * Query using a prepared statement. Most other query methods use
	 * this method.
	 * @param psc Callback handler that can create a PreparedStatement
	 * given a Connection
	 * @param rse object that will extract results.
	 * @throws DataAccessException if there is any problem
	 */
	void doWithResultSetFromPreparedQuery(PreparedStatementCreator psc, ResultSetExtractor rse) throws DataAccessException;
	
	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * PreparedStatementSetter implementation that knows how to bind values
	 * to the query.
	 * @param sql SQL to execute
	 * @param pss object that knows how to set values on the prepared statement.
	 * If this is null, the SQL will be assumed to contain no bind parameters.
	 * Even if there are no bind parameters, this object may be used to
	 * set fetch size and other performance options.
	 * @param callbackHandler object that will extract results
	 * @throws DataAccessException if the query fails
	 */
	void query(final String sql, final PreparedStatementSetter pss, RowCallbackHandler callbackHandler) throws DataAccessException;
	
	/**
	 * Issue a single SQL update.
	 * @param sql static SQL to execute
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem.
	 */
	int update(final String sql) throws DataAccessException;
	
	/**
	 * Issue an update using a PreparedStatementCreator to provide SQL and any required
	 * parameters
	 * @param psc helper: callback object that provides SQL and any necessary parameters
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int update(PreparedStatementCreator psc) throws DataAccessException;
	/**
	 * Issue multiple updates using multiple PreparedStatementCreators to provide SQL
	 * and any required parameters.
	 * @param pscs array of callback objects that provide SQL and any necessary parameters
	 * @return an array of the number of rows affected by each statement
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int[] update(PreparedStatementCreator[] pscs) throws DataAccessException;
	
	/**
	 * Issue an update using a PreparedStatementSetter to set bind parameters,
	 * with given SQL. Simpler than using a PreparedStatementCreator
	 * as this method will create the PreparedStatement: the
	 * PreparedStatementSetter has only to set parameters.
	 * @param sql SQL, containing bind parameters
	 * @param pss helper that sets bind parameters. If this is null
	 * we run an update with static SQL
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int update(final String sql, final PreparedStatementSetter pss) throws DataAccessException;
	
	/**
	 * Issue multiple updates using JDBC 2.0 batch updates and PreparedStatementSetters to
	 * set values on a PreparedStatement created by this method
	 * @param sql defining PreparedStatement that will be reused.
	 * All statements in the batch will use the same SQL.
	 * @param setter object to set parameters on the
	 * PreparedStatement created by this method
	 * @return an array of the number of rows affected by each statement
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int[] batchUpdate(String sql, BatchPreparedStatementSetter setter) throws DataAccessException;
	
	/**
	 * Execute an Sql call using a CallableStatementCreator to provide SQL and any required
	 * parameters
	 * @param csc helper: callback object that provides SQL and any necessary parameters
	 * @return Map of extracted out parameters
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	Map execute(CallableStatementCreator csc, List declaredParameters) throws DataAccessException;
	
}