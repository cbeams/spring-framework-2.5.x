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

package org.springframework.orm.ojb;

import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;

/**
 * OJB transaction object, representing a setPersistenceBrokerHolder.
 * Used as transaction object by PersistenceBrokerTransactionManager.
 *
 * <p>Derives from JdbcTransactionObjectSupport to inherit the capability
 * to manage JDBC 3.0 Savepoints for underlying JDBC Connections.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 02.07.2004
 * @see PersistenceBrokerTransactionManager
 * @see PersistenceBrokerHolder
 */
public class PersistenceBrokerTransactionObject extends JdbcTransactionObjectSupport {

	private PersistenceBrokerHolder persistenceBrokerHolder;

	protected void setPersistenceBrokerHolder(PersistenceBrokerHolder persistenceBrokerHolder) {
		this.persistenceBrokerHolder = persistenceBrokerHolder;
	}

	public PersistenceBrokerHolder getPersistenceBrokerHolder() {
		return persistenceBrokerHolder;
	}

	public boolean isRollbackOnly() {
		return getPersistenceBrokerHolder().isRollbackOnly();
	}

}
