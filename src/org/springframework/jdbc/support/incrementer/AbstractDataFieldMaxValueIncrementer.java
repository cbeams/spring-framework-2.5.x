package org.springframework.jdbc.support.incrementer;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

/**
 * Implementation of {@link org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer}
 * Uses <b>Template Method</b> design pattern
 * Subclasses should provide implementations of protected abstract methods.
 *
 * <p><b>History:</b>
 * <li>17/04/2003 : donated to Spring by Dmitriy Kopylenko
 * <li>19/04/2003 : modified by Isabelle Muszynski, added nextDoubleValue
 * <li>09/05/2003 : modified by JPP, added nextLongValue
 * <li>17/06/2003 : modified by Ken Krebs, added common functionality for subclasses
 *
 * @author Dmitriy Kopylenko
 * @author Isabelle Muszynski
 * @author Jean-Pierre Pawlak
 * @author Ken Krebs
 * @version $Id: AbstractDataFieldMaxValueIncrementer.java,v 1.2 2004-01-02 22:01:31 jhoeller Exp $
 *
 */
public abstract class AbstractDataFieldMaxValueIncrementer implements DataFieldMaxValueIncrementer, InitializingBean {

	private DataSource dataSource;

	/** The name of the sequence/table containing the sequence */
	private String incrementerName;

	/** The name of the column for this sequence */
	private String columnName;

	/** The number of keys buffered in a cache */
	private int cacheSize = 1;

	/** Flag if dirty definition */
	private boolean dirty = true;


	/**
	 * Create a new incrementer.
	 */
	public AbstractDataFieldMaxValueIncrementer() {
	}

	/**
	 * Create a new incrementer.
	 * @param ds the datasource
	 * @param incrementerName the name of the sequence/table
	 **/
	public AbstractDataFieldMaxValueIncrementer(DataSource ds, String incrementerName) {
		this.dataSource = ds;
		this.incrementerName = incrementerName;
	}

	/**
	 * Create a new incrementer.
	 * @param ds the data source
	 * @param incrementerName the name of the sequence/table
	 * @param columnName the name of the column in the sequence table
	 **/
	public AbstractDataFieldMaxValueIncrementer(DataSource ds, String incrementerName, String columnName) {
		this.dataSource = ds;
		this.incrementerName = incrementerName;
		this.columnName = columnName;
	}

	/**
	 * Create a new incrementer.
	 * @param ds the data source
	 * @param incrementerName the name of the sequence/table
	 * @param cacheSize the number of buffered keys
	 **/
	public AbstractDataFieldMaxValueIncrementer(DataSource ds, String incrementerName, int cacheSize) {
		this.dataSource = ds;
		this.incrementerName = incrementerName;
		this.cacheSize = cacheSize;
	}

	/**
	 * Create a new incrementer.
	 * @param ds the data source
	 * @param incrementerName the name of the sequence/table
	 * @param columnName the name of the column in the sequence table
	 * @param cacheSize the number of buffered keys
	 **/
	public AbstractDataFieldMaxValueIncrementer(DataSource ds, String incrementerName, String columnName, int cacheSize) {
		this.dataSource = ds;
		this.incrementerName = incrementerName;
		this.columnName = columnName;
		this.cacheSize = cacheSize;
	}


	/**
	 * Set the data soruce.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.dirty = true;
	}

	/**
	 * Return the data source.
	 */
	public DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * Set the name of the sequence/table.
	 */
	public void setIncrementerName(String incrementerName) {
		this.incrementerName = incrementerName;
		this.dirty = true;
	}

	/**
	 * Return the name of the sequence/table.
	 */
	public String getIncrementerName() {
		return this.incrementerName;
	}

	/**
	 * Set the name of the column in the sequence table.
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
		this.dirty = true;
	}

	/**
	 * Return the name of the column in the sequence table.
	 */
	public String getColumnName() {
		return this.columnName;
	}

	/**
	 * Set the number of buffered keys.
	 */
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
		this.dirty = true;
	}

	/**
	 * Return the number of buffered keys.
	 */
	public int getCacheSize() {
		return this.cacheSize;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public void afterPropertiesSet() throws DataAccessException {
		if (this.dataSource == null) {
			throw new IllegalArgumentException("DataSource property must be set on " + getClass().getName());
		}
	}


	public int nextIntValue() throws DataAccessException {
		return incrementIntValue();
	}

	public long nextLongValue() throws DataAccessException {
		return incrementLongValue();
	}

	public double nextDoubleValue() throws DataAccessException {
		return incrementDoubleValue();
	}

	public String nextStringValue() throws DataAccessException {
		return incrementStringValue();
	}

	public Object nextValue(Class keyClass) throws DataAccessException {
		if (int.class.getName().equals(keyClass.getName()) || Integer.class.getName().equals(keyClass.getName())) {
			return new Integer(incrementIntValue());
		}
		else if (long.class.getName().equals(keyClass.getName()) || Long.class.getName().equals(keyClass.getName())) {
			return new Long(incrementLongValue());
		}
		else if (double.class.getName().equals(keyClass.getName()) || Double.class.getName().equals(keyClass.getName())) {
			return new Double(incrementDoubleValue());
		}
		else if (String.class.getName().equals(keyClass.getName())) {
			return incrementStringValue();
		}
		else {
			throw new IllegalArgumentException("Invalid key class: " + keyClass.getName());
		}
	}

	/**
	 * Template method implementation to be provided by concrete subclasses
	 * @see #nextIntValue
	 */
	protected abstract int incrementIntValue() throws DataAccessException;

	/**
	 * Template method implementation to be provided by concrete subclasses
	 * @see #nextLongValue
	 */
	protected abstract long incrementLongValue() throws DataAccessException;

	/**
	 * Template method implementation to be provided by concrete subclasses
	 * @see #nextDoubleValue
	 */
	protected abstract double incrementDoubleValue() throws DataAccessException;

	/**
	 * Template method implementation to be provided by concrete subclasses
	 * @see #nextStringValue
	 */
	protected abstract String incrementStringValue() throws DataAccessException;

}
