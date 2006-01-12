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

package org.springframework.jdbc.support;

import java.util.List;

/**
 * Class to hold information for any parsed SQL statements.
 *
 * @author Thomas Risberg
 * @since 2.0
 */
public class ParsedSql {
	private List namedParameters;
	private String newSql;
	private String sql;
	private int parameterCount;
	private int namedParameterCount;

	public ParsedSql() {
	}

	public ParsedSql(String sql) {
		this.sql = sql;
	}

	public List getNamedParameters() {
		return namedParameters;
	}

	public void setNamedParameters(List namedParameters) {
		this.namedParameters = namedParameters;
	}

	public String getNewSql() {
		return newSql;
	}

	public void setNewSql(String newSql) {
		this.newSql = newSql;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public int getParameterCount() {
		return parameterCount;
	}

	public void setParameterCount(int parameterCount) {
		this.parameterCount = parameterCount;
	}

	public int getNamedParameterCount() {
		return namedParameterCount;
	}

	public void setNamedParameterCount(int namedParameterCount) {
		this.namedParameterCount = namedParameterCount;
	}

}
