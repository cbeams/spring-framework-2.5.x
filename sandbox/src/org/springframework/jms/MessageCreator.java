/*
 * Created on Apr 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.springframework.jms;

import javax.jms.Message;
import javax.jms.Session;

/**
 * The callback interface used by JmsSender.
 * This interface creates a JMS message given a session, provided
 * by the JmsSender.  
 *
 * <p>Implementations <i>do not</i> need to concern themselves with
 * JmsException that may be thrown from operations they attempt.
 * The JmsSender will catch and handle JmsExceptions appropriately.* 
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
	 * @throws The spring JmsException
	 */
	public abstract Message createMessage(Session session) throws JmsException;


}
