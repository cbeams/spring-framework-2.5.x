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

    public void send(
        String destinationName,
        MessageCreator messageCreator,
        int deliveryMode,
        int priority,
        long timetoLive)
        throws JmsException {
        send(
            destinationName,
            null,
            false,
            messageCreator,
            false,
            deliveryMode,
            priority,
            timetoLive);
    }

    public void send(String destinationName, MessageCreator messageCreator)
        throws JmsException {
        send(destinationName, null, false, messageCreator, true, 0, 0, 0);
    }

    public void send(Destination d, MessageCreator messageCreator)
        throws JmsException {
		send(null, d, true, messageCreator, true, 0, 0, 0);
    }

    public void send(
        Destination d,
        MessageCreator messageCreator,
        int deliveryMode,
        int priority,
        long timeToLive) {
        	
			send(null,
				 d,
				 true,
				 messageCreator,
				 false,
				 deliveryMode,
				 priority,
				 timeToLive);	

    }

    public void send(String destinationName, JmsSenderCallback callback)
        throws JmsException {
        // TODO Auto-generated method stub

    }

    public void execute(SessionCallback action) throws JmsException {
        Connection connection = null;
        try {
            connection = getConnectionFactory().createConnection();
            Session session =
                connection.createSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());
            action.doInJms(session);

        } catch (JMSException e) {
            throw new JmsException("Could not call SessionCallback", e);
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

    /**
     * Send a message on a destination optionally using values for deliveryMode,
     * priority, and timetoLive.
     * @param destinationName the queue to send the message to
     * @param destination the destination to send the message to
     * @param explicitDestination if true, use the javax.jms.Destination parameter, otherwise lookup
     * the javax.jms.Destination using the string destinationName.
     * @param messageCreator the message creator callback used to create a message
     * @param ignoreQOS if true, send the message using default values of the
     * deliveryMode, priority, and timetoLive.  Some vendors administration options
     * let you set these values.
     * @param deliveryMode the delivery mode to use.
     * @param priority the priority for this message.
     * @param timetoLive the message's lifetime, in milliseconds.
     */
    private void send(
        String destinationName,
        Destination destination,
        boolean explicitDestination,
        MessageCreator messageCreator,
        boolean ignoreQOS,
        int deliveryMode,
        int priority,
        long timetoLive) {

        Connection connection = null;
        try {
            connection = getConnectionFactory().createConnection();
            Session session =
                connection.createSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());

            if (!explicitDestination) {
                destination =
                    (Destination) getJmsAdmin().lookup(
                        destinationName,
                        isEnabledDynamicDestinations(),
                        isPubSubDomain());
            }
            if (logger.isInfoEnabled()) {
                logger.info(
                    "Looked up destination with name ["
                        + destinationName
                        + "]");
            }
            MessageProducer producer = session.createProducer(destination);

            Message message = messageCreator.createMessage(session);
            if (logger.isInfoEnabled()) {
                logger.info("Message created was [" + message + "]");
            }
            if (ignoreQOS) {
                producer.send(destination, message);
            } else {
                producer.send(
                    destination,
                    message,
                    deliveryMode,
                    priority,
                    timetoLive);
            }

        } catch (JMSException e) {
            throw new JmsException(
                "Could not send message on destinaton name = "
                    + destinationName,
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

}
