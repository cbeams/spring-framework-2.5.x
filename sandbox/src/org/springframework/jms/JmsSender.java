/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;



/**
 * A helper class that simplifies sending of JMS messages.
 * Implemented by JmsSender102 that uses the JMS 1.0.2 specification and
 * JmsSender11 that uses the JMS 1.1 specification.   
 * 
 */
public interface JmsSender {
	
    /**
     * Send a message to a JMS destination.  The callback gives access to
     * the JMS session and MessageProducer in order to do more complex
     * send operations, 
     * @param destinationName
     * @param callback
     * @throws JmsException
     */
    public void send(String destinationName, JmsSenderCallback callback) throws JmsException;

	/**
	 * Send a message to a supplied JMS destination.
	 * @param destinationName The destination name is looked up first looked up in JNDI.
     * If it is not found then a call to 
	 * @param messageCreator 
	 * @throws JmsException converted checked JMSException to unchecked.
	 */
	public void send(final String destinationName,
					 final MessageCreator messageCreator) throws JmsException;
	
	/*
	//Send to a specified destination.  useful when replying to the destination
	// in an incoming message specified in the JMSReplyTo message property or
	// sending to a destination created at runtime.
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
