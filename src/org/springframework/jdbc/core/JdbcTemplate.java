/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

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
 * @version $Id: JdbcTemplate.java,v 1.25 2004-02-17 18:56:47 trisberg Exp $
 * @since May 3, 2001
 * @see org.springframework.dao
 * @see org.springframework.jdbc.object
 * @see org.springframework.jdbc.datasource
 */
public class JdbcTemplate extends JdbcAccessor implements JdbcOperations, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	/** Custom NativeJdbcExtractor */
	private NativeJdbcExtractor nativeJdbcExtractor;

	/** If this variable is false, we will throw exceptions on SQL warnings */
	private boolean ignoreWarnings = true;


	/**
	 * Construct a new JdbcTemplate for bean usage.
	 * Note: The DataSource has to be set before using the instance.
	 * This constructor can be used to prepare a JdbcTemplate via a BeanFactory,
	 * typically setting the DataSource via setDataSource.
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
	 * Set a NativeJdbcExtractor to extract native JDBC objects from wrapped handles.
	 * Useful if native Statement and/or ResultSet handles are expected for casting
	 * to database-specific implementation classes, but a connection pool that wraps
	 * JDBC objects is used (note: <i>any</i> pool will return wrapped Connections).
	 */
	public void setNativeJdbcExtractor(NativeJdbcExtractor extractor) {
		this.nativeJdbcExtractor = extractor;
	}

	/**
	 * Return the current NativeJdbcExtractor implementation.
	 */
	public NativeJdbcExtractor getNativeJdbcExtractor() {
		return this.nativeJdbcExtractor;
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


	//-------------------------------------------------------------------------
	// Query methods dealing with static SQL
	//-------------------------------------------------------------------------

	/**
	 * Execute a query given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded query method
	 * with a NOP PreparedStatement setter as a parameter.
	 * @param sql SQL query to execute
	 * @param rse object that will extract all rows of results
	 * @return an arbitrary result object, as returned by the ResultSetExtractor
	 * @throws DataAccessException if there is any problem executing the query
	 */
	public Object doWithResultSetFromStaticQuery(String sql, ResultSetExtractor rse) throws DataAccessException {
		if (sql == null) {
			throw new InvalidDataAccessApiUsageException("SQL may not be null");
		}
		if (containsBindVariables(sql)) {
			throw new InvalidDataAccessApiUsageException(
			    "Cannot execute [" + sql + "] as a static query: it contains bind variables");
		}
		Connection con = DataSourceUtils.getConnection(getDataSource());
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Executing static SQL query [" + sql + "] using a java.sql.Statement");
			}
			Connection conToUse = con;
			if (this.nativeJdbcExtractor != null &&
			    this.nativeJdbcExtractor.isNativeConnectionNecessaryForNativeStatements()) {
				conToUse = this.nativeJdbcExtractor.getNativeConnection(con);
			}
			stmt = conToUse.createStatement();
			DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
			Statement stmtToUse = stmt;
			if (this.nativeJdbcExtractor != null) {
				stmtToUse = this.nativeJdbcExtractor.getNativeStatement(stmt);
			}
			rs = stmtToUse.executeQuery(sql);
			ResultSet rsToUse = rs;
			if (this.nativeJdbcExtractor != null) {
				rsToUse = this.nativeJdbcExtractor.getNativeResultSet(rs);
			}
			Object result = rse.extractData(rsToUse);
			SQLWarning warning = stmt.getWarnings();
			throwExceptionOnWarningIfNotIgnoringWarnings(warning);
			return result;
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate("JdbcTemplate.query", sql, ex);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(stmt);
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
		}
	}

	/**
	 * Execute a query given static SQL.
	 * <p>Uses a JDBC Statement, not a PreparedStatement. If you want to execute
	 * a static query with a PreparedStatement, use the overloaded query method
	 * with null as PreparedStatementSetter argument.
	 * <p>In most cases the query methods should be preferred to the parallel
	 * doWithResultSetXXXX() method. The doWithResultSetXXXX() methods are
	 * included to allow full control over the extraction of data from ResultSets
	 * and to facilitate integration with third-party software.
	 * @param sql SQL query to execute
	 * @param callbackHandler object that will extract results
	 * @throws DataAccessException if there is any problem executing the query
	 * @see #query(String, PreparedStatementSetter, RowCallbackHandler)
	 */
	public void query(String sql, RowCallbackHandler callbackHandler) throws DataAccessException {
		doWithResultSetFromStaticQuery(sql,
		    new RowCallbackHandlerResultSetExtractor(callbackHandler));
	}

	public List queryForList(String sql) throws DataAccessException {
		return (List) doWithResultSetFromStaticQuery(sql, new ListResultSetExtractor());
	}

	public Object queryForObject(String sql, Class requiredType) throws DataAccessException {
		return doWithResultSetFromStaticQuery(sql, new ObjectResultSetExtractor(requiredType));
	}

	public int queryForInt(String sql) throws DataAccessException {
		return ((Integer) queryForObject(sql, Integer.class)).intValue();
	}


	//-------------------------------------------------------------------------
	// Query methods dealing with prepared statements
	//-------------------------------------------------------------------------

	/**
	 * Query using a prepared statement. Most other query methods use this method.
	 * @param psc Callback handler that can create a PreparedStatement given a
	 * Connection
	 * @param rse object that will extract results.
	 * @return an arbitrary result object, as returned by the ResultSetExtractor
	 * @throws DataAccessException if there is any problem
	 */
	public Object doWithResultSetFromPreparedQuery(PreparedStatementCreator psc, ResultSetExtractor rse)
	    throws DataAccessException {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Connection conToUse = con;
			if (this.nativeJdbcExtractor != null &&
			    this.nativeJdbcExtractor.isNativeConnectionNecessaryForNativePreparedStatements()) {
				conToUse = this.nativeJdbcExtractor.getNativeConnection(con);
			}
			ps = psc.createPreparedStatement(conToUse);
			if (logger.isDebugEnabled()) {
				logger.debug("Executing SQL query using PreparedStatement [" + psc + "]");
			}
			PreparedStatement psToUse = ps;
			if (this.nativeJdbcExtractor != null) {
				psToUse = this.nativeJdbcExtractor.getNativePreparedStatement(ps);
			}
			rs = psToUse.executeQuery();
			ResultSet rsToUse = rs;
			if (this.nativeJdbcExtractor != null) {
				rsToUse = this.nativeJdbcExtractor.getNativeResultSet(rs);
			}
			Object result = rse.extractData(rsToUse);
			SQLWarning warning = ps.getWarnings();
			throwExceptionOnWarningIfNotIgnoringWarnings(warning);
			return result;
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate(
			    "JdbcTemplate.query with PreparedStatementCreator [" + psc + "]",
			    null, ex);
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
		}
	}

	/**
	 * Query using a prepared statement.
	 * <p>In most cases the query methods should be preferred to the parallel
	 * doWithResultSetXXXX() method. The doWithResultSetXXXX() methods are
	 * included to allow full control over the extraction of data from ResultSets
	 * and to facilitate integration with third-party software.
	 * @param psc Callback handler that can create a PreparedStatement
	 * given a Connection
	 * @param callbackHandler object that will extract results,
	 * one row at a time
	 * @throws DataAccessException if there is any problem
	 */
	public void query(PreparedStatementCreator psc, RowCallbackHandler callbackHandler)
	    throws DataAccessException {
		doWithResultSetFromPreparedQuery(psc,
		    new RowCallbackHandlerResultSetExtractor(callbackHandler));
	}

	public void query(String sql, PreparedStatementSetter pss, RowCallbackHandler callbackHandler)
	    throws DataAccessException {
		if (sql == null) {
			throw new InvalidDataAccessApiUsageException("SQL may not be null");
		}
		query(new PreparedStatementSetterPreparedStatementCreator(sql, pss), callbackHandler);
	}

	public void query(String sql, final Object[] args, final int[] argTypes, RowCallbackHandler callbackHandler)
	    throws DataAccessException {
		if ((args != null && argTypes == null) || (args == null && argTypes != null) ||
		    (args != null && args.length != argTypes.length)) {
			throw new InvalidDataAccessApiUsageException("args and argTypes parameters must match");
		}
		query(sql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						ps.setObject(i + 1, args[i], argTypes[i]);
					}
				}
			}
		}, callbackHandler);
	}

	public void query(String sql, final Object[] args, RowCallbackHandler callbackHandler)
	    throws DataAccessException {
		query(sql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						ps.setObject(i + 1, args[i]);
					}
				}
			}
		}, callbackHandler);
	}

	public List queryForList(String sql, final Object[] args) throws DataAccessException {
		return (List) doWithResultSetFromPreparedQuery(
		    new PreparedStatementSetterPreparedStatementCreator(sql, new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						if (args != null) {
							for (int i = 0; i < args.length; i++) {
								ps.setObject(i + 1, args[i]);
							}
						}
					}
				}),
		    new ListResultSetExtractor());
	}

	public Object queryForObject(String sql, final Object[] args, Class requiredType)
	    throws DataAccessException {
		return doWithResultSetFromPreparedQuery(
		    new PreparedStatementSetterPreparedStatementCreator(sql, new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						if (args != null) {
							for (int i = 0; i < args.length; i++) {
								ps.setObject(i + 1, args[i]);
							}
						}
					}
				}),
		    new ObjectResultSetExtractor(requiredType));
	}

	public int queryForInt(String sql, final Object[] args) throws DataAccessException {
		return ((Integer) queryForObject(sql, args, Integer.class)).intValue();
	}


	//-------------------------------------------------------------------------
	// Update methods
	//-------------------------------------------------------------------------

	public int update(final String sql) throws DataAccessException {
		if (logger.isDebugEnabled()) {
			logger.debug("Running SQL update [" + sql + "]");
		}
		return update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(sql);
				DataSourceUtils.applyTransactionTimeout(ps, getDataSource());
				if (nativeJdbcExtractor != null) {
					return nativeJdbcExtractor.getNativePreparedStatement(ps);
				}
				return ps;
			}
		});
	}

	public int update(PreparedStatementCreator psc) throws DataAccessException {
		return update(new PreparedStatementCreator[]{psc})[0];
	}

	public int[] update(PreparedStatementCreator[] pscs) throws DataAccessException {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		PreparedStatement ps = null;
		int index = 0;
		try {
			Connection conToUse = con;
			if (this.nativeJdbcExtractor != null &&
			    this.nativeJdbcExtractor.isNativeConnectionNecessaryForNativePreparedStatements()) {
				conToUse = this.nativeJdbcExtractor.getNativeConnection(con);
			}
			int[] retVals = new int[pscs.length];
			for (index = 0; index < retVals.length; index++) {
				ps = pscs[index].createPreparedStatement(conToUse);
				if (logger.isDebugEnabled()) {
					logger.debug("Executing SQL update using PreparedStatement [" + pscs[index] + "]");
				}
				retVals[index] = ps.executeUpdate();
				if (logger.isDebugEnabled()) {
					logger.debug("SQL update affected " + retVals[index] + " rows");
				}
				ps.close();
			}

			// Don't worry about warnings, as we're more likely to get exception on updates
			// (for example on data truncation)
			return retVals;
		}
		catch (SQLException ex) {
			JdbcUtils.closeStatement(ps);
			throw getExceptionTranslator().translate(
			    "processing update " + (index + 1) + " of " + pscs.length + "; update was [" + pscs[index] + "]",
			    null, ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
		}
	}

	public int update(final String sql, final PreparedStatementSetter pss) throws DataAccessException {
		return update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(sql);
				DataSourceUtils.applyTransactionTimeout(ps, getDataSource());
				PreparedStatement psToUse = ps;
				if (nativeJdbcExtractor != null) {
					psToUse = nativeJdbcExtractor.getNativePreparedStatement(ps);
				}
				if (pss != null) {
					pss.setValues(psToUse);
				}
				return ps;
			}
		});
	}

	public int update(String sql, final Object[] args, final int[] argTypes) throws DataAccessException {
		return update(sql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						ps.setObject(i, args[i], argTypes[i]);
					}
				}
			}
		});
	}

	public int update(String sql, final Object[] args) throws DataAccessException {
		return update(sql, new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						ps.setObject(i, args[i]);
					}
				}
			}
		});
	}

	public int[] batchUpdate(String sql, BatchPreparedStatementSetter pss) throws DataAccessException {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		PreparedStatement ps = null;
		try {
			Connection conToUse = con;
			if (this.nativeJdbcExtractor != null &&
			    this.nativeJdbcExtractor.isNativeConnectionNecessaryForNativePreparedStatements()) {
				conToUse = this.nativeJdbcExtractor.getNativeConnection(con);
			}
			ps = conToUse.prepareStatement(sql);
			DataSourceUtils.applyTransactionTimeout(ps, getDataSource());
			PreparedStatement psToUse = ps;
			if (this.nativeJdbcExtractor != null) {
				psToUse = this.nativeJdbcExtractor.getNativePreparedStatement(ps);
			}
			int batchSize = pss.getBatchSize();
			for (int i = 0; i < batchSize; i++) {
				pss.setValues(psToUse, i);
				ps.addBatch();
			}
			int[] retVals = ps.executeBatch();
			ps.close();
			return retVals;
		}
		catch (SQLException ex) {
			JdbcUtils.closeStatement(ps);
			throw getExceptionTranslator().translate(
			    "processing batch update with size=" + pss.getBatchSize() + "; update was [" + sql + "]",
			    sql, ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
		}
	}


	//-------------------------------------------------------------------------
	// Methods dealing with callable statements
	//-------------------------------------------------------------------------

	public Map execute(CallableStatementCreator csc, List declaredParameters) throws DataAccessException {
		Connection con = DataSourceUtils.getConnection(getDataSource());
		CallableStatement cs = null;
		try {
			Connection conToUse = con;
			if (this.nativeJdbcExtractor != null &&
			    this.nativeJdbcExtractor.isNativeConnectionNecessaryForNativeCallableStatements()) {
				conToUse = this.nativeJdbcExtractor.getNativeConnection(con);
			}
			cs = csc.createCallableStatement(conToUse);
			CallableStatement csToUse = cs;
			if (this.nativeJdbcExtractor != null) {
				csToUse = this.nativeJdbcExtractor.getNativeCallableStatement(cs);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Executing call using CallableStatement [" + cs + "]");
			}
			boolean retVal = csToUse.execute();
			if (logger.isDebugEnabled()) {
				logger.debug("CallableStatement.execute returned [" + retVal + "]");
			}
			Map retMap = new HashMap();
			if (retVal) {
				retMap.putAll(extractReturnedResultSets(csToUse, declaredParameters));
			}
			retMap.putAll(extractOutputParameters(csToUse, declaredParameters));
			return retMap;
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate(
			    "JdbcTemplate.execute()",
			    csc.toString(),
			    ex);
		}
		finally {
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.closeConnectionIfNecessary(con, getDataSource());
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
							ResultSetExtractor rse = null;
							if (((SqlOutParameter) p).isRowMapperSupported()) {
								rse = new RowCallbackHandlerResultSetExtractor(((SqlOutParameter) p).newResultReader());
							}
							else {
								rse = new RowCallbackHandlerResultSetExtractor(((SqlOutParameter) p).getRowCallbackHandler());
							}
							rse.extractData((ResultSet) out);
							logger.debug("ResultSet returned from stored procedure was processed");
							if (((SqlOutParameter) p).isRowMapperSupported()) {
								outParams.put(p.getName(), ((ResultReader) ((RowCallbackHandlerResultSetExtractor) rse).getCallbackHandler()).getResults() );
							}
							else {
								outParams.put(p.getName(), "ResultSet processed.");
							}
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
						JdbcUtils.closeResultSet((ResultSet) out);
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
	private Map extractReturnedResultSets(CallableStatement cs, List parameters) throws SQLException {
		Map returnedResults = new HashMap();
		int rsIndx = 0;
		do {
			SqlParameter p = null;
			if (parameters != null && parameters.size() > rsIndx) {
				p = (SqlParameter) parameters.get(rsIndx);
			}
			if (p != null && p instanceof SqlReturnResultSet) {
				ResultSet rs = null;
				rs = cs.getResultSet();
				try {
					ResultSet rsToUse = rs;
					if (this.nativeJdbcExtractor != null) {
						rsToUse = this.nativeJdbcExtractor.getNativeResultSet(rs);
					}
					ResultSetExtractor rse = null;
					if (((SqlReturnResultSet) p).isRowMapperSupported()) {
						rse = new RowCallbackHandlerResultSetExtractor(((SqlReturnResultSet) p).newResultReader());
					}
					else {
						rse = new RowCallbackHandlerResultSetExtractor(((SqlReturnResultSet) p).getRowCallbackHandler());
					}
					rse.extractData(rsToUse);
					if (((SqlReturnResultSet) p).isRowMapperSupported()) {
						returnedResults.put(p.getName(), ((ResultReader)((RowCallbackHandlerResultSetExtractor) rse).getCallbackHandler()).getResults());
					}
					else {
						returnedResults.put(p.getName(), "ResultSet returned from stored procedure was processed");
					}
				}
				finally {
					JdbcUtils.closeResultSet(rs);
				}
			}
			else {
				logger.warn("ResultSet returned from stored procedure but a corresponding SqlReturnResultSet parameter was not declared");
			}
			rsIndx++;
		} while (cs.getMoreResults());

		return returnedResults;
	}


	/**
	 * Return whether the given SQL String contains bind variables
	 */
	private boolean containsBindVariables(String sql) {
		return sql.indexOf("?") != -1;
	}

	/**
	 * Convenience method to throw an SQLWarningException if we're
	 * not ignoring warnings.
	 * @param warning warning from current statement. May be null,
	 * in which case this method does nothing.
	 */
	private void throwExceptionOnWarningIfNotIgnoringWarnings(SQLWarning warning) throws SQLWarningException {
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
	 * Adapter to enable use of a PreparedStatementSetter inside a
	 * PreparedStatementCreator.
	 */
	private final class PreparedStatementSetterPreparedStatementCreator implements PreparedStatementCreator {

		private final String sql;

		private final PreparedStatementSetter pss;

		/**
		 * Constructor a new PreparedStatementCreator that uses the given SQL
		 * and the given PreparedStatementSetter to prepare the statement.
		 * @param sql SQL to execute
		 * @param pss object that knows how to set values on the prepared statement
		 */
		private PreparedStatementSetterPreparedStatementCreator(String sql, PreparedStatementSetter pss) {
			this.sql = sql;
			this.pss = pss;
		}

		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			PreparedStatement ps = con.prepareStatement(this.sql);
			DataSourceUtils.applyTransactionTimeout(ps, getDataSource());
			PreparedStatement psToUse = ps;
			if (nativeJdbcExtractor != null) {
				psToUse = nativeJdbcExtractor.getNativePreparedStatement(ps);
			}
			if (this.pss != null) {
				this.pss.setValues(psToUse);
			}
			return psToUse;
		}
	}


	/**
	 * Adapter to enable use of a RowCallbackHandler inside a
	 * ResultSetExtractor.
	 * <p>Uses a regular ResultSet, so we have to be careful when using it:
	 * We don't use it for navigating since this could lead to unpredictable
	 * consequences.
	 */
	private static final class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor {

		/** RowCallbackHandler to use to extract data */
		private RowCallbackHandler callbackHandler;

		/**
		 * Construct a new ResultSetExtractor that will use the given
		 * RowCallbackHandler to process each row.
		 */
		private RowCallbackHandlerResultSetExtractor(RowCallbackHandler callbackHandler) {
			this.callbackHandler = callbackHandler;
		}

		public RowCallbackHandler getCallbackHandler() {
			return callbackHandler;
		}

		public Object extractData(ResultSet rs) throws SQLException {
			while (rs.next()) {
				this.callbackHandler.processRow(rs);
			}
			return null;
		}
	}


	/**
	 * ResultSetExtractor implementation that returns an ArrayList of HashMaps.
	 */
	private static final class ListResultSetExtractor implements ResultSetExtractor {

		public Object extractData(ResultSet rs) throws SQLException {
			ResultSetMetaData rsmd = rs.getMetaData();
			int numberOfColumns = rsmd.getColumnCount();
			List listOfRows = new ArrayList();
			while (rs.next()) {
				Map mapOfColValues = new HashMap(numberOfColumns);
				for (int i = 1; i <= numberOfColumns; i++) {
					mapOfColValues.put(rsmd.getColumnName(i), rs.getObject(i));
				}
				listOfRows.add(mapOfColValues);
			}
			if (listOfRows.size() == 1 && numberOfColumns == 1) {
				return ((Map) listOfRows.get(0)).get(rsmd.getColumnName(1));
			}
			else {
				return listOfRows;
			}
		}
	}


	/**
	 * ResultSetExtractor implementation that returns single result object.
	 */
	private static final class ObjectResultSetExtractor implements ResultSetExtractor {

		private final Class requiredType;

		private ObjectResultSetExtractor(Class requiredType) {
			this.requiredType = requiredType;
		}

		public Object extractData(ResultSet rs) throws SQLException {
			ResultSetMetaData rsmd = rs.getMetaData();
			int nrOfColumns = rsmd.getColumnCount();
			if (nrOfColumns != 1) {
				throw new InvalidDataAccessApiUsageException("Expected single column, but received " + nrOfColumns + " columns");
			}
			if (!rs.next()) {
				throw new InvalidDataAccessApiUsageException("Expected single row, not empty ResultSet");
			}
			Object result = null;
			if (requiredType.equals(Integer.class) && (
			rsmd.getColumnType(1) == java.sql.Types.NUMERIC ||
			rsmd.getColumnType(1) == java.sql.Types.INTEGER ||
			rsmd.getColumnType(1) == java.sql.Types.SMALLINT ||
			rsmd.getColumnType(1) == java.sql.Types.TINYINT))
				result = new Integer(rs.getInt(1));
			else
				result = rs.getObject(1);
			if (rs.next()) {
				throw new InvalidDataAccessApiUsageException("Expected single row, not more than one");
			}
			if (this.requiredType != null && !this.requiredType.isInstance(result)) {
				throw new InvalidDataAccessApiUsageException("Result object (db-type=\"" + rsmd.getColumnTypeName(1) + "\" value=\"" + 
															result + "\") is of type [" + rsmd.getColumnClassName(1) + "] and not of required type [" +
															this.requiredType.getName() + "]");
			}
			return result;
		}
	}

}
