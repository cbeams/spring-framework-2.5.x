/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.object;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * RdbmsOperation using a JdbcTemplate and representing a SQL-based
 * operation such as a query or update, as opposed to a stored procedure.
 *
 * <p>Configures a PreparedStatementCreatorFactory based on the declared
 * parameters.
 *
 * @author Rod Johnson
 * @version $Id: SqlOperation.java,v 1.8 2004-01-02 22:01:31 jhoeller Exp $
 */
public abstract class SqlOperation extends RdbmsOperation {

	private boolean updatableResults;

	/**
	 * Object enabling us to create PreparedStatementCreators
	 * efficiently, based on this class's declared parameters.
	 */
	private PreparedStatementCreatorFactory preparedStatementFactory;

	/**
	 * Set whether to create PreparedStatementCreators that return prepared
	 * statements capable of returning updatable result sets.
	 */
	protected void setUpdatableResults(boolean updatableResults) {
		this.updatableResults = updatableResults;
	}

	/**
	 * Return whether created PreparedStatementCreators will return prepared
	 * statements capable of returning updatable result sets.
	 */
	protected boolean isUpdatableResults() {
		return updatableResults;
	}

	/**
	 * Return a PreparedStatementCreator to perform an operation
	 * with this parameters.
	 * @param params parameters. May be null.
	 */
	protected PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
		return this.preparedStatementFactory.newPreparedStatementCreator(params);
	}

	/**
	 * Overridden method to configure the PreparedStatementCreatorFactory
	 * based on our declared parameters.
	 * @see RdbmsOperation#compileInternal()
	 */
	protected final void compileInternal() {
		// validate parameter count
		int bindVarCount = 0;
		try {
			bindVarCount = JdbcUtils.countParameterPlaceholders(getSql(), '?', '\'');
		}
		catch (IllegalArgumentException e) {
			// transform JDBC-agnostic error to data access error
			throw new InvalidDataAccessApiUsageException(e.getMessage());
		}
		if (bindVarCount != getDeclaredParameters().size())
			throw new InvalidDataAccessApiUsageException("SQL '" + getSql() + "' requires " + bindVarCount +
			                                             " bind variables, but " + getDeclaredParameters().size() +
																									 " variables were declared for this object");

		this.preparedStatementFactory = new PreparedStatementCreatorFactory(getSql(), getDeclaredParameters(),
																																				this.updatableResults);
		onCompileInternal();
	}

	/**
	 * Hook method that subclasses may override to react to compilation.
	 * This implementation does nothing.
	 */
	protected void onCompileInternal() {
	}

}
