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

package org.springframework.jdbc.core;

import org.springframework.beans.BeanWrapperImpl;

import java.util.HashMap;
import java.util.Map;
import java.beans.PropertyDescriptor;

/**
 * SQL values wrapper to hold the column values for a row or parameter
 * values in a plain JavaBean where the
 * properties correspond to the column/parameter names used in the SQL.
 *
 * @author Thomas Risberg
 * @since 1.3
 */
public class SqlParameterBeanWrapper extends BeanWrapperImpl implements SqlNamedParameters {
	private Map typeMap = new HashMap();

	public void setValue(String columnName, Object value) {
		this.setPropertyValue(columnName, value);
	}

	public void setValue(String columnName, Object value, int sqlType) {
		this.setPropertyValue(columnName, value);
		this.typeMap.put(columnName, new Integer(sqlType));
	}

	public void setType(String columnName, int sqlType) {
		this.typeMap.put(columnName, new Integer(sqlType));
	}

	public Object getValue(String columnName) {
		return this.getPropertyValue(columnName);
	}

	public int getType(String columnName) {
		Integer sqlType = (Integer)typeMap.get(columnName);
		if (sqlType == null)
			return 0;
		else
			return sqlType.intValue();
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
			values.put(propName, propValue);
		}
		return values;
	}

	public void setTypes(Map typeMap) {
		this.typeMap.putAll(typeMap);
	}

	public Map getTypes() {
		return this.typeMap;
	}

}
