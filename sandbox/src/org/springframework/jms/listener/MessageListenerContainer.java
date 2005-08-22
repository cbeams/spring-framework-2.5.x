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

package org.springframework.jms.listener;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.jms.JmsException;
import org.springframework.jms.support.JmsAccessor;
import org.springframework.jms.support.JmsUtils;

/**
 * @author Juergen Hoeller
 * @since 1.3
 */
public class MessageListenerContainer extends JmsAccessor implements DisposableBean, ListenerSessionManager {

	private Destination destination;

	private String messageSelector;

	private Object messageListener;

	private ServerSessionFactory serverSessionFactory = new SimpleServerSessionFactory();

	private int maxMessages = 1;

	private boolean autoStartup = true;

	private Connection connection;

	private ConnectionConsumer consumer;


	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	protected Destination getDestination() {
		return destination;
	}

	public void setMessageSelector(String messageSelector) {
		this.messageSelector = messageSelector;
	}

	protected String getMessageSelector() {
		return messageSelector;
	}

	public void setMessageListener(Object messageListener) {
		checkMessageListener(messageListener);
		this.messageListener = messageListener;
	}

	protected void checkMessageListener(Object messageListener) {
		if (!(messageListener instanceof MessageListener ||
				messageListener instanceof SessionAwareMessageListener)) {
			throw new IllegalArgumentException(
					"messageListener needs to be of type [javax.jms.MessageListener] or " +
					"[org.springframework.jmx.listener.SessionAwareMessageListener]");
		}
	}

	protected Object getMessageListener() {
		return messageListener;
	}

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

	/**
	 * Set whether to automatically start the listener after initialization.
	 * Default is "true"; set this to "false" to allow for manual startup.
	 */
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}


	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		if (this.destination == null) {
			throw new IllegalArgumentException("destination is required");
		}
		if (this.messageListener == null) {
			throw new IllegalArgumentException("messageListener is required");
		}

		// Create JMS Connection and ConnectionConsumer.
		try {
			this.connection = createConnection();
			ServerSessionPool pool = createServerSessionPool();
			this.consumer = createConnectionConsumer(this.connection, pool);

			if (this.autoStartup) {
				this.connection.start();
			}
		}
		catch (JMSException ex) {
			JmsUtils.closeConnection(this.connection);
			throw convertJmsAccessException(ex);
		}
	}

	protected final Connection getConnection() {
		return this.connection;
	}

	protected final ConnectionConsumer getConsumer() {
		return consumer;
	}


	/**
	 * Create a JMS Connection via this template's ConnectionFactory.
	 * <p>This implementation uses JMS 1.1 API.
	 * @return the new JMS Connection
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected Connection createConnection() throws JMSException {
		return getConnectionFactory().createConnection();
	}

	/**
	 * Create a JMS ConnectionConsumer for the given Connection.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param con the JMS Connection to create a Session for
	 * @return the new JMS Session
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected ConnectionConsumer createConnectionConsumer(Connection con, ServerSessionPool pool)
			throws JMSException {

		return con.createConnectionConsumer(getDestination(), getMessageSelector(), pool, getMaxMessages());
	}

	/**
	 * Create a JMS Session for the given Connection.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param con the JMS Connection to create a Session for
	 * @return the new JMS Session
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected Session createSession(Connection con) throws JMSException {
		return con.createSession(isSessionTransacted(), getSessionAcknowledgeMode());
	}

	/**
	 * Return whether the Session is in client acknowledge mode.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param session the JMS Session to check
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected boolean isClientAcknowledge(Session session) throws JMSException {
		return (session.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE);
	}


	protected ServerSessionPool createServerSessionPool() throws JMSException {
		return new ServerSessionPool() {
			public ServerSession getServerSession() throws JMSException {
				logger.debug("JMS ConnectionConsumer requests ServerSession");
				return serverSessionFactory.getServerSession(MessageListenerContainer.this);
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


	protected void executeListener(Session session, Message message) {
		try {
			invokeListener(session, message);
			commitIfNecessary(session, message);
		}
		catch (JMSException ex) {
			rollbackOnExceptionIfNecessary(session, ex);
			convertJmsAccessException(ex);
		}
		catch (RuntimeException ex) {
			rollbackOnExceptionIfNecessary(session, ex);
			throw ex;
		}
		catch (Error err) {
			rollbackOnExceptionIfNecessary(session, err);
			throw err;
		}
	}

	protected void invokeListener(Session session, Message message) throws JMSException {
		if (getMessageListener() instanceof MessageListener) {
			((MessageListener) getMessageListener()).onMessage(message);
		}
		else if (getMessageListener() instanceof SessionAwareMessageListener) {
			((SessionAwareMessageListener) getMessageListener()).onMessage(message, session);
		}
		else {
			throw new IllegalArgumentException("Only MessageListener and SessionAwareMessageListener supported");
		}
	}

	/**
	 * Perform a commit or message acknowledgement, as appropriate.
	 * @param session the JMS Session to commit
	 * @param message the Message to acknowledge
	 * @throws JmsException in case of commit failure
	 */
	protected void commitIfNecessary(Session session, Message message) throws JmsException {
		try {
			// Commit session or acknowledge message.
			if (session.getTransacted()) {
				// Commit necessary - but avoid commit call within a JTA transaction.
				if (isSessionTransacted()) {
					// Transacted session created by this container -> commit.
					JmsUtils.commitIfNecessary(session);
				}
			}
			else if (isClientAcknowledge(session)) {
				message.acknowledge();
			}
		}
		catch (JMSException ex) {
			convertJmsAccessException(ex);
		}
	}

	/**
	 * Perform a rollback, handling rollback exceptions properly.
	 * @param session the JMS Session to rollback
	 * @param ex the thrown application exception or error
	 * @throws JmsException in case of a rollback error
	 */
	protected void rollbackOnExceptionIfNecessary(Session session, Throwable ex) throws JmsException {
		// Transacted session created by this container -> rollback.
		try {
			if (session.getTransacted() && isSessionTransacted()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Initiating transaction rollback on application exception", ex);
				}
				JmsUtils.rollbackIfNecessary(session);
			}
		}
		catch (JMSException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			throw convertJmsAccessException(ex2);
		}
		catch (RuntimeException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			throw ex2;
		}
		catch (Error err) {
			logger.error("Application exception overridden by rollback error", ex);
			throw err;
		}
	}


	public void start() throws JmsException {
		try {
			this.connection.start();
		}
		catch (JMSException ex) {
			throw convertJmsAccessException(ex);
		}
	}

	public void stop() throws JmsException {
		try {
			this.connection.stop();
		}
		catch (JMSException ex) {
			throw convertJmsAccessException(ex);
		}
	}


	public void destroy() throws JMSException {
		logger.debug("Closing ServerSessionFactory");
		this.serverSessionFactory.close(this);
		logger.debug("Closing JMS ConnectionConsumer and Connection");
		this.consumer.close();
		this.connection.close();
	}

}
