/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.object;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.ResultReader;

/**
 * Reusable RDBMS query in which concrete subclasses must implement
 * the abstract mapRow(ResultSet, int) method to map each row of
 * the JDBC ResultSet into an object.
 *
 * <p>Such manual mapping is usually preferable to "automatic"
 * mapping using reflection, which can become complex in non-trivial
 * cases. For example, the present class allows different objects
 * to be used for different rows (for example, if a subclass is indicated).
 * It allows computed fields to be set. And there's no need for
 * ResultSet columns to have the same names as bean properties.
 * The Pareto Principle in action: going the extra mile to automate
 * the extraction process makes the framework much more complex
 * and delivers little real benefit.
 *
 * <p>Subclasses can be constructed providing SQL, parameter types
 * and a DataSource. SQL will often vary between subclasses.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 * @author Jean-Pierre Pawlak
 * @see org.springframework.jdbc.object.MappingSqlQuery
 * @see org.springframework.jdbc.object.SqlQuery
 */
public abstract class MappingSqlQueryWithParameters extends SqlQuery {

	/**
	 * Constructor to allow use as a JavaBean
	 */
	public MappingSqlQueryWithParameters() {
	}

	/**
	 * Convenient constructor with DataSource and SQL string.
	 * @param ds DataSource to use to get connections
	 * @param sql SQL to run
	 */
	public MappingSqlQueryWithParameters(DataSource ds, String sql) {
		super(ds, sql);
	}

	/**
	 * Implementation of protected abstract method. This invokes the subclass's
	 * implementation of the mapRow() method.
	 */
	protected final ResultReader newResultReader(int rowsExpected, Object[] parameters, Map context) {
		return new ResultReaderImpl(rowsExpected, parameters, context);
	}

	/**
	 * Subclasses must implement this method to convert each row
	 * of the ResultSet into an object of the result type.
	 * @param rs ResultSet we're working through
	 * @param rowNum row number (from 0) we're up to
	 * @param parameters to the query (passed to the execute() method).
	 * Subclasses are rarely interested in these.
	 * It can be null if there are no parameters.
	 * @param context passed to the execute() method.
	 * It can be null if no contextual information is need.
	 * @return an object of the result type
	 * @throws SQLException if there's an error extracting data.
	 * Subclasses can simply not catch SQLExceptions, relying on the
	 * framework to clean up.
	 */
	protected abstract Object mapRow(ResultSet rs, int rowNum, Object[] parameters, Map context) throws SQLException;


	/**
	 * Implementation of ResultReader that calls the enclosing
	 * class's mapRow() method for each row.
	 */
	private class ResultReaderImpl implements ResultReader {

		/** List to save results in */
		private List results;

		private Object[] params;

		private Map context;

		private int rowNum = 0;

		/**
		 * Use an array results. More efficient if we know how many results to expect.
		 */
		public ResultReaderImpl(int rowsExpected, Object[] parameters, Map context) {
			// Use the more efficient collection if we know how many rows to expect
			this.results = (rowsExpected > 0) ? (List) new ArrayList(rowsExpected) : (List) new LinkedList();
			this.params = parameters;
			this.context = context;
		}

		public void processRow(ResultSet rs) throws SQLException {
			results.add(mapRow(rs, rowNum++, params, context));
		}

		public List getResults() {
			return results;
		}
	}

}