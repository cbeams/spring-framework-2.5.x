/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Callback interface for JMS code.  To be used with JmsSender's execute
 * method, often as an anonymous class within a method implementation. 
 * The typical implementatino will perform multiple operations on the 
 * JMS Session 
 *
 * @author Mark Pollack
 */
public interface SessionCallback
{
    void doInJms(Session session) throws JMSException;
}
