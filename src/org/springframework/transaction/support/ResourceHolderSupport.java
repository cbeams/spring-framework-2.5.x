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

package org.springframework.transaction.support;

import java.util.Date;

/**
 * Convenient base class for resource holders.
 *
 * <p>Features rollback-only support for nested Hibernate transactions.
 * Can expire after a certain number of seconds or milliseconds,
 * to determine transactional timeouts.
 *
 * @author Juergen Hoeller
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager#doBegin
 * @see org.springframework.jdbc.datasource.DataSourceUtils#applyTransactionTimeout
 */
public abstract class ResourceHolderSupport {

	private boolean synchronizedWithTransaction;

	private boolean rollbackOnly;

	private Date deadline;


	/**
	 * Mark the resource as synchronized with a transaction.
	 */
	public void setSynchronizedWithTransaction(boolean synchronizedWithTransaction) {
		this.synchronizedWithTransaction = synchronizedWithTransaction;
	}

	/**
	 * Return whether the resource is synchronized with a transaction.
	 */
	public boolean isSynchronizedWithTransaction() {
		return synchronizedWithTransaction;
	}

	/**
	 * Mark the resource transaction as rollback-only.
	 */
	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}

	/**
	 * Return whether the resource transaction is marked as rollback-only.
	 */
	public boolean isRollbackOnly() {
		return rollbackOnly;
	}

	/**
	 * Set the timeout for this object in seconds.
	 * @param seconds number of seconds until expiration
	 */
	public void setTimeoutInSeconds(int seconds) {
		setTimeoutInMillis(seconds * 1000);
	}

	/**
	 * Set the timeout for this object in milliseconds.
	 * @param millis number of milliseconds until expiration
	 */
	public void setTimeoutInMillis(long millis) {
		this.deadline = new Date(System.currentTimeMillis() + millis);
	}

	/**
	 * Return whether this object has an associated timeout.
	 */
	public boolean hasTimeout() {
		return (this.deadline != null);
	}

	/**
	 * Return the expiration deadline of this object.
	 * @return the deadline as Date object
	 */
	public Date getDeadline() {
		return deadline;
	}

	/**
	 * Return the time to live for this object in seconds.
	 * Rounds up eagerly, e.g. 9.00001 still to 10.
	 * @return number of seconds until expiration
	 */
	public int getTimeToLiveInSeconds() {
		double diff = ((double) getTimeToLiveInMillis()) / 1000;
		return (int) Math.ceil(diff);
	}

	/**
	 * Return the time to live for this object in milliseconds.
	 * @return number of millseconds until expiration
	 */
	public long getTimeToLiveInMillis() {
		if (this.deadline == null) {
			throw new IllegalStateException("No timeout specified for this resource holder");
		}
		long timeToLive = this.deadline.getTime() - System.currentTimeMillis();
		return (timeToLive >= 0 ? timeToLive : 0);
	}

	/**
	 * Clear the transaction state of this resource holder.
	 */
	public void clear() {
		this.synchronizedWithTransaction = false;
		this.rollbackOnly = false;
		this.deadline = null;
	}

}
