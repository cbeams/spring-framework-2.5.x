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

package org.springframework.transaction;

import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * @author Rod Johnson
 */
public class CountingTxManager extends AbstractPlatformTransactionManager {
	
	public int commits;
	public int rollbacks;
	public int inflight;
	public boolean rollbackOnly;

	protected Object doGetTransaction() {
		return new Object();
	}

	protected boolean isExistingTransaction(Object transaction) {
		return false;
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		++inflight;
	}

	protected Object doSuspend(Object transaction) {
		return null;
	}

	protected void doResume(Object transaction, Object suspendedResources)
	    throws TransactionException {
	}

	protected void doCommit(DefaultTransactionStatus status) {
		++commits;
		--inflight;
	}

	protected void doRollback(DefaultTransactionStatus status) {
		++rollbacks;
		--inflight;
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		rollbackOnly = true;
	}

	protected void doCleanupAfterCompletion(Object transaction) {
	}

}
