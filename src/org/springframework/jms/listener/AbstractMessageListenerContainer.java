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
 * Abstract base class for message listener containers.
 * Cam either host a standard JMS MessageListener or a
 * Spring SessionAwareMessageListener.
 *
 * <p>Holds a single JMS Connection that all listeners are
 * supposed to be registered on. The actual registration
 * process is up to concrete subclasses.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setMessageListener
 * @see javax.jms.MessageListener
 * @see SessionAwareMessageListener
 * @see DefaultMessageListenerContainer
 * @see SimpleMessageListenerContainer
 * @see org.springframework.jms.listener.serversession.ServerSessionMessageListenerContainer
 */
public abstract class AbstractMessageListenerContainer extends JmsDestinationAccessor implements DisposableBean {

	private Object destination;

	private String messageSelector;

	private Object messageListener;

	private boolean exposeListenerSession = true;

	private boolean autoStartup = true;

	private Connection connection;

	private boolean running;

	private boolean active;


	/**
	 * Create a new AbstractMessageListenerContainer,
	 * with a DynamicDestinationResolver as default DestinationResolver.
	 */
	protected AbstractMessageListenerContainer() {
		setDestinationResolver(new DynamicDestinationResolver());
	}


	/**
	 * Set the destination to receive messages from.
	 * <p>Alternatively, specify a "destinationName", to be dynamically
	 * resolved via the DestinationResolver.
	 * @see #setDestinationName(String)
	 */
	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	/**
	 * Return the destination to receive messages from.
	 */
	protected Destination getDestination() {
		return (this.destination instanceof Destination ? (Destination) this.destination : null);
	}

	/**
	 * Set the name of the destination to receive messages from. The specified
	 * name will be dynamically resolved via the DestinationResolver.
	 * <p>Alternatively, specify a JMS Destination object as "destination".
	 * @see #setDestination(javax.jms.Destination)
	 */
	public void setDestinationName(String destinationName) {
		this.destination = destinationName;
	}

	/**
	 * Return the name of the destination to receive messages from.
	 */
	protected String getDestinationName() {
		return (this.destination instanceof String ? (String) this.destination : null);
	}

	/**
	 * Set the JMS message selector expression (or <code>null</code> if none).
	 * Default is none.
	 * <p>See the JMS specification for a detailed definition of selector expressions.
	 */
	public void setMessageSelector(String messageSelector) {
		this.messageSelector = messageSelector;
	}

	/**
	 * Return the JMS message selector expression (or <code>null</code> if none).
	 */
	protected String getMessageSelector() {
		return messageSelector;
	}


	/**
	 * Set the message listener implementation to register.
	 * This can be either a standard JMS MessageListener object
	 * or a Spring SessionAwareMessageListener object.
	 * @see javax.jms.MessageListener
	 * @see SessionAwareMessageListener
	 */
	public void setMessageListener(Object messageListener) {
		checkMessageListener(messageListener);
		this.messageListener = messageListener;
	}

	/**
	 * Check the given message listener, throwing an exception
	 * if it does not correspond to a supported listener type.
	 * <p>By default, only a standard JMS MessageListener object or a
	 * Spring SessionAwareMessageListener object will be accepted.
	 * @param messageListener the message listener object to check
	 * @see javax.jms.MessageListener
	 * @see SessionAwareMessageListener
	 */
	protected void checkMessageListener(Object messageListener) {
		if (!(messageListener instanceof MessageListener ||
				messageListener instanceof SessionAwareMessageListener)) {
			throw new IllegalArgumentException(
					"messageListener needs to be of type [javax.jms.MessageListener] or " +
					"[org.springframework.jmx.listener.SessionAwareMessageListener]");
		}
	}

	/**
	 * Return the message listener object to register.
	 */
	protected Object getMessageListener() {
		return messageListener;
	}

	/**
	 * Set whether to expose the listener JMS Session to a registered
	 * SessionAwareMessageListener. Default is "true", reusing the listener's
	 * Session.
	 * <p>Turn this off to expose a fresh JMS Session fetched from the same
	 * underlying JMS Connection instead, which might be necessary on some
	 * JMS providers.
	 * @see SessionAwareMessageListener
	 */
	public void setExposeListenerSession(boolean exposeListenerSession) {
		this.exposeListenerSession = exposeListenerSession;
	}

	/**
	 * Return whether to expose the listener JMS Session to a registered
	 * SessionAwareMessageListener.
	 */
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


	/**
	 * Create a JMS Connection, register the given listener object,
	 * and start the Connection (if "autoStartup" hasn't been turned off).
	 */
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
			this.active = true;
			this.connection = createConnection();
			registerListener();

			if (this.autoStartup) {
				this.connection.start();
			}
		}
		catch (JMSException ex) {
			JmsUtils.closeConnection(this.connection);
			this.active = false;
			throw convertJmsAccessException(ex);
		}
	}

	/**
	 * Return the JMS Connection used by this message listener container.
	 * Available after initialization.
	 */
	protected final Connection getConnection() {
		return this.connection;
	}

	/**
	 * Destroy the registered listener object and close this
	 * listener container.
	 * <p>The underlying JMS Connection will receive a <code>close</code> call.
	 * @throws JmsException if shutdown failed
	 * @see #destroyListener()
	 * @see javax.jms.Connection#close()
	 */
	public final void destroy() throws JmsException {
		try {
			this.active = false;
			destroyListener();
			logger.debug("Closing JMS Connection");
			this.connection.close();
		}
		catch (JMSException ex) {
			JmsUtils.closeConnection(this.connection);
			throw convertJmsAccessException(ex);
		}
	}


	//-------------------------------------------------------------------------
	// Lifecycle methods for dynamically starting and stopping the listener
	//-------------------------------------------------------------------------

	/**
	 * Start this listener container.
	 * <p>The underlying JMS Connection will receive a <code>start</code> call.
	 * @throws JmsException if starting failed
	 * @see javax.jms.Connection#start()
	 */
	public synchronized void start() throws JmsException {
		try {
			this.connection.start();
		}
		catch (JMSException ex) {
			throw convertJmsAccessException(ex);
		}
		this.running = true;
	}

	/**
	 * Stop this listener container.
	 * <p>The underlying JMS Connection will receive a <code>stop</code> call.
	 * @throws JmsException if stopping failed
	 * @see javax.jms.Connection#stop()
	 */
	public synchronized void stop() throws JmsException {
		try {
			this.connection.stop();
		}
		catch (JMSException ex) {
			throw convertJmsAccessException(ex);
		}
		this.running = false;
	}

	/**
	 * Return whether this listener container is currently running,
	 * that is, whether it has been started and not stopped yet.
	 */
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * Return whether this listener container is currently active,
	 * that is, whether it has been set up and not destroyed yet.
	 */
	public boolean isActive() {
		return this.active;
	}



	//-------------------------------------------------------------------------
	// Template methods for listener execution
	//-------------------------------------------------------------------------

	/**
	 * Execute the specified listener,
	 * committing or rolling back the transaction afterwards (if necessary).
	 * @param session the JMS Session to work on
	 * @param message the received JMS Message
	 * @throws org.springframework.jms.JmsException if listener execution failed
	 * @see #invokeListener(javax.jms.Session, javax.jms.Message)
	 * @see #commitIfNecessary
	 * @see #rollbackOnExceptionIfNecessary
	 * @see #convertJmsAccessException
	 */
	protected void executeListener(Session session, Message message) throws JmsException {
		try {
			doExecuteListener(session, message);
		}
		catch (JMSException ex) {
			convertJmsAccessException(ex);
		}
	}

	/**
	 * Execute the specified listener,
	 * committing or rolling back the transaction afterwards (if necessary).
	 * @param session the JMS Session to work on
	 * @param message the received JMS Message
	 * @throws JMSException if thrown by JMS API methods
	 * @see #invokeListener(javax.jms.Session, javax.jms.Message)
	 * @see #commitIfNecessary
	 * @see #rollbackOnExceptionIfNecessary
	 * @see #convertJmsAccessException
	 */
	protected void doExecuteListener(Session session, Message message) throws JMSException {
		try {
			invokeListener(session, message);
		}
		catch (JMSException ex) {
			rollbackOnExceptionIfNecessary(session, ex);
			throw ex;
		}
		catch (RuntimeException ex) {
			rollbackOnExceptionIfNecessary(session, ex);
			throw ex;
		}
		catch (Error err) {
			rollbackOnExceptionIfNecessary(session, err);
			throw err;
		}
		commitIfNecessary(session, message);
	}

	/**
	 * Invoke the specified listener: either as standard JMS MessageListener
	 * or as Spring SessionAwareMessageListener
	 * @param session the JMS Session to work on
	 * @param message the received JMS Message
	 * @throws JMSException if thrown by JMS API methods
	 * @see #setMessageListener
	 * @see #invokeListener(javax.jms.MessageListener, javax.jms.Message)
	 */
	protected void invokeListener(Session session, Message message) throws JMSException {
		if (getMessageListener() instanceof MessageListener) {
			((MessageListener) getMessageListener()).onMessage(message);
		}
		else if (getMessageListener() instanceof SessionAwareMessageListener) {
			invokeListener((SessionAwareMessageListener) getMessageListener(), session, message);
		}
		else {
			throw new IllegalArgumentException("Only MessageListener and SessionAwareMessageListener supported");
		}
	}

	/**
	 * Invoke the specified listener as standard JMS MessageListener.
	 * <p>Default implementation performs a plain invocation of the
	 * <code>onMessage</code> method.
	 * @param listener the JMS MessageListener to invoke
	 * @param message the received JMS Message
	 * @throws JMSException if thrown by JMS API methods
	 * @see javax.jms.MessageListener#onMessage
	 */
	protected void invokeListener(MessageListener listener, Message message) throws JMSException {
		listener.onMessage(message);
	}

	/**
	 * Invoke the specified listener as Spring SessionAwareMessageListener,
	 * exposing a new JMS Session (potentially with its own transaction)
	 * to the listener if demanded.
	 * @param listener the Spring SessionAwareMessageListener to invoke
	 * @param session the JMS Session to work on
	 * @param message the received JMS Message
	 * @throws JMSException if thrown by JMS API methods
	 * @see SessionAwareMessageListener
	 * @see #setExposeListenerSession
	 */
	protected void invokeListener(SessionAwareMessageListener listener, Session session, Message message)
			throws JMSException {

		Session sessionToExpose = session;
		Connection con = null;
		try {
			if (!isExposeListenerSession()) {
				con = createConnection();
				sessionToExpose = createSession(con);
			}
			((SessionAwareMessageListener) getMessageListener()).onMessage(message, sessionToExpose);
			if (sessionToExpose != session) {
				if (sessionToExpose.getTransacted() && isSessionTransacted()) {
					// Transacted session created by this container -> commit.
					JmsUtils.commitIfNecessary(sessionToExpose);
				}
			}
		}
		finally {
			if (sessionToExpose != session) {
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


	//-------------------------------------------------------------------------
	// Template methods to be implemented by subclasses
	//-------------------------------------------------------------------------

	/**
	 * Register the specified listener on the underlying JMS Connection.
	 * <p>Subclasses need to implement this method for their specific
	 * listener management process.
	 * @throws JMSException if registration failed
	 * @see #getMessageListener()
	 * @see #getConnection()
	 */
	protected abstract void registerListener() throws JMSException;

	/**
	 * Destroy the registered listener.
	 * The JMS Connection will automatically be closed <i>afterwards</i>
	 * <p>Subclasses need to implement this method for their specific
	 * listener management process.
	 * @throws JMSException if destruction failed
	 */
	protected abstract void destroyListener() throws JMSException;


	//-------------------------------------------------------------------------
	// JMS 1.1 factory methods, potentially overridden for JMS 1.0.2
	//-------------------------------------------------------------------------

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

}
