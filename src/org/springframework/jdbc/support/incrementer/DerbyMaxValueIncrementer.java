package org.springframework.jdbc.support.incrementer;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class to increment maximum value of a given Derby table with the equivalent
 * of an auto-increment column. Note: If you use this class, your Derby key
 * column should <i>NOT</i> be defined as an IDENTITY column, as the sequence table does the job.
 * Thanks to Endre St¿lsvik for the suggestion!
 *
 * <p>The sequence is kept in a table. There should be one sequence table per
 * table that needs an auto-generated key.
 *
 * <p>Derby requires an additional column to be used for the insert since it is impossible
 * to insert a null into the identity column and have the value generated.  This is solved by
 * providing the name of a dummy column that also must be created in the sequence table.
 *
 * <p>Example:
 *
 * <pre class="code">create table tab (id int not null primary key, text varchar(100));
 * create table tab_sequence (value int generated always as identity, dummy char(1));
 * insert into tab_sequence (dummy) values(null);</pre>
 *
 * If cacheSize is set, the intermediate values are served without querying the
 * database. If the server or your application is stopped or crashes or a transaction
 * is rolled back, the unused values will never be served. The maximum hole size in
 * numbering is consequently the value of cacheSize.
 *
 * <b>HINT:</b>  Since Derby supports the JDBC 3.0 method getGeneratedKeys it's recommended to
 * use IDENTITY columns directly in the tables and then utilizing a {@link org.springframework.jdbc.support.KeyHolder}
 * when calling the with the update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder) method of
 * the {@link org.springframework.jdbc.core.JdbcTemplate}.
 *
 * @author Thomas Risberg
 * @since 2.5
 */
public class DerbyMaxValueIncrementer extends AbstractDataFieldMaxValueIncrementer {

	/** The default for dummy name */
	private static final String DEFAULT_DUMMY_NAME = "dummy";

	/** The name of the column for this sequence */
	private String columnName;

	/** The name of the dummy column used for inserts */
	private String dummyName = DEFAULT_DUMMY_NAME;

	/** The number of keys buffered in a cache */
	private int cacheSize = 1;

	private long[] valueCache = null;

	/** The next id to serve from the value cache */
	private int nextValueIndex = -1;


	/**
	 * Default constructor.
	 **/
	public DerbyMaxValueIncrementer() {
	}

	/**
	 * Convenience constructor.
	 * @param ds the DataSource to use
	 * @param incrementerName the name of the sequence/table to use
	 * @param columnName the name of the column in the sequence table to use
	 **/
	public DerbyMaxValueIncrementer(DataSource ds, String incrementerName, String columnName) {
		this(ds, incrementerName, columnName, DEFAULT_DUMMY_NAME);
	}

	/**
	 * Convenience constructor.
	 * @param ds the DataSource to use
	 * @param incrementerName the name of the sequence/table to use
	 * @param columnName the name of the column in the sequence table to use
	 * @param dummyName the name of the dummy column used for inserts
	 **/
	public DerbyMaxValueIncrementer(DataSource ds, String incrementerName, String columnName, String dummyName) {
		setDataSource(ds);
		setIncrementerName(incrementerName);
		this.columnName = columnName;
		this.dummyName = dummyName;
		afterPropertiesSet();
	}

	/**
	 * Set the name of the column in the sequence table.
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * Return the name of the column in the sequence table.
	 */
	public String getColumnName() {
		return this.columnName;
	}

	/**
	 * Set the name of the dummy column.
	 */
	public void setDummyName(String dummyName) {
		this.dummyName = dummyName;
	}

	/**
	 * Return the name of the dummy column.
	 */
	public String getDummyName() {
		return dummyName;
	}

	/**
	 * Set the number of buffered keys.
	 */
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	/**
	 * Return the number of buffered keys.
	 */
	public int getCacheSize() {
		return this.cacheSize;
	}

	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (this.columnName == null) {
			throw new IllegalArgumentException("columnName is required");
		}
	}


	protected synchronized long getNextKey() throws DataAccessException {
		if (this.nextValueIndex < 0 || this.nextValueIndex >= getCacheSize()) {
			/*
			* Need to use straight JDBC code because we need to make sure that the insert and select
			* are performed on the same connection (otherwise we can't be sure that last_insert_id()
			* returned the correct value)
			*/
			Connection con = DataSourceUtils.getConnection(getDataSource());
			Statement stmt = null;
			try {
				stmt = con.createStatement();
				DataSourceUtils.applyTransactionTimeout(stmt, getDataSource());
				this.valueCache = new long[getCacheSize()];
				this.nextValueIndex = 0;
				for (int i = 0; i < getCacheSize(); i++) {
					stmt.executeUpdate("insert into " + getIncrementerName() + " (" + getDummyName() + ") values(null)");
					ResultSet rs = stmt.executeQuery("select IDENTITY_VAL_LOCAL() from " + getIncrementerName());
					try {
						if (!rs.next()) {
							throw new DataAccessResourceFailureException("IDENTITY_VAL_LOCAL() failed after executing an update");
						}
						this.valueCache[i] = rs.getLong(1);
					}
					finally {
						JdbcUtils.closeResultSet(rs);
					}
				}
				long maxValue = this.valueCache[(this.valueCache.length - 1)];
				stmt.executeUpdate("delete from " + getIncrementerName() + " where " + this.columnName + " < " + maxValue);
			}
			catch (SQLException ex) {
				throw new DataAccessResourceFailureException("Could not obtain IDENTITY value", ex);
			}
			finally {
				JdbcUtils.closeStatement(stmt);
				DataSourceUtils.releaseConnection(con, getDataSource());
			}
		}
		return this.valueCache[this.nextValueIndex++];
	}

}
