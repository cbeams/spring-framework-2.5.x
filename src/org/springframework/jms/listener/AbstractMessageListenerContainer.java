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

package org.springframework.jms.listener;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;
import org.springframework.jms.JmsException;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.jms.support.destination.JmsDestinationAccessor;

/**
 * Abstract base class for message listener containers. Can either host
 * a standard JMS {@link MessageListener} or a Spring-specific
 * {@link SessionAwareMessageListener}.
 *
 * <p>Holds a single JMS {@link Connection} that all listeners are supposed to be
 * registered on. The actual registration process is up to concrete subclasses.
 *
 * <p><b>NOTE:</b> The default behavior of this message listener container
 * is to <b>never</b> propagate an exception thrown by a message listener up to
 * the JMS provider. Instead, it will log any such exception at the error level.
 * This means that from the perspective of the attendant JMS provider no such
 * listener will ever fail.
 *
 * <p>The listener container offers the following message acknowledgment options:
 * <ul>
 * <li>"sessionAcknowledgeMode" set to "AUTO_ACKNOWLEDGE" (default):
 * Automatic message acknowledgment <i>before</i> listener execution;
 * no redelivery in case of exception thrown.
 * <li>"sessionAcknowledgeMode" set to "CLIENT_ACKNOWLEDGE":
 * Automatic message acknowledgment <i>after</i> successful listener execution;
 * no redelivery in case of exception thrown.
 * <li>"sessionAcknowledgeMode" set to "DUPS_OK_ACKNOWLEDGE":
 * <i>Lazy</i> message acknowledgment during or after listener execution;
 * <i>potential redelivery</i> in case of exception thrown.
 * <li>"sessionTransacted" set to "true":
 * Transactional acknowledgment after successful listener execution;
 * <i>guaranteed redelivery</i> in case of exception thrown.
 * </ul>
 * The exact behavior might vary according to the concrete listener container
 * and JMS provider used.
 *
 * <p>Note that there is a corner case when using "sessionTransacted",
 * where the listener might have returned successfully but the server
 * died before acknowledging the message. As a consequence, a message
 * <i>might get redelivered even after successful processing</i> -
 * potentially leading to duplicate processing of the message.
 * This violates "exactly-once" semantics, at least potentially.
 *
 * <p>There are two solutions to the duplicate processing problem:
 * <ul>
 * <li>Either add <i>duplicate message detection</i> to your listener, in the
 * form of a business entity existence check or a protocol table check. This
 * usually just needs to be done in case of the JMSRedelivered flag being
 * set on the incoming message (else just process straightforwardly).
 * <li>Or wrap the <i>entire processing with an XA transaction</i>, covering the
 * reception of the message as well as the execution of the message listener.
 * This is only supported by {@link DefaultMessageListenerContainer}, through
 * specifying a "transactionManager" (typically a
 * {@link org.springframework.transaction.jta.JtaTransactionManager}, with
 * a corresponding XA-aware JMS {@link javax.jms.ConnectionFactory} passed in as
 * "connectionFactory").
 * </ul>
 * Note that XA transaction coordination adds significant runtime overhead,
 * so it might be feasible to avoid it unless absolutely necessary.
 *
 * <p><b>Recommendations:</b>
 * <ul>
 * <li>The general recommendation is to set "sessionTransacted" to "true",
 * typically in combination with local database transactions triggered by the
 * listener implementation, through Spring's standard transaction facilities.
 * This will work nicely in Tomcat or in a standalone environment, often
 * combined with custom duplicate message detection (if it is unacceptable
 * to ever process the same message twice).
 * <li>Alternatively, specify a
 * {@link org.springframework.transaction.jta.JtaTransactionManager} as "transactionManager"
 * for a full XA-aware JMS provider - typically when running on a J2EE server,
 * but also for other environments with a JTA transaction manager present.
 * This will give full "exactly-once" guarantees without custom duplicate
 * message checks, at the price of additional runtime processing overhead.
 * </ul>
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setMessageListener
 * @see javax.jms.MessageListener
 * @see SessionAwareMessageListener
 * @see #handleListenerException
 * @see DefaultMessageListenerContainer
 * @see SimpleMessageListenerContainer
 * @see org.springframework.jms.listener.serversession.ServerSessionMessageListenerContainer
 */
public abstract class AbstractMessageListenerContainer extends JmsDestinationAccessor
		implements DisposableBean, Lifecycle {

	private Object destination;

	private String messageSelector;

	private Object messageListener;

	private ExceptionListener exceptionListener;

	private boolean exposeListenerSession = true;

	private boolean autoStartup = true;

	private Connection connection;

	private boolean running = false;

	private volatile boolean active = false;


	/**
	 * Create a new {@link AbstractMessageListenerContainer} ,
	 * using a {@link DynamicDestinationResolver} as the default
     * {@link org.springframework.jms.support.destination.DestinationResolver}.
	 */
	public AbstractMessageListenerContainer() {
		setDestinationResolver(new DynamicDestinationResolver());
	}


	/**
	 * Set the destination to receive messages from.
	 * <p>Alternatively, specify a "destinationName", to be dynamically
	 * resolved via the {@link org.springframework.jms.support.destination.DestinationResolver}.
	 * @see #setDestinationName(String)
	 */
	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	/**
	 * Return the destination to receive messages from.
     * @return the destination to receive messages from (will be 
     * <code>null</code> if the configured destination is not an actual
     * {@link Destination} type; c.f. {@link #setDestinationName(String)
     * when the destination is a String})
	 */
	protected Destination getDestination() {
		return (this.destination instanceof Destination ? (Destination) this.destination : null);
	}

	/**
	 * Set the name of the destination to receive messages from.
     * <p>The specified name will be dynamically resolved via the configured
     * {@link #setDestinationResolver(org.springframework.jms.support.destination.DestinationResolver) destination resolver}.
	 * <p>Alternatively, specify a JMS {@link Destination} object as "destination".
     * @param destinationName the desired destination (can be <code>null</code>) 
	 * @see #setDestination(javax.jms.Destination)
	 */
	public void setDestinationName(String destinationName) {
		this.destination = destinationName;
	}

	/**
	 * Return the name of the destination to receive messages from.
     * @return the name of the destination to receive messages from
     * (will be <code>null</code> if the configured destination is not a
     * {@link String} type; c.f. {@link #setDestination(Destination) when
     * it is an actual Destination})
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
	 * This can be either a standard JMS {@link MessageListener} object
	 * or a Spring {@link SessionAwareMessageListener} object.
     * @throws IllegalArgumentException if the supplied listener is not a 
     * {@link MessageListener} or a {@link SessionAwareMessageListener}
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
	 * <p>By default, only a standard JMS {@link MessageListener} object or a
	 * Spring {@link SessionAwareMessageListener} object will be accepted.
	 * @param messageListener the message listener object to check
     * @throws IllegalArgumentException if the supplied listener is not a 
     * {@link MessageListener} or a {@link SessionAwareMessageListener}
	 * @see javax.jms.MessageListener
	 * @see SessionAwareMessageListener
	 */
	protected void checkMessageListener(Object messageListener) {
		if (!(messageListener instanceof MessageListener ||
				messageListener instanceof SessionAwareMessageListener)) {
            throw new IllegalArgumentException(
                    "messageListener needs to be of type [" +
                            MessageListener.class.getName() + "] or [" +
                            SessionAwareMessageListener.class.getName() + "]");
		}
	}

	/**
	 * Return the message listener object to register.
	 */
	protected Object getMessageListener() {
		return messageListener;
	}

	/**
	 * Set the JMS ExceptionListener to notify in case of a JMSException
	 * thrown by the registered message listener.
	 */
	public void setExceptionListener(ExceptionListener exceptionListener) {
		this.exceptionListener = exceptionListener;
	}

	/**
	 * Return the JMS ExceptionListener to notify in case of a JMSException
	 * thrown by the registered message listener, if any.
     * @return the JMS ExceptionListener to notify in case of a JMSException
	 * thrown by the registered message listener; may be <code>null</code> 
	 */
	protected ExceptionListener getExceptionListener() {
		return this.exceptionListener;
	}

	/**
	 * Set whether to expose the listener JMS Session to a registered
	 * {@link SessionAwareMessageListener}. Default is "true", reusing
     * the listener's {@link Session}.
	 * <p>Turn this off to expose a fresh JMS Session fetched from the same
	 * underlying JMS {@link Connection} instead, which might be necessary
     * on some JMS providers.
	 * @see SessionAwareMessageListener
	 */
	public void setExposeListenerSession(boolean exposeListenerSession) {
		this.exposeListenerSession = exposeListenerSession;
	}

	/**
	 * Return whether to expose the listener JMS {@link Session} to a
     * registered {@link SessionAwareMessageListener}.
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
	 * Validate configuration and call {@link #initialize()}.
	 * @see #initialize()
	 */
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		if (this.destination == null) {
			throw new IllegalArgumentException("destination or destinationName is required");
		}
		if (this.messageListener == null) {
			throw new IllegalArgumentException("messageListener is required");
		}

		initialize();
	}

	/**
	 * Initialize this message listener container.
	 * <p>Creates a JMS Connection, registers the
     * {@link #setMessageListener(Object) given listener object},
	 * and starts the {@link Connection}
     * (if {@link #setAutoStartup(boolean) "autoStartup"} hasn't been turned off).
	 */
	public void initialize() {
		try {
			this.active = true;
			this.connection = createConnection();
			registerListener();

			if (this.autoStartup) {
				this.connection.start();
				this.running = true;
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
	public void destroy() throws JmsException {
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
			this.running = true;
		}
		catch (javax.jms.IllegalStateException ignoreSinceAlreadyStarted) {
		}
		catch (JMSException ex) {
			throw convertJmsAccessException(ex);
		}
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
			this.running = false;
		}
		catch (javax.jms.IllegalStateException ignoreSinceNotYetStarted) {
		}
		catch (JMSException ex) {
			throw convertJmsAccessException(ex);
		}
	}

	/**
	 * Return whether this listener container is currently running,
	 * that is, whether it has been started and not stopped yet.
	 */
	public final synchronized boolean isRunning() {
		return this.running;
	}

	/**
	 * Return whether this listener container is currently active,
	 * that is, whether it has been set up and not destroyed yet.
	 */
	public final boolean isActive() {
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
	 * @see #invokeListener(javax.jms.Session, javax.jms.Message)
	 * @see #commitIfNecessary
	 * @see #rollbackOnExceptionIfNecessary
	 * @see #handleListenerException
	 */
	protected void executeListener(Session session, Message message) {
		try {
			doExecuteListener(session, message);
		}
		catch (Throwable ex) {
			handleListenerException(ex);
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
			ExceptionListener exceptionListener = getExceptionListener();
			if (exceptionListener != null) {
				exceptionListener.onException(ex);
			}
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
	 * or (preferably) as Spring SessionAwareMessageListener.
	 * @param session the JMS Session to work on
	 * @param message the received JMS Message
	 * @throws JMSException if thrown by JMS API methods
	 * @see #setMessageListener
	 * @see #invokeListener(javax.jms.MessageListener, javax.jms.Message)
	 */
	protected void invokeListener(Session session, Message message) throws JMSException {
		if (getMessageListener() instanceof SessionAwareMessageListener) {
			invokeListener((SessionAwareMessageListener) getMessageListener(), session, message);
		}
		else if (getMessageListener() instanceof MessageListener) {
			((MessageListener) getMessageListener()).onMessage(message);
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
		try {
			if (session.getTransacted() && isSessionTransacted()) {
				// Transacted session created by this container -> rollback.
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
	 * Handle the given exception that arose during listener execution.
	 * <p>The default implementation logs the exception at error level,
	 * not propagating it to the JMS provider - assuming that all handling of
	 * acknowledgement and/or transactions is done by this listener container.
	 * This can be overridden in subclasses.
	 * @param ex the exception to handle
	 */
	protected void handleListenerException(Throwable ex) {
		logger.error("Execution of JMS message listener failed", ex);
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
