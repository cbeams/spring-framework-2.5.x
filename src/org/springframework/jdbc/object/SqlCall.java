/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.object;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.CallableStatementCreatorFactory;
import org.springframework.jdbc.core.SQLExceptionTranslator;
import org.springframework.jdbc.core.QueryExecutor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

/**
 * RdbmsOperation using a JdbcTemplate and representing a SQL-based
 * call such as a stored procedure or a stored function.
 *
 * <p>Configures a CallableStatementCreatorFactory based on the declared
 * parameters.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 */
public abstract class SqlCall extends RdbmsOperation {

	/** Lower-level class used to execute SQL */
	private final JdbcTemplate jdbcTemplate = new JdbcTemplate();

	/**
	 * Object enabling us to create CallableStatementCreators
	 * efficiently, based on this class's declared parameters.
	 */
	private CallableStatementCreatorFactory callableStatementFactory;

	/**
	 * Flag used to indicate that this call is for a function and to
	 * use the {? = call get_invoice_count(?)} syntax.
	 */
	private boolean function = false;

	/**
	 * Call string as defined in java.sql.CallableStatement.
	 * String of form {call add_invoice(?, ?, ?)}
	 * or {? = call get_invoice_count(?)} if isFunction is set to true
	 * Updated after each parameter is added.
	 */
	private String callString;

	public final void setDataSource(DataSource dataSource) {
		this.jdbcTemplate.setDataSource(dataSource);
	}

	/**
	 * Set the exception translator used in this class.
	 */
	public final void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
		this.jdbcTemplate.setExceptionTranslator(exceptionTranslator);
	}

	/**
	 * Set a custom QueryExecutor implementation.
	 */
	public final void setQueryExecutor(QueryExecutor queryExecutor) {
		this.jdbcTemplate.setQueryExecutor(queryExecutor);
	}

	/**
	 * Set whether or not we want to ignore SQLWarnings.
	 * Default is true.
	 */
	public final void setIgnoreWarnings(boolean ignoreWarnings) {
		this.jdbcTemplate.setIgnoreWarnings(ignoreWarnings);
	}

	/**
	 * Return the JdbcTemplate object used by this object.
	 */
	protected final JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * Set the flag used to indicate that this call is for a function
	 * @param function true or false
	 */
	public void setFunction(boolean function) {
		this.function = function;
	}

	/**
	 * Get the flag used to indicate that this call is for a function.
	 * @return boolean
	 */
	public boolean isFunction() {
		return function;
	}

	/**
	 * Get the flag used to indicate that this call is for a function.
	 * @return boolean
	 */
	public String getCallString() {
		return this.callString;
	}

	/**
	 * Return a CallableStatementCreator to perform an operation
	 * with this parameters.
	 * @param params parameters. May be null.
	 */
	protected final CallableStatementCreator newCallableStatementCreator(Map inParams) {
		return this.callableStatementFactory.newCallableStatementCreator(inParams);
	}

	/**
	 * Overridden method to configure the CallableStatementCreatorFactory
	 * based on our declared parameters.
	 * @see RdbmsOperation#compileInternal()
	 */
	protected final void compileInternal() {
		try {
			this.jdbcTemplate.afterPropertiesSet();
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidDataAccessApiUsageException(ex.getMessage());
		}

		List parameters = getDeclaredParameters();
		int firstParameter = 0;
		if (isFunction()) {
			callString = "{? = call " + getSql() + "(";
			firstParameter = 1;
		}
		else {
			callString = "{call " + getSql() + "(";
		}
		for (int i = firstParameter; i < parameters.size(); i++) {
			SqlParameter p = (SqlParameter) parameters.get(i);
			if ((p instanceof SqlReturnResultSet)) {
				firstParameter++;
			}
			else {
				if (i > firstParameter)
					callString += ", ";
				callString += "?";
			}
		}
		callString += ")}";

		logger.info("Compiled stored procedure. Call string is [" + getCallString() + "]");

		this.callableStatementFactory = new CallableStatementCreatorFactory(getCallString(), getDeclaredParameters());

		onCompileInternal();
	}

	/**
	 * Hook method that subclasses may override to react to compilation.
	 * This implementation does nothing.
	 */
	protected void onCompileInternal() {
	}

}
