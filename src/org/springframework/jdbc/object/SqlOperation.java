/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.object;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.util.JdbcUtils;

/**
 * RdbmsOperation using a JdbcTemplate and representing a SQL-based
 * operation such as a query or update, as opposed to a stored procedure.
 *
 * <p>Configures a PreparedStatementCreatorFactory based on the declared
 * parameters.
 *
 * @author Rod Johnson
 * @version $Id: SqlOperation.java,v 1.1.1.1 2003-08-14 16:20:33 trisberg Exp $
 */
public abstract class SqlOperation extends RdbmsOperation {

	/** Lower-level class used to execute SQL */
	private JdbcTemplate jdbcTemplate;

	/**
	 * Object enabling us to create PreparedStatementCreators
	 * efficiently, based on this class's declared parameters
	 */
	private PreparedStatementCreatorFactory preparedStatementFactory;

	/**
	 * Create a new SqlOperation.
	 */
	public SqlOperation() {
	}

	/**
	 * Return the JdbcTemplate object used by this object
	 */
	protected final JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * Return a PreparedStatementCreator to perform an operation
	 * with this parameters
	 * @param params parameters. May be null.
	 */
	protected final PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
		return this.preparedStatementFactory.newPreparedStatementCreator(params);
	}

	/**
	 * Overriden method to configure the PreparedStatementCreatorFactory
	 * based on our declared parameters.
	 * @see RdbmsOperation#compileInternal()
	 */
	protected final void compileInternal() {
		this.jdbcTemplate = new JdbcTemplate(getDataSource());
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
