/*
 * Copyright 2002-2007 the original author or authors.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;
import org.springframework.jms.JmsException;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.JmsDestinationAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Abstract base class for message listener containers. Can either host
 * a standard JMS {@link MessageListener} or a Spring-specific
 * {@link SessionAwareMessageListener}.
 *
 * <p>Usually holds a single JMS {@link Connection} that all listeners are
 * supposed to be registered on, which is the standard JMS way of managing
 * listeners. Can alternatively also be used with a fresh Connection per
 * listener, for J2EE-style XA-aware JMS messaging. The actual registration
 * process is up to concrete subclasses.
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

	private String clientId;

	private Object destination;

	private String messageSelector;

	private Object messageListener;

	private boolean subscriptionDurable = false;

	private String durableSubscriptionName;

	private ExceptionListener exceptionListener;

	private boolean exposeListenerSession = true;

	private boolean autoStartup = true;

	private boolean acceptMessagesWhileStopping = false;

	private Connection sharedConnection;

	private final Object sharedConnectionMonitor = new Object();

	private boolean active = false;

	private boolean running = false;

	private final List pausedTasks = new LinkedList();

	private final Object lifecycleMonitor = new Object();


	/**
	 * Specify the JMS client id for a shared Connection created and used
	 * by this messager listener container.
	 * <p>Note that client ids need to be unique among all active Connections
	 * of the underlying JMS provider. Furthermore, a client id can only be
	 * assigned if the original ConnectionFactory hasn't already assigned one.
	 * @see javax.jms.Connection#setClientID
	 * @see #setConnectionFactory
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * Return the JMS client ID for the shared Connection created and used
	 * by this messager listener container, if any.
	 */
	protected String getClientId() {
		return this.clientId;
	}

	/**
	 * Set the destination to receive messages from.
	 * <p>Alternatively, specify a "destinationName", to be dynamically
	 * resolved via the {@link org.springframework.jms.support.destination.DestinationResolver}.
	 * @see #setDestinationName(String)
	 */
	public void setDestination(Destination destination) {
		Assert.notNull(destination, "destination must not be null");
		this.destination = destination;
		if (destination instanceof Topic && !(destination instanceof Queue)) {
			// Clearly a Topic: let's se the "pubSubDomain" flag.
			setPubSubDomain(true);
		}
	}

	/**
	 * Return the destination to receive messages from. Will be <code>null</code>
	 * if the configured destination is not an actual {@link Destination} type;
	 * c.f. {@link #setDestinationName(String) when the destination is a String}.
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
		Assert.notNull(destinationName, "destinationName must not be null");
		this.destination = destinationName;
	}

	/**
	 * Return the name of the destination to receive messages from.
	 * Will be <code>null</code> if the configured destination is not a
	 * {@link String} type; c.f. {@link #setDestination(Destination) when
	 * it is an actual Destination}.
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
		return this.messageSelector;
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
		if (this.durableSubscriptionName == null) {
			// Use message listener class name as default name for a durable subscription.
			this.durableSubscriptionName = messageListener.getClass().getName();
		}
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
					"messageListener needs to be of type [" + MessageListener.class.getName() +
					"] or [" + SessionAwareMessageListener.class.getName() + "]");
		}
	}

	/**
	 * Return the message listener object to register.
	 */
	protected Object getMessageListener() {
		return this.messageListener;
	}

	/**
	 * Set whether to make the subscription durable. The durable subscription name
	 * to be used can be specified through the "durableSubscriptionName" property.
	 * <p>Default is "false". Set this to "true" to register a durable subscription,
	 * typically in combination with a "durableSubscriptionName" value (unless
	 * your message listener class name is good enough as subscription name).
	 * <p>Only makes sense when listening to a topic (pub-sub domain).
	 * @see #setDurableSubscriptionName
	 */
	public void setSubscriptionDurable(boolean subscriptionDurable) {
		this.subscriptionDurable = subscriptionDurable;
	}

	/**
	 * Return whether to make the subscription durable.
	 */
	protected boolean isSubscriptionDurable() {
		return this.subscriptionDurable;
	}

	/**
	 * Set the name of a durable subscription to create. To be applied in case
	 * of a topic (pub-sub domain) with subscription durability activated.
	 * <p>The durable subscription name needs to be unique within this client's
	 * JMS client id. Default is the class name of the specified message listener.
	 * <p>Note: Only 1 concurrent consumer (which is the default of this
	 * message listener container) is allowed for each durable subscription.
	 * @see #setSubscriptionDurable
	 * @see #setClientId
	 * @see #setMessageListener
	 */
	public void setDurableSubscriptionName(String durableSubscriptionName) {
		Assert.notNull(durableSubscriptionName, "durableSubscriptionName must not be null");
		this.durableSubscriptionName = durableSubscriptionName;
	}

	/**
	 * Return the name of a durable subscription to create, if any.
	 */
	protected String getDurableSubscriptionName() {
		return this.durableSubscriptionName;
	}

	/**
	 * Set the JMS ExceptionListener to notify in case of a JMSException thrown
	 * by the registered message listener or the invocation infrastructure.
	 */
	public void setExceptionListener(ExceptionListener exceptionListener) {
		this.exceptionListener = exceptionListener;
	}

	/**
	 * Return the JMS ExceptionListener to notify in case of a JMSException thrown
	 * by the registered message listener or the invocation infrastructure, if any.
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
		return this.exposeListenerSession;
	}

	/**
	 * Set whether to automatically start the listener after initialization.
	 * <p>Default is "true"; set this to "false" to allow for manual startup.
	 */
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	/**
	 * Set whether to accept received messages while the listener container
	 * in the process of stopping.
	 * <p>Default is "false", rejecting such messages through aborting the
	 * receive attempt. Switch this flag on to fully process such messages
	 * even in the stopping phase, with the drawback that even newly sent
	 * messages might still get processed (if coming in before all receive
	 * timeouts have expired).
	 * <p><b>NOTE:</b> Aborting receive attempts for such incoming messages
	 * might lead to the provider's retry count decreasing for the affected
	 * messages. If you have a high number of concurrent consumers, make sure
	 * that the number of retries is higher than the number of consumers,
	 * to be on the safe side for all potential stopping scenarios.
	 */
	public void setAcceptMessagesWhileStopping(boolean acceptMessagesWhileStopping) {
		this.acceptMessagesWhileStopping = acceptMessagesWhileStopping;
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
		if (isSubscriptionDurable() && !isPubSubDomain()) {
			throw new IllegalArgumentException("A durable subscription requires a topic (pub-sub domain)");
		}

		initialize();
	}

	/**
	 * Initialize this message listener container.
	 * <p>Creates a JMS Connection, registers the
	 * {@link #setMessageListener(Object) given listener object},
	 * and starts the {@link Connection}
	 * (if {@link #setAutoStartup(boolean) "autoStartup"} hasn't been turned off).
	 * @throws JmsException if startup failed
	 */
	public void initialize() throws JmsException {
		try {
			synchronized (this.lifecycleMonitor) {
				this.active = true;
				this.lifecycleMonitor.notifyAll();
			}

			if (sharedConnectionEnabled()) {
				establishSharedConnection();
			}

			if (this.autoStartup) {
				doStart();
			}

			registerListener();
		}
		catch (JMSException ex) {
			synchronized (this.sharedConnectionMonitor) {
				ConnectionFactoryUtils.releaseConnection(this.sharedConnection, getConnectionFactory(), this.autoStartup);
			}
			throw convertJmsAccessException(ex);
		}
	}


	/**
	 * Establish a shared Connection for this message listener container.
	 * <p>The default implementation delegates to <code>refreshSharedConnection</code>,
	 * which does one immediate attempt and throws an exception if it fails.
	 * Can be overridden to have a recovery proces in place, retrying
	 * until a Connection can be successfully established.
	 * @throws JMSException if thrown by JMS API methods
	 * @see #refreshSharedConnection()
	 */
	protected void establishSharedConnection() throws JMSException {
		refreshSharedConnection();
	}

	/**
	 * Refresh the shared Connection that this listener container holds.
	 * <p>Called on startup and also after an infrastructure exception
	 * that occured during listener setup and/or execution.
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected final void refreshSharedConnection() throws JMSException {
		boolean running = isRunning();
		synchronized (this.sharedConnectionMonitor) {
			ConnectionFactoryUtils.releaseConnection(this.sharedConnection, getConnectionFactory(), running);
			this.sharedConnection = null;
			Connection con = createConnection();
			try {
				prepareSharedConnection(con);
			}
			catch (JMSException ex) {
				JmsUtils.closeConnection(con);
				throw ex;
			}
			this.sharedConnection = con;
		}
	}

	/**
	 * Prepare the given Connection, which is about to be registered
	 * as shared Connection for this message listener container.
	 * <p>The default implementation sets the specified client id, if any.
	 * Subclasses can override this to apply further settings.
	 * @param connection the Connection to prepare
	 * @throws JMSException if the preparation efforts failed
	 * @see #setClientId
	 */
	protected void prepareSharedConnection(Connection connection) throws JMSException {
		if (getClientId() != null) {
			connection.setClientID(getClientId());
		}
	}

	/**
	 * Return the shared JMS Connection maintained by this message listener container.
	 * Available after initialization.
	 * @throws IllegalStateException if this listener container does not maintain a
	 * shared Connection, or if the Connection hasn't been initialized yet
	 * @see #sharedConnectionEnabled()
	 */
	protected final Connection getSharedConnection() {
		if (!sharedConnectionEnabled()) {
			throw new IllegalStateException(
					"This message listener container does not maintain a shared Connection");
		}
		synchronized (this.sharedConnectionMonitor) {
			if (this.sharedConnection == null) {
				throw new SharedConnectionNotInitializedException(
						"This message listener container's shared Connection has not been initialized yet");
			}
			return this.sharedConnection;
		}
	}


	/**
	 * Calls <code>shutdown</code> when the BeanFactory destroys
	 * the message listener container instance.
	 * @see #shutdown()
	 */
	public void destroy() {
		shutdown();
	}

	/**
	 * Shut down the registered listeners and close this
	 * listener container.
	 * @throws JmsException if shutdown failed
	 * @see #destroyListener()
	 */
	public void shutdown() throws JmsException {
		logger.debug("Shutting down message listener container");
		boolean wasRunning = false;
		synchronized (this.lifecycleMonitor) {
			wasRunning = this.running;
			this.running = false;
			this.active = false;
			this.lifecycleMonitor.notifyAll();
		}

		// Stop shared Connection early, if necessary.
		if (wasRunning && sharedConnectionEnabled()) {
			try {
				stopSharedConnection();
			}
			catch (Throwable ex) {
				logger.debug("Could not stop JMS Connection on shutdown", ex);
			}
		}

		// Shut down the actual listener setup.
		try {
			destroyListener();
		}
		catch (JMSException ex) {
			throw convertJmsAccessException(ex);
		}
		finally {
			if (sharedConnectionEnabled()) {
				synchronized (this.sharedConnectionMonitor) {
					ConnectionFactoryUtils.releaseConnection(this.sharedConnection, getConnectionFactory(), false);
				}
			}
		}
	}

	/**
	 * Return whether this listener container is currently active,
	 * that is, whether it has been set up but not shut down yet.
	 */
	public final boolean isActive() {
		synchronized (this.lifecycleMonitor) {
			return this.active;
		}
	}


	//-------------------------------------------------------------------------
	// Lifecycle methods for dynamically starting and stopping the listener
	//-------------------------------------------------------------------------

	/**
	 * Start this listener container.
	 * @throws JmsException if starting failed
	 * @see #doStart
	 */
	public void start() throws JmsException {
		try {
			doStart();
		}
		catch (JMSException ex) {
			throw convertJmsAccessException(ex);
		}
	}

	/**
	 * Start the shared Connection, if any, and notify all listener tasks.
	 * @throws JMSException if thrown by JMS API methods
	 * @see #startSharedConnection
	 */
	protected void doStart() throws JMSException {
		synchronized (this.lifecycleMonitor) {
			this.running = true;
			this.lifecycleMonitor.notifyAll();
			for (Iterator it = this.pausedTasks.iterator(); it.hasNext();) {
				doRescheduleTask(it.next());
				it.remove();
			}
		}

		if (sharedConnectionEnabled()) {
			startSharedConnection();
		}
	}

	/**
	 * Start the shared Connection.
	 * @throws JMSException if thrown by JMS API methods
	 * @see javax.jms.Connection#start()
	 */
	protected void startSharedConnection() throws JMSException {
		synchronized (this.sharedConnectionMonitor) {
			if (this.sharedConnection != null) {
				try {
					this.sharedConnection.start();
				}
				catch (javax.jms.IllegalStateException ex) {
					logger.debug("Ignoring Connection start exception - assuming already started", ex);
				}
			}
		}
	}

	/**
	 * Stop this listener container.
	 * @throws JmsException if stopping failed
	 * @see #doStop
	 */
	public void stop() throws JmsException {
		try {
			doStop();
		}
		catch (JMSException ex) {
			throw convertJmsAccessException(ex);
		}
	}

	/**
	 * Notify all listener tasks and stop the shared Connection, if any.
	 * @throws JMSException if thrown by JMS API methods
	 * @see #stopSharedConnection
	 */
	protected void doStop() throws JMSException {
		synchronized (this.lifecycleMonitor) {
			this.running = false;
			this.lifecycleMonitor.notifyAll();
		}

		if (sharedConnectionEnabled()) {
			stopSharedConnection();
		}
	}

	/**
	 * Stop the shared Connection.
	 * @throws JMSException if thrown by JMS API methods
	 * @see javax.jms.Connection#start()
	 */
	protected void stopSharedConnection() throws JMSException {
		synchronized (this.sharedConnectionMonitor) {
			if (this.sharedConnection != null) {
				try {
					this.sharedConnection.stop();
				}
				catch (javax.jms.IllegalStateException ex) {
					logger.debug("Ignoring Connection stop exception - assuming already stopped", ex);
				}
			}
		}
	}

	/**
	 * Return whether this listener container is currently running,
	 * that is, whether it has been started and not stopped yet.
	 */
	public final boolean isRunning() {
		synchronized (this.lifecycleMonitor) {
			return this.running;
		}
	}

	/**
	 * Wait while this listener container is not running.
	 * <p>To be called by asynchronous tasks that want to block
	 * while the listener container is in stopped state.
	 */
	protected final void waitWhileNotRunning() {
		synchronized (this.lifecycleMonitor) {
			while (this.active && !this.running) {
				try {
					this.lifecycleMonitor.wait();
				}
				catch (InterruptedException ex) {
					// Re-interrupt current thread, to allow other threads to react.
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	/**
	 * Take the given task object and reschedule it,
	 * either immediately if this listener container is currently running,
	 * or later once this listener container has been restarted.
	 * <p>If this listener container has already been shut down,
	 * the task will not get rescheduled at all.
	 * @param task the task object to reschedule
	 * @return whether the task has been rescheduled
	 * (either immediately or for a restart of this container)
	 * @see #doRescheduleTask
	 */
	protected final boolean rescheduleTaskIfNecessary(Object task) {
		Assert.notNull(task, "Task object must not be null");
		synchronized (this.lifecycleMonitor) {
			if (this.running) {
				doRescheduleTask(task);
				return true;
			}
			else if (this.active) {
				this.pausedTasks.add(task);
				return true;
			}
			else {
				return false;
			}
		}
	}

	/**
	 * Reschedule the given task object immediately.
	 * <p>To be implemented by subclasses if they ever call
	 * <code>rescheduleTaskIfNecessary</code>.
	 * This implementation throws an UnsupportedOperationException.
	 * @param task the task object to reschedule
	 * @see #rescheduleTaskIfNecessary
	 */
	protected void doRescheduleTask(Object task) {
		throw new UnsupportedOperationException(
				ClassUtils.getShortName(getClass()) + " does not support rescheduling of tasks");
	}


	//-------------------------------------------------------------------------
	// Template methods for listener execution
	//-------------------------------------------------------------------------

	/**
	 * Execute the specified listener,
	 * committing or rolling back the transaction afterwards (if necessary).
	 * @param session the JMS Session to operate on
	 * @param message the received JMS Message
	 * @see #invokeListener
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
	 * @param session the JMS Session to operate on
	 * @param message the received JMS Message
	 * @throws JMSException if thrown by JMS API methods
	 * @see #invokeListener
	 * @see #commitIfNecessary
	 * @see #rollbackOnExceptionIfNecessary
	 * @see #convertJmsAccessException
	 */
	protected void doExecuteListener(Session session, Message message) throws JMSException {
		if (!this.acceptMessagesWhileStopping && !isRunning()) {
			if (session.getTransacted() && isSessionTransacted()) {
				// Transacted session created by this container -> rollback.
				if (logger.isWarnEnabled()) {
					logger.warn("Initiating transaction rollback for received message because of the " +
							"listener container having been stopped in the meantime: " + message);
				}
				JmsUtils.rollbackIfNecessary(session);
			}
			return;
		}
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
	 * or (preferably) as Spring SessionAwareMessageListener.
	 * @param session the JMS Session to operate on
	 * @param message the received JMS Message
	 * @throws JMSException if thrown by JMS API methods
	 * @see #setMessageListener
	 */
	protected void invokeListener(Session session, Message message) throws JMSException {
		if (getMessageListener() instanceof SessionAwareMessageListener) {
			doInvokeListener((SessionAwareMessageListener) getMessageListener(), session, message);
		}
		else if (getMessageListener() instanceof MessageListener) {
			doInvokeListener((MessageListener) getMessageListener(), message);
		}
		else {
			throw new IllegalArgumentException("Only MessageListener and SessionAwareMessageListener supported");
		}
	}

	/**
	 * Invoke the specified listener as Spring SessionAwareMessageListener,
	 * exposing a new JMS Session (potentially with its own transaction)
	 * to the listener if demanded.
	 * @param listener the Spring SessionAwareMessageListener to invoke
	 * @param session the JMS Session to operate on
	 * @param message the received JMS Message
	 * @throws JMSException if thrown by JMS API methods
	 * @see SessionAwareMessageListener
	 * @see #setExposeListenerSession
	 */
	protected void doInvokeListener(SessionAwareMessageListener listener, Session session, Message message)
			throws JMSException {

		Connection conToClose = null;
		Session sessionToClose = null;
		try {
			Session sessionToUse = session;
			if (!isExposeListenerSession()) {
				// We need to expose a separate Session.
				conToClose = createConnection();
				sessionToClose = createSession(conToClose);
				sessionToUse = sessionToClose;
			}
			// Actually invoke the message listener...
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking listener with message of type [" + message.getClass() +
						"] and session [" + sessionToUse + "]");
			}
			listener.onMessage(message, sessionToUse);
			// Clean up specially exposed Session, if any.
			if (sessionToUse != session) {
				if (sessionToUse.getTransacted() && isSessionTransacted()) {
					// Transacted session created by this container -> commit.
					JmsUtils.commitIfNecessary(sessionToUse);
				}
			}
		}
		finally {
			JmsUtils.closeSession(sessionToClose);
			JmsUtils.closeConnection(conToClose);
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
	protected void doInvokeListener(MessageListener listener, Message message) throws JMSException {
		listener.onMessage(message);
	}

	/**
	 * Perform a commit or message acknowledgement, as appropriate.
	 * @param session the JMS Session to commit
	 * @param message the Message to acknowledge
	 * @throws javax.jms.JMSException in case of commit failure
	 */
	protected void commitIfNecessary(Session session, Message message) throws JMSException {
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

	/**
	 * Perform a rollback, handling rollback exceptions properly.
	 * @param session the JMS Session to rollback
	 * @param ex the thrown application exception or error
	 * @throws javax.jms.JMSException in case of a rollback error
	 */
	protected void rollbackOnExceptionIfNecessary(Session session, Throwable ex) throws JMSException {
		try {
			if (session.getTransacted() && isSessionTransacted()) {
				// Transacted session created by this container -> rollback.
				if (logger.isDebugEnabled()) {
					logger.debug("Initiating transaction rollback on application exception", ex);
				}
				JmsUtils.rollbackIfNecessary(session);
			}
		}
		catch (IllegalStateException ex2) {
			logger.debug("Could not roll back because Session already closed", ex2);
		}
		catch (JMSException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			throw ex2;
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
		if (ex instanceof JMSException) {
			invokeExceptionListener((JMSException) ex);
		}
		if (isActive()) {
			// Regular case: failed while active.
			// Log at error level.
			logger.error("Execution of JMS message listener failed", ex);
		}
		else {
			// Rare case: listener thread failed after container shutdown.
			// Log at debug level, to avoid spamming the shutdown log.
			logger.debug("Listener exception after container shutdown", ex);
		}
	}

	/**
	 * Invoke the registered JMS ExceptionListener, if any.
	 * @param ex the exception that arose during JMS processing
	 * @see #setExceptionListener
	 */
	protected void invokeExceptionListener(JMSException ex) {
		ExceptionListener exceptionListener = getExceptionListener();
		if (exceptionListener != null) {
			exceptionListener.onException(ex);
		}
	}


	//-------------------------------------------------------------------------
	// Template methods to be implemented by subclasses
	//-------------------------------------------------------------------------

	/**
	 * Return whether a shared JMS Connection should be maintained
	 * by this listener container base class.
	 * @see #getSharedConnection()
	 */
	protected abstract boolean sharedConnectionEnabled();

	/**
	 * Register the specified listener.
	 * <p>Subclasses need to implement this method for their specific
	 * listener management process.
	 * <p>A shared JMS Connection, if any, will already have been
	 * started at this point.
	 * @throws JMSException if registration failed
	 * @see #getMessageListener()
	 * @see #getSharedConnection()
	 */
	protected abstract void registerListener() throws JMSException;

	/**
	 * Destroy the registered listener.
	 * <p>Subclasses need to implement this method for their specific
	 * listener management process.
	 * <p>A shared JMS Connection, if any, will automatically be closed
	 * <i>afterwards</i>.
	 * @throws JMSException if destruction failed
	 * @see #shutdown()
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


	/**
	 * Exception that indicates that the initial setup of this listener container's
	 * shared JMS Connection failed. This is indicating to invokers that they need
	 * to establish the shared Connection themselves on first access.
	 */
	protected static class SharedConnectionNotInitializedException extends RuntimeException {

		public SharedConnectionNotInitializedException(String msg) {
			super(msg);
		}
	}

}
