/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.object;

import java.sql.ResultSet;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * RdbmsOperation using a JdbcTemplate and representing a SQL-based
 * operation such as a query or update, as opposed to a stored procedure.
 *
 * <p>Configures a PreparedStatementCreatorFactory based on the
 * declared parameters.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: SqlOperation.java,v 1.9 2004-03-08 16:56:51 jhoeller Exp $
 */
public abstract class SqlOperation extends RdbmsOperation {

	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	private boolean updatableResults = false;

	/**
	 * Object enabling us to create PreparedStatementCreators
	 * efficiently, based on this class's declared parameters.
	 */
	private PreparedStatementCreatorFactory preparedStatementFactory;


	/**
	 * Set whether to use prepared statements that return a
	 * specific type of ResultSet.
	 * @param resultSetType the ResultSet type
	 * @see java.sql.ResultSet#TYPE_FORWARD_ONLY
	 * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE
	 * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE
	 */
	protected void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}

	/**
	 * Return whether prepared statements will return a specific
	 * type of ResultSet.
	 */
	protected int getResultSetType() {
		return resultSetType;
	}

	/**
	 * Set whether to use prepared statements capable of returning
	 * updatable ResultSets.
	 */
	protected void setUpdatableResults(boolean updatableResults) {
		this.updatableResults = updatableResults;
	}

	/**
	 * Return whether prepared statements will return updatable ResultSets.
	 */
	protected boolean isUpdatableResults() {
		return updatableResults;
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
		catch (IllegalArgumentException ex) {
			// transform JDBC-agnostic error to data access error
			throw new InvalidDataAccessApiUsageException(ex.getMessage());
		}
		if (bindVarCount != getDeclaredParameters().size())
			throw new InvalidDataAccessApiUsageException("SQL '" + getSql() + "' requires " + bindVarCount +
			                                             " bind variables, but " + getDeclaredParameters().size() +
																									 " variables were declared for this object");

		this.preparedStatementFactory = new PreparedStatementCreatorFactory(getSql(), getDeclaredParameters());
		this.preparedStatementFactory.setResultSetType(this.resultSetType);
		this.preparedStatementFactory.setUpdatableResults(this.updatableResults);
		onCompileInternal();
	}

	/**
	 * Hook method that subclasses may override to react to compilation.
	 * This implementation does nothing.
	 */
	protected void onCompileInternal() {
	}

	/**
	 * Return a PreparedStatementCreator to perform an operation
	 * with this parameters.
	 * @param params parameters. May be null.
	 */
	protected PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
		return this.preparedStatementFactory.newPreparedStatementCreator(params);
	}

}
