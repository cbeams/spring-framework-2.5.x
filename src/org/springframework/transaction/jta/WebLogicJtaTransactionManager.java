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

package org.springframework.transaction.jta;

import java.lang.reflect.Method;

import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionUsageException;

/**
 * Special JtaTransactionManager version for BEA WebLogic.
 * Invokes WebLogic's proprietary "forceResume" method if standard JTA resume
 * failed, to also resume if the target transaction was marked rollback-only.
 * If you're not relying on this feature of transaction suspension in the first
 * place, simply use Spring's standard JtaTransactionManager.
 *
 * <p>Also sets JtaTransactionManager's "transactionManagerName" property to
 * WebLogic's default JNDI name for its JTA TransactionManager:
 * "javax.transaction.TransactionManager". This can alternatively be achieved
 * through a standard JtaTransactionManager definition with a corresponding
 * "transactionManagerName" property value.
 *
 * <p>Currently tested on BEA WebLogic 8.1 SP2 and 7.0 SP2. Thanks to
 * Eugene Kuleshov for tracking down and reporting the "forceResume" issue!
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setTransactionManagerName
 * @see #DEFAULT_TRANSACTION_MANAGER_NAME
 * @see weblogic.transaction.TransactionManager#forceResume
 */
public class WebLogicJtaTransactionManager extends JtaTransactionManager {

	/**
	 * Default JNDI name of WebLogic's JTA TransactionManager:
	 * "javax.transaction.TransactionManager".
	 * @see #setTransactionManagerName
	 */
	public static final String DEFAULT_TRANSACTION_MANAGER_NAME = "javax.transaction.TransactionManager";

	private static final String TRANSACTION_MANAGER_CLASS_NAME = "weblogic.transaction.TransactionManager";

	private final Method forceResumeMethod;

	/**
	 * This constructor retrieves the WebLogic JTA TransactionManager interface,
	 * so we can invoke the forceResume method using reflection.
	 */
	public WebLogicJtaTransactionManager() {
		setTransactionManagerName(DEFAULT_TRANSACTION_MANAGER_NAME);
		try {
			Class transactionManagerClass = getClass().getClassLoader().loadClass(TRANSACTION_MANAGER_CLASS_NAME);
			this.forceResumeMethod = transactionManagerClass.getMethod("forceResume", new Class[] {Transaction.class});
		}
		catch (Exception ex) {
			throw new TransactionUsageException(
					"Couldn't initialize WebLogicJtaTransactionManager because WebLogic API classes are not available", ex);
		}
	}

	protected void doJtaResume(Transaction suspendedTransaction) throws SystemException {
		try {
			getTransactionManager().resume(suspendedTransaction);
		}
		catch (InvalidTransactionException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Standard JTA resume threw InvalidTransactionException: " + ex.getMessage() +
				    " - trying WebLogic JTA forceResume");
			}
			/*
			weblogic.transaction.TransactionManager wtm =
					(weblogic.transaction.TransactionManager) getTransactionManager();
			wtm.forceResume(suspendedTransaction);
			*/
			try {
				this.forceResumeMethod.invoke(getTransactionManager(), new Object[] {suspendedTransaction});
			}
			catch (Exception ex2) {
				throw new TransactionSystemException("Could not invoke WebLogic's forceResume method", ex2);
			}
		}
	}

}
