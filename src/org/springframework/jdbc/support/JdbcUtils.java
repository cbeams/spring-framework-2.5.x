/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Generic utility methods for working with JDBC. Mainly for internal use
 * within the framework, but also useful for custom JDBC access code.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 */
public abstract class JdbcUtils {

	private static final Log logger = LogFactory.getLog(JdbcUtils.class);


	/**
	 * Close the given JDBC Connection and ignore any thrown exception.
	 * This is useful for typical finally blocks in manual JDBC code.
	 * @param con the JDBC Connection to close
	 */
	public static void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			}
			catch (SQLException ex) {
				logger.error("Could not close JDBC Connection", ex);
			}
			catch (Throwable ex) {
				// We don't trust the JDBC driver: It might throw RuntimeException or Error.
				logger.error("Unexpected exception on closing JDBC Connection", ex);
			}
		}
	}

	/**
	 * Close the given JDBC Statement and ignore any thrown exception.
	 * This is useful for typical finally blocks in manual JDBC code.
	 * @param stmt the JDBC Statement to close
	 */
	public static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			}
			catch (SQLException ex) {
				logger.warn("Could not close JDBC Statement", ex);
			}
			catch (Throwable ex) {
				// We don't trust the JDBC driver: It might throw RuntimeException or Error.
				logger.error("Unexpected exception on closing JDBC Statement", ex);
			}
		}
	}

	/**
	 * Close the given JDBC ResultSet and ignore any thrown exception.
	 * This is useful for typical finally blocks in manual JDBC code.
	 * @param rs the JDBC ResultSet to close
	 */
	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			}
			catch (SQLException ex) {
				logger.warn("Could not close JDBC ResultSet", ex);
			}
			catch (Throwable ex) {
				// We don't trust the JDBC driver: It might throw RuntimeException or Error.
				logger.error("Unexpected exception on closing JDBC ResultSet", ex);
			}
		}
	}

	/**
	 * Retrieve a JDBC column value from a ResultSet, using the most appropriate
	 * value type. The returned value should be a detached value object, not having
	 * any ties to the active ResultSet: in particular, it should not be a Blob or
	 * Clob object but rather a byte array respectively String representation.
	 * <p>Uses the <code>getObject(index)</code> method, but includes additional "hacks"
	 * to get around Oracle 10g returning a non-standard object for its TIMESTAMP
	 * datatype and a <code>java.sql.Date</code> for DATE columns leaving out the
	 * time portion: These columns will explicitly be extracted as standard
	 * <code>java.sql.Timestamp</code> object.
	 * @param rs is the ResultSet holding the data
	 * @param index is the column index
	 * @return the value object
	 * @see java.sql.Blob
	 * @see java.sql.Clob
	 * @see java.sql.Timestamp
	 * @see oracle.sql.TIMESTAMP
	 */
	public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
		Object obj = rs.getObject(index);
		if (obj instanceof Blob) {
			obj = rs.getBytes(index);
		}
		else if (obj instanceof Clob) {
			obj = rs.getString(index);
		}
		else if (obj != null && obj.getClass().getName().startsWith("oracle.sql.TIMESTAMP")) {
			obj = rs.getTimestamp(index);
		}
		else if (obj != null && obj instanceof java.sql.Date) {
			if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
				obj = rs.getTimestamp(index);
			}
		}
		return obj;
	}

	/**
	 * Extract database meta data via the given DatabaseMetaDataCallback.
	 * <p>This method will open a connection to the database and retrieve the database metadata.
	 * Since this method is called before the exception translation feature is configured for
	 * a datasource, this method can not rely on the SQLException translation functionality.
	 * <p>Any exceptions will be wrapped in a MetaDataAccessException. This is a checked exception
	 * and any calling code should catch and handle this exception. You can just log the
	 * error and hope for the best, but there is probably a more serious error that will
	 * reappear when you try to access the database again.
	 * @param dataSource the DataSource to extract metadata for
	 * @param action callback that will do the actual work
	 * @return object containing the extracted information, as returned by
	 * the DatabaseMetaDataCallback's <code>processMetaData</code> method
	 * @throws MetaDataAccessException if meta data access failed
	 */
	public static Object extractDatabaseMetaData(DataSource dataSource, DatabaseMetaDataCallback action)
			throws MetaDataAccessException {

		Connection con = null;
		try {
			con = DataSourceUtils.getConnection(dataSource);
			if (con == null) {
				// should only happen in test environments
				throw new MetaDataAccessException("Connection returned by DataSource [" + dataSource + "] was null");
			}
			DatabaseMetaData metaData = con.getMetaData();
			if (metaData == null) {
				// should only happen in test environments
				throw new MetaDataAccessException("DatabaseMetaData returned by Connection [" + con + "] was null");
			}
			return action.processMetaData(metaData);
		}
		catch (CannotGetJdbcConnectionException ex) {
			throw new MetaDataAccessException("Could not get Connection for extracting meta data", ex);
		}
		catch (SQLException ex) {
			throw new MetaDataAccessException("Error while extracting DatabaseMetaData", ex);
		}
		catch (AbstractMethodError err) {
			throw new MetaDataAccessException(
					"JDBC DatabaseMetaData method not implemented by JDBC driver - upgrade your driver", err);
		}
		finally {
			DataSourceUtils.releaseConnection(con, dataSource);
		}
	}

	/**
	 * Call the specified method on DatabaseMetaData for the given DataSource,
	 * and extract the invocation result.
	 * @param dataSource the DataSource to extract meta data for
	 * @param metaDataMethodName the name of the DatabaseMetaData method to call
	 * @return the object returned by the specified DatabaseMetaData method
	 * @throws MetaDataAccessException if we couldn't access the DatabaseMetaData
	 * or failed to invoke the specified method
	 * @see java.sql.DatabaseMetaData
	 */
	public static Object extractDatabaseMetaData(DataSource dataSource, final String metaDataMethodName)
			throws MetaDataAccessException {

		return extractDatabaseMetaData(dataSource,
				new DatabaseMetaDataCallback() {
					public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
						try {
							Method method = dbmd.getClass().getMethod(metaDataMethodName, (Class[]) null);
							return method.invoke(dbmd, (Object[]) null);
						}
						catch (NoSuchMethodException ex) {
							throw new MetaDataAccessException("No method named '" + metaDataMethodName +
									"' found on DatabaseMetaData instance [" + dbmd + "]", ex);
						}
						catch (IllegalAccessException ex) {
							throw new MetaDataAccessException(
									"Could not access DatabaseMetaData method '" + metaDataMethodName + "'", ex);
						}
						catch (InvocationTargetException ex) {
							if (ex.getTargetException() instanceof SQLException) {
								throw (SQLException) ex.getTargetException();
							}
							throw new MetaDataAccessException(
									"Invocation of DatabaseMetaData method '" + metaDataMethodName + "' failed", ex);
						}
					}
				});
	}

	/**
	 * Return whether the given JDBC driver supports JDBC 2.0 batch updates.
	 * <p>Typically invoked right before execution of a given set of statements:
	 * to decide whether the set of SQL statements should be executed through
	 * the JDBC 2.0 batch mechanism or simply in a traditional one-by-one fashion.
	 * <p>Logs a warning if the "supportsBatchUpdates" methods throws an exception
	 * and simply returns false in that case.
	 * @param con the Connection to check
	 * @return whether JDBC 2.0 batch updates are supported
	 * @see java.sql.DatabaseMetaData#supportsBatchUpdates
	 */
	public static boolean supportsBatchUpdates(Connection con) {
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			if (dbmd != null) {
				if (dbmd.supportsBatchUpdates()) {
					if (logger.isDebugEnabled()) {
						logger.debug("JDBC driver supports batch updates");
					}
					return true;
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("JDBC driver does not support batch updates");
					}
				}
			}
		}
		catch (SQLException ex) {
			logger.warn("JDBC driver 'supportsBatchUpdates' method threw exception", ex);
		}
		catch (AbstractMethodError err) {
			logger.warn("JDBC driver does not support JDBC 2.0 'supportsBatchUpdates' method", err);
		}
		return false;
	}

	/**
	 * Count the occurrences of the character <code>placeholder</code> in an SQL string
	 * <code>str</code>. The character <code>placeholder</code> is not counted if it
	 * appears within a literal as determined by the <code>delim</code> that is passed in.
	 * Delegates to the overloaded method that takes a String with multiple delimiters.
	 * @param str string to search in. Returns 0 if this is null.
	 * @param placeholder the character to search for and count
	 * @param delim the delimiter for character literals
	 */
	public static int countParameterPlaceholders(String str, char placeholder, char delim) {
		return countParameterPlaceholders(str, placeholder, "" + delim);
	}

	/**
	 * Count the occurrences of the character <code>placeholder</code> in an SQL string
	 * <code>str</code>. The character <code>placeholder</code> is not counted if it
	 * appears within a literal as determined by the <code>delimiters</code> that are passed in.
	 * <p>Examples: If one of the delimiters is the single quote, and the character to count the
	 * occurrences of is the question mark, then:
	 * <p><code>The big ? 'bad wolf?'</code> gives a count of one.<br>
	 * <code>The big ?? bad wolf</code> gives a count of two.<br>
	 * <code>The big  'ba''ad?' ? wolf</code> gives a count of one.
	 * <p>The grammar of the string passed in should obey the rules of the JDBC spec
	 * which is close to no rules at all: one placeholder per parameter, and it should
	 * be valid SQL for the target database.
	 * @param str string to search in. Returns 0 if this is null
	 * @param placeholder the character to search for and count.
	 * @param delimiters the delimiters for character literals.
	 */
	public static int countParameterPlaceholders(String str, char placeholder, String delimiters) {
		int count = 0;
		boolean insideLiteral = false;
		int activeLiteral = -1;
		for (int i = 0; str != null && i < str.length(); i++) {
			if (str.charAt(i) == placeholder) {
				if (!insideLiteral)
					count++;
			}
 			else {
				if (delimiters.indexOf(str.charAt(i)) > -1) {
					if (!insideLiteral) {
						insideLiteral = true;
						activeLiteral = delimiters.indexOf(str.charAt(i));
					}
					else {
						if (activeLiteral == delimiters.indexOf(str.charAt(i))) {
							insideLiteral = false;
							activeLiteral = -1;
						}
					}
				}
			}
		}
		return count;
	}

	/**
	 * Check that a SQL type is numeric.
	 * @param sqlType the SQL type to be checked
	 * @return if the type is numeric
	 */
	public static boolean isNumeric(int sqlType) {
		return Types.BIT == sqlType || Types.BIGINT == sqlType || Types.DECIMAL == sqlType ||
				Types.DOUBLE == sqlType || Types.FLOAT == sqlType || Types.INTEGER == sqlType ||
				Types.NUMERIC == sqlType || Types.REAL == sqlType || Types.SMALLINT == sqlType ||
				Types.TINYINT == sqlType;
	}

	/**
	 * Translate a SQL type into one of a few values:
	 * All string types are translated to String.
	 * All integer types are translated to Integer.
	 * All real types are translated to Double.
	 * All other types are left untouched.
	 * @param sqlType the type to be translated into a simpler type
	 * @return the new SQL type
	 * @deprecated This is only used by deprecated constructors in
	 * SqlFunction and will be removed alongside those constructors.
	 * @see org.springframework.jdbc.object.SqlFunction#SqlFunction(javax.sql.DataSource, String, int)
	 */
	public static int translateType(int sqlType) {
		int retType = sqlType;
		if (Types.CHAR == sqlType || Types.VARCHAR == sqlType) {
			retType = Types.VARCHAR;
		}
		else if (Types.BIT == sqlType || Types.TINYINT == sqlType || Types.SMALLINT == sqlType ||
				Types.INTEGER == sqlType) {
			retType = Types.INTEGER;
		}
		else if (Types.DECIMAL == sqlType || Types.DOUBLE == sqlType || Types.FLOAT == sqlType ||
				Types.NUMERIC == sqlType || Types.REAL == sqlType) {
			retType = Types.NUMERIC;
		}
		return retType;
	}

}
