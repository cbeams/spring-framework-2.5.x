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

/**
 * Subclass of SqlOutParameter to represent a returned ResultSet
 * from a stored procedure call.
 *
 * <p>Must declare a ResultSetExtractor, RowCallbackHandler or RowMapper
 * to handle any returned rows. No additional properties: instanceof will
 * be used to check for such types.
 *
 * <p>Returned ResultSets - like all stored procedure parameters -
 * must have names.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 */
public class SqlReturnResultSet extends ResultSetSupportingSqlParameter {

	/**
	 * Create a new SqlReturnResultSet.
	 * @param name name of the parameter, as used in input and output maps
	 * @param rse ResultSetExtractor to use for parsing the ResultSet
	 */
	public SqlReturnResultSet(String name, ResultSetExtractor rse) {
		super(name, 0, rse);
	}

	/**
	 * Create a new SqlReturnResultSet.
	 * @param name name of the parameter, as used in input and output maps
	 * @param rch RowCallbackHandler to use for parsing the ResultSet
	 */
	public SqlReturnResultSet(String name, RowCallbackHandler rch) {
		super(name, 0, rch);
	}

	/**
	 * Create a new SqlReturnResultSet.
	 * @param name name of the parameter, as used in input and output maps
	 * @param rm RowMapper to use for parsing the ResultSet
	 */
	public SqlReturnResultSet(String name, RowMapper rm) {
		super(name, 0, rm);
	}

	/**
	 * Create a new SqlReturnResultSet.
	 * @param name name of the parameter, as used in input and output maps
	 * @param rm RowMapper to use for parsing the ResultSet
	 * @param rowsExpected number of expected rows
	 */
	public SqlReturnResultSet(String name, RowMapper rm, int rowsExpected) {
		super(name, 0, rm, rowsExpected);
	}

}
