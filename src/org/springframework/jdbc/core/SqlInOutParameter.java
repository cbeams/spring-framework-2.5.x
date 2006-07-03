package org.springframework.jdbc.core;

/**
 * Subclass of SqlOutParameter to represent an INOUT parameter.
 * Will always return true for "isInputValueProvided" test.
 *
 * <p>Output parameters - like all stored procedure parameters -
 * must have names.
 *
 * @author Thomas Risberg
 */
public class SqlInOutParameter extends SqlOutParameter {

	private SqlReturnType sqlReturnType;


	/**
	 * Create a new SqlInOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 */
	public SqlInOutParameter(String name, int sqlType) {
		super(name, sqlType);
	}

	/**
	 * Create a new SqlInOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param typeName the type name of the parameter (optional)
	 */
	public SqlInOutParameter(String name, int sqlType, String typeName) {
		super(name, sqlType, typeName);
	}

	/**
	 * Create a new SqlInOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param typeName the type name of the parameter (optional)
	 * @param sqlReturnType custom value handler for complex type (optional)
	 */
	public SqlInOutParameter(String name, int sqlType, String typeName, SqlReturnType sqlReturnType) {
		super(name, sqlType, typeName);
		this.sqlReturnType = sqlReturnType;
	}

	/**
	 * Create a new SqlInOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rse ResultSetExtractor to use for parsing the ResultSet
	 */
	public SqlInOutParameter(String name, int sqlType, ResultSetExtractor rse) {
		super(name, sqlType, rse);
	}

	/**
	 * Create a new SqlInOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rch RowCallbackHandler to use for parsing the ResultSet
	 */
	public SqlInOutParameter(String name, int sqlType, RowCallbackHandler rch) {
		super(name, sqlType, rch);
	}

	/**
	 * Create a new SqlInOutParameter.
	 * @param name name of the parameter, as used in input and output maps
	 * @param sqlType SQL type of the parameter according to java.sql.Types
	 * @param rm RowMapper to use for parsing the ResultSet
	 */
	public SqlInOutParameter(String name, int sqlType, RowMapper rm) {
		super(name, sqlType, rm);
	}


	/**
	 * Return whether this parameter holds a custom return type.
	 */
	public boolean isReturnTypeSupported() {
		return (this.sqlReturnType != null);
	}

	/**
	 * Return the custom return type, if any.
	 */
	public SqlReturnType getSqlReturnType() {
		return sqlReturnType;
	}

	/**
	 * Return whether this parameter holds input values that should be set
	 * before execution even if they are null.
	 */
	public boolean isInputValueProvided() {
		return true;
	}

}
