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

/**
 * Hibernate transaction object, representing a SessionHolder.
 * Used as transaction object by HibernateTransactionManager.
 *
 * <p>Instances of this class are the transaction objects that
 * HibernateTransactionManager returns. They nest the thread-bound
 * SessionHolder internally.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see HibernateTransactionManager
 * @see SessionHolder
 */
public class HibernateTransactionObject {

	private SessionHolder sessionHolder;

	private boolean newSessionHolder;

	private Integer previousIsolationLevel;

	private FlushMode previousFlushMode;

	/**
	 * Create HibernateTransactionObject for new SessionHolder.
	 */
	protected HibernateTransactionObject() {
	}

	/**
	 * Create HibernateTransactionObject for existing SessionHolder.
	 */
	protected HibernateTransactionObject(SessionHolder sessionHolder) {
		this.sessionHolder = sessionHolder;
		this.newSessionHolder = false;
	}

	/**
	 * Set new SessionHolder.
	 */
	protected void setSessionHolder(SessionHolder sessionHolder) {
		this.sessionHolder = sessionHolder;
		this.newSessionHolder = (sessionHolder != null);
	}

	public SessionHolder getSessionHolder() {
		return sessionHolder;
	}

	public boolean isNewSessionHolder() {
		return newSessionHolder;
	}

	public boolean hasTransaction() {
		return (sessionHolder != null && sessionHolder.getTransaction() != null);
	}

	protected void setPreviousIsolationLevel(Integer previousIsolationLevel) {
		this.previousIsolationLevel = previousIsolationLevel;
	}

	public Integer getPreviousIsolationLevel() {
		return previousIsolationLevel;
	}

	protected void setPreviousFlushMode(FlushMode previousFlushMode) {
		this.previousFlushMode = previousFlushMode;
	}

	public FlushMode getPreviousFlushMode() {
		return previousFlushMode;
	}

}
