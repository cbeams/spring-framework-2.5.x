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

package org.springframework.transaction.interceptor;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * Used for testing only (for example, when we must replace the
 * behaviour of a PlatformTransactionManager bean we don't have access to).
 * Allows behaviour of an entire class to change with static delegate change.
 * Not multithreaded.
 * @author Rod Johnson
 * @since 26-Apr-2003
 */
public class PlatformTransactionManagerFacade implements PlatformTransactionManager {
	
	/**
	 * This member can be changed to change behaviour class-wide.
	 */
	public static PlatformTransactionManager delegate;

	/**
	 * @see org.springframework.transaction.PlatformTransactionManager#getTransaction(org.springframework.transaction.TransactionDefinition)
	 */
	public TransactionStatus getTransaction(TransactionDefinition definition) {
		return delegate.getTransaction(definition);
	}

	/**
	 * @see org.springframework.transaction.PlatformTransactionManager#commit(org.springframework.transaction.TransactionStatus)
	 */
	public void commit(TransactionStatus status) {
		delegate.commit(status);
	}

	/**
	 * @see org.springframework.transaction.PlatformTransactionManager#rollback(org.springframework.transaction.TransactionStatus)
	 */
	public void rollback(TransactionStatus status) {
		delegate.rollback(status);
	}

}
