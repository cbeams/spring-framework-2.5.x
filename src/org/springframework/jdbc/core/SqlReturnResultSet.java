/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
