package org.springframework.jdbc.core.support;

import java.sql.SQLException;

import javax.sql.RowSetMetaData;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * @author trisberg
 */
public class SqlRowSetMetaDataImpl implements SqlRowSetMetaData {
	private RowSetMetaData rowSetMetaData;
	private String[] columnNames;

	public SqlRowSetMetaDataImpl(RowSetMetaData rowSetMetaData) {
		this.rowSetMetaData = rowSetMetaData;
	}
	
	// MetaData methods supported
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	public String[] getColumnNames() throws DataAccessResourceFailureException {
		if (columnNames == null) {
			columnNames = new String[getColumnCount()];
			for (int i = 0; i < getColumnCount(); i++) {
				columnNames[i] = getColumnName(i+1);
			}
		}
		return this.columnNames;
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	public String getCatalogName(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getCatalogName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnClassName(int)
	 */
	public String getColumnClassName(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getColumnClassName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 */
	public int getColumnCount() throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getColumnCount();
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
	 */
	public int getColumnDisplaySize(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getColumnDisplaySize(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnLabel(int)
	 */
	public String getColumnLabel(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getColumnLabel(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnName(int)
	 */
	public String getColumnName(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getColumnName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnType(int)
	 */
	public int getColumnType(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getColumnType(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
	 */
	public String getColumnTypeName(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getColumnTypeName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getPrecision(int)
	 */
	public int getPrecision(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getPrecision(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getScale(int)
	 */
	public int getScale(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getScale(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getSchemaName(int)
	 */
	public String getSchemaName(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getSchemaName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getTableName(int)
	 */
	public String getTableName(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.getTableName(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
	 */
	public boolean isCaseSensitive(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.isCaseSensitive(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#isCurrency(int)
	 */
	public boolean isCurrency(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.isCurrency(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#isSigned(int)
	 */
	public boolean isSigned(int column) throws DataAccessResourceFailureException {
		try {
			return rowSetMetaData.isSigned(column);
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	
}
