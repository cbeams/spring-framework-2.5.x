/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.jms;

import java.util.Properties;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.NamingException;

/**
 * Provides common administration tasks.  Currenlty for centralizing
 * lookups of destinations and creation of dymamic queues.
 * Create a vendor specific implementation for you needs if the
 * supplied DefaultJmsAdmin is not sufficient.
 *
 * @author Mark Pollack
 */
public interface JmsAdmin {

	/**
	 * Create a dynamic JMS queue.
	 * @param queueInfo description of the queue to create
	 * @return description of the queue after creation.
	 */
	QueueInfo createQueue(QueueInfo queueInfo);

	/**
	 * Create a dynamic JMS topic
	 * @param topicInfo description of the topic to crate.
	 * @return description of the topic after creation.
	 */
	TopicInfo createTopic(TopicInfo topicInfo);

	/**
	 * Lookup JMS destinations.  If createDynamic is enabled, then
	 * the lookup will go against a cache of dynamic destination.  The
	 * last parameter determines what JMS destination type will be
	 * created.
	 *
	 * @param destinationName The name of a dynamic destination.
	 * @param createDynamic If true, create a dynamic destination if not
	 * found in JNDI.
	 * @param isPubSubDomain if true, create a Topic for a dynamic destination,
	 * otherwise create a Queue (point-to-point)
	 * @return The JMS destination object.  Null if not found.
	 */
	Destination lookup(String destinationName, boolean createDynamic, boolean isPubSubDomain);

	/**
	 * Perform a JNDI lookup of a JMS destination.
	 * @param destinationName
	 * @return The JMS destination object.
	 */
	Destination lookup(String destinationName) throws NamingException;

	/**
	 * Lookup topics that have been created with JmsAdmin
	 * @param topicName The name of a dynamic topic
	 * @return The JMS Topic.  Null if not found.
	 */
	Topic lookupDynamicTopic(String topicName);

	/**
	 * Lookup dynamic queues that have been created with JmsAdmin
	 * @param queueName The name of a dynamic queue.
	 * @return the JMS Queue.  Null if not found.
	 */
	Queue lookupDynamicQueue(String queueName);

	/**
	 * Set the JNDI environment to configure the lookup of JNDI
	 * resources.
	 * @param jndiEnvironment Properties to configure JNDI.
	 */
	void setJndiEnvironment(Properties jndiEnvironment);


}
