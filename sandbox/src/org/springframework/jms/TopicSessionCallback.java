/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.JMSException;
import javax.jms.TopicSession;

/**
 * Callback interface for JMS code.  To be used with JmsSender's execute
 * method, often as an anonymous class within a method implementation. 
 * The typical implementatino will perform multiple operations on the 
 * JMS QueueSession.  This is useful when using a JMS 1.0.2 provider
 * to access functionality specific to the topic messaging domain.  
 * The JMS 1.1 added these domain specific methods at the Session level.  
 *
 * @author Mark Pollack
 */
public interface TopicSessionCallback
{
    void doInJms(TopicSession session) throws JMSException;
}
