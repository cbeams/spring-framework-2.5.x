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

package org.springframework.jdbc.support.rowset;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * Meta data interface for Spring's SqlRowSet,
 * analogous to <code>javax.sql.ResultSetMetaData</code>
 *
 * <p>The main difference to the standard JDBC RowSetMetaData is that an SQLException
 * is never thrown here. This allows an SqlRowSetMetaData to be used without having to deal
 * with checked exceptions. An SqlRowSetMetaData will throw Spring's
 * <code>org.springframework.dao.DataRetrievalFailureException</code> instead
 * (when appropriate).
 * @author Thomas Risberg
 * @since 1.2
 * @see SqlRowSet#getMetaData
 * @see java.sql.ResultSetMetaData
 */
public interface SqlRowSetMetaData {

	String[] getColumnNames() throws DataRetrievalFailureException;

	/**
	 * Retrieves the catalog name of the table that served as the source for the specified column.
	 * @param columnIndex
	 * @return the catalog name
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	String getCatalogName(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the fully qualified class that the specified column will be mapped to.
	 * @param columnIndex
	 * @return the class name as a String
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getColumnClassName(int)
	 */
	String getColumnClassName(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrives the number of columns in the RowSet.
	 * @return the number of coulmns
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 */
	int getColumnCount() throws DataRetrievalFailureException;

	/**
	 * Retrieves the maximum width of the designated column.
	 * @param columnIndex
	 * @return the width of the column
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
	 */
	int getColumnDisplaySize(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieve the suggested column title for the column specified.
	 * @param columnIndex
	 * @return the column title
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getColumnLabel(int)
	 */
	String getColumnLabel(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieve the column name for the indicated column.
	 * @param columnIndex
	 * @return the column name
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getColumnName(int)
	 */
	String getColumnName(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieve the SQL type code for the indicated column.
	 * @param columnIndex
	 * @return the SQL type code
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getColumnType(int)
	 * @see java.sql.Types
	 */
	int getColumnType(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the DBMS-specific type name for the indicated column.
	 * @param columnIndex
	 * @return the type name
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
	 */
	String getColumnTypeName(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the precision for the indicated column.
	 * @param columnIndex	 * @see java.sql.Types

	 * @return the precision
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getPrecision(int)
	 */
	int getPrecision(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the scale of the indicated column.
	 * @param columnIndex
	 * @return the scale
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getScale(int)
	 */
	int getScale(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the schema name of the table that served as the source for the specified column.
	 * @param columnIndex
	 * @return the schema name
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getSchemaName(int)
	 */
	String getSchemaName(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the name of the table that served as the source for the specified column.
	 * @param columnIndex
	 * @return the name of the table
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#getTableName(int)
	 */
	String getTableName(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Indicates whether the case of the designated column is significant.
	 * @param columnIndex
	 * @return true if the case sensitive, false otherwise
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
	 */
	boolean isCaseSensitive(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Indicates whether the designated column contains a currency value.
	 * @param columnIndex
	 * @return true if the value is a currency value, false otherwise
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#isCurrency(int)
	 */
	boolean isCurrency(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Indicates whether the designated column contains a signed number.
	 * @param columnIndex
	 * @return true if the column contains a signed number, false otherwise
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSetMetaData#isSigned(int)
	 */
	boolean isSigned(int columnIndex) throws DataRetrievalFailureException;

}
