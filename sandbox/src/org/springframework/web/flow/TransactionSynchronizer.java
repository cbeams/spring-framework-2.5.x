/*
 * Copyright 2002-2005 the original author or authors.
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
package org.springframework.web.flow;

/**
 * Interface to demarcate an application transaction for a flow session using a
 * token-based mechanism.
 * @author Keith Donald
 */
public interface TransactionSynchronizer {

	/**
	 * Is the active flow session participating in a transaction?
	 * @param clear indicates whether or not the transaction should end after
	 *        checking it
	 * @return True if it is participating in the active transaction, false
	 *         otherwise
	 */
	public boolean inTransaction(boolean clear);

	/**
	 * Assert that the active flow session is participating in a transaction.
	 * @param clear indicates whether or not the transaction should end after
	 *        checking it
	 * @throws IllegalStateException The flow execution is not participating in
	 *         a active transaction
	 */
	public void assertInTransaction(boolean clear) throws IllegalStateException;

	/**
	 * Start a new transaction on the active flow session.
	 */
	public void beginTransaction();

	/**
	 * End the active transaction on the active flow session.
	 */
	public void endTransaction();

}