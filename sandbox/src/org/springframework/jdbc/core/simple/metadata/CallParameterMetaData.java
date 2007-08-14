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
 * @author trisberg
 */
public class CallParameterMetaData {
	private String parameterName;
	private int parameterType;
	private int sqlType;
	private String typeName;
	private boolean nullable;


	public CallParameterMetaData(String columnName, int columnType, int sqlType, String typeName, boolean nullable) {
		this.parameterName = columnName;
		this.parameterType = columnType;
		this.sqlType = sqlType;
		this.typeName = typeName;
		this.nullable = nullable;
	}


	public String getParameterName() {
		return parameterName;
	}

	public int getParameterType() {
		return parameterType;
	}

	public int getSqlType() {
		return sqlType;
	}

	public String getTypeName() {
		return typeName;
	}

	public boolean isNullable() {
		return nullable;
	}
}