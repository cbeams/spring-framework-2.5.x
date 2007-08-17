/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jdbc.core.simple.metadata;

/**
 * Holder of metadata for a specific parameter that is used for table processing.
 *
 * @author trisberg
 * @since 2.1
 */
public class TableParameterMetaData {
	private String parameterName;
	private int sqlType;
	private boolean generated;
	private boolean nullable;


	/**
	 * Constructor taking all the properties
	 */
	public TableParameterMetaData(String columnName, int sqlType, boolean generated, boolean nullable) {
		this.parameterName = columnName;
		this.sqlType = sqlType;
		this.nullable = nullable;
	}


	/**
	 * Get the parameter name.
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * Get the parameter SQL type.
	 */
	public int getSqlType() {
		return sqlType;
	}

	/**
	 * Get the parameter/column is auto generated.
	 */
	public boolean isGenerated() {
		return generated;
	}

	/**
	 * Get wheter the parameter/column is nullable.
	 */
	public boolean isNullable() {
		return nullable;
	}
}
