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
 * Common base class for ResultSet-supporting SqlParameters like
 * SqlOutParameter and SqlReturnResultSet.
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 * @see SqlOutParameter
 * @see SqlReturnResultSet
 */
public class ResultSetSupportingSqlParameter extends SqlParameter {

	private ResultSetExtractor resultSetExtractor;

	private RowCallbackHandler rowCallbackHandler;

	private RowMapper rowMapper;

	private int rowsExpected = 0;


	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType) {
		super(name, sqlType);
	}

	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param typeName the type name of the parameter (optional)
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType, String typeName) {
		super(name, sqlType, typeName);
	}

	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rse ResultSetExtractor to use for parsing the ResultSet
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType, ResultSetExtractor rse) {
		super(name, sqlType);
		this.resultSetExtractor = rse;
	}

	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rch RowCallbackHandler to use for parsing the ResultSet
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType, RowCallbackHandler rch) {
		super(name, sqlType);
		this.rowCallbackHandler = rch;
	}

	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rm RowMapper to use for parsing the ResultSet
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType, RowMapper rm) {
		super(name, sqlType);
		this.rowMapper = rm;
	}

	/**
	 * Create a new ResultSetSupportingSqlParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rm RowMapper to use for parsing the ResultSet
	 * @param rowsExpected number of expected rows
	 */
	public ResultSetSupportingSqlParameter(String name, int sqlType, RowMapper rm, int rowsExpected) {
		super(name, sqlType);
		this.rowMapper = rm;
		this.rowsExpected = rowsExpected;
	}


	/**
	 * Does this parameter support a ResultSet, i.e. does it hold a
	 * ResultSetExtractor, RowCallbackHandler or RowMapper?
	 */
	public boolean isResultSetSupported() {
		return (this.resultSetExtractor != null || this.rowCallbackHandler != null || this.rowMapper != null);
	}

	/**
	 * Does this parameter support a RowCallbackHandler,
	 * i.e. does it hold a RowCallbackHandler or RowMapper?
	 */
	public boolean isRowCallbackHandlerSupported() {
		return (this.rowCallbackHandler != null || this.rowMapper != null);
	}

	/**
	 * Return the ResultSetExtractor held by this parameter, if any.
	 */
	public ResultSetExtractor getResultSetExtractor() {
		return resultSetExtractor;
	}

	/**
	 * Return a new instance of the implementation of a RowCallbackHandler,
	 * usable for returned ResultSets. This implementation invokes a given
	 * RowMapper via the RowMapperResultReader adapter, of returns a given
	 * RowCallbackHandler directly.
	 * @see RowMapperResultReader
	 */
	public RowCallbackHandler getRowCallbackHandler() {
		if (this.rowMapper != null) {
			return new RowMapperResultReader(this.rowMapper, this.rowsExpected);
		}
		else {
			return this.rowCallbackHandler;
		}
	}

}
