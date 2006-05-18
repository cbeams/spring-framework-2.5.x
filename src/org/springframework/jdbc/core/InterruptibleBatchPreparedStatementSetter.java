package org.springframework.jdbc.core;

/**
 * Callback interface used by the JdbcTemplate class.
 *
 * <p>This interface sets values on a PreparedStatement provided by the
 * JdbcTemplate class for each of a number of updates in a batch using the
 * same SQL. Implementations are responsible for setting any necessary
 * parameters. SQL with placeholders will already have been supplied.
 *
 * <p>This interface allows you to signal the end of a batch rather than
 * having to determine the exact batch size upfront.  Batch size is still being
 * honored but it is now the maximum size of the batch.
 *
 * <p>The isBatchComplete method is called after each call to setValues to determine whether
 * there were some values added or if the batch was determined to be complete and no additional
 * values were provided during the last call to setValues.
 *
 * <p>Implementations <i>do not</i> need to concern themselves with
 * SQLExceptions that may be thrown from operations they attempt.
 * The JdbcTemplate class will catch and handle SQLExceptions appropriately.
 *
 * @author Thomas Risberg
 * @since 2.0
 * @see org.springframework.jdbc.core.JdbcTemplate#batchUpdate(String, org.springframework.jdbc.core.BatchPreparedStatementSetter)
 */
public interface InterruptibleBatchPreparedStatementSetter extends BatchPreparedStatementSetter {

	/**
	 * Return the whether batch is complete, there were no additional values to be added.
	 * @param i index of the statement we're issuing in the batch, starting from 0
	 */
	boolean isBatchComplete(int i);

}
