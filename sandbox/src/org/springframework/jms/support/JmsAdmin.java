/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms.support;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * Provides commons administration tasks.  The creation 
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
	 * Destroy a dynamic JMS queue.
	 * @param queueName name of the queue to destroy.
	 */
	void destroyQueue(String queueName);
	
	/**
	 * Create a dynamic JMS topic
	 * @param topicInfo description of the topic to crate.
	 * @return description of the topic after creation.
	 */
	TopicInfo createTopic(TopicInfo topicInfo);
	
	/**
	 * Destroy a dynamic JMS topic
	 * @param topicName description of the topic to create
	 */
	void destroyTopic(String topicName);
	
	/**
	 * Lookup dynamic destinations that have been created with 
	 * JmsAdmin 
	 * @param destinationName The name of a dynamic destination.
	 * @return The JMS destination object.  Null if not found.
	 */
	Destination lookup(String destinationName);
	
	/**
	 * Lookup dynamic topics that have been created with JmsAdmin
	 * @param topicName The name of a dynamic topic
	 * @return The JMS Topic.  Null if not found.
	 */
	Topic lookupTopic(String topicName);
	
	/**
	 * Lookup dynamic queues that have been created with JmsAdmin
	 * @param queueName The name of a dynamic queue.
	 * @return the JMS Queue.  Null if not found.
	 */
	Queue lookupQueue(String queueName);

	
	
}
