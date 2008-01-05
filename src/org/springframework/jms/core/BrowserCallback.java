package org.springframework.jms.core;

import javax.jms.Session;
import javax.jms.MessageProducer;
import javax.jms.JMSException;
import javax.jms.QueueBrowser;

/**
 * Created by IntelliJ IDEA.
 * User: Jürgen
 * Date: 04.01.2008
 * Time: 15:37:36
 * To change this template use File | Settings | File Templates.
 */
public interface BrowserCallback {

	/**
	 * Perform operations on the given {@link javax.jms.Session} and {@link javax.jms.MessageProducer}.
	 * <p>The message producer is not associated with any destination.
	 * @param session the JMS <code>Session</code> object to use
	 * @param producer the JMS <code>MessageProducer</code> object to use
	 * @return a result object from working with the <code>Session</code>, if any (can be <code>null</code>)
	 * @throws javax.jms.JMSException if thrown by JMS API methods
	 */
	Object doInJms(Session session, QueueBrowser browser) throws JMSException;

}
