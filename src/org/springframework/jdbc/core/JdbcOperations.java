/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of JDBC operations.
 * Implemented by JdbcTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Alternatively, the standard JDBC infrastructure can be mocked.
 * However, mocking this interface constitutes significantly less work.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: JdbcOperations.java,v 1.5 2004-03-03 10:45:05 jhoeller Exp $
 * @see JdbcTemplate
 */
public interface JdbcOperations {

	//-------------------------------------------------------------------------
	// Query methods dealing with static SQL
	//-------------------------------------------------------------------------

	/**
	 * Execute a query given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded query method
	 * with null as PreparedStatementSetter argument.
	 * @param sql SQL query to execute
	 * @param callbackHandler object that will extract results
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #query(String, PreparedStatementSetter, RowCallbackHandler)
	 */
	void query(String sql, RowCallbackHandler callbackHandler) throws DataAccessException;

	/**
	 * Execute a query for a result list, given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded queryForList
	 * method with null as argument array.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The results will be mapped to an ArrayList (one entry for each row) of
	 * HashMaps (one entry for each column using the column name as the key).
	 * @param sql SQL query to execute
	 * @return an ArrayList that contains a HashMap per row
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #queryForList(String, Object[])
	 */
	List queryForList(String sql) throws DataAccessException;

	/**
	 * Execute a query for a result object, given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded queryForObject
	 * method with null as argument array.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query; the returned
	 * result will be directly mapped to the corresponding object type.
	 * @param sql SQL query to execute
	 * @param requiredType the type that the result object is expected to match
	 * @return the result object of the required type
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #queryForObject(String, Object[], Class)
	 */
	Object queryForObject(String sql, Class requiredType) throws DataAccessException;

	/**
	 * Execute a query that results in a long value, given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded queryForLong
	 * method with null as argument array.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query that results
	 * in a long value.
	 * @param sql SQL query to execute
	 * @return the long value
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #queryForLong(String, Object[])
	 */
	long queryForLong(String sql) throws DataAccessException;

	/**
	 * Execute a query that results in an int value, given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded queryForInt
	 * method with null as argument array.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query that results
	 * in an int value.
	 * @param sql SQL query to execute
	 * @return the int value
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #queryForInt(String, Object[])
	 */
	int queryForInt(String sql) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Query methods dealing with prepared statements
	//-------------------------------------------------------------------------

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
	void query(String sql, PreparedStatementSetter pss, RowCallbackHandler callbackHandler)
	    throws DataAccessException;
	
	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query.
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * @param argTypes SQL types of the arguments
	 * (constants from java.sql.Types)
	 * @param callbackHandler object that will extract results
	 * @throws DataAccessException if the query fails
	 * @see java.sql.Types
	 */
	void query(String sql, final Object[] args, final int[] argTypes, RowCallbackHandler callbackHandler)
	    throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query.
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @param callbackHandler object that will extract results
	 * @throws DataAccessException if the query fails
	 */
	void query(String sql, final Object[] args, RowCallbackHandler callbackHandler)
	    throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result list.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The results will be mapped to an ArrayList (one entry for each row) of
	 * HashMaps (one entry for each column using the column name as the key).
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @return an ArrayList that contains a HashMap per row
	 * @throws DataAccessException if the query fails
	 * @see #queryForList(String)
	 */
	List queryForList(String sql, final Object[] args) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, expecting a result object.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query; the returned
	 * result will be directly mapped to the corresponding object type.
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @param requiredType the type that the result object is expected to match
	 * @return the result object of the required type
	 * @throws DataAccessException if the query fails
	 * @see #queryForObject(String, Class)
	 */
	Object queryForObject(String sql, final Object[] args, Class requiredType)
	    throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, resulting in a long value.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query that results
	 * in a long value.
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @return the long value
	 * @throws DataAccessException if the query fails
	 * @see #queryForLong(String)
	 */
	long queryForLong(String sql, final Object[] args) throws DataAccessException;

	/**
	 * Query given SQL to create a prepared statement from SQL and a
	 * list of arguments to bind to the query, resulting in an int value.
	 * <p>This method is useful for running static SQL with a known outcome.
	 * The query is expected to be a single row/single column query that results
	 * in an int value.
	 * @param sql SQL to execute
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @return the int value
	 * @throws DataAccessException if the query fails
	 * @see #queryForInt(String)
	 */
	int queryForInt(String sql, final Object[] args) throws DataAccessException;

	
	//-------------------------------------------------------------------------
	// Execute and update methods
	//-------------------------------------------------------------------------

	/**
	 * Issue a single SQL execute, typically a DDL statement.
	 * @param sql static SQL to execute
	 * @throws DataAccessException if there is any problem.
	 */
	void execute(final String sql) throws DataAccessException;

	/**
	 * Issue a single SQL update.
	 * @param sql static SQL to execute
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem.
	 */
	int update(final String sql) throws DataAccessException;
	
	/**
	 * Issue an update using a PreparedStatementCreator to provide SQL and any
	 * required parameters.
	 * @param psc callback object that provides SQL and any necessary parameters
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int update(PreparedStatementCreator psc) throws DataAccessException;

	/**
	 * Issue multiple updates using multiple PreparedStatementCreators to provide
	 * SQL and any required parameters.
	 * @param pscs array of callback objects that provide SQL and any necessary parameters
	 * @return an array of the number of rows affected by each statement
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int[] update(PreparedStatementCreator[] pscs) throws DataAccessException;
	
	/**
	 * Issue an update using a PreparedStatementSetter to set bind parameters,
	 * with given SQL. Simpler than using a PreparedStatementCreator as this
	 * method will create the PreparedStatement: The PreparedStatementSetter
	 * just needs to set parameters.
	 * @param sql SQL, containing bind parameters
	 * @param pss helper that sets bind parameters. If this is null
	 * we run an update with static SQL.
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int update(final String sql, final PreparedStatementSetter pss) throws DataAccessException;
	
	/**
	 * Issue an update via a prepared statement, binding the given arguments.
	 * @param sql SQL, containing bind parameters
	 * @param args arguments to bind to the query
	 * @param argTypes SQL types of the arguments
	 * (constants from java.sql.Types)
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int update(String sql, final Object[] args, final int[] argTypes) throws DataAccessException;

	/**
	 * Issue an update via a prepared statement, binding the given arguments.
	 * @param sql SQL, containing bind parameters
	 * @param args arguments to bind to the query
	 * (leaving it to the PreparedStatement to guess the respective SQL type)
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int update(String sql, final Object[] args) throws DataAccessException;

	/**
	 * Issue multiple updates using JDBC 2.0 batch updates and PreparedStatementSetters
	 * to set values on a PreparedStatement created by this method
	 * @param sql defining PreparedStatement that will be reused.
	 * All statements in the batch will use the same SQL.
	 * @param pss object to set parameters on the
	 * PreparedStatement created by this method
	 * @return an array of the number of rows affected by each statement
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Methods dealing with callable statements
	//-------------------------------------------------------------------------

	/**
	 * Execute a SQL call using a CallableStatementCreator to provide SQL and any required
	 * parameters.
	 * @param csc callback object that provides SQL and any necessary parameters
	 * @return Map of extracted out parameters
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	Map call(CallableStatementCreator csc, List declaredParameters) throws DataAccessException;
	
}
