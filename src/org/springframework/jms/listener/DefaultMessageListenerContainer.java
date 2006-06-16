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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.support.JmsUtils;
import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Message listener container that uses plain JMS client API, specifically
 * looped <code>MessageConsumer.receive()</code> calls that also allow for
 * transactional reception of messages (registering them with XA transactions).
 *
 * <p>This is a simple but nevertheless powerful form of a message listener container.
 * It creates a fixed number of JMS Sessions to invoke the listener, not allowing
 * for dynamic adaptation to runtime demands. Like SimpleMessageListenerContainer,
 * its main advantage is its low level of complexity and the minimum requirements
 * on the JMS provider: Not even the ServerSessionPool facility is required.
 *
 * <p>Actual MessageListener execution happens in separate threads that are
 * created through Spring's TaskExecutor abstraction. By default, the appropriate
 * number of threads are created on startup, according to the "concurrentConsumers"
 * setting. Specify an alternative TaskExecutor to integrate with an existing
 * thread pool facility, for example.
 *
 * <p>Message reception and listener execution can automatically be wrapped
 * in transactions through passing a Spring PlatformTransactionManager into the
 * "transactionManager" property. This will usually be a JtaTransactionManager,
 * in combination with a JTA-aware ConnectionFactory that this message listener
 * container fetches its Connections from.
 *
 * <p><b>NOTE:</b> Turn off the "cacheSessions" flag on JBoss 4.0 to make JMS
 * message reception properly participate in XA transactions, where JBoss requires
 * each listener thread to reobtain its JMS Session for each receive attempt.
 *
 * <p>See the {@link AbstractMessageListenerContainer AbstractMessageListenerContainer}
 * javadoc for details on acknowledge modes and transaction options.
 *
 * <p>This class requires a JMS 1.1+ provider, because it builds on the
 * domain-independent API. <b>Use the {@link DefaultMessageListenerContainer102
 * DefaultMessageListenerContainer102} subclass for JMS 1.0.2 providers.</b>
 *
 * <p>For dynamic adaptation of the active number of Sessions, consider using
 * ServerSessionMessageListenerContainer.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setTransactionManager
 * @see #setCacheSessions
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see javax.jms.MessageConsumer#receive(long)
 * @see SimpleMessageListenerContainer
 * @see org.springframework.jms.listener.serversession.ServerSessionMessageListenerContainer
 * @see DefaultMessageListenerContainer102
 */
public class DefaultMessageListenerContainer extends AbstractMessageListenerContainer {

	/**
	 * Default thread name prefix: "SimpleAsyncTaskExecutor-".
	 */
	public static final String DEFAULT_THREAD_NAME_PREFIX =
			ClassUtils.getShortName(DefaultMessageListenerContainer.class) + "-";

	/**
	 * The default receive timeout: 1000 ms = 1 second.
	 */
	public static final long DEFAULT_RECEIVE_TIMEOUT = 1000;


	private boolean pubSubNoLocal = false;

	private TaskExecutor taskExecutor;

	private int concurrentConsumers = 1;

	private int maxMessagesPerTask = Integer.MIN_VALUE;

	private TransactionTemplate transactionTemplate = new TransactionTemplate();

	private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

	private boolean cacheSessions = true;


	/**
	 * Set whether to inhibit the delivery of messages published by its own connection.
	 * Default is "false".
	 * @see javax.jms.TopicSession#createSubscriber(javax.jms.Topic, String, boolean)
	 */
	public void setPubSubNoLocal(boolean pubSubNoLocal) {
		this.pubSubNoLocal = pubSubNoLocal;
	}

	/**
	 * Return whether to inhibit the delivery of messages published by its own connection.
	 */
	protected boolean isPubSubNoLocal() {
		return pubSubNoLocal;
	}

	/**
	 * Set the Spring TaskExecutor to use for running the listener threads.
	 * Default is SimpleAsyncTaskExecutor, starting up a number of new threads,
	 * according to the specified number of concurrent consumers.
	 * <p>Specify an alternative TaskExecutor for integration with an existing
	 * thread pool. Note that this really only adds value if the threads are
	 * managed in a specific fashion, for example within a J2EE environment.
	 * A plain thread pool does not add much value, as this listener container
	 * will occupy a number of threads for its entire lifetime.
	 * @see #setConcurrentConsumers
	 * @see org.springframework.core.task.SimpleAsyncTaskExecutor
	 * @see org.springframework.scheduling.commonj.WorkManagerTaskExecutor
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Specify the number of concurrent consumers to create.
	 * Default is 1.
	 */
	public void setConcurrentConsumers(int concurrentConsumers) {
		Assert.isTrue(concurrentConsumers > 0, "concurrentConsumers must be positive");
		this.concurrentConsumers = concurrentConsumers;
	}

	/**
	 * Set the maximum number of messages to process in one task.
	 * More concretely, this limits the number of message reception attempts,
	 * which includes receive iterations that did not actually pick up a
	 * message until they hit their timeout (see "receiveTimeout" property).
	 * <p>Default is unlimited (-1) in case of a standard TaskExecutor,
	 * and 1 in case of a SchedulingTaskExecutor that indicates a preference for
	 * short-lived tasks. Specify a number of 10 to 100 messages to balance
	 * between extremely long-lived and extremely short-lived tasks here.
	 * <p>Long-lived tasks avoid frequent thread context switches through
	 * sticking with the same thread all the way through, while short-lived
	 * tasks allow thread pools to control the scheduling. Hence, thread
	 * pools will usually prefer short-lived tasks.
	 * @see #setTaskExecutor
	 * @see #setReceiveTimeout
	 * @see org.springframework.scheduling.SchedulingTaskExecutor#isShortLivedPreferred()
	 */
	public void setMaxMessagesPerTask(int maxMessagesPerTask) {
		Assert.isTrue(maxMessagesPerTask != 0, "maxMessagesPerTask must not be 0");
		this.maxMessagesPerTask = maxMessagesPerTask;
	}

	/**
	 * Specify the Spring TransactionTemplate to use for transactional
	 * wrapping of message reception plus listener execution.
	 * Default is none, not performing any transactional wrapping.
	 * <p>Alternatively, pass in a Spring PlatformTransactionManager directly
	 * into the "transactionManager" property.
	 * @see #setTransactionManager
	 */
	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = (transactionTemplate != null ? transactionTemplate : new TransactionTemplate());
	}

	/**
	 * Specify the Spring PlatformTransactionManager to use for transactional
	 * wrapping of message reception plus listener execution.
	 * Default is none, not performing any transactional wrapping.
	 * <p>If specified, this will usually be a Spring JtaTransactionManager,
	 * in combination with a JTA-aware ConnectionFactory that this message
	 * listener container fetches its Connections from.
	 * <p>Alternatively, pass in a fully configured Spring TransactionTemplate
	 * into the "transactionTemplate" property.
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionTemplate.setTransactionManager(transactionManager);
	}

	/**
	 * Specify the transaction timeout to use for transactional wrapping, in seconds.
	 * Default is none, using the transaction manager's default timeout.
	 * @see org.springframework.transaction.TransactionDefinition#getTimeout()
	 * @see #setReceiveTimeout
	 */
	public void setTransactionTimeout(int timeout) {
		this.transactionTemplate.setTimeout(timeout);
	}

	/**
	 * Set the timeout to use for receive calls, in milliseconds.
	 * The default is 1000 ms, that is, 1 second.
	 * <p><b>NOTE:</b> This value needs to be smaller than the transaction
	 * timeout used by the transaction manager (in the appropriate unit,
	 * of course). -1 indicates no timeout at all; however, this is only
	 * feasible if not running within a transaction manager.
	 * @see javax.jms.MessageConsumer#receive(long)
	 * @see javax.jms.MessageConsumer#receive
	 * @see #setTransactionTimeout
	 */
	public void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	/**
	 * Specify whether to cache JMS Sessions in the listener threads,
	 * using each Session for an unlimited number of message receive attempts.
	 * Default is "true".
	 * <p>Turn this flag off to reobtain the JMS Sessions for each receive operation:
	 * This is usually just worth considering in the context of XA transactions,
	 * where the JMS provider (or J2EE server) might only register itself with an
	 * ongoing XA transaction in case of a freshly obtained JMS Session.
	 * <p>Currently known providers that require this flag to be turned off
	 * for proper XA transaction participation: JBoss 4.0
	 */
	public void setCacheSessions(boolean cacheSessions) {
		this.cacheSessions = cacheSessions;
	}


	//-------------------------------------------------------------------------
	// Implementation of AbstractMessageListenerContainer's template methods
	//-------------------------------------------------------------------------

	public void initialize() {
		// Prepare taskExecutor and maxMessagesPerTask.
		if (this.taskExecutor == null) {
			this.taskExecutor = new SimpleAsyncTaskExecutor(DEFAULT_THREAD_NAME_PREFIX);
		}
		else if (this.taskExecutor instanceof SchedulingTaskExecutor &&
				((SchedulingTaskExecutor) this.taskExecutor).isShortLivedPreferred() &&
				this.maxMessagesPerTask == Integer.MIN_VALUE) {
			// TaskExecutor indicated a preference for short-lived tasks. According to
			// setMaxMessagesPerTask javadoc, we'll use 1 message per task in this case
			// unless the user specified a custom value.
			this.maxMessagesPerTask = 1;
		}

		// Proceed with actual listener initialization.
		super.initialize();
	}

	/**
	 * Creates the specified number of concurrent consumers,
	 * in the form of a JMS Session plus associated MessageConsumer
	 * running in a separate thread.
	 * @see #setTaskExecutor
	 */
	protected void registerListener() throws JMSException {
		for (int i = 0; i < this.concurrentConsumers; i++) {
			this.taskExecutor.execute(new AsyncMessageListenerInvoker());
		}
	}

	/**
	 * Create a MessageConsumer for the given JMS Session,
	 * registering a MessageListener for the specified listener.
	 * @param session the JMS Session to work on
	 * @return the MessageConsumer
	 * @throws javax.jms.JMSException if thrown by JMS methods
	 * @see #executeListener
	 */
	protected MessageConsumer createListenerConsumer(final Session session) throws JMSException {
		Destination destination = getDestination();
		if (destination == null) {
			destination = resolveDestinationName(session, getDestinationName());
		}
		return createConsumer(session, destination);
	}

	protected void executeListener(final Session session, final MessageConsumer consumer) throws JMSException {
		if (this.transactionTemplate.getTransactionManager() != null) {
			this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						doExecuteListener(session, consumer);
					}
					catch (Throwable ex) {
						if (logger.isDebugEnabled()) {
							logger.debug("Rolling back transaction because of listener exception thrown: " + ex);
						}
						status.setRollbackOnly();
						handleListenerException(ex);
					}
				}
			});
		}
		else {
			try {
				doExecuteListener(session, consumer);
			}
			catch (Throwable ex) {
				handleListenerException(ex);
			}
		}
	}

	protected void doExecuteListener(Session session, MessageConsumer consumer) throws JMSException {
		Message message = receiveMessage(consumer, this.receiveTimeout);
		if (message != null) {
			doExecuteListener(session, message);
		}
	}

	protected Message receiveMessage(MessageConsumer consumer, long receiveTimeout) throws JMSException {
		return (receiveTimeout < 0 ? consumer.receive() : consumer.receive(receiveTimeout));
	}

	/**
	 * Destroy the registered JMS Sessions and associated MessageConsumers.
	 */
	protected void destroyListener() throws JMSException {
		logger.debug("Shutting down JMS message listener invokers");
		// Give the async receive tasks a typical time to finish.
		if (this.receiveTimeout > 0) {
			try {
				Thread.sleep(this.receiveTimeout);
			}
			catch (InterruptedException ex) {
				// Re-interrupt current thread, to allow other threads to react.
				Thread.currentThread().interrupt();
			}
		}
	}


	//-------------------------------------------------------------------------
	// JMS 1.1 factory methods, potentially overridden for JMS 1.0.2
	//-------------------------------------------------------------------------

	/**
	 * Create a JMS MessageConsumer for the given Session and Destination.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param session the JMS Session to create a MessageConsumer for
	 * @param destination the JMS Destination to create a MessageConsumer for
	 * @return the new JMS MessageConsumer
	 * @throws javax.jms.JMSException if thrown by JMS API methods
	 */
	protected MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
		// Only pass in the NoLocal flag in case of a Topic:
		// Some JMS providers, such as WebSphere MQ 6.0, throw IllegalStateException
		// in case of the NoLocal flag being specified for a Queue.
		if (destination instanceof Topic) {
			return session.createConsumer(destination, getMessageSelector(), isPubSubNoLocal());
		}
		else {
			return session.createConsumer(destination, getMessageSelector());
		}
	}


	//-------------------------------------------------------------------------
	// Inner class that serves as loop in a separate thread
	//-------------------------------------------------------------------------

	/**
	 * Runnable that performs looped <code>MessageConsumer.receive()</code> calls.
	 */
	private class AsyncMessageListenerInvoker implements SchedulingAwareRunnable {

		private Session session;

		private MessageConsumer consumer;

		public void run() {
			try {
				if (maxMessagesPerTask < 0) {
					while (isActive()) {
						invokeListener();
					}
				}
				else {
					int messageCount = 0;
					while (isActive() && messageCount < maxMessagesPerTask) {
						invokeListener();
						messageCount++;
					}
				}
			}
			catch (Throwable ex) {
				logger.error("Setup of JMS message listener invoker failed", ex);
				clearResources();
			}
			if (isActive()) {
				// Reschedule this Runnable for receiving another message.
				// Reuses the Session and MessageConsumer unless an infrastructure
				// exception was raised during the last attempt.
				taskExecutor.execute(this);
			}
			else {
				// We're shutting down completely.
				clearResources();
			}
		}

		private void invokeListener() throws JMSException {
			initResourcesIfNecessary();
			executeListener(this.session, this.consumer);
			if (!cacheSessions) {
				clearResources();
			}
		}

		private void initResourcesIfNecessary() throws JMSException {
			if (this.session == null) {
				this.session = createSession(getConnection());
				this.consumer = createListenerConsumer(this.session);
			}
		}

		private void clearResources() {
			JmsUtils.closeMessageConsumer(this.consumer);
			JmsUtils.closeSession(this.session);
			this.consumer = null;
			this.session = null;
		}

		public boolean isLongLived() {
			return (maxMessagesPerTask < 0);
		}
	}

}
