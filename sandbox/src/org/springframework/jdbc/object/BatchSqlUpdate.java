/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.jdbc.object;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

/**
 * SqlUpdate subclass that performs batch update operations. Encapsulates
 * queuing up records to be updated, and adds them as a single batch once
 * the given batch size has been met.
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 27.04.2004
 */
public class BatchSqlUpdate extends SqlUpdate {

	/**
	 * Default number of inserts to accumulate before commiting a batch (5000).
	 */
	public static int DEFAULT_BATCH_SIZE = 5000;

	private int batchSize = DEFAULT_BATCH_SIZE;

	private final LinkedList parameterQueue = new LinkedList();

	private final LinkedList rowsAffected = new LinkedList();


	/**
	 * Constructor to allow use as a JavaBean. DataSource and SQL
	 * must be supplied before compilation and use.
	 */
	public BatchSqlUpdate() {
		super();
	}

	/**
	 * Constructs an update object with a given DataSource and SQL.
	 * @param ds DataSource to use to obtain connections
	 * @param sql SQL statement to execute
	 */
	public BatchSqlUpdate(DataSource ds, String sql) {
		super(ds, sql);
	}

	/**
	 * Construct an update object with a given DataSource, SQL
	 * and anonymous parameters.
	 * @param ds DataSource to use to obtain connections
	 * @param sql SQL statement to execute
	 * @param types anonymous parameter declarations
	 */
	public BatchSqlUpdate(DataSource ds, String sql, int[] types) {
		super(ds, sql, types);
	}

	/**
	 * Construct an update object with a given DataSource, SQL,
	 * anonymous parameters and specifying the maximum number of rows
	 * that may be affected.
	 * @param ds DataSource to use to obtain connections
	 * @param sql SQL statement to execute
	 * @param types anonymous parameter declarations.
	 * @param batchSize the number of statements that will trigger
	 * an automatic intermediate flush
	 */
	public BatchSqlUpdate(DataSource ds, String sql, int[] types,
												int batchSize) {
		super(ds, sql, types, batchSize);
	}

	/**
	 * Set the number of statements that will trigger an automatic intermediate
	 * flush. "update" invocations respectively the given statement parameters
	 * will be queued until the batch size is met, at which it will empty the
	 * queue and execute the batch.
	 */
	public void setBatchSize(int batchSize) {
			this.batchSize = batchSize;
	}


	/**
	 * Overridden version of update that adds the given statement
	 * parameters to the queue rather than executing them immediately.
	 * You need to call flush to actually execute the batch.
	 * <p>All other update methods of the SqlUpdate base class go
	 * through this method and will thus behave similarly.
	 * @param args array of object arguments
	 * @return the number of rows affected by the update (always -1,
	 * meaning "not applicable", as the statement is not actually
	 * executed by this method)
	 * @see #flush
	 */
	public int update(Object[] args) throws DataAccessException {
		validateParameters(args);
		this.parameterQueue.add(args);

		if (this.parameterQueue.size() == this.batchSize) {
			if (logger.isDebugEnabled()) {
				logger.debug("Triggering auto-flush because queue reached batch size of " + this.batchSize);
			}
			flush();
		}

		return -1;
	}

	/**
	 * Return the current number of statements respectively statement
	 * parameters in the queue.
	 */
	public int getQueueCount() {
		return this.parameterQueue.size();
	}

	/**
	 * Trigger any queued update operations to be added as a final batch.
	 * @return an array of the number of rows affected by each statement
	 */
	public int[] flush() {
		if (this.parameterQueue.isEmpty()) {
			return new int[0];
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Executing batch update statement [" + getSql() + "]");
		}
		int[] rowsAffected = getJdbcTemplate().batchUpdate(
				getSql(),
				new BatchPreparedStatementSetter() {
				 public int getBatchSize() {
					 return parameterQueue.size();
				 }
				 public void setValues(PreparedStatement ps, int index) throws SQLException {
					 Object[] params = (Object[]) parameterQueue.removeFirst();
					 newPreparedStatementSetter(params).setValues(ps);
				 }
				});

		for (int i = 0; i < rowsAffected.length; i++) {
			this.rowsAffected.add(new Integer(rowsAffected[i]));
		}
		for (int i = 0; i < rowsAffected.length; i++) {
			checkRowsAffected(rowsAffected[i]);
		}
		return rowsAffected;
	}

	/**
	 * Return the number of affected rows for all already executed statements.
	 * Accumulates all of <code>flush</code>'s return values until
	 * <code>clear</code> is invoked.
	 * @return an array of the number of rows affected by each statement
	 */
	public int[] getRowsAffected() {
		int[] result = new int[this.rowsAffected.size()];
		for (int i = 0; i < this.rowsAffected.size(); i++) {
			Integer rowCount = (Integer) this.rowsAffected.get(i);
			result[i] = rowCount.intValue();
		}
		return result;
	}

	/**
	 * Return the number of already executed statements.
	 */
	public int getExecutionCount() {
		return this.rowsAffected.size();
	}

	/**
	 * Reset the statement queue, the rows affected cache,
	 * and the execution count.
	 */
	public void reset() {
		this.parameterQueue.clear();
		this.rowsAffected.clear();
	}

}
