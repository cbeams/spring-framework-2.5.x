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

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.SmartTransactionObject;

/**
 * JTA transaction object, representing a UserTransaction.
 * Used as transaction object by JtaTransactionManager.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see JtaTransactionManager
 * @see javax.transaction.UserTransaction
 */
public class JtaTransactionObject implements SmartTransactionObject {

	private final UserTransaction userTransaction;

	public JtaTransactionObject(UserTransaction userTransaction) {
		this.userTransaction = userTransaction;
	}

	public UserTransaction getUserTransaction() {
		return userTransaction;
	}

	public boolean isRollbackOnly() {
		try {
			return (getUserTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK);
		}
		catch (SystemException ex) {
			throw new TransactionSystemException("JTA failure on getStatus", ex);
		}
	}

}
