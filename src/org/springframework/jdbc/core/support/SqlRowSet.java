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

package org.springframework.jdbc.core.support;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DataAccessResourceFailureException;

import javax.sql.RowSet;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * Interface to be implemented for support of RowSets.  A mirror implementation
 * for the javax.sql.RowSet minus the SQLException thrown from all methods.  This
 * will allow a RowSet to be used without having to deal with the SQLException.
 * It will instead throw Spring's DataAccessExceptions when appropriate.
 *
 * @author Thomas Risberg
 * @since 1.2
 */
public interface SqlRowSet {
	
	// Configuration methods exposed to calling classes
	
	/**
	 * Sets the RowSet that this instance will wrap.
	 * @param rowSet the RowSet
	 * @throws DataAccessResourceFailureException if there is any problem setting the RowSet
	 * @see #getRowSet()
	 */
	public void setRowSet(RowSet rowSet) throws DataAccessResourceFailureException;

	/**
	 * Retrieves the wrapped RowSet instance.
	 * @return the wrapped RowSet
	 * @see #setRowSet(javax.sql.RowSet)
	 */
	public abstract RowSet getRowSet();

	/**
	 * Sets the SQL command that was used to create this RowSet.
	 * @param command the SQL command
	 * @see #getCommand()
	 */
	public void setCommand(String command);

	/**
	 * Retrieves the SQL command that was used to create this RowSet.
	 * @return the SQL command
	 * @see #setCommand(String)
	 */
	public String getCommand();

	
	// MetaData methods supported
	
	/**
	 * Retrieves the meta data - number, types and properties for the columns
	 * of this RowSet
	 * @return wrapper class for RowSetMetaData instance
	 * @see java.sql.ResultSet#getMetaData()
	 */
	public SqlRowSetMetaData getMetaData();


	// ResultSet/RowSet methods supported to retrieve data

	/**
	 * Maps the given column name to its column index.
	 * @param columnName the name of the column
	 * @return the column index for the given column name
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#findColumn(String)
	 */
	public abstract int findColumn(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an Array object.
	 * @param columnIndex the column index
	 * @return an Array object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getArray(int)
	 */
	public abstract Array getArray(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an Array object.
	 * @param columnName the column name
	 * @return an Array object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getArray(java.lang.String)
	 */
	public abstract Array getArray(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an BigDecimal object.
	 * @param columnIndex the column index
	 * @return an BigDecimal object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	public abstract BigDecimal getBigDecimal(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an BigDecimal object.
	 * @param columnName the column name
	 * @return an BigDecimal object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */
	public abstract BigDecimal getBigDecimal(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a boolean.
	 * @param columnIndex the column index
	 * @return a boolean representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	public abstract boolean getBoolean(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a boolean.
	 * @param columnName the column name
	 * @return a boolean representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */
	public abstract boolean getBoolean(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a byte.
	 * @param columnIndex the column index
	 * @return a byte representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getByte(int)
	 */
	public abstract byte getByte(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a byte.
	 * @param columnName the column name
	 * @return a byte representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */
	public abstract byte getByte(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Date object.
	 * @param columnIndex the column index
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Date object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */
	public abstract Date getDate(int columnIndex, Calendar cal)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Date object.
	 * @param columnIndex the column index
	 * @return a Date object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getDate(int)
	 */
	public abstract Date getDate(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Date object.
	 * @param columnName the column name
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Date object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	public abstract Date getDate(String columnName, Calendar cal)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Date object.
	 * @param columnName the column name
	 * @return a Date object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */
	public abstract Date getDate(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Double object.
	 * @param columnIndex the column index
	 * @return a Double object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	public abstract double getDouble(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Double object.
	 * @param columnName the column name
	 * @return a Double object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getDouble(java.lang.String)
	 */
	public abstract double getDouble(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a float.
	 * @param columnIndex the column index
	 * @return a float representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	public abstract float getFloat(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a float.
	 * @param columnName the column name
	 * @return a float representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getFloat(java.lang.String)
	 */
	public abstract float getFloat(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an int.
	 * @param columnIndex the column index
	 * @return an int representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getInt(int)
	 */
	public abstract int getInt(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an int.
	 * @param columnName the column name
	 * @return an int representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getInt(java.lang.String)
	 */
	public abstract int getInt(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a long.
	 * @param columnIndex the column index
	 * @return a long representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getLong(int)
	 */
	public abstract long getLong(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a long.
	 * @param columnName the column name
	 * @return a long representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getLong(java.lang.String)
	 */
	public abstract long getLong(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an Object.
	 * @param columnIndex the column index
	 * @param map a Map object containing the mapping from SQL types to Java types
	 * @return a Object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	public abstract Object getObject(int columnIndex, Map map)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an Object.
	 * @param columnIndex the column index
	 * @return a Object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getObject(int)
	 */
	public abstract Object getObject(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an Object.
	 * @param columnName the column name
	 * @param map a Map object containing the mapping from SQL types to Java types
	 * @return a Object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	public abstract Object getObject(String columnName, Map map)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an Object.
	 * @param columnName the column name
	 * @return a Object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */
	public abstract Object getObject(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the current row number.
	 * @return the current row number
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getRow()
	 */
	public abstract int getRow() throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a short.
	 * @param columnIndex the column index
	 * @return a short representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getShort(int)
	 */
	public abstract short getShort(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a short.
	 * @param columnName the column name
	 * @return a short representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */
	public abstract short getShort(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a String.
	 * @param columnIndex the column index
	 * @return a String representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getString(int)
	 */
	public abstract String getString(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a String.
	 * @param columnName the column name
	 * @return a String representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */
	public abstract String getString(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Time object.
	 * @param columnIndex the column index
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Time object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	public abstract Time getTime(int columnIndex, Calendar cal)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Time object.
	 * @param columnIndex the column index
	 * @return a Time object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getTime(int)
	 */
	public abstract Time getTime(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Time object.
	 * @param columnName the column name
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Time object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	public abstract Time getTime(String columnName, Calendar cal)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Time object.
	 * @param columnName the column name
	 * @return a Time object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	public abstract Time getTime(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Timestamp object.
	 * @param columnIndex the column index
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Timestamp object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	public abstract Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Timestamp object.
	 * @param columnIndex the column index
	 * @return a Timestamp object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	public abstract Timestamp getTimestamp(int columnIndex)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Timestamp object.
	 * @param columnName the column name
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Timestamp object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	public abstract Timestamp getTimestamp(String columnName, Calendar cal)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Timestamp object.
	 * @param columnName the column name
	 * @return a Timestamp object representing the column value
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	public abstract Timestamp getTimestamp(String columnName)
			throws DataRetrievalFailureException;

	/**
	 * Retrieves the type of the wrapped RowSet Object.
	 * @return the RowSet type
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#getType()
	 */
	public abstract int getType() throws DataRetrievalFailureException;

	/**
	 * Retrieves whether the wrappeed RowSet is read-only.
	 * @return true if the wrpped RowSet is read-only, false if it is updatable
	 * @throws DataRetrievalFailureException
	 * @see javax.sql.RowSet#isReadOnly()
	 */
	 public abstract boolean isReadOnly();


	// ResultSet/RowSet navigation

	/**
	 * Moves the cursor to the given row number in the RowSet, just after the last row.
	 * @param row the number of the row where the cursor should move
	 * @return true if the cursor is on the RowSet, false otherwise
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#absolute(int)
	 */
	public abstract boolean absolute(int row)
			throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the end of this RowSet.
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#afterLast()
	 */
	public abstract void afterLast() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the front of this RowSet, just before the first row.
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	public abstract void beforeFirst() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the first row of this RowSet.
	 * @throws DataRetrievalFailureException
	 * @return true if the cursor is on a valid row, false otherwise
	 * @see java.sql.ResultSet#first()
	 */
	public abstract boolean first() throws DataRetrievalFailureException;

	/**
	 * Retrieves whether the cursor is after the last row of this RowSet.
	 * @throws DataRetrievalFailureException
	 * @return true if the cursor is after the last row, false otherwise
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	public abstract boolean isAfterLast()
			throws DataRetrievalFailureException;

	/**
	 * Retrieves whether the cursor is after the first row of this RowSet.
	 * @throws DataRetrievalFailureException
	 * @return true if the cursor is after the first row, false otherwise
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	public abstract boolean isBeforeFirst()
			throws DataRetrievalFailureException;

	/**
	 * Retrieves whether the cursor is on the first row of this RowSet.
	 * @throws DataRetrievalFailureException
	 * @return true if the cursor is after the first row, false otherwise
	 * @see java.sql.ResultSet#isFirst()
	 */
	public abstract boolean isFirst() throws DataRetrievalFailureException;

	/**
	 * Retrieves whether the cursor is on the last row of this RowSet.
	 * @throws DataRetrievalFailureException
	 * @return true if the cursor is after the last row, false otherwise
	 * @see java.sql.ResultSet#isLast()
	 */
	public abstract boolean isLast() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the last row of this RowSet.
	 * @throws DataRetrievalFailureException
	 * @return true if the cursor is on a valid row, false otherwise
	 * @see java.sql.ResultSet#last()
	 */
	public abstract boolean last() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the current row.  This method has no effect unless
	 * the cursor is on the insert row.
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	public abstract void moveToCurrentRow()
			throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the next row.
	 * @return true if the new row is valid, false if there are no more rows
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#next()
	 */
	public abstract boolean next() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the previous row.
	 * @return true if the new row is valid, false if it is off the RowSet
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#previous()
	 */
	public abstract boolean previous() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor a relative number f rows, either positive or negative.
	 * @return true if the cursor is on a row, false otherwise
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#relative(int)
	 */
	public abstract boolean relative(int rows)
			throws DataRetrievalFailureException;

	/**
	 * Reports whether the last column read had a value of SQL <code>NULL</code>.  Note that
	 * you must first call one of the getter methods and then call the <code>wasNull</code> method.
	 * @return true if the most recent coumn retrieved was SQL <code>NULL</code>, false otherwise
	 * @throws DataRetrievalFailureException
	 * @see java.sql.ResultSet#wasNull()
	 */
	public abstract boolean wasNull() throws DataRetrievalFailureException;
}