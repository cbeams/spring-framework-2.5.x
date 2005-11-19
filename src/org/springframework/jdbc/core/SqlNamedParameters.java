package org.springframework.jdbc.core;

import java.util.Map;

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

/**
 * SQL values wrapper to hold the column values for a row.
 *
 * <p>Intended to wrap various implementatations like a Map or a JavaBean with a
 * consistent interface.
 *
 * @author Thomas Risberg
 * @since 1.3
 */
public interface SqlNamedParameters {

	public void setValue(String columnName, Object value);

	public void setValue(String columnName, Object value, int sqlType);

	public void setType(String columnName, int sqlType);

	public Object getValue(String columnName);

	public int getType(String columnName);

	public void setValues(Map valueMap);

	public Map getValues();

	public void setTypes(Map typeMap);

	public Map getTypes();

}
