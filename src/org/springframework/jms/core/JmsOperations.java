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

package org.springframework.jms.core;

import javax.jms.Destination;
import javax.jms.Message;

import org.springframework.jms.JmsException;

/**
 * Interface that implements a basic set of JMS operations.
 * Implemented by JmsTemplate102 that uses the JMS 1.0.2 specification and
 * JmsTemplate11 that uses the JMS 1.1 specification.  Not often used
 * but a useful option to enhance testability, as it can easily be 
 * mocked or stubbed.
 *
 * @author Mark Pollack
 */
public interface JmsOperations {

	/**
	 * Execute the action specified by the given action object within a
	 * JMS Session.
	 * <p>When used with a 1.0.2 provider, you may need to downcast
	 * to the appropriate domain implementation, either QueueSession or
	 * TopicSession in the action objects doInJms callback method.
	 * <p>Note: The value of isPubSubDomain affects the behavior of this method.
	 * If isPubSubDomain equals true, then a TopicSession is passed to the callback.
	 * If false, then a QueueSession is passed to the callback.
	 * @param action callback object that exposes the session.
	 * @return The result object from working with the session.
	 * @throws JmsException if there is any problem
	 */
	public Object execute(SessionCallback action) throws JmsException;

	/**
	 * Send a message to a JMS destination.  The callback gives access to
	 * the JMS session and MessageProducer in order to do more complex
	 * send operations.
	 * @param action callback object that exposes the session/producer pair.
	 * @return The result object from working with the session.
	 * @throws JmsException checked JMSException converted to unchecked
	 */
	public Object execute(ProducerCallback action) throws JmsException;


	/**
	 * Send a message to the default destination.
	 * <p>This will only work with a default destination specified!
	 * @param messageCreator callback to create a message.
	 * @throws JmsException checked JMSException converted to unchecked
	 */
	public void send(MessageCreator messageCreator) throws JmsException;

	/**
	 * Send to the specified destination.  This method is useful when replying to
	 * the destination in an incoming message specified in the JMSReplyTo message
	 * property.
	 * @param destination the destination to send this message to
	 * @param messageCreator callback to create a message.
	 * @throws JmsException checked JMSException converted to unchecked
	 */
	public void send(Destination destination, MessageCreator messageCreator) throws JmsException;

	/**
	 * Send a message to a JMS destination. The MessageCreator callback creates
	 * the message given a session.
	 * @param destinationName The destination name can refer to
	 * either a dynamic destination or one residing in JNDI.
	 * @param messageCreator callback to create a message.
	 * @throws JmsException checked JMSException converted to unchecked
	 */
	public void send(String destinationName, MessageCreator messageCreator) throws JmsException;


	/**
	 * Send an object to the default destination, converting the object
	 * to a JMS message with the template's converter.
	 * <p>This will only work with a default destination specified!
	 * @param message the object to convert to a message
	 * @throws JmsException converted checked JMSException to unchecked
	 */
	public void convertAndSend(Object message) throws JmsException;

	/**
	 * Send an object to the destination, converting the object to a JMS message
	 * with the template's converter.
	 * @param destination the destination to send this message to
	 * @param message the object to convert to a message
	 * @throws JmsException converted checked JMSException to unchecked
	 */
	public void convertAndSend(Destination destination, Object message) throws JmsException;

	/**
	 * Send an object, converting to a JMS message, to the given destination.
	 * @param destination the destination to send this message to.
	 * A JNDI destination or a dynamic destination if enabled.
	 * @param message the object to convert to a message
	 * @throws JmsException checked JMSException converted to unchecked
	 */
	public void convertAndSend(String destination, Object message) throws JmsException;

	/**
	 * Send an object to the default destination, converting the object to a JMS message.
	 * The callback allows for modification of the message after conversion.
	 * <p>This will only work with a default destination specified!
	 * @param message the object to convert to a message
	 * @param postProcessor the callback to modify the message
	 * @throws JmsException checked JMSException converted to unchecked
	 */
	public void convertAndSend(Object message, MessagePostProcessor postProcessor)
	    throws JmsException;

	/**
	 * Send an object to the destination, converting the object to a JMS message.
	 * The callback allows for modification of the message after conversion.
	 * @param destination the destination to send this message to
	 * @param message the object to convert to a message
	 * @param postProcessor the callback to modify the message
	 * @throws JmsException checked JMSException converted to unchecked
	 */
	public void convertAndSend(Destination destination, Object message, MessagePostProcessor postProcessor)
	    throws JmsException;

	/**
	 * Send an object to the destination, converting the object to a JMS message.
	 * The callback allows for modification of the message after conversion.
	 * @param destination the destination to send this message to.
	 * A JNDI destination or a dynamic destination if enabled.
	 * @param message the object to convert to a message.
	 * @param postProcessor the callback to modify the message
	 * @throws JmsException checked JMSException converted to unchecked
	 */
	public void convertAndSend(String destination, Object message, MessagePostProcessor postProcessor)
	    throws JmsException;


	/**
	 * Receive a message synchronously but only wait up to a specified time
	 * for delivery.
	 * This method should be used carefully, since it will block the thread
	 * until the message becomes available or until the timeout value is exceeded.
	 * @param destination The destination to receive a message from.
	 * @param timeout the timeout value (in milliseconds)
	 * @return the message produced for the consumer or null if the timeout expires.
	 * @throws JmsException checked JMSException converted to unchecked
	 */
	public Message receive(Destination destination, long timeout) throws JmsException;

	/**
	 * Receive a message synchronously but only wait up to a specified time for
	 * delivery.  This method should be used carefully, since it will block the
	 * thread until the message becomes available or until the timeout value is exceeded.
	 * @param destination The destination to receive a message from.   The name can refer to
	 * either a dynamic destination or one residing in JNDI.
	 * @param timeout the timeout value (in milliseconds)
	 * @return the message produced for the consumer or null if the timeout expires.
	 * @throws JmsException checked JMSException converted to unchecked
	 */
	public Message receive(String destination, long timeout) throws JmsException;

}
