package org.springframework.jdbc.support.incrementer;

import javax.sql.DataSource;

/**
 * DataFieldMaxValueIncrementer that retrieves the next value of a given HSQL sequence.
 * Thanks to Guillaume Bilodeau for the suggestion!
 *
 * NOTE: This is an alternative to using a regular table to support generating unique keys that
 * was necessary in previous versions of HSQL.
 *
 * @author Thomas Risberg
 * @see org.springframework.jdbc.support.incrementer.HsqlMaxValueIncrementer
 */
public class HsqlSequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

	/**
	 * Default constructor.
	 **/
	public HsqlSequenceMaxValueIncrementer() {
	}

	/**
	 * Convenience constructor.
	 * @param ds the DataSource to use
	 * @param incrementerName the name of the sequence to use
	 */
	public HsqlSequenceMaxValueIncrementer(DataSource ds, String incrementerName) {
		setDataSource(ds);
		setIncrementerName(incrementerName);
		afterPropertiesSet();
	}

	protected String getSequenceQuery() {
		return "call next value for " + getIncrementerName();
	}

}
