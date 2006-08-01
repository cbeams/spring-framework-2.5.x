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

package org.springframework.jms.connection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Session;

import org.springframework.jms.support.JmsUtils;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

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
public class JmsResourceHolder extends ResourceHolderSupport {

	private final boolean frozen;

	private final List connections = new LinkedList();

	private final List sessions = new LinkedList();


	/**
	 * Create a new JmsResourceHolder that is open for resources to be added.
	 * @see #addConnection
	 * @see #addSession
	 */
	public JmsResourceHolder() {
		this.frozen = false;
	}

	/**
	 * Create a new JmsResourceHolder for the given JMS resources.
	 * @param connection the JMS Connection
	 * @param session the JMS Session
	 */
	public JmsResourceHolder(Connection connection, Session session) {
		addConnection(connection);
		addSession(session);
		this.frozen = true;
	}

	public final boolean isFrozen() {
		return frozen;
	}

	public final void addConnection(Connection connection) {
		Assert.isTrue(!this.frozen, "Cannot add Connection because JmsResourceHolder is frozen");
		Assert.notNull(connection, "Connection must not be null");
		this.connections.add(connection);
	}

	public final void addSession(Session session) {
		Assert.isTrue(!this.frozen, "Cannot add Session because JmsResourceHolder is frozen");
		Assert.notNull(session, "Session must not be null");
		this.sessions.add(session);
	}


	public Connection getConnection() {
		return (!this.connections.isEmpty() ? (Connection) this.connections.get(0) : null);
	}

	public Session getSession() {
		return (!this.sessions.isEmpty() ? (Session)  this.sessions.get(0) : null);
	}

	public Connection getConnection(Class connectionType) {
		return (Connection) CollectionUtils.findValueOfType(this.connections, connectionType);
	}

	public Session getSession(Class sessionType) {
		return (Session) CollectionUtils.findValueOfType(this.sessions, sessionType);
	}


	public void closeAll() {
		for (Iterator it = this.sessions.iterator(); it.hasNext();) {
			JmsUtils.closeSession((Session) it.next());
		}
		for (Iterator it = this.connections.iterator(); it.hasNext();) {
			JmsUtils.closeConnection((Connection) it.next());
		}
	}

}
