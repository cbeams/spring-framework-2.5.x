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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * An implementation of the JmsTemplate interface that uses the JMS 1.1 specification.
 * 
 * <b>The automatic creation of dynamic destinations is turned off by default. </b> Use
 * {@link AbstractJmsTemplate#setEnabledDynamicDestinations(boolean) setEnabledDynamicDestinations}to enable this
 * functionality.
 * 
 * If dynamic destination creation is enabled, you must specify the type of JMS destination to create using the method
 * {@link AbstractJmsTemplate#setPubSubDomain(boolean) setPubSubDomain}.
 * 
 * 
 * @version $Id: JmsTemplate11.java,v 1.6 2004-07-15 16:10:05 markpollack Exp $
 * @author Mark Pollack
 */
public class JmsTemplate11 extends AbstractJmsTemplate {

    /**
     * Construct a new JmsTemplate for bean usage.
     * Note: The ConnectionFactory has to be set before using the instance.
     * This constructor can be used to prepare a JmsTemplate via a BeanFactory,
     * typically setting the ConnectionFactory via setConnectionFactory.
     * @see #setConnectionFactory
     */
    public JmsTemplate11() {
    }

    /**
     * Construct a new JmsTemplate, given a ConnectionFactory to obtain connections from.
     * @param cf JMS ConnectionFactory to obtain connections from
     */
    public JmsTemplate11(ConnectionFactory cf) {
        setConnectionFactory(cf);
        afterPropertiesSet();
    }

    public void send(MessageCreator messageCreator) {
        if (this.getDefaultDestination() == null) {
            logger.warn(
                "No Default Destination specified. Check configuration of JmsSender");
            return;
        } else {
            send(null, getDefaultDestination(), true, messageCreator);
        }

    }

    public void send(String destinationName, MessageCreator messageCreator)
        throws JmsException {
        send(destinationName, null, false, messageCreator);
    }

    public void send(Destination d, MessageCreator messageCreator) {
        send(null, d, true, messageCreator);

    }

    public Object execute(ProducerCallback action) throws JmsException {
        Connection connection = null;
        try {
            connection = getConnectionFactory().createConnection();
            Session session =
                connection.createSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());

            MessageProducer producer = session.createProducer(null);

            return action.doInJms(session, producer);

        } catch (JMSException e) {
            throw convertJMSException("Call JmsSenderCallback", e);
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

    public Object execute(SessionCallback action) throws JmsException {
        Connection connection = null;
        try {
            connection = getConnectionFactory().createConnection();
            Session session =
                connection.createSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());
            return action.doInJms(session);

        } catch (JMSException e) {
            throw convertJMSException("Call SessionCallback", e);
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
     * Send a message on a destination optionally using values for deliveryMode, priority, and timetoLive.
     * 
     * @param destinationName
     *            the queue to send the message to
     * @param destination
     *            the destination to send the message to
     * @param explicitDestination
     *            if true, use the javax.jms.Destination parameter, otherwise lookup the javax.jms.Destination using the
     *            string destinationName.
     * @param messageCreator
     *            the message creator callback used to create a message
     * @param ignoreQOS
     *            if true, send the message using default values of the deliveryMode, priority, and timetoLive. Some
     *            vendors administration options let you set these values.
     * @param deliveryMode
     *            the delivery mode to use.
     * @param priority
     *            the priority for this message.
     * @param timetoLive
     *            the message's lifetime, in milliseconds.
     */
    private void send(
        String destinationName,
        Destination destination,
        boolean explicitDestination,
        MessageCreator messageCreator) {

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
                        isDynamicDestinationEnabled(),
                        isPubSubDomain());
            }

            MessageProducer producer = session.createProducer(destination);

            Message message = messageCreator.createMessage(session);
            if (logger.isInfoEnabled()) {
                logger.info("Message created was [" + message + "]");
            }
            if (isExplicitQosEnabled()) {
                producer.send(
                    message,
                    getDeliveryMode(),
                    getPriority(),
                    getTimeToLive());
            } else {
                producer.send(message);
            }
            if (isSessionTransacted()) {
                session.commit();
            }

        } catch (JMSException e) {
            throw convertJMSException(
                "Send message on destinaton name = " + destinationName,
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

    public void send(Destination d, final Object o) {
        if (this.getJmsConverter() == null) {
            logger.warn("No JmsConverter. Check configuration of JmsSender");
            return;
        } else {
            send(d, new MessageCreator() {
                public Message createMessage(Session session)
                    throws JMSException {
                    return getJmsConverter().toMessage(o, session);
                }
            });
        }
    }

    public void send(String d, final Object o) {
        if (this.getJmsConverter() == null) {
            logger.warn("No JmsConverter. Check configuration of JmsSender");
            return;
        } else {
            send(d, new MessageCreator() {
                public Message createMessage(Session session)
                    throws JMSException {
                    return getJmsConverter().toMessage(o, session);
                }
            });
        }
    }

    public void send(
        Destination d,
        final Object o,
        final MessagePostProcessor postProcessor) {
        if (this.getJmsConverter() == null) {
            logger.warn("No JmsConverter. Check configuration of JmsSender");
            return;
        } else {
            send(d, new MessageCreator() {
                public Message createMessage(Session session)
                    throws JMSException {
                    Message m = getJmsConverter().toMessage(o, session);
                    return postProcessor.postProcess(m);
                }
            });
        }
    }

    public void send(
        String d,
        final Object o,
        final MessagePostProcessor postProcessor) {
        if (this.getJmsConverter() == null) {
            logger.warn("No JmsConverter. Check configuration of JmsSender");
            return;
        } else {
            send(d, new MessageCreator() {
                public Message createMessage(Session session)
                    throws JMSException {
                    Message m = getJmsConverter().toMessage(o, session);
                    return postProcessor.postProcess(m);
                }
            });
        }
    }

    public Message receive(final Destination d, final long timeout) {
        Message returnMessage = null;
        execute(new SessionCallback() {
            public Object doInJms(Session session) throws JMSException {
                MessageConsumer mc = session.createConsumer(d);
                return mc.receive(timeout);
            }
        });
        return returnMessage;
    }

    public Message receive(final String destinationName, final long timeout) {
        final Destination destination =
            (Destination) getJmsAdmin().lookup(
                destinationName,
                isDynamicDestinationEnabled(),
                isPubSubDomain());
        Message returnMessage = (Message) execute(new SessionCallback() {
            public Object doInJms(Session session) throws JMSException {
                MessageConsumer mc = session.createConsumer(destination);
                System.out.println("Receiving on " + destination);
                return mc.receive(timeout);
            }
        });
        return returnMessage;
    }

}