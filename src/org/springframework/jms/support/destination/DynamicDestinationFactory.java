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

package org.springframework.jms.support.destination;

import javax.jms.Queue;
import javax.jms.Topic;

import org.springframework.jms.JmsException;

/**
 * Interface that encapsulates the creation of dynamic JMS Destinations.
 * Used by DestionationResolver to allow for creating dynamic destinations
 * without tight coupling to the calling code.
 *
 * <p>Implemented by JmsTemplate, which passed itself to a DestinationResolver.
 *
 * @author Juergen Hoeller
 * @since 20.07.2004
 * @see DynamicDestinationResolver
 * @see org.springframework.jms.core.JmsTemplate
 */
public interface DynamicDestinationFactory {

	/**
	 * Create a new dynamic JMS Queue.
	 * @param queueName the name of the queue
	 * @return the new Queue
	 * @throws JmsException if creation failed
	 */
	Queue createQueue(String queueName) throws JmsException;

	/**
	 * Create a new dynamic JMS Topic.
	 * @param topicName the name of the topic
	 * @return the new Topic
	 * @throws JmsException if creation failed
	 */
	Topic createTopic(String topicName) throws JmsException;

}
