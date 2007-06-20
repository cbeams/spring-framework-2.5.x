package org.springframework.jdbc.support.incrementer;

import javax.sql.DataSource;

/**
 * DataFieldMaxValueIncrementer that retrieves the next value of a given H2 Database sequence.
 *
 * @author Thomas Risberg
 */
public class H2SequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

	/**
	 * Default constructor.
	 **/
	public H2SequenceMaxValueIncrementer() {
	}

	/**
	 * Convenience constructor.
	 * @param ds the DataSource to use
	 * @param incrementerName the name of the sequence to use
	 */
	public H2SequenceMaxValueIncrementer(DataSource ds, String incrementerName) {
		setDataSource(ds);
		setIncrementerName(incrementerName);
		afterPropertiesSet();
	}

	protected String getSequenceQuery() {
		return "select " + getIncrementerName() + ".nextval from dual";
	}

}
