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
 * <p>Will work out-of-the-box on BEA WebLogic 8.1 and higher (tested on 8.1 SP2).
 * On WebLogic 7.0 SP2, a "forceResume" call on the TransactionManager reference
 * obtained from JNDI unfortunately fails with a mysterious, WebLogic-internal
 * NullPointerException. (Thanks to Eugene Kuleshov and Dmitri Maximovich for
 * tracking down and reporting this issue!)
 *
 * <p>The solution for WebLogic 7.0 is to wire the "transactionManager" property
 * with a WebLogicServerTransactionManagerFactoryBean. This factory bean provides
 * a reference to the ServerTransactionManagerImpl via WebLogic's TxHelper class.
 * This has been tested on WebLogic 7.0 SP5. The TxHelper lookup is available on
 * WebLogic 8.1, but deprecated - so we recommend the default JNDI lookup there.
 *
 * <pre>
 * &lt;bean id="wlsTm" class="org.springframework.transaction.jta.WebLogicServerTransactionManagerFactoryBean"/&gt;
 *
 * &lt;bean id="transactionManager" class="org.springframework.transaction.jta.WebLogicJtaTransactionManager"&gt;
 *   &lt;property name="transactionManager"&gt;&lt;ref local="wlsTm"/&gt;&lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setTransactionManagerName
 * @see #DEFAULT_TRANSACTION_MANAGER_NAME
 * @see WebLogicServerTransactionManagerFactoryBean
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
			Class transactionManagerClass =
			    getClass().getClassLoader().loadClass(TRANSACTION_MANAGER_CLASS_NAME);
			this.forceResumeMethod =
			    transactionManagerClass.getMethod("forceResume", new Class[] {Transaction.class});
		}
		catch (Exception ex) {
			throw new TransactionUsageException(
					"Couldn't initialize WebLogicJtaTransactionManager because WebLogic API classes are not available",
			    ex);
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
