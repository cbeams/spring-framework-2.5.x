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

package org.springframework.orm.hibernate;

import net.sf.hibernate.FlushMode;

import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;

/**
 * Hibernate transaction object, representing a SessionHolder.
 * Used as transaction object by HibernateTransactionManager.
 *
 * <p>Derives from JdbcTransactionObjectSupport to inherit the capability
 * to manage JDBC 3.0 Savepoints for underlying JDBC Connections.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see HibernateTransactionManager
 * @see SessionHolder
 */
public class HibernateTransactionObject extends JdbcTransactionObjectSupport {

	private SessionHolder sessionHolder;

	private boolean newSessionHolder;

	protected void setSessionHolder(SessionHolder sessionHolder, boolean newSessionHolder) {
		this.sessionHolder = sessionHolder;
		this.newSessionHolder = newSessionHolder;
	}

	public SessionHolder getSessionHolder() {
		return sessionHolder;
	}

	public boolean isNewSessionHolder() {
		return newSessionHolder;
	}

	public boolean hasTransaction() {
		return (this.sessionHolder != null && this.sessionHolder.getTransaction() != null);
	}

	public boolean isRollbackOnly() {
		return getSessionHolder().isRollbackOnly();
	}

}
