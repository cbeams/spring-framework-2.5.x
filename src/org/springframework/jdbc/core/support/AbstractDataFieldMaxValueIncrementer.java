package org.springframework.jdbc.core.support;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.DataFieldMaxValueIncrementer;

/**
 * Implementation of {@link org.springframework.jdbc.core.DataFieldMaxValueIncrementer}
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
 * @version $Id: AbstractDataFieldMaxValueIncrementer.java,v 1.2 2003-10-21 07:47:19 jhoeller Exp $
 *
 */
public abstract class AbstractDataFieldMaxValueIncrementer implements DataFieldMaxValueIncrementer, InitializingBean {

	private DataSource dataSource;

	/** The name of the sequence/table containing the sequence */
	private String incrementerName;

	/** The name of the column to use for this sequence */
	private String columnName;

	/** The number of keys buffered in a cache */
	private int cacheSize = 1;

	/** Flag if dirty definition */
	private boolean dirty = true;

	/** Get the state of the dirty flag */
	public boolean isDirty() {
		return this.dirty;
	}

	/**
	 * Default constructor
	 **/
	public AbstractDataFieldMaxValueIncrementer() {
	}

	/**
	 * Constructor
	 * @param ds the datasource to use
	 * @param incrementerName the name of the sequence/table to use
	 **/
	public AbstractDataFieldMaxValueIncrementer(DataSource ds, String incrementerName) {
		this.dataSource = ds;
		this.incrementerName = incrementerName;
	}

	/**
	 * Constructor
	 * @param ds the datasource to use
	 * @param incrementerName the name of the sequence/table to use
	 * @param columnName the name of the column in the sequence table to use
	 **/
	public AbstractDataFieldMaxValueIncrementer(DataSource ds, String incrementerName, String columnName) {
		this.dataSource = ds;
		this.incrementerName = incrementerName;
		this.columnName = columnName;
	}

	/**
	 * Constructor
	 * @param ds the datasource to use
	 * @param incrementerName the name of the sequence/table to use
	 * @param cacheSize the number of buffered keys
	 **/
	public AbstractDataFieldMaxValueIncrementer(DataSource ds, String incrementerName, int cacheSize) {
		this.dataSource = ds;
		this.incrementerName = incrementerName;
		this.cacheSize = cacheSize;
	}

	/**
	 * Constructor
	 * @param ds the datasource to use
	 * @param incrementerName the name of the sequence/table to use
	 * @param columnName the name of the column in the sequence table to use
	 * @param cacheSize the number of buffered keys
	 **/
	public AbstractDataFieldMaxValueIncrementer(DataSource ds, String incrementerName, String columnName, int cacheSize) {
		this.dataSource = ds;
		this.incrementerName = incrementerName;
		this.columnName = columnName;
		this.cacheSize = cacheSize;
	}

	/** Set the state of the dirty flag	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * Set the data source.
	 * @param dataSource The data source to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.dirty = true;
	}

	/**
	 * Get the data source.
	 * @return ds The data source to return
	 */
	public DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * Set the incrementerName.
	 * @param incrementerName The incrementerName to set
	 */
	public void setIncrementerName(String incrementerName) {
		this.incrementerName = incrementerName;
		this.dirty = true;
	}

	/**
	 * Get the incrementerName.
	 * @return incrementerName The incrementerName to return
	 */
	public String getIncrementerName() {
		return this.incrementerName;
	}

	/**
	 * Set the columnName.
	 * @param columnName The columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
		this.dirty = true;
	}

	/**
	 * Get the columnName.
	 * @return columnName The columnName to return
	 */
	public String getColumnName() {
		return this.columnName;
	}

	/**
	 * Set the cacheSize.
	 * @param cacheSize The number of buffered keys
	 */
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
		this.dirty = true;
	}

	/**
	 * Get the cacheSize.
	 * @return cacheSize The cacheSize to return
	 */
	public int getCacheSize() {
		return this.cacheSize;
	}

	/**
	 * Template method
	 * @see org.springframework.jdbc.core.DataFieldMaxValueIncrementer#nextIntValue
	 */
	public final int nextIntValue() throws DataAccessException {
		return incrementIntValue();
	}

	/**
	 * Template method
	 * @see org.springframework.jdbc.core.DataFieldMaxValueIncrementer#nextLongValue
	 */
	public final long nextLongValue() throws DataAccessException {
		return incrementLongValue();
	}

	/**
	 * Template method
	 * @see org.springframework.jdbc.core.DataFieldMaxValueIncrementer#nextDoubleValue
	 */
	public final double nextDoubleValue() throws DataAccessException {
		return incrementDoubleValue();
	}

	/**
	 * Template method
	 * @see org.springframework.jdbc.core.DataFieldMaxValueIncrementer#nextStringValue()
	 */
	public final String nextStringValue() throws DataAccessException {
		return incrementStringValue();
	}

	/**
	 * Template method
	 * @see org.springframework.jdbc.core.DataFieldMaxValueIncrementer#nextValue(java.lang.Class)
	 */
	public final Object nextValue(Class keyClass) throws DataAccessException {
		if (int.class.getName().equals(keyClass.getName()) || Integer.class.getName().equals(keyClass.getName()))
			return new Integer(incrementIntValue());
		else if (long.class.getName().equals(keyClass.getName()) || Long.class.getName().equals(keyClass.getName()))
			return new Long(incrementLongValue());
		else if (double.class.getName().equals(keyClass.getName()) || Double.class.getName().equals(keyClass.getName()))
			return new Double(incrementDoubleValue());
		else if (String.class.getName().equals(keyClass.getName()))
			return incrementStringValue();
		else
			throw new IllegalArgumentException("Invalid key class");
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

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws DataAccessException {
		if (this.dataSource == null)
			throw new InvalidMaxValueIncrementerApiUsageException("DataSource property must be set on " + getClass().getName());
	}
}
