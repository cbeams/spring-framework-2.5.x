package org.springframework.jdbc.core;

import java.sql.Types;

/**
 * Represents a returned {@link java.sql.ResultSet} from a stored procedure call.
 *
 * <p>A {@link org.springframework.jdbc.core.ResultSetExtractor}, {@link org.springframework.jdbc.core.RowCallbackHandler} or {@link org.springframework.jdbc.core.RowMapper}
 * must be provided to handle any returned rows.
 *
 * <p>Returned {@link java.sql.ResultSet ResultSets} - like all stored procedure
 * parameters - <b>must</b> have names.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 */
public class SqlReturnUpdateCount extends SqlParameter {

	/**
	 * Create a new instance of the {@link SqlReturnUpdateCount} class.
	 * @param name name of the parameter, as used in input and output maps
	 */
	public SqlReturnUpdateCount(String name) {
		super(name, Types.INTEGER);
	}


	/**
	 * Return whether this parameter holds input values that should be set
	 * before execution even if they are <code>null</code>.
	 * <p>This implementation always returns <code>false</code>.
	 */
	public boolean isInputValueProvided() {
		return false;
	}

	/**
	 * Return whether this parameter is an implicit return parameter used during the
	 * reults preocessing of the CallableStatement.getMoreResults/getUpdateCount.
	 * <p>This implementation always returns <code>true</code>.
	 */
	public boolean isResultsParameter() {
		return true;
	}
}
