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
 * Subclass of SqlParameter to represent a returned resultset from a
 * stored procedure call.
 *
 * <p>Must declare a RowCallbackHandler to handle any returned rows.
 * No additional properties: instanceof will be used to check
 * for such types.
 *
 * <p>Output parameters - like all stored procedure parameters -
 * must have names.
 *
 * @author Thomas Risberg
 */
public class SqlReturnResultSet extends SqlParameter {

	private RowCallbackHandler rowCallbackHandler;

	private RowMapper rowMapper = null;

	private int rowsExpected = 0;

	private boolean rowMapperSupported = false;

	public SqlReturnResultSet(String name, RowCallbackHandler rch) {
		super(name, 0);
		this.rowCallbackHandler = rch;
	}

	public SqlReturnResultSet(String name, RowMapper rm, int rowsExpected) {
		super(name, 0);
		this.rowMapper = rm;
		this.rowsExpected = rowsExpected;
		this.rowMapperSupported = true;
	}
	
	public SqlReturnResultSet(String name, RowMapper rm) {
		this(name, rm, 0);
	}
		
	public RowCallbackHandler getRowCallbackHandler() {
		return rowCallbackHandler;
	}

	public boolean isRowMapperSupported() {
		return rowMapperSupported;
	}
	
	/**
	 * Return new instance of the implementation of a ResultReader usable for returned resultsets. This implementation 
	 * invokes the RowMapper's implementation of the mapRow() method.
	 */
	protected final ResultReader newResultReader() {
		return new ResultReaderStoredProcImpl(rowsExpected, rowMapper);
	}

}
