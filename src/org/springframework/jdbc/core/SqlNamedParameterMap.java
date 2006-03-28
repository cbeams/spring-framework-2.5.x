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

import org.apache.commons.collections.map.LinkedMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Map implementation to hold parameter values for named parameters without specifying the SQL Type.
 * This class is intended for passing in a set of parameter values to methods of the
 * NamedParameterJdbcTemplate class.  This clas provides methods that will make adding several values
 * to the Map easier.  The addValue method returns a reference to the Map itself so you can string several
 * method calls together within a single statement.
 *
 * <p>This class can also be used when passing in named parameter values to an SqlCommand object.
 *
 * @author Thomas Risberg
 * @since 2.0
 * @see org.springframework.jdbc.core.NamedParameterJdbcOperations
 * //ToDo: @see org.springframework.jdbc.object.SqlCommand
 */
public class SqlNamedParameterMap extends HashMap {

	/**
	 * Create SqlNamedParameterMap
	 */
	public SqlNamedParameterMap() {
	}

	/**
	 * Create SqlNamedParameterMap
	 * @param dataMap a Map holding existing parameter values
	 */
	public SqlNamedParameterMap(Map dataMap) {
		this.putAll(dataMap);
	}

	/**
	 * Create SqlNamedParameterMap
	 * @param columnName the name of the first parameter
	 * @param value the value of the first parameter
	 */
	public SqlNamedParameterMap(String columnName, Object value) {
		this.put(columnName, value);
	}

	/**
	 * Add a parameter value and it's associated name to this Map
	 * @param columnName the name of the parameter
	 * @param value the value of the parameter
	 * @return a reference of this Map so it's possible to chain several calls together
	 */
	public SqlNamedParameterMap addValue(String columnName, Object value) {
		this.put(columnName, value);
		return this;
	}

	/**
	 * Get the parameter value specified for a named parameter
	 * @param columnName the name of the parameter
	 * @return the value of the requested parameter
	 */
	public Object getValue(String columnName) {
		return this.get(columnName);
	}

	/**
	 * Add a group of named parameter values to this Map
	 * @param valueMap the Map holding the parameter names and values
	 */
	public void setValues(Map valueMap) {
		this.putAll(valueMap);
	}

}
