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

package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * An abstract class to be used for setting values for more complex Types not supported
 * by the standard setObject method. Implementations extending of this class perform 
 * the actual work of setting the actual values.  They must implement the callback method 
 * setTypeValue which can throw SQLExceptions that will be caught and translated by the 
 * calling code.  Method has access to the connection if that should be needed to create any
 * database specific objects.
 *
 * <p>A usage example from a StoredProcedure:
 *
 * <pre>
 * proc.declareParameter(new SqlParameter("myarray", Types.ARRAY, "NUMBERS"));
 * 
 * ...
 * 
 * Map in = new HashMap();
 * in.put("myarray", new SqlTypeValue(value) {
 *     public void setTypeValue(Connection con,  PreparedStatement ps, int parameterIndex, int sqlType)  throws SQLException {
 *	       oracle.sql.ArrayDescriptor desc = new oracle.sql.ArrayDescriptor("NUMBERS", con);
 *	       oracle.sql.ARRAY nums = new oracle.sql.ARRAY(desc, con, seats);
 *	       ps.setObject(parameterIndex, nums, sqlType);
 *     }
 * });
 * Map out = execute(in);
 * </pre>
 *
 * @author trisberg
 * @since 24.06.2004
 */
public abstract class SqlTypeValue {
	private Object value;
	
	/**
	 * Create a new type value as a JavaBean
	 * @param sql SQL to execute
	 * @param types int array of JDBC types
	 */
	public SqlTypeValue() {
	}

	/**
	 * Create a new type value passing in a value
	 * @param value The value we will set
	 */
	public SqlTypeValue(Object value) {
		this.value = value;
	}
	
	/**
	* @param con JDBC connection. 
	* @param ps PreparedStatement. 
	* @param parameterIndex the index for the column we need to set the value. 
	* @param sqlType SQL Type of the parameter we are setting. 
	* @throws SQLException if a SQLException is encountered setting
	* parameter values (that is, there's no need to catch SQLException)
	*/
	public abstract void setTypeValue(Connection con, PreparedStatement ps, int parameterIndex, int sqlType) throws SQLException;

	/**
	 * @return Returns the value.
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value The value to set.
	 */
	public void setValue(Object value) {
		this.value = value;
	}
}
