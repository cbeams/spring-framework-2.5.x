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

import java.util.HashMap;
import java.util.Map;

import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.FlushMode;

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

	private static final Object DEFAULT_KEY = new Object();

	private final Map sessionMap = new HashMap(1);

	private Transaction transaction;

	private FlushMode previousFlushMode;


	public SessionHolder(Session session) {
		addSession(DEFAULT_KEY, session);
	}

	public SessionHolder(Object key, Session session) {
		addSession(key, session);
	}


	public Session getSession() {
		return getSession(DEFAULT_KEY);
	}

	public Session getSession(Object key) {
		return (Session) this.sessionMap.get(key);
	}

	public Session getAnySession() {
		return (Session) this.sessionMap.values().iterator().next();
	}

	public void addSession(Object key, Session session) {
		this.sessionMap.put(key, session);
	}

	public Session removeSession(Object key) {
		return (Session) this.sessionMap.remove(key);
	}

	public boolean containsSession(Session session) {
		return this.sessionMap.containsValue(session);
	}

	public boolean isEmpty() {
		return this.sessionMap.isEmpty();
	}


	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setPreviousFlushMode(FlushMode previousFlushMode) {
		this.previousFlushMode = previousFlushMode;
	}

	public FlushMode getPreviousFlushMode() {
		return previousFlushMode;
	}

}
