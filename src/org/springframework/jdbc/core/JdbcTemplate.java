/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * <b>This is the central class in the JDBC core package.</b>
 * It simplifies the use of JDBC and helps to avoid common errors. It executes
 * core JDBC workflow, leaving application code to provide SQL and extract results.
 * This class executes SQL queries or updates, initating iteration over
 * ResultSets and catching JDBC exceptions and translating them to
 * the generic, more informative, exception hierarchy defined in
 * the org.springframework.dao package.
 *
 * <p>Code using this class need only implement callback interfaces,
 * giving them a clearly defined contract. The PreparedStatementCreator callback
 * interface creates a prepared statement given a Connection provided by this class,
 * providing SQL and any necessary parameters. The RowCallbackHandler interface
 * extracts values from each row of a ResultSet.
 *
 * <p>Can be used within a service implementation via direct instantiation
 * with a DataSource reference, or get prepared in an application context
 * and given to services as bean reference. Note: The DataSource should
 * always be configured as a bean in the application context, in the first case
 * given to the service directly, in the second case to the prepared template.
 *
 * <p>The motivation and design of this class is discussed
 * in detail in
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">Expert One-On-One J2EE Design and Development</a>
 * by Rod Johnson (Wrox, 2002).
 *
 * <p>Because this class is parameterizable by the callback interfaces and the
 * SQLExceptionTranslator interface, it isn't necessary to subclass it.
 * All SQL issued by this class is logged.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Yann Caroff
 * @author Thomas Risberg
 * @author Isabelle Muszynski
 * @version $Id: JdbcTemplate.java,v 1.10 2003-11-03 09:27:46 johnsonr Exp $
 * @since May 3, 2001
 * @see org.springframework.dao
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public class JdbcTemplate implements InitializingBean {

	/**
	 * Constant for use as a parameter to query methods to force use of a PreparedStatement
	 * rather than a Statement, even when there are no bind parameters.
	 * For example, query(sql, JdbcTemplate.PREPARE_STATEMENT, callbackHandler)
	 * will force the use of a JDBC PreparedStatement even if the SQL
	 * passed in has no bind parameters.
	 */
	public static final PreparedStatementSetter PREPARE_STATEMENT =
	    new PreparedStatementSetter() {
		    public void setValues(PreparedStatement ps) throws SQLException {
			    // do nothing
		    }
	    };

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Used to obtain connections throughout the lifecycle of this object.
	 * This enables this class to close connections if necessary.
	 **/
	private DataSource dataSource;

	/** Helper to translate SQL exceptions to DataAccessExceptions */
	private SQLExceptionTranslator exceptionTranslator;

	/** 
	 * Custom query executor. This default may be overridden by a JavaBean property
	 * if desired.
	 */
	private QueryExecutor queryExecutor = new DefaultQueryExecutor();

	/** If this variable is false, we will throw exceptions on SQL warnings */
	private boolean ignoreWarnings = true;


	/**
	 * Construct a new JdbcTemplate for bean usage.
	 * Note: The DataSource has to be set before using the instance.
	 * This constructor can be used to prepare a JdbcTemplate via a BeanFactory,
	 * typically setting the DataSource via setDataSourceName.
	 * @see #setDataSource
	 */
	public JdbcTemplate() {
	}

	/**
	 * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
	 * Note: This will trigger eager initialization of the exception translator.
	 * @param dataSource JDBC DataSource to obtain connections from
	 */
	public JdbcTemplate(DataSource dataSource) {
		setDataSource(dataSource);
		afterPropertiesSet();
	}

	/**
	 * Set the JDBC DataSource to obtain connections from.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Return the DataSource used by this template.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Set the exception translator used in this class.
	 * If no custom translator is provided, a default is used
	 * which examines the SQLException's SQLState code.
	 * @param exceptionTranslator custom exception translator
	 */
	public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
		this.exceptionTranslator = exceptionTranslator;
	}

	/**
	 * Return the exception translator for this instance.
	 * Creates a default one for the specified DataSource if none set.
	 */
	public synchronized SQLExceptionTranslator getExceptionTranslator() {
		if (this.exceptionTranslator == null) {
			this.exceptionTranslator =
			    SQLExceptionTranslatorFactory.getInstance().getDefaultTranslator(this.dataSource);
		}
		return this.exceptionTranslator;
	}

	/**
	 * Set a custom QueryExecutor implementation.
	 */
	public void setQueryExecutor(QueryExecutor queryExecutor) {
		this.queryExecutor = queryExecutor;
	}

	/**
	 * Set whether or not we want to ignore SQLWarnings.
	 * Default is true.
	 */
	public void setIgnoreWarnings(boolean ignoreWarnings) {
		this.ignoreWarnings = ignoreWarnings;
	}

	/**
	 * Return whether or not we ignore SQLWarnings.
	 * Default is true.
	 */
	public boolean getIgnoreWarnings() {
		return ignoreWarnings;
	}

	/**
	 * Eagerly initialize the exception translator,
	 * creating a default one for the specified DataSource if none set.
	 */
	public void afterPropertiesSet() {
		if (this.dataSource == null) {
			throw new IllegalArgumentException("dataSource is required");
		}
		getExceptionTranslator();
	}


	//-------------------------------------------------------------------------
	// Public methods
	//-------------------------------------------------------------------------

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
	 * @see #PREPARE_STATEMENT
	 */
	public void query(String sql, RowCallbackHandler callbackHandler) throws DataAccessException {
		doWithResultSetFromStaticQuery(sql,
		    new RowCallbackHandlerResultSetExtractor(callbackHandler));
	}

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
	public void doWithResultSetFromStaticQuery(String sql, ResultSetExtractor rse) throws DataAccessException {
		if (sql == null)
			throw new InvalidDataAccessApiUsageException("SQL may not be null");
		if (containsBindVariables(sql))
			throw new InvalidDataAccessApiUsageException(
			    "Cannot execute [" + sql + "] as a static query: it contains bind variables");

		Connection con = DataSourceUtils.getConnection(this.dataSource);;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, this.dataSource);

			if (logger.isDebugEnabled())
				logger.debug("Executing static SQL query [" + sql + "] using a java.sql.Statement");

			rs = this.queryExecutor.executeQuery(stmt, sql);
			rse.extractData(rs);

			SQLWarning warning = stmt.getWarnings();
			throwExceptionOnWarningIfNotIgnoringWarnings(warning);
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate("JdbcTemplate.query(sql)", sql, ex);
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				}
				catch (SQLException ignore) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException ignore) {
				}
			}
			DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
		}
	}

	/**
	 * Query using a prepared statement.
	 * @param psc Callback handler that can create a PreparedStatement
	 * given a Connection
	 * @param callbackHandler object that will extract results,
	 * one row at a time
	 * @throws DataAccessException if there is any problem
	 */
	public void query(PreparedStatementCreator psc, RowCallbackHandler callbackHandler)
	    throws DataAccessException {
		doWithResultSetFromPreparedQuery( psc,
		    new RowCallbackHandlerResultSetExtractor(callbackHandler));
	}

	/**
	 * Query using a prepared statement. Most other query methods use
	 * this method.
	 * @param psc Callback handler that can create a PreparedStatement
	 * given a Connection
	 * @param rse object that will extract results.
	 * @throws DataAccessException if there is any problem
	 */
	public void doWithResultSetFromPreparedQuery(PreparedStatementCreator psc, ResultSetExtractor rse)
	    throws DataAccessException {
		Connection con = DataSourceUtils.getConnection(this.dataSource);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = psc.createPreparedStatement(con);

			if (logger.isDebugEnabled())
				logger.debug("Executing SQL query using PreparedStatement [" + psc + "]");

			rs = this.queryExecutor.executeQuery(ps);
			rse.extractData(rs);

			SQLWarning warning = ps.getWarnings();
			throwExceptionOnWarningIfNotIgnoringWarnings(warning);
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate(
			    "JdbcTemplate.query with PreparedStatementCreator [" + psc + "]",
			    null, ex);
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				}
				catch (SQLException ignore) {
				}
			}
			if (ps != null) {
				try {
					ps.close();
				}
				catch (SQLException ignore) {
				}
			}
			DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
		}
	}

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
	public void query(final String sql, final PreparedStatementSetter pss, RowCallbackHandler callbackHandler)
	    throws DataAccessException {
		if (sql == null)
			throw new InvalidDataAccessApiUsageException("SQL may not be null");

		if (pss == null) {
			// Check there are no bind parameters, in which case pss could not be null
			if (containsBindVariables(sql))
				throw new InvalidDataAccessApiUsageException(
				    "SQL [" + sql + "] requires at least one bind variable, but PreparedStatementSetter parameter was null");
			query(sql, callbackHandler);
		}
		else {
			// Wrap it in a new PreparedStatementCreator
			query(new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection conn)
				    throws SQLException {
					PreparedStatement ps = conn.prepareStatement(sql);
					DataSourceUtils.applyTransactionTimeout(ps, dataSource);
					pss.setValues(ps);
					return ps;
				}
			}, callbackHandler);
		}
	}

	/**
	 * Return whether the given SQL String contains bind variables
	 */
	private boolean containsBindVariables(String sql) {
		return sql.indexOf("?") != -1;
	}

	/**
	 * Issue a single SQL update.
	 * @param sql static SQL to execute
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem.
	 */
	public int update(final String sql) throws DataAccessException {
		if (logger.isDebugEnabled())
			logger.debug("Running SQL update [" + sql + "]");

		return update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection conn)
			    throws SQLException {
				PreparedStatement ps = conn.prepareStatement(sql);
				DataSourceUtils.applyTransactionTimeout(ps, dataSource);
				return ps;
			}
		});
	}

	/**
	 * Issue an update using a PreparedStatementCreator to provide SQL and any required
	 * parameters
	 * @param psc helper: callback object that provides SQL and any necessary parameters
	 * @return the number of rows affected
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	public int update(PreparedStatementCreator psc) throws DataAccessException {
		return update(new PreparedStatementCreator[]{psc})[0];
	}

	/**
	 * Issue multiple updates using multiple PreparedStatementCreators to provide SQL
	 * and any required parameters.
	 * @param pscs array of callback objects that provide SQL and any necessary parameters
	 * @return an array of the number of rows affected by each statement
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	public int[] update(PreparedStatementCreator[] pscs) throws DataAccessException {
		Connection con = DataSourceUtils.getConnection(this.dataSource);
		PreparedStatement ps = null;
		int index = 0;
		try {
			int[] retvals = new int[pscs.length];
			for (index = 0; index < retvals.length; index++) {
				ps = pscs[index].createPreparedStatement(con);
				if (logger.isDebugEnabled())
					logger.debug("Executing SQL update using PreparedStatement [" + pscs[index] + "]");
				retvals[index] = ps.executeUpdate();
				if (logger.isDebugEnabled())
					logger.debug("SQL update affected " + retvals[index] + " rows");
				ps.close();
			}

			// Don't worry about warnings, as we're more likely to get exception on updates
			// (for example on data truncation)
			return retvals;
		}
		catch (SQLException ex) {
			if (ps != null) {
				try {
					ps.close();
				}
				catch (SQLException ignore) {
				}
			}
			throw getExceptionTranslator().translate(
			    "processing update " + (index + 1) + " of " + pscs.length + "; update was [" + pscs[index] + "]",
			    null, ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
		}
	}

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
	public int update(final String sql, final PreparedStatementSetter pss) throws DataAccessException {
		if (pss == null)
			return update(sql);

		return update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection conn)
			    throws SQLException {
				PreparedStatement ps = conn.prepareStatement(sql);
				DataSourceUtils.applyTransactionTimeout(ps, dataSource);
				pss.setValues(ps);
				return ps;
			}
		});
	}

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
	public int[] batchUpdate(String sql, BatchPreparedStatementSetter setter) throws DataAccessException {
		Connection con = DataSourceUtils.getConnection(this.dataSource);
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(sql);
			DataSourceUtils.applyTransactionTimeout(ps, dataSource);
			int batchSize = setter.getBatchSize();
			for (int i = 0; i < batchSize; i++) {
				setter.setValues(ps, i);
				ps.addBatch();
			}
			int[] retvals = ps.executeBatch();
			ps.close();
			return retvals;
		}
		catch (SQLException ex) {
			if (ps != null) {
				try {
					ps.close();
				}
				catch (SQLException ignore) {
				}
			}
			throw getExceptionTranslator().translate(
			    "processing batch update with size=" + setter.getBatchSize() + "; update was [" + sql + "]",
			    sql, ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
		}
	}

	/**
	 * Execute an Sql call using a CallableStatementCreator to provide SQL and any required
	 * parameters
	 * @param csc helper: callback object that provides SQL and any necessary parameters
	 * @return Map of extracted out parameters
	 * @throws DataAccessException if there is any problem issuing the update
	 */
	public Map execute(CallableStatementCreator csc, List declaredParameters) throws DataAccessException {
		Connection con = DataSourceUtils.getConnection(this.dataSource);
		CallableStatement cs = null;
		try {
			cs = csc.createCallableStatement(con);
			if (logger.isDebugEnabled())
				logger.debug("Executing call using CallableStatement [" + cs + "]");
			boolean retval = cs.execute();
			if (logger.isDebugEnabled())
				logger.debug("CallableStatement.execute returned [" + retval + "]");
			if (retval)
				extractReturnedResultSets(cs, declaredParameters);
			Map retMap = extractOutputParameters(cs, declaredParameters);
			return retMap;
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate(
			    "JdbcTemplate.execute()",
			    cs.toString(),
			    ex);
		}
		finally {
			if (cs != null) {
				try {
					cs.close();
				}
				catch (SQLException ignore) {
				}
			}
			DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
		}
	}

	/**
	 * Extract output parameters from the completed stored procedure.
	 * @param cs JDBC wrapper for the stored procedure
	 * @param parameters parameter list for the stored procedure
	 * @return parameters to the stored procedure
	 */
	private Map extractOutputParameters(CallableStatement cs, List parameters) throws SQLException {
		Map outParams = new HashMap();
		int sqlColIndx = 1;
		for (int i = 0; i < parameters.size(); i++) {
			SqlParameter p = (SqlParameter) parameters.get(i);
			if (p instanceof SqlOutParameter) {
				Object out = null;
				out = cs.getObject(sqlColIndx);
				if (out instanceof ResultSet) {
					// We can't pass back a resultset since the connection will be closed - we must process it
					try {
						if (((SqlOutParameter) p).isResultSetSupported()) {
							ResultSetExtractor rse = new RowCallbackHandlerResultSetExtractor(((SqlOutParameter) p).getRowCallbackHandler());
							rse.extractData((ResultSet) out);
							logger.debug("ResultSet returned from stored procedure was processed");
							outParams.put(p.getName(), "ResultSet processed.");
						}
						else {
							logger.warn("ResultSet returned from stored procedure but a corresponding SqlOutParameter with a RowCallbackHandler was not declared");
							outParams.put(p.getName(), "ResultSet was returned but not processed.");
						}
					}
					catch (SQLException se) {
						throw se;
					}
					finally {
						try {
							((ResultSet) out).close();
						}
						catch (SQLException ignore) {
						}
					}
				}
				else {
					outParams.put(p.getName(), out);
				}
			}
			if (!(p instanceof SqlReturnResultSet)) {
				sqlColIndx++;
			}
		}
		return outParams;
	}

	/**
	 * Extract returned resultsets from the completed stored procedure.
	 * @param cs JDBC wrapper for the stored procedure
	 * @param parameters Parameter list for the stored procedure
	 */
	private void extractReturnedResultSets(CallableStatement cs, List parameters) throws SQLException {
		int rsIndx = 0;
		do {
			SqlParameter p = null;
			if (parameters != null && parameters.size() > rsIndx)
				p = (SqlParameter) parameters.get(rsIndx);
			if (p != null && p instanceof SqlReturnResultSet) {
				ResultSet rs = null;
				rs = cs.getResultSet();
				try {
					new RowCallbackHandlerResultSetExtractor(
					    ((SqlReturnResultSet) p)
					    .getRowCallbackHandler())
					    .extractData(
					        rs);
				}
				catch (SQLException se) {
					throw se;
				}
				finally {
					try {
						rs.close();
					}
					catch (SQLException ignore) {
					}
				}
			}
			else {
				logger.warn("ResultSet returned from stored procedure but a corresponding SqlReturnResultSet parameter was not declared");
			}
			rsIndx++;
		}
		while (cs.getMoreResults());
	}

	/**
	 * Convenience method to throw an SQLWarningException if we're
	 * not ignoring warnings.
	 * @param warning warning from current statement. May be null,
	 * in which case this method does nothing.
	 */
	private void throwExceptionOnWarningIfNotIgnoringWarnings(SQLWarning warning)
	    throws SQLWarningException {
		if (warning != null) {
			if (this.ignoreWarnings) {
				logger.warn("SQLWarning ignored: " + warning);
			}
			else {
				throw new SQLWarningException("Warning not ignored", warning);
			}
		}
	}


	/**
	 * Default implementation of the QueryExecutor interface.
	 * Simply executes the respective query on the given statement.
	 */
	private static final class DefaultQueryExecutor implements QueryExecutor {

		public ResultSet executeQuery(Statement stmt, String sql) throws SQLException {
			return stmt.executeQuery(sql);
		}

		public ResultSet executeQuery(PreparedStatement ps) throws SQLException {
			return ps.executeQuery();
		}
	}


	/**
	 * Adapter to enable use of a RowCallbackHandler inside a
	 * ResultSetExtractor. Uses a  regular ResultSet, so we have
	 * to be careful when using it, so we don't use it for navigating
	 * since this could lead to unpreditable consequences.
	 */
	private static final class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor {

		/**
		 * RowCallbackHandler to use to extract data
		 */
		private RowCallbackHandler callbackHandler;

		/**
		 * Construct a new ResultSetExtractor that will use the given
		 * RowCallbackHandler to process each row.
		 */
		private RowCallbackHandlerResultSetExtractor(RowCallbackHandler callbackHandler) {
			this.callbackHandler = callbackHandler;
		}

		public void extractData(ResultSet rs) throws SQLException {
			while (rs.next()) {
				this.callbackHandler.processRow(rs);
			}
		}
	}

}	// class JdbcTemplate
