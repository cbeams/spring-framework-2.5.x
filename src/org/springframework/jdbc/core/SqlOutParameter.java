/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

/**
 * Subclass of SqlParameter to represent an output parameter.
 * No additional properties: instanceof will be used to check
 * for such types.
 * Output parameters--like all stored procedure parameters--
 * must have names.
 * @author Rod Johnson
 * @author Thomas Risberg
 **/
public class SqlOutParameter extends SqlParameter {

	/**
	 * Create a new OutputParameter, supplying name and SQL type
	 * @param name name of the parameter, as used in input and
	 * output maps
	 * @param type SQL type of the parameter, as defined
	 * in a constant in the java.sql.Types class.
	 */
	private boolean resultSetSupported = false;
	private RowCallbackHandler rowCallbackHandler = null;
	private RowMapper rowMapper = null;
	private boolean rowMapperSupported = false;
	private int rowsExpected = 0;

	public SqlOutParameter(String name, int type) {
		super(name, type);
	}

	public SqlOutParameter(String name, int type, String typeName) {
		super(name, type, typeName);
	}

	public SqlOutParameter(String name, int type, RowCallbackHandler rch) {
		super(name, type);
		this.rowCallbackHandler = rch;
		resultSetSupported = true;
	}

	public SqlOutParameter(String name, int type, RowMapper rm, int rowsExpected) {
		super(name, type);
		this.rowMapper = rm;
		resultSetSupported = true;
		this.rowsExpected = rowsExpected;
		this.rowMapperSupported = true;
	}

	public SqlOutParameter(String name, int type, RowMapper rm) {
		this(name, type, rm, 0);
	}
	
	public boolean isResultSetSupported() {
		return resultSetSupported;
	}
	
	public boolean isRowMapperSupported() {
		return rowMapperSupported;
	}

	public RowCallbackHandler getRowCallbackHandler() {
		return rowCallbackHandler;
	}

	/**
	 * Return new instance of the implementation of a ResultReader usable for returned resultsets. This implementation 
	 * invokes the RowMapper's implementation of the mapRow() method.
	 */
	protected final ResultReader newResultReader() {
		return new ResultReaderStoredProcImpl(rowsExpected, rowMapper);
	}
	
}