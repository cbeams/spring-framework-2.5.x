package org.springframework.jdbc.datasource;


/**
 * DataSource transaction object, representing a ConnectionHolder.
 * Used as transaction object by DataSourceTransactionManager.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see DataSourceTransactionManager
 * @see ConnectionHolder
 * @version $Id: DataSourceTransactionObject.java,v 1.3 2003-11-27 14:32:42 johnsonr Exp $
 */
public class DataSourceTransactionObject {

	private ConnectionHolder connectionHolder;

	private Integer previousIsolationLevel;
	
	private boolean mustRestoreAutoCommit;

	/**
	 * Create DataSourceTransactionObject for new ConnectionHolder.
	 */
	public DataSourceTransactionObject() {
	}

	/**
	 * Create DataSourceTransactionObject for existing ConnectionHolder.
	 */
	protected DataSourceTransactionObject(ConnectionHolder connectionHolder) {
		this.connectionHolder = connectionHolder;
	}

	/**
	 * Set new ConnectionHolder.
	 */
	protected void setConnectionHolder(ConnectionHolder connectionHolder) {
		this.connectionHolder = connectionHolder;
	}

	public ConnectionHolder getConnectionHolder() {
		return connectionHolder;
	}

	protected void setPreviousIsolationLevel(Integer previousIsolationLevel) {
		this.previousIsolationLevel = previousIsolationLevel;
	}

	public Integer getPreviousIsolationLevel() {
		return previousIsolationLevel;
	}

	/**
	 * @return was autocommit previously set?
	 */
	public boolean getMustRestoreAutoCommit() {
		return mustRestoreAutoCommit;
	}
	/**
	 * @param whether autocommit was previously set?
	 */
	public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
		this.mustRestoreAutoCommit = mustRestoreAutoCommit;
	}
}
