/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.Destination;

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
     * 
     * When used with a 1.0.2 provider, you may need to downcast
     * to the appropriate domain implementation, either QueueSession or
     * TopicSession in the action objects doInJms callback method.
     * 
     * Note: The value of isPubSubDomain affects the behavior
     * of this method.  If isPubSubDomain equals true, then a TopicSession
     * is passed to the callback.  If false, then a QueueSession is passed to
     * the callback.  
     * 
     *
     * @param action callback object that specifies the action
     * @throws JmsException if there is any problem
     */
    public void execute(SessionCallback action) throws JmsException;

    /**
     * Send a message to a JMS destination.  The callback gives access to
     * the JMS session and MessageProducer in order to do more complex
     * send operations.
     * @param destinationName
     * @param callback
     * @throws JmsException
     */
    public void send(String destinationName, JmsSenderCallback callback)
        throws JmsException;

    /**
     * Send a message to a JMS destination.  The messageCreator callback creates
     * the message given a session.
     * @param destinationName The destination name can refer to
     * either the a dynamic destination or one residing in JNDI.   
     * @param messageCreator callback to create a message.
     * @throws JmsException converted checked JMSException to unchecked.
     */
    public void send(
        final String destinationName,
        final MessageCreator messageCreator)
        throws JmsException;

    /**
     * Send a message the a JMS Destination with additional quality of service
     * parameters.  The messageCreator callback creates the message give the session.
     * @param destinationName The destination name can refer to
     * either the a dynamic destination or one residing in JNDI.   
     * 
     * @param messageCreator callback to create a message.
     * @param deliveryMode the delivery mode to use
     * @param priority the priority for this message
     * @param timetoLive the message's lifetime (in milliseconds)
     * @throws JmsException converted checked JMSException to unchecked.
     */
    public void send(
        String destinationName,
        MessageCreator messageCreator,
        int deliveryMode,
        int priority,
        long timetoLive)
        throws JmsException;

    /**
     * Send to the specified destination.  This method is useful when replying to 
     * the destination in an incoming message specified in the JMSReplyTo message 
     * property.
     * @param d the destination to send this message to
     * @param messageCreator callback to create a message.
     * @throws JmsException converted checked JMSException to unchecked.
     */
    public void send(Destination d, MessageCreator messageCreator)
        throws JmsException;

    /**
     * Send to the specified destination with additional quality of service
     * parameters.  This method is useful when replying to 
     * the destination in an incoming message specified in the JMSReplyTo message 
     * property.  The messageCreator callback creates the message give the session.
     * 
     * @param d the destination to send this message to
     * @param messageCreator callback to create a message.
     * @param deliveryMode the delivery mode to use
     * @param priority the priority for this message
     * @param timetoLive the message's lifetime (in milliseconds)
     * @throws JmsException converted checked JMSException to unchecked.
     */
    public void send(
        Destination d,
        MessageCreator messageCreator,
        int deliveryMode,
        int priority,
        long timeToLive);

    /*
    //Send to the default destination of the MessageProducer.
    public void send(MessageCreator messageCreator){
    }
    */

}
