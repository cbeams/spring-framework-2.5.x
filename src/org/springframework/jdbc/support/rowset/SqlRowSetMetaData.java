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
 * @author Thomas Risberg
 * @since 1.2
 * @see SqlRowSet#getMetaData
 * @see java.sql.ResultSetMetaData
 */
public interface SqlRowSetMetaData {

	String[] getColumnNames() throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	String getCatalogName(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getColumnClassName(int)
	 */
	String getColumnClassName(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 */
	int getColumnCount() throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
	 */
	int getColumnDisplaySize(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getColumnLabel(int)
	 */
	String getColumnLabel(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getColumnName(int)
	 */
	String getColumnName(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getColumnType(int)
	 */
	int getColumnType(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
	 */
	String getColumnTypeName(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getPrecision(int)
	 */
	int getPrecision(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getScale(int)
	 */
	int getScale(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getSchemaName(int)
	 */
	String getSchemaName(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#getTableName(int)
	 */
	String getTableName(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
	 */
	boolean isCaseSensitive(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#isCurrency(int)
	 */
	boolean isCurrency(int column) throws DataRetrievalFailureException;

	/**
	 * @see java.sql.ResultSetMetaData#isSigned(int)
	 */
	boolean isSigned(int column) throws DataRetrievalFailureException;

}
