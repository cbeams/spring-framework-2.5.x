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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Default implementation of Spring's SqlRowSetMetaData interface.
 * Used by SqlRowSetImpl.
 * @author Thomas Risberg
 * @since 1.2
 * @see SqlRowSetImpl#getMetaData
 */
public class SqlRowSetMetaDataImpl implements SqlRowSetMetaData {

	private final ResultSetMetaData resultSetMetaData;

	private String[] columnNames;

	public SqlRowSetMetaDataImpl(ResultSetMetaData resultSetMetaData) {
		this.resultSetMetaData = resultSetMetaData;
	}
	
	public String[] getColumnNames() throws DataAccessResourceFailureException {
		if (this.columnNames == null) {
			this.columnNames = new String[getColumnCount()];
			for (int i = 0; i < getColumnCount(); i++) {
				this.columnNames[i] = getColumnName(i+1);
			}
		}
		return this.columnNames;
	}

	public String getCatalogName(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getCatalogName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	
	public String getColumnClassName(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getColumnClassName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public int getColumnCount() throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getColumnCount();
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public int getColumnDisplaySize(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getColumnDisplaySize(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public String getColumnLabel(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getColumnLabel(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public String getColumnName(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getColumnName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public int getColumnType(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getColumnType(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public String getColumnTypeName(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getColumnTypeName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public int getPrecision(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getPrecision(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public int getScale(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getScale(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public String getSchemaName(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getSchemaName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public String getTableName(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.getTableName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public boolean isCaseSensitive(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.isCaseSensitive(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public boolean isCurrency(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.isCurrency(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	public boolean isSigned(int column) throws DataAccessResourceFailureException {
		try {
			return this.resultSetMetaData.isSigned(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	
}
