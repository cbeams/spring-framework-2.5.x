/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * The callback interface used by JmsSender.
 * This interface creates a JMS message given a session, provided
 * by the JmsSender.  
 *
 * <p>Implementations <i>do not</i> need to concern themselves with
 * checked JMSException (from javax.jms) that may be thrown from 
 * operations they attempt.  The JmsSender will catch and handle 
 * these JMSExceptions appropriately.
 *
 * If extra parameters need to be set, such as the delivery mode, priority or time
 * to live, override any of the getters with your own implementation.
 */
public interface MessageCreator {

	/**
	 * Implement this method to return a message to be sent.
	 *
	 * @param The JMS session
	 * @return The message to be sent.
	 * @throws The JMS Checked Exception.  Do not catch it, it will be
	 * handled correctly by the JmsSender.
	 */
	public abstract Message createMessage(Session session) throws JMSException;


}
