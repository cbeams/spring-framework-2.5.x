package org.springframework.jdbc.support.incrementer;

import javax.sql.DataSource;

/**
 * Class to retrieve the next value of a given PostgreSQL Sequence.
 * @author Tomislav Urban
 * @author Juergen Hoeller
 */
public class PostgreSQLSequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

	/**
	 * Default constructor.
	 **/
	public PostgreSQLSequenceMaxValueIncrementer() {
	}

	/**
	 * Convenience constructor.
	 * @param ds the DataSource to use
	 * @param incrementerName the name of the sequence/table to use
	 */
	public PostgreSQLSequenceMaxValueIncrementer(DataSource ds, String incrementerName) {
		setDataSource(ds);
		setIncrementerName(incrementerName);
		afterPropertiesSet();
	}

	protected String getSequenceQuery() {
		return "select nextval('" + getIncrementerName() + "')";
	}

}
