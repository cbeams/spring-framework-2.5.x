/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import org.springframework.jms.support.JmsAdmin;



/**
 * A helper class that simplifies sending of JMS messages.
 * Implemented by JmsSender102 that uses the JMS 1.0.2 specification and
 * JmsSender11 that uses the JMS 1.1 specification.   
 * 
 */
public interface JmsSender {
	
	/**
	 * Get the administration helper class.  Used to support dynamic
	 * destination functionality.
	 * @return the JMS administration helper class.
	 */
	public JmsAdmin getJmsAdmin();
	
	/**
	 * Set the administration helper class.  
	 * @param admin the JMS administhration helper class.
	 */
	public void setJmsAdmin(JmsAdmin admin);
	
	/**
	 * Execute the action specified by the given action object within a 
	 * JMS Session.  
	 * @param action callback object that specifies the action
	 * @throws JmsException if there is any problem
	 */
	public void execute(SessionCallback action) throws JmsException;
	
	/**
	 * Execute the action specified by the given action object within
	 * a JMS TopicSession.  This version of execute should be used with
	 * 1.0.2 providers so topic specific methods are available to the
	 * action. 
	 * @param action callback object that specifies the action
	 * @throws JmsException if there is any problem
	 */
	public void execute(TopicSessionCallback action) throws JmsException;
	
	/**
	 * Execute the action specified by the given action object within
	 * a JMS TopicSession.  This version of execute should be used with
	 * 1.0.2 providers so queue specifici methods are availale to the action.
	 * The template method execute(SessionCallback) should be used with
	 * JMS 1.1 providers.
	 * @param action callback object that specifies the action.
	 * @throws JmsException if there is any problem
	 */
	public void execute(QueueSessionCallback action) throws JmsException;
	
    /**
     * Send a message to a JMS destination.  The callback gives access to
     * the JMS session and MessageProducer in order to do more complex
     * send operations.
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
					 
	/**
	 * Send a message the a JMS Destination.
	 * @param destinationName The name of the destination to resolve using JNDI
	 * 
	 * @param messageCreator
	 * @param deliveryMode
	 * @param priority
	 * @param timetoLive
	 * @throws JmsException
	 */
	/*
	public void send(String destinationName,
					 MessageCreator messageCreator, 
					 int deliveryMode,
					 int priority,
					 int timetoLive) throws JmsException					 
	*/
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
