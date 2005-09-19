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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.springframework.jms.support.JmsUtils;

/**
 * @author Juergen Hoeller
 * @since 1.3
 */
public class SimpleMessageListenerContainer extends AbstractMessageListenerContainer {

	private boolean pubSubNoLocal = false;

	private int concurrentConsumers = 1;

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
	public boolean isPubSubNoLocal() {
		return pubSubNoLocal;
	}

	public void setConcurrentConsumers(int concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
	}


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


	protected MessageConsumer createListenerConsumer(final Session session) throws JMSException {
		Destination destination = getDestination();
		if (destination == null) {
			destination = resolveDestinationName(session, getDestinationName());
		}
		MessageConsumer consumer = createConsumer(session, destination);

		consumer.setMessageListener(new MessageListener() {
			public void onMessage(Message message) {
				executeListener(session, message);
			}
		});

		return consumer;
	}


	/**
	 * Create a JMS MessageConsumer for the given Session and Destination.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param session the JMS Session to create a MessageConsumer for
	 * @param destination the JMS Destination to create a MessageConsumer for
	 * @return the new JMS MessageConsumer
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
		return session.createConsumer(destination, getMessageSelector(), isPubSubNoLocal());
	}

}
