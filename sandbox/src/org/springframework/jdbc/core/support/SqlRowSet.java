/*
 * Created on Jun 14, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.springframework.jdbc.core.support;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import javax.sql.RowSet;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * @author trisberg
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface SqlRowSet {
	
	// Configuration methods exposed to calling classes
	
	public void setRowSet(RowSet rowSet);

	public abstract RowSet getRowSet();

	public void setCommand(String command);

	public String getCommand();

	
	// MetaData methods supported
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	public SqlRowSetMetaData getMetaData();

	// ResultSet/RowSet methods supported to retrieve data
	public abstract int findColumn(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getArray(int)
	 */public abstract Array getArray(int i)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getArray(java.lang.String)
	 */public abstract Array getArray(String colName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */public abstract BigDecimal getBigDecimal(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */public abstract BigDecimal getBigDecimal(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getBoolean(int)
	 */public abstract boolean getBoolean(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */public abstract boolean getBoolean(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getByte(int)
	 */public abstract byte getByte(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */public abstract byte getByte(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */public abstract Date getDate(int columnIndex, Calendar cal)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(int)
	 */public abstract Date getDate(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */public abstract Date getDate(String columnName, Calendar cal)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */public abstract Date getDate(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDouble(int)
	 */public abstract double getDouble(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDouble(java.lang.String)
	 */public abstract double getDouble(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getFloat(int)
	 */public abstract float getFloat(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getFloat(java.lang.String)
	 */public abstract float getFloat(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getInt(int)
	 */public abstract int getInt(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getInt(java.lang.String)
	 */public abstract int getInt(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getLong(int)
	 */public abstract long getLong(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getLong(java.lang.String)
	 */public abstract long getLong(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */public abstract Object getObject(int i, Map map)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(int)
	 */public abstract Object getObject(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */public abstract Object getObject(String colName, Map map)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */public abstract Object getObject(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRow(int)
	 */public abstract int getRow() throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getShort(int)
	 */public abstract short getShort(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */public abstract short getShort(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getString(int)
	 */public abstract String getString(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */public abstract String getString(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */public abstract Time getTime(int columnIndex, Calendar cal)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(int)
	 */public abstract Time getTime(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */public abstract Time getTime(String columnName, Calendar cal)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */public abstract Time getTime(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */public abstract Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */public abstract Timestamp getTimestamp(int columnIndex)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */public abstract Timestamp getTimestamp(String columnName, Calendar cal)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */public abstract Timestamp getTimestamp(String columnName)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getType()
	 */public abstract int getType() throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isReadOnly()
	 */
	 public abstract boolean isReadOnly();

	// ResultSet/RowSet navigation
	public abstract boolean absolute(int row)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#afterLast()
	 */public abstract void afterLast() throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#beforeFirst()
	 */public abstract void beforeFirst() throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#first()
	 */public abstract boolean first() throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isAfterLast()
	 */public abstract boolean isAfterLast()
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */public abstract boolean isBeforeFirst()
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isFirst()
	 */public abstract boolean isFirst() throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isLast()
	 */public abstract boolean isLast() throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#last()
	 */public abstract boolean last() throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */public abstract void moveToCurrentRow()
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#next()
	 */public abstract boolean next() throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#previous()
	 */public abstract boolean previous() throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#relative(int)
	 */public abstract boolean relative(int rows)
			throws DataRetrievalFailureException;

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#wasNull()
	 */public abstract boolean wasNull() throws DataRetrievalFailureException;
}