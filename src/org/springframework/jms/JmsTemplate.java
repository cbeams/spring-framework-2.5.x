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

import javax.jms.Destination;

import org.springframework.jms.support.JmsAdmin;

/**
 * A helper class that simplifies sending of JMS messages.
 * Implemented by JmsTemplate102 that uses the JMS 1.0.2 specification and
 * JmsTemplate11 that uses the JMS 1.1 specification.   
 * 
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 * 
 */
public interface JmsTemplate {

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
     * @param action callback object that specified the action.
     * @throws JmsException
     */
    public void execute(JmsSenderCallback action)
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
     * Ssend to the default destination, if sepcified .  If no default destination is
     * specified, do not throw an exception, but log a WARNING.
     * @param messageCreator callback to create a message.
     * @throws JmsException if the provider fails to send the message.  Converted checked JMSException
     * to unchecked.
     */
    public void send(MessageCreator messageCreator) throws JmsException;
    
    
    /**
     * Set the destination to be used on send operations that do not have a destination parameter.
     * @param d the destination to send messages to when no destination is specified.
     */
    public void setDefaultDestination(Destination d);
    
    /**
     * Set the delivery mode to use when sending a message.  Since a default value may be defined administratively,
     * it is only used when isExplicitQosEnabled equals true
     * 
     * @param deliveryMode the delivery mode to use
     * 
     */
    public void setDeliveryMode(int deliveryMode);
    
    /**
     * Set the priority of the message when sending.  Since a default value may be defined
     * administratively, it is only used when isExplicitQosEnabled equals true.  
     * @param priority
     */
    public void setPriority(int priority);
    
    
    /**
     * Set the timetoLive of the message when sending.  Since a default value may be defined
     * administratively, it is only used when isDefaultQosEnabled equals true
     * @param timeToLive the message's lifetime (in milliseconds)
     */
    public void setTimeToLive(long timeToLive);
    
    /**
     * If true, then the values of deliveryMode, priority, and timeToLive will be used when sending a message.
     * Otherwise, the default values, that may be set administratively, will be used.
     *
     * @return true if overriding default values of QOS parameters (deliveyrMode, priority, and timeToLive)
     */
    public boolean isExplicitQosEnabled();
    
    
    //TODO  send(Object), send(Destination, Object) send(Dest, Object, Map, map)
    
    //TODO  receive(Destination, timeout)
    
    //TODO  Message sendRequestReply(Destination, MessageCreator, timeout)

}
