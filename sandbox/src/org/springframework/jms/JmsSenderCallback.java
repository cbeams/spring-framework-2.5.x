/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Callback interface for JMS code.  To be used with JmsSender's send method,
 * often as an anonymous class within a method implementation.  The typical
 * implementatino will perform multiple operations on the JMS Session and 
 * MessageProducer.   When used with a 1.0.2 provider, you need to downcast
 * to the appropriate domain implementation, either QueueSender or TopicPublisher,
 * to send a message.
 *
 * @author Mark Pollack
 */
public interface JmsSenderCallback
{
    void doInJms(Session session, MessageProducer msgProducer);
}
