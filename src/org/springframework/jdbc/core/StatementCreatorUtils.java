/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility methods for PreparedStatementCreator and CallableStatementCreator
 * implementations, providing sophisticated parameter management (including
 * support for LOB values).
 *
 * <p>Used in PreparedStatementCreatorFactory and CallableStatementCreatorFactory.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 26.06.2004
 * @see PreparedStatementCreator
 * @see CallableStatementCreator
 * @see PreparedStatementCreatorFactory
 * @see CallableStatementCreatorFactory
 * @see SqlParameter
 * @see SqlTypeValue
 * @see org.springframework.jdbc.core.support.SqlLobValue
 */
public class StatementCreatorUtils {

	private static final Log logger = LogFactory.getLog(StatementCreatorUtils.class);

	/**
	 * Set the value for a parameter. The method used is based on the SQL type
	 * of the parameter and we can handle complex types like arrays and LOBs.
	 * @param ps the prepared statement or callable statement
	 * @param paramIndex index of the parameter we are setting
	 * @param declaredParam the parameter as it is declared including type
	 * @param inValue the value to set
	 * @throws SQLException if thrown by PreparedStatement methods
	 */
	public static void setParameterValue(
	    PreparedStatement ps, int paramIndex, SqlParameter declaredParam, Object inValue)
	    throws SQLException {
		setParameterValue(ps, paramIndex, declaredParam.getSqlType(), declaredParam.getTypeName(), inValue);
	}

	/**
	 * Set the value for a parameter. The method used is based on the SQL type
	 * of the parameter and we can handle complex types like arrays and LOBs.
	 * @param ps the prepared statement or callable statement
	 * @param paramIndex index of the parameter we are setting
	 * @param sqlType the SQL type of the parameter
	 * @param typeName the type name of the parameter
	 * @param inValue the value to set
	 * @throws SQLException if thrown by PreparedStatement methods
	 */
	public static void setParameterValue(
	    PreparedStatement ps, int paramIndex, int sqlType, String typeName, Object inValue)
	    throws SQLException {

		if (logger.isDebugEnabled()) {
			logger.debug("Setting SQL statement parameter value; columnIndex=" + paramIndex +
					", parameter value='" + inValue +
					"', valueClass=" + (inValue != null ? inValue.getClass().getName() : "null") +
					", sqlType=" + (sqlType == SqlTypeValue.TYPE_UNKNOWN ? "unknown" : Integer.toString(sqlType)));
		}

		if (inValue == null) {
			if (sqlType == SqlTypeValue.TYPE_UNKNOWN) {
				// possible alternative: ps.setNull(paramIndex, Types.NULL);
				ps.setObject(paramIndex, null);
			}
			else if (typeName != null) {
				ps.setNull(paramIndex, sqlType, typeName);
			}
			else {
				ps.setNull(paramIndex, sqlType);
			}
		}

		else {  // inValue != null
			if (inValue instanceof SqlTypeValue) {
				((SqlTypeValue) inValue).setTypeValue(ps, paramIndex, sqlType, typeName);
			}
			else if (sqlType == Types.VARCHAR) {
				ps.setString(paramIndex, inValue.toString());
			}
			else if (sqlType == Types.DATE) {
				if (inValue instanceof java.util.Date) {
					if (inValue instanceof java.sql.Date) {
						ps.setDate(paramIndex, (java.sql.Date) inValue);
					}
					else {
						ps.setDate(paramIndex, new java.sql.Date(((java.util.Date) inValue).getTime()));
					}
				}
				else if (inValue instanceof java.util.Calendar) {
					java.util.Calendar cal = (java.util.Calendar) inValue;
					ps.setDate(paramIndex, new java.sql.Date(cal.getTime().getTime()), cal);
				}
				else {
					ps.setObject(paramIndex, inValue, Types.DATE);
				}
			}
			else if (sqlType == Types.TIME) {
				if (inValue instanceof java.util.Date) {
					if (inValue instanceof java.sql.Time) {
						ps.setTime(paramIndex, (java.sql.Time) inValue);
					}
					else {
						ps.setTime(paramIndex, new java.sql.Time(((java.util.Date) inValue).getTime()));
					}
				}
				else if (inValue instanceof java.util.Calendar) {
					java.util.Calendar cal = (java.util.Calendar) inValue;
					ps.setTime(paramIndex, new java.sql.Time(cal.getTime().getTime()), cal);
				}
				else {
					ps.setObject(paramIndex, inValue, Types.TIME);
				}
			}
			else if (sqlType == Types.TIMESTAMP) {
				if (inValue instanceof java.util.Date) {
					if (inValue instanceof java.sql.Timestamp) {
						ps.setTimestamp(paramIndex, (java.sql.Timestamp) inValue);
					}
					else {
						ps.setTimestamp(paramIndex, new java.sql.Timestamp(((java.util.Date) inValue).getTime()));
					}
				}
				else if (inValue instanceof java.util.Calendar) {
					java.util.Calendar cal = (java.util.Calendar) inValue;
					ps.setTimestamp(paramIndex, new java.sql.Timestamp(cal.getTime().getTime()), cal);
				}
				else {
					ps.setObject(paramIndex, inValue, Types.TIMESTAMP);
				}
			}
			else if (sqlType == SqlTypeValue.TYPE_UNKNOWN) {
				if ((inValue instanceof java.util.Date) && !(inValue instanceof java.sql.Date ||
						inValue instanceof java.sql.Time || inValue instanceof java.sql.Timestamp)) {
					ps.setObject(paramIndex, new java.sql.Timestamp(((java.util.Date) inValue).getTime()));
				}
				else if (inValue instanceof java.util.Calendar) {
					ps.setObject(paramIndex, new java.sql.Timestamp(((java.util.Calendar) inValue).getTime().getTime()));
				}
				else {
					ps.setObject(paramIndex, inValue);
				}
			}
			else {
				ps.setObject(paramIndex, inValue, sqlType);
			}
		}
	}

	/**
	 * Clean up all resources held by parameter values which were passed to an
	 * execute method. This is for example important for closing LOB values.
	 * @param paramValues parameter values supplied. May be null.
	 * @see DisposableSqlTypeValue#cleanup
	 * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup
	 */
	public static void cleanupParameters(Object[] paramValues) {
		if (paramValues != null) {
			cleanupParameters(Arrays.asList(paramValues));
		}
	}

	/**
	 * Clean up all resources held by parameter values which were passed to an
	 * execute method. This is for example important for closing LOB values.
	 * @param paramValues parameter values supplied. May be null.
	 * @see DisposableSqlTypeValue#cleanup
	 * @see org.springframework.jdbc.core.support.SqlLobValue#cleanup
	 */
	public static void cleanupParameters(Collection paramValues) {
		if (paramValues != null) {
			for (Iterator it = paramValues.iterator(); it.hasNext();) {
				Object inValue = it.next();
				if (inValue instanceof DisposableSqlTypeValue) {
					((DisposableSqlTypeValue) inValue).cleanup();
				}
			}
		}
	}

}
