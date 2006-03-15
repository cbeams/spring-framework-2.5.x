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

package org.springframework.jdbc.support;

import java.util.List;

/**
 * Holds information for parsed SQL statements.
 *
 * @author Thomas Risberg
 * @since 2.0
 */
public class ParsedSql {

	private List parameters;
	private String newSql;
	private String sql;
	private int parameterCount;
	private int namedParameterCount;


	/**
	 * Creates a new instance of the {@link ParsedSql} class.
	 */
	public ParsedSql() {
	}

	/**
	 * Creates a new instance of the {@link ParsedSql} class.
	 * @param sql the SQL statement that is being (or is to be) parsed
	 */
	public ParsedSql(String sql) {
		this.sql = sql;
	}


	/**
	 * Gets all of the parameters (bind variables) to (and for) the parsed SQL statement.
	 * @return the parameters to (and for) the parsed SQL statement
	 */
	public List getParameters() {
		return parameters;
	}

	/**
	 *  Sets the parameters (bind variables) to (and for) the parsed SQL statement.
	 * @param parameters the parameters to (and for) the parsed SQL statement
	 */
	public void setParameters(List parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets the new (parsed) SQL.
	 * @return the new (parsed) SQL
	 */
	public String getNewSql() {
		return newSql;
	}

	/**
	 * Sets the new (parsed) SQL.
	 * @param newSql the new (parsed) SQL
	 */
	public void setNewSql(String newSql) {
		this.newSql = newSql;
	}

	/**
	 * Gets the SQL statement that is being (or is to be) parsed.
	 * @return the SQL statement that is being (or is to be) parsed
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Sets the SQL statement that is being (or is to be) parsed.
	 * @param sql the SQL statement that is being (or is to be) parsed
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * Gets the count of all of the parameters to the SQL statement.
	 * @return the count of all of the parameters to the SQL statement
	 */
	public int getParameterCount() {
		return parameterCount;
	}

	/**
	 * Sets the count of all of the parameters to the SQL statement.
	 * @param parameterCount the count of all of the parameters to the SQL statement
	 */
	public void setParameterCount(int parameterCount) {
		this.parameterCount = parameterCount;
	}

	/**
	 * Gets the count of all of the named parameters to the SQL statement.
	 * @return the count of all of the named parameters to the SQL statement
	 */
	public int getNamedParameterCount() {
		return namedParameterCount;
	}

	/**
	 * Sets the count of all of the named parameters to the SQL statement.
	 * @param namedParameterCount the count of all of the (named) parameters to the SQL statement
	 */
	public void setNamedParameterCount(int namedParameterCount) {
		this.namedParameterCount = namedParameterCount;
	}

}
