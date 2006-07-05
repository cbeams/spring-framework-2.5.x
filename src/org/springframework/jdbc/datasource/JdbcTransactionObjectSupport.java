/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.datasource;

import java.sql.Savepoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionUsageException;
import org.springframework.transaction.support.SmartTransactionObject;

/**
 * Convenient base class for JDBC-aware transaction objects.
 * Can contain a ConnectionHolder, and implements the SavepointManager
 * interface based on that ConnectionHolder.
 *
 * <p>Implements the SavepointManager interface to allow for programmatic
 * management of JDBC 3.0 Savepoints. DefaultTransactionStatus will
 * automatically delegate to this, as it auto-detects transaction objects
 * that implement the SavepointManager interface.
 *
 * <p>Note that savepoints are only supported for JDBC 3.0.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see java.sql.Savepoint
 */
public abstract class JdbcTransactionObjectSupport implements SavepointManager, SmartTransactionObject {

	private static final String SAVEPOINT_CLASS_NAME = "java.sql.Savepoint";

	private static final Log logger = LogFactory.getLog(JdbcTransactionObjectSupport.class);

	private static boolean savepointClassAvailable;

	static {
		try {
			Class.forName(SAVEPOINT_CLASS_NAME);
			savepointClassAvailable = true;
			logger.info("JDBC 3.0 Savepoint class is available");
		}
		catch (ClassNotFoundException ex) {
			savepointClassAvailable = false;
			logger.info("JDBC 3.0 Savepoint class is not available");
		}
	}


	private ConnectionHolder connectionHolder;

	private Integer previousIsolationLevel;

	private boolean savepointAllowed;


	public void setConnectionHolder(ConnectionHolder connectionHolder) {
		this.connectionHolder = connectionHolder;
	}

	public ConnectionHolder getConnectionHolder() {
		return connectionHolder;
	}

	public boolean hasConnectionHolder() {
		return (this.connectionHolder != null);
	}

	public void setPreviousIsolationLevel(Integer previousIsolationLevel) {
		this.previousIsolationLevel = previousIsolationLevel;
	}

	public Integer getPreviousIsolationLevel() {
		return previousIsolationLevel;
	}

	public void setSavepointAllowed(boolean savepointAllowed) {
		this.savepointAllowed = savepointAllowed;
	}

	public boolean isSavepointAllowed() {
		return savepointAllowed;
	}


	//---------------------------------------------------------------------
	// Implementation of SavepointManager
	//---------------------------------------------------------------------

	/**
	 * This implementation creates a JDBC 3.0 Savepoint and returns it.
	 * @see java.sql.Connection#setSavepoint
	 */
	public Object createSavepoint() throws TransactionException {
		if (!savepointClassAvailable) {
			throw new NestedTransactionNotSupportedException(
					"Cannot create a nested JDBC transaction because you are not running on JDK 1.4 or higher");
		}
		ConnectionHolder conHolder = getConnectionHolderForSavepoint();
		try {
			if (!conHolder.supportsSavepoints()) {
				throw new NestedTransactionNotSupportedException(
						"Cannot create a nested transaction because savepoints are not supported by your JDBC driver");
			}
		}
		catch (Throwable ex) {
			throw new NestedTransactionNotSupportedException(
					"Cannot create a nested transaction because your JDBC driver is not a JDBC 3.0 driver", ex);
		}
		try {
			return conHolder.createSavepoint();
		}
		catch (Throwable ex) {
			throw new CannotCreateTransactionException("Could not create JDBC savepoint", ex);
		}
	}

	/**
	 * This implementation rolls back to the given JDBC 3.0 Savepoint.
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollbackToSavepoint(Object savepoint) throws TransactionException {
		try {
			getConnectionHolderForSavepoint().getConnection().rollback((Savepoint) savepoint);
		}
		catch (Throwable ex) {
			throw new TransactionSystemException("Could not roll back to JDBC savepoint", ex);
		}
	}

	/**
	 * This implementation releases the given JDBC 3.0 Savepoint.
	 * @see java.sql.Connection#releaseSavepoint
	 */
	public void releaseSavepoint(Object savepoint) throws TransactionException {
		try {
			getConnectionHolderForSavepoint().getConnection().releaseSavepoint((Savepoint) savepoint);
		}
		catch (Throwable ex) {
			logger.debug("Could not explicitly release JDBC savepoint", ex);
		}
	}

	protected ConnectionHolder getConnectionHolderForSavepoint() throws TransactionException {
		if (!isSavepointAllowed()) {
			throw new NestedTransactionNotSupportedException(
					"Transaction manager does not allow nested transactions");
		}
		if (!hasConnectionHolder()) {
			throw new TransactionUsageException(
					"Cannot create nested transaction if not exposing a JDBC transaction");
		}
		return getConnectionHolder();
	}

}
