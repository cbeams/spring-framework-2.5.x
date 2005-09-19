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

package org.springframework.jms.listener.server;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;

import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.support.JmsUtils;

/**
 * @author Juergen Hoeller
 * @since 1.3
 */
public class ServerMessageListenerContainer extends AbstractMessageListenerContainer
		implements ListenerSessionManager {

	private ServerSessionFactory serverSessionFactory = new SimpleServerSessionFactory();

	private int maxMessages = 1;

	private ConnectionConsumer consumer;


	public void setServerSessionFactory(ServerSessionFactory serverSessionFactory) {
		this.serverSessionFactory = serverSessionFactory;
	}

	protected ServerSessionFactory getServerSessionFactory() {
		return serverSessionFactory;
	}

	public void setMaxMessages(int maxMessages) {
		this.maxMessages = maxMessages;
	}

	protected int getMaxMessages() {
		return maxMessages;
	}


	protected void registerListener() throws JMSException {
		Connection con = getConnection();
		Destination destination = getDestination();
		if (destination == null) {
			Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
			try {
				destination = resolveDestinationName(session, getDestinationName());
			}
			finally {
				JmsUtils.closeSession(session);
			}
		}
		ServerSessionPool pool = createServerSessionPool();
		this.consumer = createConsumer(con, destination, pool);
	}

	protected final ConnectionConsumer getConsumer() {
		return consumer;
	}

	protected void destroyListener() throws JMSException {
		logger.debug("Closing ServerSessionFactory");
		this.serverSessionFactory.close(this);
		logger.debug("Closing JMS ConnectionConsumer");
		this.consumer.close();
	}


	protected ServerSessionPool createServerSessionPool() throws JMSException {
		return new ServerSessionPool() {
			public ServerSession getServerSession() throws JMSException {
				logger.debug("JMS ConnectionConsumer requests ServerSession");
				return serverSessionFactory.getServerSession(ServerMessageListenerContainer.this);
			}
		};
	}

	public Session createListenerSession() throws JMSException {
		final Session session = createSession(getConnection());

		session.setMessageListener(new MessageListener() {
			public void onMessage(Message message) {
				executeListener(session, message);
			}
		});

		return session;
	}

	public void executeListenerSession(Session session) {
		session.run();
	}


	/**
	 * Create a JMS ConnectionConsumer for the given Connection.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param con the JMS Connection to create a Session for
	 * @return the new JMS Session
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected ConnectionConsumer createConsumer(Connection con, Destination destination, ServerSessionPool pool)
			throws JMSException {

		return con.createConnectionConsumer(destination, getMessageSelector(), pool, getMaxMessages());
	}

}
