/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.ConnectionFactory;

/**
 * A helper class that simplifies sending of JMS messages.
 * Implemented by JmsSender102 that uses the JMS 1.0.2 specification and
 * JmsSender11 that uses the JMS 1.1 specification.   
 * 
 */
public interface JmsSender {
	
	/**
	 * Get the connection factory to obtain JMS connections.
	 * @return the JMS connection factory.
	 */
	ConnectionFactory getConnectionFactory();
	
	/**
	 * Set the connection factory to obtain JMS connections
	 * from.
	 * @param cf The JMS connection factory.
	 */
	void setConnectionFactory(ConnectionFactory cf);

	/**
	 * Send a message to a supplied JMS destination.
	 * @param jndiDestinationName
	 * @param messageCreator 
	 * @throws JmsException spring converted JmsException
	 */
	public void send(final String jndiDestinationName,
					 final MessageCreator messageCreator) throws JmsException;
	
	/*
	//Send to a specified destination.  useful when replying to the destination
	// in an incoming message specified in the JMSReplyTo message property.
	public void send(Destination d, MessageCreator messageCreator) {
	}
	
	//Send overriding default values for deliverymode, priority and timetolive.
	public void send(
		Destination d,
		MessageCreator messageCreator,
		int deliveryMode,
		int priority,
		long timeToLive) {
	}

	//Send to the default destination of the MessageProducer.
	public void send(MessageCreator messageCreator){
	}
	*/
				 
					 
}
