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

package org.springframework.jms.connection;

import org.springframework.transaction.support.SmartTransactionObject;

/**
 * JMS transaction object, representing a ConnectionHolder.
 * Used as transaction object by JmsTransactionManager.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see JmsTransactionManager
 */
public class JmsTransactionObject implements SmartTransactionObject {

	private ConnectionHolder connectionHolder;

	protected void setConnectionHolder(ConnectionHolder connectionHolder) {
		this.connectionHolder = connectionHolder;
	}

	public ConnectionHolder getConnectionHolder() {
		return connectionHolder;
	}

	public boolean isRollbackOnly() {
		return getConnectionHolder().isRollbackOnly();
	}

}
