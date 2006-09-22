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

package org.springframework.transaction;

import java.util.List;

import javax.transaction.Synchronization;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.jta.JtaAfterCompletionSynchronization;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.transaction.support.CallbackTransactionExecutor;
import org.springframework.transaction.support.TransactionCallback;

/**
 * @author Juergen Hoeller
 * @since 19.09.2006
 */
public class TestCallbackPreferringJtaTransactionManager extends JtaTransactionManager
		implements CallbackPreferringPlatformTransactionManager, InitializingBean {

	private final CallbackBasedTransactionService transactionService;

	private CallbackTransactionExecutor callbackTransactionExecutor;


	public TestCallbackPreferringJtaTransactionManager(CallbackBasedTransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		this.callbackTransactionExecutor = new CallbackTransactionExecutor(getTransactionSynchronization());
	}


	public Object execute(TransactionDefinition definition, TransactionCallback callback) throws TransactionException {
		TransactionServiceRunnableAdapter adapter = new TransactionServiceRunnableAdapter(definition, callback);
		boolean createNew = (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
				definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		boolean suspendExisting = (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
				definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
		this.transactionService.executeWithinTransaction(definition.getTimeout(), createNew, suspendExisting, adapter);
		return adapter.getResult();
	}


	public interface CallbackBasedTransactionService {

		boolean isTransactionActive();

		void executeWithinTransaction(int timeout, boolean createNew, boolean suspendExistingIfNecessary, TransactionServiceRunnable runnable);
	}


	public interface TransactionServiceRunnable {

		void run(TransactionStatusFacility transactionStatus);
	}


	public interface TransactionStatusFacility {

		void setRollbackOnly();

		void registerSynchronization(Synchronization synch);
	}


	public class TransactionServiceRunnableAdapter implements TransactionServiceRunnable {

		private final TransactionDefinition definition;

		private final TransactionCallback callback;

		private Object result;

		public TransactionServiceRunnableAdapter(TransactionDefinition definition, TransactionCallback callback) {
			this.definition = definition;
			this.callback = callback;
		}

		public void run(final TransactionStatusFacility transactionStatus) {
			this.result = callbackTransactionExecutor.execute(this.definition, this.callback,
					new CallbackTransactionExecutor.CallbackTransactionObject() {
						public boolean isExistingTransaction() {
							return transactionService.isTransactionActive();
						}
						public void setRollbackOnly() {
							transactionStatus.setRollbackOnly();
						}
						public void registerAfterCompletion(List synchronizations) {
							transactionStatus.registerSynchronization(new JtaAfterCompletionSynchronization(synchronizations));
						}
					});
		}

		public Object getResult() {
			return result;
		}
	}

}
