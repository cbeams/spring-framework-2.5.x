/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.object;

import javax.sql.DataSource;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.SQLExceptionTranslator;
import org.springframework.jdbc.core.QueryExecutor;
import org.springframework.jdbc.util.JdbcUtils;

/**
 * RdbmsOperation using a JdbcTemplate and representing a SQL-based
 * operation such as a query or update, as opposed to a stored procedure.
 *
 * <p>Configures a PreparedStatementCreatorFactory based on the declared
 * parameters.
 *
 * @author Rod Johnson
 * @version $Id: SqlOperation.java,v 1.2 2003-08-26 17:31:16 jhoeller Exp $
 */
public abstract class SqlOperation extends RdbmsOperation {

	/** Lower-level class used to execute SQL */
	private final JdbcTemplate jdbcTemplate = new JdbcTemplate();

	/**
	 * Object enabling us to create PreparedStatementCreators
	 * efficiently, based on this class's declared parameters.
	 */
	private PreparedStatementCreatorFactory preparedStatementFactory;

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
	 * Return a PreparedStatementCreator to perform an operation
	 * with this parameters.
	 * @param params parameters. May be null.
	 */
	protected final PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
		return this.preparedStatementFactory.newPreparedStatementCreator(params);
	}

	/**
	 * Overridden method to configure the PreparedStatementCreatorFactory
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

		// Validate parameter count
		int bindVarCount = 0;
		try {
			bindVarCount = JdbcUtils.countParameterPlaceholders(getSql(), '?', '\'');
		}
		catch (IllegalArgumentException e) {
			// Transform jdbc-agnostic error to data-access error
			throw new InvalidDataAccessApiUsageException(e.getMessage());
		}
		if (bindVarCount != getDeclaredParameters().size())
			throw new InvalidDataAccessApiUsageException("SQL '" + getSql() + "' requires " + bindVarCount +
			                                             " bind variables, but " + getDeclaredParameters().size() + " variables were declared for this object");

		this.preparedStatementFactory = new PreparedStatementCreatorFactory(getSql(), getDeclaredParameters());
		onCompileInternal();
	}

	/**
	 * Hook method that subclasses may override to react to compilation.
	 * This implementation does nothing.
	 */
	protected void onCompileInternal() {
	}

}
