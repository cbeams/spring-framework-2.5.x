/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.orm.jdo;

import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;

/**
 * JDO transaction object, representing a PersistenceManagerHolder.
 * Used as transaction object by JdoTransactionManager.
 *
 * <p>Derives from JdbcTransactionObjectSupport to inherit the capability
 * to manage JDBC 3.0 Savepoints for underlying JDBC Connections.
 * 
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 13.06.2003
 * @see JdoTransactionManager
 * @see PersistenceManagerHolder
 */
public class JdoTransactionObject extends JdbcTransactionObjectSupport {

	private PersistenceManagerHolder persistenceManagerHolder;

	private boolean newPersistenceManagerHolder;

	private Object transactionData;

	protected void setPersistenceManagerHolder(PersistenceManagerHolder persistenceManagerHolder,
																						 boolean newPersistenceManagerHolder) {
		this.persistenceManagerHolder = persistenceManagerHolder;
		this.newPersistenceManagerHolder = newPersistenceManagerHolder;
	}

	public PersistenceManagerHolder getPersistenceManagerHolder() {
		return persistenceManagerHolder;
	}

	public boolean isNewPersistenceManagerHolder() {
		return newPersistenceManagerHolder;
	}

	public boolean hasTransaction() {
		return (this.persistenceManagerHolder != null &&
				this.persistenceManagerHolder.getPersistenceManager() != null &&
		    this.persistenceManagerHolder.getPersistenceManager().currentTransaction().isActive());
	}

	protected void setTransactionData(Object transactionData) {
		this.transactionData = transactionData;
	}

	public Object getTransactionData() {
		return transactionData;
	}

	public boolean isRollbackOnly() {
		return getPersistenceManagerHolder().isRollbackOnly();
	}

}
