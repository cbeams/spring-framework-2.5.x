package org.springframework.jdbc.core;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

/**
 * Interface usable on Java 5 and above exposing a set of
 * common JDBC operations, whose interface is simplified thorugh
 * the use of var args and autoboxing.
 * 
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 1.3
 * @see JdbcTemplate
 */
public interface SimpleJdbcOperations {

	/**
	 * Expose the classic Spring JdbcTemplate to allow invocation of less
	 * commonly used methods. Can also be used to obtain the DataSource.
	 * @return {@link JdbcTemplate} wrapped by this class.
	 */
	JdbcOperations getJdbcOperations();

	/**
	 * Query for an <code>int</code> passing in a SQL query and a variable
	 * number of arguments.
	 */
	int queryForInt(String sql, Object... args) throws DataAccessException;

	/**
	 * Query for an <code>long</code> passing in a SQL query and a variable
	 * number of arguments.
	 */
	long queryForLong(String sql, Object... args) throws DataAccessException;

	/**
	 * Query for an object of type <code>T</code> identified by the supplied @{@link Class}.
	 * @param sql the SQL query to run.
	 * @param requiredType the required type of the return value.
	 * @param args the args for the query.
	 * @see JdbcOperations#queryForObject(String, Class)
	 * @see JdbcOperations#queryForObject(String, Object[], Class)
	 */
	<T> T queryForObject(String sql, Class<T> requiredType, Object... args)
			throws DataAccessException;

	/**
	 * Query for an object of type <code>T</code> using the supplied
	 * {@link ParameterizedRowMapper} to the query results to the object.
	 * @param sql the SQL query to run.
	 * @param rm the @{@link ParameterizedRowMapper} to use for result mapping
	 * @param args the args for the query.
	 * @see JdbcOperations#queryForObject(String, RowMapper)
	 * @see JdbcOperations#queryForObject(String, Object[], RowMapper)
	 */
	<T> T queryForObject(String sql, ParameterizedRowMapper<T> rm, Object... args)
			throws DataAccessException;

	/**
	 * Query for a {@link List} of <code>Objects</code> of type <code>T</code> using
	 * the supplied {@link ParameterizedRowMapper} to the query results to the object.
	 * @param sql the SQL query to run.
	 * @param rm the @{@link ParameterizedRowMapper} to use for result mapping
	 * @param args the args for the query.
	 * @see JdbcOperations#queryForObject(String, RowMapper)
	 * @see JdbcOperations#queryForObject(String, Object[], RowMapper)
	 */
	<T> List<T> query(String sql, ParameterizedRowMapper<T> rm, Object... args)
			throws DataAccessException;

	/**
	 * Execute the supplied query with the (optional) supplied arguments.
	 * <p>The query is expected to be a single row query; the result row will be
	 * mapped to a Map (one entry for each column, using the column name as the key).
	 * @see JdbcOperations#queryForMap(String)
	 * @see JdbcOperations#queryForMap(String, Object[])
	 */
	Map<String, Object> queryForMap(String sql, Object... args)
			throws DataAccessException;

	/**
	 * Execute the supplied query with the (optional) supplied arguments.
	 * <p>Each element in the returned {@link List} is constructed as a {@link Map}
	 * as described in {@link #queryForMap}
	 * @see JdbcOperations#queryForList(String)
	 * @see JdbcOperations#queryForList(String, Object[]) 
	 */
	List<Map<String, Object>> queryForList(String sql, Object... args)
			throws DataAccessException;

	/**
	 * Executes the supplied SQL statement with (optional) supplied arguments.
	 * @return the numbers of rows affected by the update.
	 * @see JdbcOperations#update(String)
	 * @see JdbcOperations#update(String, Object[])
	 */
	int update(String sql, Object... args) throws DataAccessException;

}