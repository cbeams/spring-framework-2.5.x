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
 * the abstract updateRow(ResultSet, int, context) method to update each 
 * row of the JDBC ResultSet and optionally map contents into an object.
 *
 * <p>Subclasses can be constructed providing SQL, parameter types
 * and a DataSource. SQL will often vary between subclasses.
 *
 * @author Thomas Risberg
 * @see org.springframework.jdbc.object.SqlQuery
 */
public abstract class UpdatableSqlQuery extends SqlQuery {

	/**
	 * Constructor to allow use as a JavaBean
	 */
	public UpdatableSqlQuery() {
		setUpdatableResults(true);
	}

	/**
	 * Convenient constructor with DataSource and SQL string.
	 * @param ds DataSource to use to get connections
	 * @param sql SQL to run
	 */
	public UpdatableSqlQuery(DataSource ds, String sql) {
		super(ds, sql);
		setUpdatableResults(true);
	}

	/**
	 * Implementation of protected abstract method. This invokes the subclass's
	 * implementation of the updateRow() method.
	 */
	protected ResultReader newResultReader(int rowsExpected, Object[] parameters, Map context) {
		return new ResultReaderImpl(rowsExpected, context);
	}

	/**
	 * Subclasses must implement this method to update each row of the 
	 * ResultSet and optionally create object of the result type.
	 * @param rs ResultSet we're working through
	 * @param rowNum row number (from 0) we're up to
	 * @param context passed to the execute() method.
	 * It can be null if no contextual information is need.  If you
	 * need to pass in data for each row, you can pass in a HashMap with 
	 * the primary key of the row being the key for the HashMap.  That way
	 * it is easy to locate the updates for each row 
	 * @return an object of the result type
	 * @throws SQLException if there's an error updateing data.
	 * Subclasses can simply not catch SQLExceptions, relying on the
	 * framework to clean up.
	 */
	protected abstract Object updateRow(ResultSet rs, int rowNum, Map context) throws SQLException;


	/**
	 * Implementation of ResultReader that calls the enclosing
	 * class's updateRow() method for each row.
	 */
	private class ResultReaderImpl implements ResultReader {

		/** List to save results in */
		private List results;

		private Map context;

		private int rowNum = 0;

		/**
		 * Use an array results. More efficient if we know how many results to expect.
		 */
		public ResultReaderImpl(int rowsExpected, Map context) {
			// Use the more efficient collection if we know how many rows to expect
			this.results = (rowsExpected > 0) ? (List) new ArrayList(rowsExpected) : (List) new LinkedList();
			this.context = context;
		}

		public void processRow(ResultSet rs) throws SQLException {
			results.add(updateRow(rs, rowNum++, context));
			rs.updateRow();
		}

		public List getResults() {
			return results;
		}
	}

}
