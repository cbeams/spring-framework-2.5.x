/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

/**
 * An implementation of the JmsSender interface that uses the 
 * JMS 1.0.2 specification.  You must specify the domain or
 * style of messaging to be either Point-to-Point (Queues) 
 * or Publish/Subscribe(Topics) using the method
 * {@link AbstractJmsSender#setPubSubDomain(boolean) setPubSubDomain}
 * 
 *
 * <b>The default JMS domain is the Point-to-Point domain (Queues).</b>   
 * 
 * This boolean property is an implementation detail due to the use of similar
 * but seperate class heirarchies in the JMS 1.0.2 API.  JMS 1.1
 * provides a new domain independent API that allows for easy 
 * mix-and-match use of Point-to-Point and Publish/Subscribe domain.
 * 
 * <b>The automatic creation of dynamic destinations is turned off by
 * default.</b>  Use
 * {@link AbstractJmsSender#setEnabledDynamicDestinations(boolean) setEnabledDynamicDestinations}
 * to enable this functionality.
 */
public class JmsSender102 extends AbstractJmsSender {

    /**
     * Construct a new JmsSender for bean usage.
     * Note: The ConnectionFactory has to be set before using the instance.
     * This constructor can be used to prepare a JmsSender via a BeanFactory,
     * typically setting the ConnectionFactory via setConnectionFactory.
     * @see #setConnectionFactory
     */
    public JmsSender102() {
    }

    public void send(
        String destinationName,
        MessageCreator messageCreator,
        int deliveryMode,
        int priority,
        int timetoLive)
        throws JmsException {
        if (isPubSubDomain()) {
            sendPubSub(
                destinationName,
                messageCreator,
                false,
                deliveryMode,
                priority,
                timetoLive);
        } else {
            sendP2P(
                destinationName,
                messageCreator,
                false,
                deliveryMode,
                priority,
                timetoLive);
        }
    }

    public void send(String destinationName, MessageCreator messageCreator)
        throws JmsException {
        if (isPubSubDomain()) {
            sendPubSub(destinationName, messageCreator, true, 0, 0, 0);
        } else {
            sendP2P(destinationName, messageCreator, true, 0, 0, 0);
        }
    }

    /**
     * Send using the JMS 1.0.2 Topic class hierarchy.
     * @param topicName the name of the topic
     * @param messageCreator the message callback.
     */
    private void sendPubSub(
        String topicName,
        MessageCreator messageCreator,
        boolean ignoreQOS,
        int deliveryMode,
        int priority,
        int timetoLive) {
        TopicConnection topicConnection = null;
        try {
            topicConnection =
                ((TopicConnectionFactory) getConnectionFactory())
                    .createTopicConnection();
            TopicSession topicSession =
                topicConnection.createTopicSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());

            Topic topic =
                (Topic) getJmsAdmin().lookup(
                    topicName,
                    isEnabledDynamicDestinations(),
                    isPubSubDomain());

            TopicPublisher publisher = topicSession.createPublisher(topic);

            Message message = messageCreator.createMessage(topicSession);
            if (logger.isInfoEnabled()) {
                logger.info("Message created was [" + message + "]");
            }
            if (ignoreQOS) {
                publisher.publish(topic, message);
            } else {
                publisher.publish(
                    topic,
                    message,
                    deliveryMode,
                    priority,
                    timetoLive);
            }

        } catch (JMSException e) {
            throw new JmsException(
                "Could not send message on topic name = " + topicName,
                e);
        } finally {
            if (topicConnection != null) {
                try {
                    topicConnection.close();
                } catch (JMSException e) {
                    logger.warn("Failed to close the topic connection", e);
                }
            }
        }
    }

    /**
     * Send a message on a queue optionally using values for deliveryMode,
     * priority, and timetoLive.
     * @param queueName the queue to send the message to
     * @param messageCreator the message creator callback used to create a message
     * @param ignoreQOS if true, send the message using default values of the
     * deliveryMode, priority, and timetoLive.  Some vendors administration options
     * let you set these values.
     * @param deliveryMode the delivery mode to use.
     * @param priority the priority for this message.
     * @param timetoLive the message's lifetime, in milliseconds.
     */
    private void sendP2P(
        String queueName,
        MessageCreator messageCreator,
        boolean ignoreQOS,
        int deliveryMode,
        int priority,
        int timetoLive) {
        QueueConnection queueConnection = null;
        try {
            queueConnection =
                ((QueueConnectionFactory) getConnectionFactory())
                    .createQueueConnection();

            QueueSession queueSession =
                queueConnection.createQueueSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());

            Queue queue =
                (Queue) getJmsAdmin().lookup(
                    queueName,
                    isEnabledDynamicDestinations(),
                    isPubSubDomain());
            if (logger.isInfoEnabled()) {
                logger.info("Looked up queue with name [" + queueName + "]");
            }
            QueueSender queueSender = queueSession.createSender(queue);
            Message message = messageCreator.createMessage(queueSession);
            //TODO put in pretty printer.
            if (logger.isInfoEnabled()) {
                logger.info("Message created was [" + message + "]");
            }
            if (ignoreQOS) {

                queueSender.send(queue, message);
            } else {
                queueSender.send(
                    queue,
                    message,
                    deliveryMode,
                    priority,
                    timetoLive);
            }
            logger.info("Message sent OK");

        } catch (JMSException e) {
            throw new JmsException(
                "Could not send message on queue name = " + queueName,
                e);
        } finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {
                    logger.warn("Failed to close the queue connection", e);
                }
            }
        }
    }

    /**
     * In addition to checking if the connection factory is set, make sure
     * that the supplied connection factory is of the appropriate type for
     * the specified destination type.  QueueConnectionFactory for queues
     * and TopicConnectionFactory for topics.
     */
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        //Make sure that the ConnectionFactory passed is consistent
        //with sending on the specifies destination type.
        //Some providers implementations of the ConnectionFactory implement both
        //domain interfaces under the cover, so just check if the selected
        //domain is consistent with the type of connection factory.
        if (isPubSubDomain()) {
            if (getConnectionFactory() instanceof TopicConnectionFactory) {
                //Do nothing, all ok.
            } else {
                throw new IllegalArgumentException("Specified a Spring JMS 1.0.2 Sender for topics but did not supply an instance of a TopicConnectionFactory");
            }
        } else {
            if (getConnectionFactory() instanceof QueueConnectionFactory) {
                //Do nothing, all ok.
            } else {
                throw new IllegalArgumentException("Specified a Spring JMS 1.0.2 Sender for queues but did not supply an instance of a QueueConnectionFactory");
            }
        }

    }

    public void send(String destinationName, JmsSenderCallback callback)
        throws JmsException {
        // TODO Auto-generated method stub

    }

    public void execute(SessionCallback action) throws JmsException {
        throw new RuntimeException("not yet impl.");
        // TODO Auto-generated method stub

    }

    public void execute(TopicSessionCallback action) throws JmsException {
        throw new RuntimeException("not yet impl.");
        // TODO Auto-generated method stub

    }

    public void execute(QueueSessionCallback action) throws JmsException {
        throw new RuntimeException("not yet impl.");
        // TODO Auto-generated method stub

    }

}
