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

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanWrapperImpl;

/**
 * SQL values wrapper to hold the column values for a row or parameter
 * values in a plain JavaBean where the properties correspond to the
 * column/parameter names used in the SQL.
 *
 * @author Thomas Risberg
 * @since 2.0
 */
public class SqlParameterBeanWrapper extends BeanWrapperImpl implements SqlNamedParameterHolder {

	private Map sqlTypes = new HashMap();


	public SqlParameterBeanWrapper() {
	}

	public SqlParameterBeanWrapper(Object object) {
		super(object);
	}


	public void setTypes(Map sqlTypes) {
		this.sqlTypes.putAll(sqlTypes);
	}

	public Object getValue(String columnName) {
		return this.getPropertyValue(columnName);
	}

	public int getType(String columnName) {
		if (sqlTypes.containsKey(columnName)) {
			return ((Integer) sqlTypes.get(columnName)).intValue();
		}
		return 0;
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

	public void setSqlTypes(SqlNamedParameterTypes sqlTypesHolder) {
		this.sqlTypes.putAll(sqlTypesHolder.getTypes());
	}

	public void setSqlTypes(Map sqlTypes) {
		this.sqlTypes.putAll(sqlTypes);
	}

	public Map getTypes() {
		return this.sqlTypes;
	}

}
