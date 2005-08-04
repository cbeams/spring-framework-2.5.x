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

package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.NumberUtils;

/**
 * RowMapper implementation that converts a single column into
 * a single result value per row. Expects to work on a ResultSet
 * that just contains a single column.
 *
 * <p>The type of the result value for each row can be specified.
 * The value for the single column will be extracted from the ResultSet
 * and converted into the specified target type.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see JdbcTemplate#queryForList(String, Class)
 * @see JdbcTemplate#queryForObject(String, Class)
 */
public class SingleColumnRowMapper implements RowMapper {

	private Class requiredType;


	/**
	 * Create a new SingleColumnRowMapper.
	 * @see #setRequiredType
	 */
	public SingleColumnRowMapper() {
	}

	/**
	 * Create a new SingleColumnRowMapper.
	 * @param requiredType the type that each result object is expected to match
	 */
	public SingleColumnRowMapper(Class requiredType) {
		this.requiredType = requiredType;
	}

	/**
	 * Set the type that each result object is expected to match.
	 * <p>If not specified, the column value will be exposed as
	 * returned by the JDBC driver.
	 */
	public void setRequiredType(Class requiredType) {
		this.requiredType = requiredType;
	}


	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int nrOfColumns = rsmd.getColumnCount();
		if (nrOfColumns != 1) {
			throw new IncorrectResultSizeDataAccessException(
					"Expected single column but found " + nrOfColumns, 1, nrOfColumns);
		}
		Object result = getColumnValue(rs, 1);
		if (result != null && this.requiredType != null && !this.requiredType.isInstance(result)) {
			if (String.class.equals(this.requiredType)) {
				result = result.toString();
			}
			else if (Number.class.isAssignableFrom(this.requiredType) && Number.class.isInstance(result)) {
				try {
					result = NumberUtils.convertNumberToTargetClass(((Number) result), this.requiredType);
				}
				catch (IllegalArgumentException ex) {
					throw new TypeMismatchDataAccessException(ex.getMessage());
				}
			}
			else {
				throw new TypeMismatchDataAccessException(
						"Result object for row number " + rowNum + " and column type '" + rsmd.getColumnTypeName(1) +
						"' and value [" + result + "] is of type [" + result.getClass().getName() +
						"] and could not be converted to required type [" + this.requiredType.getName() + "]");
			}
		}
		return result;
	}

	/**
	 * Retrieve a JDBC object value for the specified column.
	 * <p>The default implementation uses the <code>getObject</code> method.
	 * Additionally, this implementation includes a "hack" to get around Oracle
	 * returning a non-standard object for their TIMESTAMP datatype.
	 * @param rs is the ResultSet holding the data
	 * @param index is the column index
	 * @return the Object returned
	 * @see org.springframework.jdbc.support.JdbcUtils#getResultSetValue
	 */
	protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index);
	}

}
