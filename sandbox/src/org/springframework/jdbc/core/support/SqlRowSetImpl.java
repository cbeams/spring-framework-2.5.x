package org.springframework.jdbc.core.support;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.sql.RowSetMetaData;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * @author trisberg
 */
public class SqlRowSetImpl implements SqlRowSet {
	private RowSet rowSet;
	private SqlRowSetMetaData rowSetMetaData;
	private DataSource dataSource;
	private String sql;

	public SqlRowSetImpl() {
	}

	public SqlRowSetImpl(RowSet rowSet) throws DataAccessResourceFailureException {
		this.setRowSet(rowSet);
	}

	
	// Configuration methods exposed to calling classes

	/* Set the underlying RowSet 
	 */
	public void setRowSet(RowSet rowSet) {
		this.rowSet = rowSet;
		try {
			rowSetMetaData = new SqlRowSetMetaDataImpl((RowSetMetaData)rowSet.getMetaData());
		}
		catch (SQLException se) {
			throw new DataAccessResourceFailureException(se.getMessage(), se);
		}
	}
	
	/* Return the underlying RowSet 
	 */
	public RowSet getRowSet() {
		return this.rowSet;
	}

	/* Set the underlying SQL 
	 */
	public void setCommand(String command) {
		this.sql = command;
	}

	/* Return the underlying SQL 
	 */
	public String getCommand() {
		return sql;
	}

		
	// MetaData methods supported
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	public SqlRowSetMetaData getMetaData() {
		return this.rowSetMetaData;
	}
	
	// ResultSet/RowSet methods supported to retrieve data
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#findColumn(java.lang.String)
	 */
	public int findColumn(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.findColumn(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getArray(int)
	 */
	public Array getArray(int i) throws DataRetrievalFailureException {
		try {
			return rowSet.getArray(i);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getArray(java.lang.String)
	 */
	public Array getArray(String colName) throws DataRetrievalFailureException {
		try {
			return rowSet.getArray(colName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getBigDecimal(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */
	public BigDecimal getBigDecimal(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getBigDecimal(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	public boolean getBoolean(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getBoolean(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getBoolean(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getByte(int)
	 */
	public byte getByte(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getByte(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */
	public byte getByte(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getByte(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */
	public Date getDate(int columnIndex, Calendar cal) throws DataRetrievalFailureException {
		try {
			return rowSet.getDate(columnIndex, cal);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(int)
	 */
	public Date getDate(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getDate(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	public Date getDate(String columnName, Calendar cal) throws DataRetrievalFailureException {
		try {
			return rowSet.getDate(columnName, cal);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */
	public Date getDate(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getDate(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	public double getDouble(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getDouble(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getDouble(java.lang.String)
	 */
	public double getDouble(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getDouble(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	public float getFloat(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getFloat(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getFloat(java.lang.String)
	 */
	public float getFloat(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getFloat(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getInt(int)
	 */
	public int getInt(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getInt(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getInt(java.lang.String)
	 */
	public int getInt(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getInt(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getLong(int)
	 */
	public long getLong(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getLong(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getLong(java.lang.String)
	 */
	public long getLong(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getLong(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	public Object getObject(int i, Map map) throws DataRetrievalFailureException {
		try {
			return rowSet.getObject(i, map);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(int)
	 */
	public Object getObject(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getObject(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	public Object getObject(String colName, Map map) throws DataRetrievalFailureException {
		try {
			return rowSet.getObject(colName, map);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */
	public Object getObject(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getObject(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRow(int)
	 */
	public int getRow() throws DataRetrievalFailureException {
		try {
			return rowSet.getRow();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getShort(int)
	 */
	public short getShort(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getShort(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */
	public short getShort(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getShort(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getString(int)
	 */
	public String getString(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getString(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */
	public String getString(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getString(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	public Time getTime(int columnIndex, Calendar cal) throws DataRetrievalFailureException {
		try {
			return rowSet.getTime(columnIndex, cal);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(int)
	 */
	public Time getTime(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getTime(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	public Time getTime(String columnName, Calendar cal) throws DataRetrievalFailureException {
		try {
			return rowSet.getTime(columnName, cal);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	public Time getTime(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getTime(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws DataRetrievalFailureException {
		try {
			return rowSet.getTimestamp(columnIndex, cal);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int columnIndex) throws DataRetrievalFailureException {
		try {
			return rowSet.getTimestamp(columnIndex);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	public Timestamp getTimestamp(String columnName, Calendar cal)
			throws DataRetrievalFailureException {
		try {
			return rowSet.getTimestamp(columnName, cal);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	public Timestamp getTimestamp(String columnName) throws DataRetrievalFailureException {
		try {
			return rowSet.getTimestamp(columnName);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getType()
	 */
	public int getType() throws DataRetrievalFailureException {
		try {
			return rowSet.getType();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isReadOnly()
	 */
	public boolean isReadOnly() {
	 	return rowSet.isReadOnly();
	 }

	 // ResultSet/RowSet navigation
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#absolute(int)
	 */
	public boolean absolute(int row) throws DataRetrievalFailureException {
		try {
			return rowSet.absolute(row);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#afterLast()
	 */
	public void afterLast() throws DataRetrievalFailureException {
		try {
			rowSet.afterLast();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	public void beforeFirst() throws DataRetrievalFailureException {
		try {
			rowSet.beforeFirst();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#first()
	 */
	public boolean first() throws DataRetrievalFailureException {
		try {
			return rowSet.first();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	public boolean isAfterLast() throws DataRetrievalFailureException {
		try {
			return rowSet.isAfterLast();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	public boolean isBeforeFirst() throws DataRetrievalFailureException {
		try {
			return rowSet.isBeforeFirst();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isFirst()
	 */
	public boolean isFirst() throws DataRetrievalFailureException {
		try {
			return rowSet.isFirst();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isLast()
	 */
	public boolean isLast() throws DataRetrievalFailureException {
		try {
			return rowSet.isLast();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#last()
	 */
	public boolean last() throws DataRetrievalFailureException {
		try {
			return rowSet.last();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	public void moveToCurrentRow() throws DataRetrievalFailureException {
		try {
			rowSet.moveToCurrentRow();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#next()
	 */
	public boolean next() throws DataRetrievalFailureException {
		try {
			return rowSet.next();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#previous()
	 */
	public boolean previous() throws DataRetrievalFailureException {
		try {
			return rowSet.previous();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#relative(int)
	 */
	public boolean relative(int rows) throws DataRetrievalFailureException {
		try {
			return rowSet.relative(rows);
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see java.sql.ResultSet#wasNull()
	 */
	public boolean wasNull() throws DataRetrievalFailureException {
		try {
			return rowSet.wasNull();
		}
		catch (SQLException se) {
			throw new DataRetrievalFailureException(se.getMessage());
		}
	}

}
