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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.support.JmsUtils;

/**
 * Message listener container that the plain JMS client API's
 * <code>MessageConsumer.setMessageListener()</code> method to
 * create concurrent MessageConsumers for the specified listeners.
 *
 * <p>This is the simplest form of a message listener container.
 * It creates a fixed number of JMS Sessions to invoke the listener,
 * not allowing for dynamic adaptation to runtime demands. Its main
 * advantage is its low level of complexity and the minimum requirements
 * on the JMS provider: Not even the ServerSessionPool facility is required.
 *
 * <p>See the {@link AbstractMessageListenerContainer AbstractMessageListenerContainer}
 * javadoc for details on acknowledge modes and transaction options.
 *
 * <p>For a different style of MessageListener handling, through looped
 * <code>MessageConsumer.receive()</code> calls that also allow for
 * transactional reception of messages (registering them with XA transactions),
 * see {@link DefaultMessageListenerContainer}. For dynamic adaptation of the active
 * number of Sessions, consider using
 * {@link org.springframework.jms.listener.serversession.ServerSessionMessageListenerContainer}.
 *
 * <p>This class requires a JMS 1.1+ provider, because it builds on the
 * domain-independent API. <b>Use the
 * {@link SimpleMessageListenerContainer102 SimpleMessageListenerContainer102}
 * subclass for JMS 1.0.2 providers.</b>
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see javax.jms.MessageConsumer#setMessageListener
 * @see DefaultMessageListenerContainer
 * @see org.springframework.jms.listener.serversession.ServerSessionMessageListenerContainer
 * @see SimpleMessageListenerContainer102
 */
public class SimpleMessageListenerContainer extends AbstractMessageListenerContainer {

	private boolean pubSubNoLocal = false;

	private int concurrentConsumers = 1;

	private TaskExecutor taskExecutor;

	private Set sessions;

	private Set consumers;


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
	 * Set the Spring TaskExecutor to use for executing the listeners.
	 * Default is none, that is, to run in the JMS provider's own receive thread,
	 * blocking the provider's receive endpoint while executing the listener.
	 * <p>Specify a TaskExecutor for integration with an existing thread pool,
	 * executing the listener with a received message in a different thread -
	 * taken from the thread pool, rather than blocking the JMS provider.
	 * @see #setConcurrentConsumers
	 * @see org.springframework.core.task.SimpleAsyncTaskExecutor
	 * @see org.springframework.scheduling.commonj.WorkManagerTaskExecutor
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}


	/**
	 * Validates this instance's configuration.
	 */
	public void afterPropertiesSet() {
		if (this.concurrentConsumers <= 0) {
			throw new IllegalArgumentException("concurrentConsumers value must be at least 1 (one)");
		}
		if (getDurableSubscriptionName() != null && this.concurrentConsumers != 1) {
			throw new IllegalArgumentException("Only 1 concurrent consumer supported for durable subscription");
		}

		super.afterPropertiesSet();
	}


	//-------------------------------------------------------------------------
	// Implementation of AbstractMessageListenerContainer's template methods
	//-------------------------------------------------------------------------

	/**
	 * Always use a shared JMS Connection.
	 */
	protected final boolean sharedConnectionEnabled() {
		return true;
	}

	/**
	 * Creates the specified number of concurrent consumers,
	 * in the form of a JMS Session plus associated MessageConsumer.
	 * @see #createListenerConsumer
	 */
	protected void registerListener() throws JMSException {
		this.sessions = new HashSet(this.concurrentConsumers);
		this.consumers = new HashSet(this.concurrentConsumers);
		for (int i = 0; i < this.concurrentConsumers; i++) {
			Session session = createSession(getConnection());
			MessageConsumer consumer = createListenerConsumer(session);
			this.sessions.add(session);
			this.consumers.add(consumer);
		}
	}

	/**
	 * Create a MessageConsumer for the given JMS Session,
	 * registering a MessageListener for the specified listener.
	 * @param session the JMS Session to work on
	 * @return the MessageConsumer
	 * @throws JMSException if thrown by JMS methods
	 * @see #executeListener
	 */
	protected MessageConsumer createListenerConsumer(final Session session) throws JMSException {
		Destination destination = getDestination();
		if (destination == null) {
			destination = resolveDestinationName(session, getDestinationName());
		}
		MessageConsumer consumer = createConsumer(session, destination);
		if (this.taskExecutor != null) {
			consumer.setMessageListener(new MessageListener() {
				public void onMessage(final Message message) {
					taskExecutor.execute(new Runnable() {
						public void run() {
							executeListener(session, message);
						}
					});
				}
			});
		}
		else {
			consumer.setMessageListener(new MessageListener() {
				public void onMessage(Message message) {
					executeListener(session, message);
				}
			});
		}
		return consumer;
	}

	/**
	 * Destroy the registered JMS Sessions and associated MessageConsumers.
	 */
	protected void destroyListener() throws JMSException {
		logger.debug("Closing JMS MessageConsumers");
		for (Iterator it = this.consumers.iterator(); it.hasNext();) {
			MessageConsumer consumer = (MessageConsumer) it.next();
			JmsUtils.closeMessageConsumer(consumer);
		}
		logger.debug("Closing JMS Sessions");
		for (Iterator it = this.sessions.iterator(); it.hasNext();) {
			Session session = (Session) it.next();
			JmsUtils.closeSession(session);
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
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
		// Only pass in the NoLocal flag in case of a Topic:
		// Some JMS providers, such as WebSphere MQ 6.0, throw IllegalStateException
		// in case of the NoLocal flag being specified for a Queue.
		if (destination instanceof Topic) {
			if (getDurableSubscriptionName() != null) {
				return session.createDurableSubscriber(
						(Topic) destination, getDurableSubscriptionName(), getMessageSelector(), isPubSubNoLocal());
			}
			else {
				return session.createConsumer(destination, getMessageSelector(), isPubSubNoLocal());
			}
		}
		else {
			return session.createConsumer(destination, getMessageSelector());
		}
	}

}
