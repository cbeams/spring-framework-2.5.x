/*
 * Copyright 2002-2006 the original author or authors.
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

import org.springframework.jdbc.core.SqlNamedParameterHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL parameter values and SQL type wrapper to hold all parameters for a specific SQL statement.
 * This class is intended for passing in a set of parameter values to methods of the
 * NamedParameterJdbcTemplate class.  This class provides methods that will make adding several values
 * to the Map easier.  The addValue method returns a reference to the Map itself so you can chain several
 * method calls together within a single statement.
 *
 * <p>This class can also be used when passing in named parameter values to an SqlCommand object.
 *
 * @author Thomas Risberg
 * @since 2.0
 * @see org.springframework.jdbc.core.NamedParameterJdbcOperations
 * //ToDo: @see org.springframework.jdbc.object.SqlCommand
 */
public class SqlNamedParameterWrapper implements SqlNamedParameterHolder {

	private Map dataValues = new HashMap();

	private Map sqlTypes = new HashMap();


	/**
	 * Create SqlNamedParameterWrapper
	 */
	public SqlNamedParameterWrapper() {
	}

	/**
	 * Create SqlNamedParameterMap
	 * @param dataMap a Map holding existing parameter values
	 */
	public SqlNamedParameterWrapper(Map dataMap) {
		this.dataValues.putAll(dataMap);
	}

	/**
	 * Create SqlNamedParameterMap
	 * @param dataMap a Map holding existing parameter values
	 * @param sqlTypes a Map holding SQL types for the parameters
	 */
	public SqlNamedParameterWrapper(Map dataMap, Map sqlTypes) {
		this.dataValues.putAll(dataMap);
		this.sqlTypes.putAll(sqlTypes);
	}

	/**
	 * Create SqlNamedParameterMap - convenience constructor for single parameter value
	 * @param columnName the name of the first parameter
	 * @param value the first parameter value
	 */
	public SqlNamedParameterWrapper(String columnName, Object value) {
		this.dataValues.put(columnName, value);
	}

	/**
	 * Create SqlNamedParameterMap - convenience constructor for single parameter value
	 * with SQL type specified
	 * @param columnName the name of the first parameter
	 * @param value the first parameter value
	 * @param sqlType the SQL type iof the first parameter
	 */
	public SqlNamedParameterWrapper(String columnName, Object value, int sqlType) {
		this.dataValues.put(columnName, value);
		this.sqlTypes.put(columnName, new Integer(sqlType));
	}

	/**
	 * Add a parameter value and it's associated name to this Wrapper
	 * @param columnName the name of the parameter
	 * @param value the value of the parameter
	 * @return a reference of this Map so it's possible to chain several calls together
	 */
	public SqlNamedParameterWrapper addValue(String columnName, Object value) {
		this.dataValues.put(columnName, value);
		return this;
	}

	/**
	 * Add a parameter value and it's associated name plus the SQL type to this Wrapper
	 * @param columnName the name of the parameter
	 * @param value the value of the parameter
	 * @param sqlType the SQL type of the parameter
	 * @return a reference of this Map so it's possible to chain several calls together
	 */
	public SqlNamedParameterWrapper addValue(String columnName, Object value, int sqlType) {
		this.dataValues.put(columnName, value);
		this.sqlTypes.put(columnName, new Integer(sqlType));
		return this;
	}

	/**
	 * Get the parameter value for the specified named parameter
	 * @param columnName the name of the parameter
	 * @return the value of the specified parameter
	 */
	public Object getValue(String columnName) {
		return this.dataValues.get(columnName);
	}

	/**
	 * Get the parameter value for the specified named parameter
	 * @param columnName the name of the parameter
	 * @return the value of the specified parameter
	 */
	public int getType(String columnName) {
		return ((Integer)this.sqlTypes.get(columnName)).intValue();
	}

	public void setValues(Map valueMap) {
		this.dataValues.putAll(valueMap);
	}

	public void setTypes(Map sqlTypes) {
		this.sqlTypes.putAll(sqlTypes);
	}

	public Map getValues() {
		return this.dataValues;
	}

	public Map getTypes() {
		return this.sqlTypes;
	}

}
