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
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.jms.JmsException;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.jms.support.destination.JmsDestinationAccessor;

/**
 * @author Juergen Hoeller
 * @since 1.3
 */
public abstract class AbstractMessageListenerContainer extends JmsDestinationAccessor implements DisposableBean {

	private Object destination;

	private String messageSelector;

	private Object messageListener;

	private boolean exposeListenerSession = true;

	private boolean autoStartup = true;

	private Connection connection;


	protected AbstractMessageListenerContainer() {
		setDestinationResolver(new DynamicDestinationResolver());
	}


	/**
	 * Set the destination to be used on send operations that do not
	 * have a destination parameter.
	 * <p>Alternatively, specify a "destinationName", to be dynamically
	 * resolved via the DestinationResolver.
	 * @see #setDestinationName(String)
	 */
	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	/**
	 * Return the destination to be used on send operations that do not
	 * have a destination parameter.
	 */
	protected Destination getDestination() {
		return (this.destination instanceof Destination ? (Destination) this.destination : null);
	}

	/**
	 * Set the destination name to be used on send operations that do not
	 * have a destination parameter. The specified name will be dynamically
	 * resolved via the DestinationResolver.
	 * <p>Alternatively, specify a JMS Destination object as "destination".
	 * @see #setDestination(javax.jms.Destination)
	 */
	public void setDestinationName(String destinationName) {
		this.destination = destinationName;
	}

	/**
	 * Return the destination name to be used on send operations that do not
	 * have a destination parameter.
	 */
	protected String getDestinationName() {
		return (this.destination instanceof String ? (String) this.destination : null);
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

	public void setExposeListenerSession(boolean exposeListenerSession) {
		this.exposeListenerSession = exposeListenerSession;
	}

	protected boolean isExposeListenerSession() {
		return exposeListenerSession;
	}

	/**
	 * Set whether to automatically start the listener after initialization.
	 * Default is "true"; set this to "false" to allow for manual startup.
	 */
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}


	public final void afterPropertiesSet() {
		super.afterPropertiesSet();

		if (this.destination == null) {
			throw new IllegalArgumentException("destination or destinationName is required");
		}
		if (this.messageListener == null) {
			throw new IllegalArgumentException("messageListener is required");
		}

		// Create JMS Connection and Sessions with MessageConsumers.
		try {
			this.connection = createConnection();
			registerListener(this.connection);

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


	public final void destroy() throws JMSException {
		destroyListener();
		logger.debug("Closing JMS Connection");
		this.connection.close();
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
			invokeSessionAwareListener(session, message);
		}
		else {
			throw new IllegalArgumentException("Only MessageListener and SessionAwareMessageListener supported");
		}
	}

	protected void invokeSessionAwareListener(Session session, Message message) throws JMSException {
		Session sessionToExpose = session;
		Connection con = null;
		try {
			if (!isExposeListenerSession()) {
				con = createConnection();
				sessionToExpose = createSession(con);
			}
			((SessionAwareMessageListener) getMessageListener()).onMessage(message, sessionToExpose);
			if (con != null) {
				if (session.getTransacted() && isSessionTransacted()) {
					// Transacted session created by this container -> commit.
					JmsUtils.commitIfNecessary(session);
				}
			}
		}
		finally {
			if (con != null) {
				JmsUtils.closeSession(sessionToExpose);
				JmsUtils.closeConnection(con);
			}
		}
	}

	/**
	 * Perform a commit or message acknowledgement, as appropriate.
	 * @param session the JMS Session to commit
	 * @param message the Message to acknowledge
	 * @throws org.springframework.jms.JmsException in case of commit failure
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
	 * @throws org.springframework.jms.JmsException in case of a rollback error
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


	/**
	 * Create a JMS Connection via this template's ConnectionFactory.
	 * <p>This implementation uses JMS 1.1 API.
	 * @return the new JMS Connection
	 * @throws javax.jms.JMSException if thrown by JMS API methods
	 */
	protected Connection createConnection() throws JMSException {
		return getConnectionFactory().createConnection();
	}

	/**
	 * Create a JMS Session for the given Connection.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param con the JMS Connection to create a Session for
	 * @return the new JMS Session
	 * @throws javax.jms.JMSException if thrown by JMS API methods
	 */
	protected Session createSession(Connection con) throws JMSException {
		return con.createSession(isSessionTransacted(), getSessionAcknowledgeMode());
	}

	/**
	 * Return whether the Session is in client acknowledge mode.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param session the JMS Session to check
	 * @throws javax.jms.JMSException if thrown by JMS API methods
	 */
	protected boolean isClientAcknowledge(Session session) throws JMSException {
		return (session.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE);
	}


	protected abstract void registerListener(Connection con) throws JMSException;

	protected abstract void destroyListener() throws JMSException;

}
