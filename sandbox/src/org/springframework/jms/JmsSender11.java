/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * An implementation of the JmsSender interface that uses the 
 * JMS 1.1 specification.
 * 
 * <b>The automatic creation of dynamic destinations is turned off by
 * default.</b>  Use
 * {@link AbstractJmsSender#setEnabledDynamicDestinations(boolean) setEnabledDynamicDestinations}
 * to enable this functionality.
 * 
 * If dynamic destination creation is enabled, you must specify the type of
 * JMS destination to create using the method
 * {@link AbstractJmsSender#setPubSubDomain(boolean) setPubSubDomain}.  
 *  
 * 
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class JmsSender11 extends AbstractJmsSender {

	public void send(String destinationName, MessageCreator messageCreator)
		throws JmsException {
		Connection connection = null;
		try {
			connection = getConnectionFactory().createConnection();
			Session session =
				connection.createSession(
					isSessionTransacted(),
					getSessionAcknowledgeMode());
			Destination dest =
				(Destination) getJmsAdmin().lookup(
					destinationName,
					isEnabledDynamicDestinations(),
					isPubSubDomain());
			if (logger.isInfoEnabled()) {
				logger.info(
					"Looked up destination with name ["
						+ destinationName
						+ "]");
			}
			MessageProducer producer = session.createProducer(dest);

			Message message = messageCreator.createMessage(session);
			if (logger.isInfoEnabled()) {
				logger.info("Message created was [" + message + "]");
			}
			producer.send(dest, message);

		} catch (JMSException e) {
			throw new JmsException(
				"Could not send message on destinaton name = " + destinationName,
				e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (JMSException e) {
					logger.warn("Failed to close the connection", e);
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.springframework.jms.JmsSender#send(java.lang.String, org.springframework.jms.JmsSenderCallback)
	 */
	public void send(String destinationName, JmsSenderCallback callback)
		throws JmsException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.springframework.jms.JmsSender#execute(org.springframework.jms.SessionCallback)
	 */
	public void execute(SessionCallback action) throws JmsException {
		throw new RuntimeException("not yet impl.");
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.springframework.jms.JmsSender#execute(org.springframework.jms.TopicSessionCallback)
	 */
	public void execute(TopicSessionCallback action) throws JmsException {
		throw new RuntimeException("not yet impl.");
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.springframework.jms.JmsSender#execute(org.springframework.jms.QueueSessionCallback)
	 */
	public void execute(QueueSessionCallback action) throws JmsException {
		throw new RuntimeException("not yet impl.");
		// TODO Auto-generated method stub

	}

}
