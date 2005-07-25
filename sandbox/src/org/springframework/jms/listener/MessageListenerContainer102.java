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
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

/**
 * @author Juergen Hoeller
 * @since 1.3
 */
public class MessageListenerContainer102 extends MessageListenerContainer {

	private boolean pubSubDomain = false;


	/**
	 * Configure the MessageListenerContainer with knowledge of the
	 * JMS domain used. Default is Point-to-Point (Queues).
	 * <p>This tells the JMS provider which class hierarchy to use
	 * for Connections, ConnectionConsumers, and Sessions.
	 * @param pubSubDomain true for Publish/Subscribe domain (Topics),
	 * false for Point-to-Point domain (Queues)
	 */
	public void setPubSubDomain(boolean pubSubDomain) {
		this.pubSubDomain = pubSubDomain;
	}

	/**
	 * Return whether the Publish/Subscribe domain (Topics) is used.
	 * Otherwise, the Point-to-Point domain (Queues) is used.
	 */
	public boolean isPubSubDomain() {
		return pubSubDomain;
	}


	/**
	 * This implementation overrides the superclass method to use JMS 1.0.2 API.
	 */
	protected Connection createConnection() throws JMSException {
		if (isPubSubDomain()) {
			return ((TopicConnectionFactory) getConnectionFactory()).createTopicConnection();
		}
		else {
			return ((QueueConnectionFactory) getConnectionFactory()).createQueueConnection();
		}
	}

	/**
	 * This implementation overrides the superclass method to use JMS 1.0.2 API.
	 */
	protected ConnectionConsumer createConnectionConsumer(Connection con, ServerSessionPool pool) throws JMSException {
		if (isPubSubDomain()) {
			return ((TopicConnection) con).createConnectionConsumer(
					(Topic) getDestination(), getMessageSelector(), pool, getMaxMessages());
		}
		else {
			return ((QueueConnection) con).createConnectionConsumer(
					(Queue) getDestination(), getMessageSelector(), pool, getMaxMessages());
		}
	}

	/**
	 * This implementation overrides the superclass method to use JMS 1.0.2 API.
	 */
	protected Session createSession(Connection con) throws JMSException {
		if (isPubSubDomain()) {
			return ((TopicConnection) con).createTopicSession(isSessionTransacted(), getSessionAcknowledgeMode());
		}
		else {
			return ((QueueConnection) con).createQueueSession(isSessionTransacted(), getSessionAcknowledgeMode());
		}
	}

}
