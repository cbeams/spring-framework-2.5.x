/*
 * Created on Jun 14, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.springframework.jdbc.core.support;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * @author trisberg
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface SqlRowSetMetaData {
	// MetaData methods supported
	public abstract String[] getColumnNames()
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */public abstract String getCatalogName(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnClassName(int)
	 */public abstract String getColumnClassName(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 */public abstract int getColumnCount()
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
	 */public abstract int getColumnDisplaySize(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnLabel(int)
	 */public abstract String getColumnLabel(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnName(int)
	 */public abstract String getColumnName(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnType(int)
	 */public abstract int getColumnType(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
	 */public abstract String getColumnTypeName(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getPrecision(int)
	 */public abstract int getPrecision(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getScale(int)
	 */public abstract int getScale(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getSchemaName(int)
	 */public abstract String getSchemaName(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getTableName(int)
	 */public abstract String getTableName(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
	 */public abstract boolean isCaseSensitive(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#isCurrency(int)
	 */public abstract boolean isCurrency(int column)
			throws DataAccessResourceFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#isSigned(int)
	 */public abstract boolean isSigned(int column)
			throws DataAccessResourceFailureException;

}