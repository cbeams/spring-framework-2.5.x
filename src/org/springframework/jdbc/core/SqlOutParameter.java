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
	private boolean resultSetSupported;
	private RowCallbackHandler rch;

	public SqlOutParameter(String name, int type) {
		super(name, type);
		resultSetSupported = false;
	}

	public SqlOutParameter(String name, int type, String typeName) {
		super(name, type, typeName);
		resultSetSupported = false;
	}

	public SqlOutParameter(String name, int type, RowCallbackHandler rch) {
		super(name, type);
		this.rch = rch;
		resultSetSupported = true;
	}
	
	public boolean isResultSetSupported() {
		return resultSetSupported;
	}
	
	public RowCallbackHandler getRowCallbackHandler() {
		return rch;
	}
	
}