/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

/**
 * Subclass of SqlParameter to represent a returned resultset from a stored procedure call.
 * Must declare a RowCallbackHandler to handle any returned rows.
 * No additional properties: instanceof will be used to check
 * for such types.
 * Output parameters--like all stored procedure parameters--
 * must have names.
 * @author Thomas Risberg
 **/
public class SqlReturnResultSet extends SqlParameter {

	/**
	 * Create a new OutputParameter, supplying name and SQL type
	 * @param name name of the parameter, as used in input and
	 * output maps
	 * @param RowCallbackHandler.
	 */
	private RowCallbackHandler rch;

	public SqlReturnResultSet(String name, RowCallbackHandler rch) {
		super(name, 0);
		this.rch = rch;
	}
	
	public RowCallbackHandler getRowCallbackHandler() {
		return rch;
	}
	
}