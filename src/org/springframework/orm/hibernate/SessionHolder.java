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

import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Session holder, wrapping a Hibernate Session and a Hibernate Transaction.
 * HibernateTransactionManager binds instances of this class
 * to the thread, for a given SessionFactory.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 06.05.2003
 * @see HibernateTransactionManager
 * @see HibernateTransactionObject
 * @see SessionFactoryUtils
 */
public class SessionHolder extends ResourceHolderSupport {

	private final Session session;

	private Transaction transaction;

	private boolean synchronizedWithTransaction;

	public SessionHolder(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setSynchronizedWithTransaction(boolean synchronizedWithTransaction) {
		this.synchronizedWithTransaction = synchronizedWithTransaction;
	}

	public boolean isSynchronizedWithTransaction() {
		return synchronizedWithTransaction;
	}

}
