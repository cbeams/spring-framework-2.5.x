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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.support.JmsUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
 * number of threads gets created on startup, according to the "concurrentConsumers"
 * setting. Specify an alternative TaskExecutor to integrate with an existing
 * thread pool facility, for example.
 *
 * <p>Message reception and listener execution can automatically be wrapped
 * in transactions through passing a Spring PlatformTransactionManager into the
 * "transactionManager" property. This will usually be a JtaTransactionManager,
 * in combination with a JTA-aware ConnectionFactory that this message listener
 * container fetches its Connections from.
 *
 * <p>For dynamic adaptation of the active number of Sessions, consider using
 * ServerSessionMessageListenerContainer.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see javax.jms.MessageConsumer#receive(long)
 * @see SimpleMessageListenerContainer
 * @see org.springframework.jms.listener.serversession.ServerSessionMessageListenerContainer
 */
public class DefaultMessageListenerContainer extends AbstractMessageListenerContainer {

	/**
	 * The default receive timeout: 1000 ms = 1 second.
	 */
	public static final long DEFAULT_RECEIVE_TIMEOUT = 1000;


	private boolean pubSubNoLocal = false;

	private int concurrentConsumers = 1;

	private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("DefaultMessageListenerContainer");

	private TransactionTemplate transactionTemplate = new TransactionTemplate();

	private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

	private int listenersRunning = 0;


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
	 * Specify the number of concurrent consumers to create.
	 * Default is 1.
	 */
	public void setConcurrentConsumers(int concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
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
	 * Specify the Spring TransactionTemplate to use for transactional
	 * wrapping of message reception plus listener execution.
	 * Default is none, not performing any transactional wrapping.
	 * <p>Alternatively, pass in a Spring PlatformTransactionManager directly
	 * into the "transactionManager" property.
	 * @see #setTransactionManager
	 */
	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
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


	//-------------------------------------------------------------------------
	// Implementation of AbstractMessageListenerContainer's template methods
	//-------------------------------------------------------------------------

	/**
	 * Creates the specified number of concurrent consumers,
	 * in the form of a JMS Session plus associated MessageConsumer
	 * running in a separate thread.
	 * @see #setTaskExecutor
	 */
	protected void registerListener() throws JMSException {
		Runnable invoker = new AsyncMessageListenerInvoker();
		for (int i = 0; i < this.concurrentConsumers; i++) {
			this.taskExecutor.execute(invoker);
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

	protected void executeListener(final Session session, final MessageConsumer consumer) {
		if (this.transactionTemplate.getTransactionManager() != null) {
			this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						doExecuteListener(session, consumer);
					}
					catch (Throwable ex) {
						status.setRollbackOnly();
						logger.error("Execution of JMS message listener failed - rolling back transaction", ex);
					}
				}
			});
		}
		else {
			try {
				doExecuteListener(session, consumer);
			}
			catch (JMSException ex) {
				logger.error("Execution of JMS message listener failed", ex);
			}
		}
	}

	protected void doExecuteListener(Session session, MessageConsumer consumer) throws JMSException {
		Message message =
				(this.receiveTimeout < 0 ? consumer.receive() : consumer.receive(this.receiveTimeout));
		if (message != null) {
			doExecuteListener(session, message);
		}
	}

	/**
	 * Destroy the registered JMS Sessions and associated MessageConsumers.
	 */
	protected void destroyListener() throws JMSException {
		logger.debug("Shutting down JMS message listener invokers");
		if (this.receiveTimeout > 0) {
			while (this.listenersRunning > 0) {
				try {
					Thread.sleep(this.receiveTimeout);
				}
				catch (InterruptedException ex) {
				}
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
	private class AsyncMessageListenerInvoker implements Runnable {

		public void run() {
			listenersRunning++;
			Session session = null;
			MessageConsumer consumer = null;
			try {
				session = createSession(getConnection());
				consumer = createListenerConsumer(session);
				while (isActive()) {
					executeListener(session, consumer);
				}
			}
			catch (JMSException ex) {
				logger.error("Setup of JMS message listener invoker failed", ex);
			}
			finally {
				JmsUtils.closeMessageConsumer(consumer);
				JmsUtils.closeSession(session);
				listenersRunning--;
			}
		}
	}

}
