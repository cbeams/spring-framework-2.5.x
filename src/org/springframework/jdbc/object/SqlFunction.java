/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.jdbc.object;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * SQL "function" wrapper for a query that returns a single row of results. 
 * The default behavior is to return an int, but that can be overridden by 
 * using the methods with an extra return type parameter.
 *
 * <p>Intended to use to call SQL functions that return a single result using a
 * query like "select user()" or "select sysdate from dual". It is not intended
 * for calling more complex stored functions or for using a CallableStatement to
 * invoke a stored procedure or stored function.  Use StoredProcedure or SqlCall 
 * for this type of processing.
 *
 * <p>This is a concrete class, which there is normally no need to subclass.
 * Code using this package can create an object of this type, declaring SQL
 * and parameters, and then invoke the appropriate run method repeatedly to
 * execute the function.
 *
 * <p>Like all RdbmsOperation objects, SqlFunction objects are threadsafe.
 *
 * @author Rod Johnson
 * @author Isabelle Muszynski
 * @author Jean-Pierre Pawlak
 * @see org.springframework.jdbc.object.StoredProcedure
 */

public class SqlFunction extends MappingSqlQuery {

	/** The SQL return type of the function */
	private int retType;

	/**
	 * Constructor to allow use as a JavaBean.
	 * A DataSource, SQL and any parameters must be supplied
	 * before invoking the compile() method and using this object.
	 */
	public SqlFunction() {
	}

	/**
	 * Create a new SQLFunction object with SQL and parameters.
	 * @param ds DataSource to obtain connections from
	 * @param sql SQL to execute
	 * @param types SQL types of the parameters, as defined
	 * in the java.sql.Types class
	 */
	public SqlFunction(DataSource ds, String sql, int[] types) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
		this.retType = Types.INTEGER;
		setRowsExpected(1);
	}

	/**
	 * Create a new SQLFunction object with SQL, parameters and a
	 * return type
	 * @param ds DataSource to obtain connections from
	 * @param sql SQL to execute
	 * @param types SQL types of the parameters, as defined
	 * in the java.sql.Types class
	 * @param retType SQL type of the return value, as defined
	 * in the java.sql.Types class
	 * @exception InvalidDataAccessApiUsageException is thrown if the return
	 * type is not numeric or char
	 */
	public SqlFunction(DataSource ds, String sql, int[] types, int retType)
	    throws InvalidDataAccessApiUsageException {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
		this.retType = JdbcUtils.translateType(retType);
		setRowsExpected(1);
	}

	/**
	 * Create a new SQLFunction object with SQL, but without parameters.
	 * Must add parameters or settle with none.
	 * @param ds DataSource to obtain connections from
	 * @param sql SQL to execute
	 */
	public SqlFunction(DataSource ds, String sql) {
		setDataSource(ds);
		setSql(sql);
		this.retType = Types.INTEGER;
		setRowsExpected(1);
	}

	/**
	 * Create a new SQLFunction object with SQL and return type, but without parameters.
	 * Must add parameters or settle with none.
	 * @param ds DataSource to obtain connections from
	 * @param sql SQL to execute
	 * @param retType SQL type of the return value, as defined
	 * in the java.sql.Types class
	 */
	public SqlFunction(DataSource ds, String sql, int retType) {
		setDataSource(ds);
		setSql(sql);
		this.retType = JdbcUtils.translateType(retType);
		setRowsExpected(1);
	}

	/**
	 * This implementation of this method extracts a single value from the
	 * single row returned by the function. If there are a different number
	 * of rows returned, this is treated as an error.
	 */
	protected Object mapRow(ResultSet rs, int rowNum) throws SQLException, InvalidDataAccessApiUsageException {
		if (rowNum != 0) {
			throw new InvalidDataAccessApiUsageException("SQL function '" + getSql() + "' can't return more than one row");
		}
		Object obj = null;
		switch (this.retType) {
			case Types.INTEGER:
				obj = new Integer(rs.getInt(1));
				break;
			case Types.NUMERIC:
				obj = new Double(rs.getDouble(1));
				break;
			case Types.VARCHAR:
				obj = new String(rs.getString(1));
				break;
			case Types.BIGINT:
				obj = new Long(rs.getLong(1));
				break;
			default:
				obj = rs.getObject(1);
				break;
		}
		return obj;
	}

	/**
	 * Convenient method to run the function without arguments.
	 * @return the value of the function
	 */
	public int run() {
		Integer i = (Integer) super.findObject((Object[]) null);
		return i.intValue();
	}

	/**
	 * Convenient method to run the function with a single int argument.
	 * @param p single int argument
	 * @return the value of the function
	 */
	public int run(int p) {
		Integer i = (Integer) super.findObject(p);
		return i.intValue();
	}

	/**
	 * Analogous to the SqlQuery.execute([]) method. This is a
	 * generic method to execute a query, taken a number of arguments.
	 * @param args array of arguments. These will be objects or
	 * object wrapper types for primitives.
	 * @return the value of the function
	 */
	public int run(Object[] args) {
		Integer i = (Integer) super.findObject(args);
		return i.intValue();
	}

	/**
	 * Convenient method to run the function without arguments,
	 * returning the value as an object
	 * @return the value of the function
	 */
	public Object runGeneric() {
		return super.findObject((Object[]) null);
	}

	/**
	 * Convenient method to run the function with a single int argument.
	 * @param p single int argument
	 * @return the value of the function as an Object
	 */
	public Object runGeneric(int p) {
		return super.findObject(p);
	}

	/**
	 * Analogous to the SqlQuery.execute([]) method. This is a
	 * generic method to execute a query, taken a number of arguments.
	 * @param args array of arguments. These will be objects or
	 * object wrapper types for primitives.
	 * @return the value of the function, as an Object
	 */
	public Object runGeneric(Object[] args) {
		return super.findObject(args);
	}

}
