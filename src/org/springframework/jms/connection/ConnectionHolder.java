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

package org.springframework.jms.connection;

import javax.jms.Connection;
import javax.jms.Session;

import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;

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


	/**
	 * Create a new ConnectionHolder for the given JMS resources.
	 * @param connection the JMS Connection
	 * @param session the JMS Session
	 */
	public ConnectionHolder(Connection connection, Session session) {
		Assert.notNull(connection, "Connection must not be null");
		Assert.notNull(session, "Session must not be null");
		this.connection = connection;
		this.session = session;
	}

	/**
	 * Return this holder's JMS Connection.
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Return this holder's JMS Session.
	 */
	public Session getSession() {
		return session;
	}

}
