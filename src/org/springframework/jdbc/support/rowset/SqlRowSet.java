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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * Mirror interface for <code>javax.sql.RowSet</code>, representing
 * disconnected <code>java.sql.ResultSet</code> data.
 *
 * <p>The main difference to the standard JDBC RowSet is that no SQLException
 * is thrown here. This allows a SqlRowSet to be used without having to deal
 * with checked exceptions. A SqlRowSet will throw Spring's
 * <code>org.springframework.dao.DataRetrievalFailureException</code> instead
 * (when appropriate).
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.2
 * @see javax.sql.RowSet
 * @see java.sql.ResultSet
 * @see org.springframework.jdbc.core.JdbcTemplate#queryForRowSet
 */
public interface SqlRowSet {

	/**
	 * Retrieves the meta data (number, types and properties for the columns)
	 * of this row set.
	 * @return a corresponding SqlRowSetMetaData instance
	 * @see java.sql.ResultSet#getMetaData()
	 */
	SqlRowSetMetaData getMetaData();


	// ResultSet/RowSet methods supported to retrieve data

	/**
	 * Maps the given column name to its column index.
	 * @param columnName the name of the column
	 * @return the column index for the given column name
	 * @see java.sql.ResultSet#findColumn(String)
	 */
	int findColumn(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an BigDecimal object.
	 * @param columnIndex the column index
	 * @return an BigDecimal object representing the column value
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	BigDecimal getBigDecimal(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an BigDecimal object.
	 * @param columnName the column name
	 * @return an BigDecimal object representing the column value
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */
	BigDecimal getBigDecimal(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a boolean.
	 * @param columnIndex the column index
	 * @return a boolean representing the column value
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	boolean getBoolean(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a boolean.
	 * @param columnName the column name
	 * @return a boolean representing the column value
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */
	boolean getBoolean(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a byte.
	 * @param columnIndex the column index
	 * @return a byte representing the column value
	 * @see java.sql.ResultSet#getByte(int)
	 */
	byte getByte(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a byte.
	 * @param columnName the column name
	 * @return a byte representing the column value
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */
	byte getByte(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Date object.
	 * @param columnIndex the column index
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Date object representing the column value
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */
	Date getDate(int columnIndex, Calendar cal) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Date object.
	 * @param columnIndex the column index
	 * @return a Date object representing the column value
	 * @see java.sql.ResultSet#getDate(int)
	 */
	Date getDate(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Date object.
	 * @param columnName the column name
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Date object representing the column value
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	Date getDate(String columnName, Calendar cal) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Date object.
	 * @param columnName the column name
	 * @return a Date object representing the column value
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */
	Date getDate(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Double object.
	 * @param columnIndex the column index
	 * @return a Double object representing the column value
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	double getDouble(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Double object.
	 * @param columnName the column name
	 * @return a Double object representing the column value
	 * @see java.sql.ResultSet#getDouble(java.lang.String)
	 */
	double getDouble(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a float.
	 * @param columnIndex the column index
	 * @return a float representing the column value
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	float getFloat(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a float.
	 * @param columnName the column name
	 * @return a float representing the column value
	 * @see java.sql.ResultSet#getFloat(java.lang.String)
	 */
	float getFloat(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an int.
	 * @param columnIndex the column index
	 * @return an int representing the column value
	 * @see java.sql.ResultSet#getInt(int)
	 */
	int getInt(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an int.
	 * @param columnName the column name
	 * @return an int representing the column value
	 * @see java.sql.ResultSet#getInt(java.lang.String)
	 */
	int getInt(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a long.
	 * @param columnIndex the column index
	 * @return a long representing the column value
	 * @see java.sql.ResultSet#getLong(int)
	 */
	long getLong(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a long.
	 * @param columnName the column name
	 * @return a long representing the column value
	 * @see java.sql.ResultSet#getLong(java.lang.String)
	 */
	long getLong(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an Object.
	 * @param columnIndex the column index
	 * @param map a Map object containing the mapping from SQL types to Java types
	 * @return a Object representing the column value
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	Object getObject(int columnIndex, Map map) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an Object.
	 * @param columnIndex the column index
	 * @return a Object representing the column value
	 * @see java.sql.ResultSet#getObject(int)
	 */
	Object getObject(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an Object.
	 * @param columnName the column name
	 * @param map a Map object containing the mapping from SQL types to Java types
	 * @return a Object representing the column value
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	Object getObject(String columnName, Map map) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * an Object.
	 * @param columnName the column name
	 * @return a Object representing the column value
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */
	Object getObject(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the current row number.
	 * @return the current row number
	 * @see java.sql.ResultSet#getRow()
	 */
	int getRow() throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a short.
	 * @param columnIndex the column index
	 * @return a short representing the column value
	 * @see java.sql.ResultSet#getShort(int)
	 */
	short getShort(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a short.
	 * @param columnName the column name
	 * @return a short representing the column value
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */
	short getShort(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a String.
	 * @param columnIndex the column index
	 * @return a String representing the column value
	 * @see java.sql.ResultSet#getString(int)
	 */
	String getString(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a String.
	 * @param columnName the column name
	 * @return a String representing the column value
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */
	String getString(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Time object.
	 * @param columnIndex the column index
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Time object representing the column value
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	Time getTime(int columnIndex, Calendar cal) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Time object.
	 * @param columnIndex the column index
	 * @return a Time object representing the column value
	 * @see java.sql.ResultSet#getTime(int)
	 */
	Time getTime(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Time object.
	 * @param columnName the column name
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Time object representing the column value
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	Time getTime(String columnName, Calendar cal) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Time object.
	 * @param columnName the column name
	 * @return a Time object representing the column value
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	Time getTime(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Timestamp object.
	 * @param columnIndex the column index
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Timestamp object representing the column value
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	Timestamp getTimestamp(int columnIndex, Calendar cal) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Timestamp object.
	 * @param columnIndex the column index
	 * @return a Timestamp object representing the column value
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	Timestamp getTimestamp(int columnIndex) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Timestamp object.
	 * @param columnName the column name
	 * @param cal the Calendar to use in constructing the Date
	 * @return a Timestamp object representing the column value
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	Timestamp getTimestamp(String columnName, Calendar cal) throws DataRetrievalFailureException;

	/**
	 * Retrieves the value of the indicated column in the current row as
	 * a Timestamp object.
	 * @param columnName the column name
	 * @return a Timestamp object representing the column value
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	Timestamp getTimestamp(String columnName) throws DataRetrievalFailureException;

	/**
	 * Retrieves the type of the wrapped RowSet Object.
	 * @return the RowSet type
	 * @see java.sql.ResultSet#getType()
	 */
	int getType() throws DataRetrievalFailureException;


	// ResultSet/RowSet navigation

	/**
	 * Moves the cursor to the given row number in the RowSet, just after the last row.
	 * @param row the number of the row where the cursor should move
	 * @return true if the cursor is on the RowSet, false otherwise
	 * @see java.sql.ResultSet#absolute(int)
	 */
	boolean absolute(int row) throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the end of this RowSet.
	 * @see java.sql.ResultSet#afterLast()
	 */
	void afterLast() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the front of this RowSet, just before the first row.
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	void beforeFirst() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the first row of this RowSet.
	 * @return true if the cursor is on a valid row, false otherwise
	 * @see java.sql.ResultSet#first()
	 */
	boolean first() throws DataRetrievalFailureException;

	/**
	 * Retrieves whether the cursor is after the last row of this RowSet.
	 * @return true if the cursor is after the last row, false otherwise
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	boolean isAfterLast() throws DataRetrievalFailureException;

	/**
	 * Retrieves whether the cursor is after the first row of this RowSet.
	 * @return true if the cursor is after the first row, false otherwise
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	boolean isBeforeFirst() throws DataRetrievalFailureException;

	/**
	 * Retrieves whether the cursor is on the first row of this RowSet.
	 * @return true if the cursor is after the first row, false otherwise
	 * @see java.sql.ResultSet#isFirst()
	 */
	boolean isFirst() throws DataRetrievalFailureException;

	/**
	 * Retrieves whether the cursor is on the last row of this RowSet.
	 * @return true if the cursor is after the last row, false otherwise
	 * @see java.sql.ResultSet#isLast()
	 */
	boolean isLast() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the last row of this RowSet.
	 * @return true if the cursor is on a valid row, false otherwise
	 * @see java.sql.ResultSet#last()
	 */
	boolean last() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the next row.
	 * @return true if the new row is valid, false if there are no more rows
	 * @see java.sql.ResultSet#next()
	 */
	boolean next() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor to the previous row.
	 * @return true if the new row is valid, false if it is off the RowSet
	 * @see java.sql.ResultSet#previous()
	 */
	boolean previous() throws DataRetrievalFailureException;

	/**
	 * Moves the cursor a relative number f rows, either positive or negative.
	 * @return true if the cursor is on a row, false otherwise
	 * @see java.sql.ResultSet#relative(int)
	 */
	boolean relative(int rows) throws DataRetrievalFailureException;

	/**
	 * Reports whether the last column read had a value of SQL <code>NULL</code>.
	 * Note that you must first call one of the getter methods and then call
	 * the <code>wasNull</code> method.
	 * @return true if the most recent coumn retrieved was SQL <code>NULL</code>,
	 * false otherwise
	 * @see java.sql.ResultSet#wasNull()
	 */
	boolean wasNull() throws DataRetrievalFailureException;

}
