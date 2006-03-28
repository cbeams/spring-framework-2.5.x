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

import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * BeanWrapper to hold a bean containing parameter values and SQL types representing all parameters for
 * a specific SQL statement.  This class is intended for passing in a set of parameter values to methods
 * of the NamedParameterJdbcTemplate class.  This class provides methods that will make adding several types
 * to the Map easier.  The addType method returns a reference to the Map itself so you can chain several
 * method calls together within a single statement.
 *
 * <p>This class can also be used when passing in named parameter values to an SqlCommand object.
 *
 * @author Thomas Risberg
 * @since 2.0
 * @see org.springframework.jdbc.core.NamedParameterJdbcOperations
 * //ToDo: @see org.springframework.jdbc.object.SqlCommand
 */
public class SqlParameterBeanWrapper extends BeanWrapperImpl implements SqlNamedParameterHolder {

	private Map sqlTypes = new HashMap();


	/**
	 * Create SqlParameterBeanWrapper
	 */
	public SqlParameterBeanWrapper() {
	}

	/**
	 * Create SqlParameterBeanWrapper
	 * @param object the wrapped bean instance
	 */
	public SqlParameterBeanWrapper(Object object) {
		super(object);
	}


	/**
	 * Get the parameter value for the specified property/parameter
	 * @param columnName the name of the parameter
	 * @return the value of the specified parameter
	 */
	public Object getValue(String columnName) {
		return this.getPropertyValue(columnName);
	}

	/**
	 * Get the parameter value for the specified property/parameter
	 * @param columnName the name of the parameter
	 * @return the value of the specified parameter
	 */
	public int getType(String columnName) {
		if (sqlTypes.containsKey(columnName)) {
			return ((Integer) sqlTypes.get(columnName)).intValue();
		}
		return 0;
	}

	/**
	 * Add a property's/parameter's SQL type to this Wrapper
	 * @param columnName the name of the parameter
	 * @param sqlType the SQL type of the parameter
	 * @return a reference of this Map so it's possible to chain several calls together
	 */
	public SqlParameterBeanWrapper addType(String columnName, int sqlType) {
		sqlTypes.put(columnName, new Integer(sqlType));
		return this;
	}

	public void setValues(Map valueMap) {
		this.setPropertyValues(valueMap);
	}

	public Map getValues() {
		PropertyDescriptor[] propDescriptors = this.getPropertyDescriptors();
		Map values = new HashMap(propDescriptors.length);
		for (int i = 0; i < propDescriptors.length; i++) {
			String propName = propDescriptors[i].getName();
			Object propValue = this.getPropertyValue(propName);
			if (!"class".equals(propName)) {
				values.put(propName, propValue);
			}
		}
		return values;
	}

	public void setTypes(Map sqlTypes) {
		this.sqlTypes.putAll(sqlTypes);
	}

	public Map getTypes() {
		return this.sqlTypes;
	}

}
