package org.springframework.jdbc.core;

import javax.sql.DataSource;

/**
 * Base class for JdbcTemplate and other JDBC-accessing DAO helpers,
 * defining common properties like exception translator.
 *
 * <p>Not intended to be used directly. See JdbcTemplate.
 *
 * @author Juergen Hoeller
 * @since 28.11.2003
 * @see JdbcTemplate
 */
public class JdbcAccessor {

	/**
	 * Used to obtain connections throughout the lifecycle of this object.
	 * This enables this class to close connections if necessary.
	 **/
	protected DataSource dataSource;

	/** Helper to translate SQL exceptions to DataAccessExceptions */
	private SQLExceptionTranslator exceptionTranslator;

	/**
	 * Set the JDBC DataSource to obtain connections from.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Return the DataSource used by this template.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Set the exception translator for this instance.
	 * If no custom translator is provided, a default is used
	 * which examines the SQLException's vendor-specific error code.
	 * @param exceptionTranslator exception translator
	 * @see org.springframework.jdbc.core.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.core.SQLStateSQLExceptionTranslator
	 *
	 */
	public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
		this.exceptionTranslator = exceptionTranslator;
	}

	/**
	 * Return the exception translator for this instance.
	 * Creates a default one for the specified DataSource if none set.
	 */
	public synchronized SQLExceptionTranslator getExceptionTranslator() {
		if (this.exceptionTranslator == null) {
			this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
		}
		return this.exceptionTranslator;
	}

	/**
	 * Eagerly initialize the exception translator,
	 * creating a default one for the specified DataSource if none set.
	 */
	public void afterPropertiesSet() {
		if (this.dataSource == null) {
			throw new IllegalArgumentException("dataSource is required");
		}
		getExceptionTranslator();
	}
	
}
