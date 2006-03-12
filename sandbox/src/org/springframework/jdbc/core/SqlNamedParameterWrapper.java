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
 * SQL values wrapper to hold the column values for a row in a HashMap.
 * 
 * @author Thomas Risberg
 * @since 2.0
 */
public class SqlNamedParameterWrapper implements SqlNamedParameterHolder {

	private Map dataValues = new HashMap();

	private Map sqlTypes = new HashMap();


	public SqlNamedParameterWrapper() {
	}

	public SqlNamedParameterWrapper(Map dataMap) {
		this.dataValues.putAll(dataMap);
	}

	public SqlNamedParameterWrapper(Map dataMap, Map sqlTypes) {
		this.dataValues.putAll(dataMap);
		this.sqlTypes.putAll(sqlTypes);
	}

	public SqlNamedParameterWrapper(String columnName, Object value) {
		this.dataValues.put(columnName, value);
	}

	public SqlNamedParameterWrapper(String columnName, Object value, int sqlType) {
		this.dataValues.put(columnName, value);
		this.sqlTypes.put(columnName, new Integer(sqlType));
	}

	public SqlNamedParameterWrapper addValue(String columnName, Object value) {
		this.dataValues.put(columnName, value);
		return this;
	}

	public SqlNamedParameterWrapper addValue(String columnName, Object value, int sqlType) {
		this.dataValues.put(columnName, value);
		this.sqlTypes.put(columnName, new Integer(sqlType));
		return this;
	}


	public Object getValue(String columnName) {
		return this.dataValues.get(columnName);
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
