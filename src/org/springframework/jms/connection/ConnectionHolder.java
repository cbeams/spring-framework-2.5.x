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

import javax.jms.Connection;
import javax.jms.Session;

import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * Connection holder, wrapping a JMS Connection and a JMS Session.
 * JmsTransactionManager binds instances of this class to the thread,
 * for a given JMS ConnectionFactory.
 *
 * <p>Note: This is an SPI class, not intended to be used by applications.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see JmsTransactionManager
 * @see org.springframework.jms.core.JmsTemplate
 */
public class ConnectionHolder extends ResourceHolderSupport {

	private final Connection connection;

	private final Session session;

	public ConnectionHolder(Connection connection, Session session) {
		this.connection = connection;
		this.session = session;
	}

	public Connection getConnection() {
		return connection;
	}

	public Session getSession() {
		return session;
	}

}
