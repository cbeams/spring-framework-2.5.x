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
import javax.jms.JMSException;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;
import org.springframework.jms.JmsException;
import org.springframework.jms.connection.ConnectionFactoryUtils;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.destination.JmsDestinationAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Common base class for all containers which need to implement listening
 * based on a JMS Connection (either shared or freshly obtained for each attempt).
 * Inherits basic Connection and Session configuration handling from the
 * {@link org.springframework.jms.support.JmsAccessor} base class.
 *
 * <p>This class provides basic lifecycle management, in particular management
 * of a shared JMS Connection. Subclasses are supposed to plug into this
 * lifecycle, implementing the {@link #sharedConnectionEnabled()} as well
 * as the {@link #doInitialize()} and {@link #doShutdown()} template methods.
 *
 * <p>This base class does not assume any specific listener programming model
 * or listener invoker mechanism. It just provides the general runtime
 * lifecycle management needed for any kind of JMS-based listening mechanism
 * that operates on a JMS Connection/Session.
 *
 * <p>For a concrete listener programming model, check out the
 * {@link AbstractMessageListenerContainer} subclass. For a concrete listener
 * invoker mechanism, check out the {@link DefaultMessageListenerContainer} class.
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see #sharedConnectionEnabled()
 * @see #doInitialize()
 * @see #doShutdown()
 */
public abstract class AbstractJmsListeningContainer extends JmsDestinationAccessor
		implements Lifecycle, BeanNameAware, DisposableBean {

	private String clientId;

	private boolean autoStartup = true;

	private String beanName;

	private Connection sharedConnection;

	private final Object sharedConnectionMonitor = new Object();

	private boolean active = false;

	private boolean running = false;

	private final List pausedTasks = new LinkedList();

	private final Object lifecycleMonitor = new Object();


	/**
	 * Specify the JMS client id for a shared Connection created and used
	 * by this container.
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
	 * by this container, if any.
	 */
	public String getClientId() {
		return this.clientId;
	}

	/**
	 * Set whether to automatically start the container after initialization.
	 * <p>Default is "true"; set this to "false" to allow for manual startup
	 * through the {@link #start()} method.
	 */
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Return the bean name that this listener container has been assigned
	 * in its containing bean factory, if any.
	 */
	protected final String getBeanName() {
		return this.beanName;
	}


	/**
	 * Delegates to {@link #validateConfiguration()} and {@link #initialize()}.
	 */
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		validateConfiguration();
		initialize();
	}

	/**
	 * Validate the configuration of this container.
	 * <p>The default implementation is empty. To be overridden in subclasses.
	 */
	protected void validateConfiguration() {
	}


	/**
	 * Initialize this container.
	 * <p>Creates a JMS Connection, starts the {@link javax.jms.Connection}
	 * (if {@link #setAutoStartup(boolean) "autoStartup"} hasn't been turned off),
	 * and calls {@link #doInitialize()}.
	 * @throws org.springframework.jms.JmsException if startup failed
	 */
	public void initialize() throws JmsException {
		try {
			synchronized (this.lifecycleMonitor) {
				this.active = true;
				this.lifecycleMonitor.notifyAll();
			}
			if (this.autoStartup) {
				doStart();
			}
			doInitialize();
		}
		catch (JMSException ex) {
			synchronized (this.sharedConnectionMonitor) {
				ConnectionFactoryUtils.releaseConnection(this.sharedConnection, getConnectionFactory(), this.autoStartup);
			}
			throw convertJmsAccessException(ex);
		}
	}

	/**
	 * Establish a shared Connection for this container.
	 * <p>The default implementation delegates to {@link #createSharedConnection()},
	 * which does one immediate attempt and throws an exception if it fails.
	 * Can be overridden to have a recovery proces in place, retrying
	 * until a Connection can be successfully established.
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected void establishSharedConnection() throws JMSException {
		synchronized (this.sharedConnectionMonitor) {
			if (this.sharedConnection == null) {
				this.sharedConnection = createSharedConnection();
				logger.debug("Established shared JMS Connection");
			}
		}
	}

	/**
	 * Refresh the shared Connection that this container holds.
	 * <p>Called on startup and also after an infrastructure exception
	 * that occured during invoker setup and/or execution.
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected final void refreshSharedConnection() throws JMSException {
		boolean running = isRunning();
		synchronized (this.sharedConnectionMonitor) {
			ConnectionFactoryUtils.releaseConnection(this.sharedConnection, getConnectionFactory(), running);
			this.sharedConnection = createSharedConnection();
		}
	}

	/**
	 * Create a shared Connection for this container.
	 * <p>The default implementation creates a standard Connection
	 * and prepares it through {@link #prepareSharedConnection}.
	 * @return the prepared Connection
	 * @throws JMSException if the creation failed
	 */
	protected Connection createSharedConnection() throws JMSException {
		Connection con = createConnection();
		try {
			prepareSharedConnection(con);
			return con;
		}
		catch (JMSException ex) {
			JmsUtils.closeConnection(con);
			throw ex;
		}
	}

	/**
	 * Prepare the given Connection, which is about to be registered
	 * as shared Connection for this container.
	 * <p>The default implementation sets the specified client id, if any.
	 * Subclasses can override this to apply further settings.
	 * @param connection the Connection to prepare
	 * @throws JMSException if the preparation efforts failed
	 * @see #getClientId()
	 */
	protected void prepareSharedConnection(Connection connection) throws JMSException {
		String clientId = getClientId();
		if (clientId != null) {
			connection.setClientID(clientId);
		}
	}

	/**
	 * Return the shared JMS Connection maintained by this container.
	 * Available after initialization.
	 * @return the shared Connection (never <code>null</code>)
	 * @throws IllegalStateException if this container does not maintain a
	 * shared Connection, or if the Connection hasn't been initialized yet
	 * @see #sharedConnectionEnabled()
	 */
	protected final Connection getSharedConnection() {
		if (!sharedConnectionEnabled()) {
			throw new IllegalStateException(
					"This listener container does not maintain a shared Connection");
		}
		synchronized (this.sharedConnectionMonitor) {
			if (this.sharedConnection == null) {
				throw new SharedConnectionNotInitializedException(
						"This listener container's shared Connection has not been initialized yet");
			}
			return this.sharedConnection;
		}
	}


	/**
	 * Calls {@link #shutdown()} when the BeanFactory destroys the container instance.
	 * @see #shutdown()
	 */
	public void destroy() {
		shutdown();
	}

	/**
	 * Stop the shared Connection, call {@link #doShutdown()},
	 * and close this container.
	 * @throws JmsException if shutdown failed
	 */
	public void shutdown() throws JmsException {
		logger.debug("Shutting down JMS listener container");
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

		// Shut down the invokers.
		try {
			doShutdown();
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
	 * Return whether this container is currently active,
	 * that is, whether it has been set up but not shut down yet.
	 */
	public final boolean isActive() {
		synchronized (this.lifecycleMonitor) {
			return this.active;
		}
	}


	//-------------------------------------------------------------------------
	// Lifecycle methods for dynamically starting and stopping the container
	//-------------------------------------------------------------------------

	/**
	 * Start this container.
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
	 * Start the shared Connection, if any, and notify all invoker tasks.
	 * @throws JMSException if thrown by JMS API methods
	 * @see #startSharedConnection
	 */
	protected void doStart() throws JMSException {
		// Lazily establish a shared Connection, if necessary.
		if (sharedConnectionEnabled()) {
			establishSharedConnection();
		}

		// Reschedule paused tasks, if any.
		synchronized (this.lifecycleMonitor) {
			this.running = true;
			this.lifecycleMonitor.notifyAll();
			for (Iterator it = this.pausedTasks.iterator(); it.hasNext();) {
				doRescheduleTask(it.next());
				it.remove();
			}
		}

		// Start the shared Connection, if any.
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
	 * Stop this container.
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
	 * Notify all invoker tasks and stop the shared Connection, if any.
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
	 * Return whether this container is currently running,
	 * that is, whether it has been started and not stopped yet.
	 */
	public final boolean isRunning() {
		synchronized (this.lifecycleMonitor) {
			return this.running;
		}
	}

	/**
	 * Wait while this container is not running.
	 * <p>To be called by asynchronous tasks that want to block
	 * while the container is in stopped state.
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
	 * Take the given task object and reschedule it, either immediately if
	 * this container is currently running, or later once this container
	 * has been restarted.
	 * <p>If this container has already been shut down, the task will not
	 * get rescheduled at all.
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
	// Template methods to be implemented by subclasses
	//-------------------------------------------------------------------------

	/**
	 * Return whether a shared JMS Connection should be maintained
	 * by this container base class.
	 * @see #getSharedConnection()
	 */
	protected abstract boolean sharedConnectionEnabled();

	/**
	 * Register any invokers within this container.
	 * <p>Subclasses need to implement this method for their specific
	 * invoker management process.
	 * <p>A shared JMS Connection, if any, will already have been
	 * started at this point.
	 * @throws JMSException if registration failed
	 * @see #getSharedConnection()
	 */
	protected abstract void doInitialize() throws JMSException;

	/**
	 * Close the registered invokers.
	 * <p>Subclasses need to implement this method for their specific
	 * invoker management process.
	 * <p>A shared JMS Connection, if any, will automatically be closed
	 * <i>afterwards</i>.
	 * @throws JMSException if shutdown failed
	 * @see #shutdown()
	 */
	protected abstract void doShutdown() throws JMSException;


	/**
	 * Exception that indicates that the initial setup of this container's
	 * shared JMS Connection failed. This is indicating to invokers that they need
	 * to establish the shared Connection themselves on first access.
	 */
	public static class SharedConnectionNotInitializedException extends RuntimeException {

		/**
		 * Create a new SharedConnectionNotInitializedException.
		 * @param msg the detail message
		 */
		protected SharedConnectionNotInitializedException(String msg) {
			super(msg);
		}
	}

}
