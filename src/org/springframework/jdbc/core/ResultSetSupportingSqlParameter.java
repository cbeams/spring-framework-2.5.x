/*
 * Copyright 2002-2004 the original author or authors.
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
 * @author Juergen Hoeller
 * @since 26.05.2004
 * @see SqlOutParameter
 * @see SqlReturnResultSet
 */
public class ResultSetSupportingSqlParameter extends SqlParameter {

	private ResultSetExtractor resultSetExtractor = null;

	private RowCallbackHandler rowCallbackHandler = null;

	private RowMapper rowMapper = null;

	private int rowsExpected = 0;

	/**
	 * Create a new ResultSetSupportingSqlParameter, supplying name and SQL type.
	 * @param name name of the parameter, as used in input and output maps
	 * @param type SQL type of the parameter according to java.sql.Types
	 */
	public ResultSetSupportingSqlParameter(String name, int type) {
		super(name, type);
	}

	public ResultSetSupportingSqlParameter(String name, int type, String typeName) {
		super(name, type, typeName);
	}

	public ResultSetSupportingSqlParameter(String name, int type, ResultSetExtractor resultSetExtractor) {
		super(name, type);
		this.resultSetExtractor = resultSetExtractor;
	}

	public ResultSetSupportingSqlParameter(String name, int type, RowCallbackHandler rch) {
		super(name, type);
		this.rowCallbackHandler = rch;
	}

	public ResultSetSupportingSqlParameter(String name, int type, RowMapper rm) {
		super(name, type);
		this.rowMapper = rm;
	}

	public ResultSetSupportingSqlParameter(String name, int type, RowMapper rm, int rowsExpected) {
		super(name, type);
		this.rowMapper = rm;
		this.rowsExpected = rowsExpected;
	}

	public boolean isResultSetSupported() {
		return (this.resultSetExtractor != null || this.rowCallbackHandler != null || this.rowMapper != null);
	}

	protected boolean isRowCallbackHandlerSupported() {
		return (this.rowCallbackHandler != null || this.rowMapper != null);
	}

	protected ResultSetExtractor getResultSetExtractor() {
		return resultSetExtractor;
	}

	/**
	 * Return a new instance of the implementation of a RowCallbackHandler,
	 * usable for returned ResultSets. This implementation invokes a given
	 * RowMapper via the RowMapperResultReader adapter, respectively returns
	 * a given RowCallbackHandler directly.
	 * @see RowMapperResultReader
	 */
	protected RowCallbackHandler getRowCallbackHandler() {
		if (this.rowMapper != null) {
			return new RowMapperResultReader(this.rowMapper, this.rowsExpected);
		}
		else {
			return this.rowCallbackHandler;
		}
	}

}
