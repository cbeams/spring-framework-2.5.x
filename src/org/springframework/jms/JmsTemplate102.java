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

import javax.jms.Destination;
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
 * An implementation of the JmsTemplate interface that uses the 
 * JMS 1.0.2 specification.  You must specify the domain or
 * style of messaging to be either Point-to-Point (Queues) 
 * or Publish/Subscribe(Topics) using the method
 * {@link AbstractJmsTemplate#setPubSubDomain(boolean) setPubSubDomain}
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
 * {@link AbstractJmsTemplate#setEnabledDynamicDestinations(boolean) setEnabledDynamicDestinations}
 * to enable this functionality.
 * 
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class JmsTemplate102 extends AbstractJmsTemplate {

    /**
     * Construct a new JmsTemplate for bean usage.
     * Note: The ConnectionFactory has to be set before using the instance.
     * This constructor can be used to prepare a JmsTemplate via a BeanFactory,
     * typically setting the ConnectionFactory via setConnectionFactory.
     * @see #setConnectionFactory
     */
    public JmsTemplate102() {
    }

    public void send(String destinationName, MessageCreator messageCreator)
        throws JmsException {
        if (isPubSubDomain()) {
            sendPubSub(destinationName, null, false, messageCreator);
        } else {
            sendP2P(destinationName, null, false, messageCreator);
        }
    }

    public void send(Destination d, MessageCreator messageCreator)
        throws JmsException {
        if (isPubSubDomain()) {
            sendPubSub(null, (Topic) d, true, messageCreator);
        } else {
            sendP2P(null, (Queue) d, true, messageCreator);
        }
    }

    public void send(MessageCreator messageCreator) throws JmsException {
        if (this.getDefaultDestination() == null) {
            logger.warn(
                "No Default Destination was specified. Check configuration of JmsSender");
            return;
        } else {
            send(getDefaultDestination(), messageCreator);
        }
    }

    /**
     * Send a message on a topic optionally using values for deliveryMode,
     * priority, and timetoLive.
     * @param topicName the topic to send the message to
     * @param topic the topic to send the message to
     * @param explicitDestination if true, use the javax.jms.Topic parameter, otherwise
     * resolve the javax.jms.Topic using the string topicName.
     * @param messageCreator the message creator callback used to create a message
     * @param ignoreQOS if true, send the message using default values of the
     * deliveryMode, priority, and timetoLive.  Some vendors administration options
     * let you set these values.
     * @param deliveryMode the delivery mode to use.
     * @param priority the priority for this message.
     * @param timetoLive the message's lifetime, in milliseconds.
     */
    private void sendPubSub(
        String topicName,
        Topic topic,
        boolean explicitDestination,
        MessageCreator messageCreator) {
        TopicConnection topicConnection = null;
        try {
            topicConnection =
                ((TopicConnectionFactory) getConnectionFactory())
                    .createTopicConnection();
            TopicSession topicSession =
                topicConnection.createTopicSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());
            if (!explicitDestination) {
                topic =
                    (Topic) getJmsAdmin().lookup(
                        topicName,
                        isDynamicDestinationEnabled(),
                        isPubSubDomain());
            }
            TopicPublisher publisher = topicSession.createPublisher(topic);

            Message message = messageCreator.createMessage(topicSession);
            if (logger.isInfoEnabled()) {
                logger.info("Message created was [" + message + "]");
            }
            if (isExplicitQosEnabled()) {
                publisher.publish(
                    topic,
                    message,
                    getDeliveryMode(),
                    getPriority(),
                    getTimeToLive());

            } else {
                publisher.publish(topic, message);
            }

        } catch (JMSException e) {
            throw convertJMSException(
                "Send message on topic name = " + topicName,
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
     * @param queue the queue to send the message to
     * @param explicitDestination if true, use the javax.jms.Queue parameter, otherwise resolve 
     * the javax.jms.Queue using the string destinationName.
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
        Queue queue,
        boolean explicitDestination,
        MessageCreator messageCreator) {
        QueueConnection queueConnection = null;
        try {
            queueConnection =
                ((QueueConnectionFactory) getConnectionFactory())
                    .createQueueConnection();

            QueueSession queueSession =
                queueConnection.createQueueSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());
            if (!explicitDestination) {
                queue =
                    (Queue) getJmsAdmin().lookup(
                        queueName,
                        isDynamicDestinationEnabled(),
                        isPubSubDomain());
            }
            if (logger.isInfoEnabled()) {
                logger.info("Looked up queue with name [" + queueName + "]");
            }
            QueueSender queueSender = queueSession.createSender(queue);
            Message message = messageCreator.createMessage(queueSession);
            //TODO put in pretty printer.
            if (logger.isInfoEnabled()) {
                logger.info("Message created was [" + message + "]");
            }
            if (this.isExplicitQosEnabled()) {
                queueSender.send(
                    queue,
                    message,
                    getDeliveryMode(),
                    getPriority(),
                    getTimeToLive());

            } else {
                queueSender.send(queue, message);
            }
            logger.info("Message sent OK");

        } catch (JMSException e) {
            throw convertJMSException(
                "Send message on queue name = " + queueName,
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

    public void execute(JmsSenderCallback action) throws JmsException {
        if (isPubSubDomain()) {
            executeTopicJmsSenderCallback(action);
        } else {
            executeQueueJmsSenderCallback(action);
        }

    }

    public void execute(SessionCallback action) throws JmsException {
        if (isPubSubDomain()) {
            executeTopicSessionCallback(action);
        } else {
            executeQueueSessionCallback(action);
        }

    }

    private void executeTopicJmsSenderCallback(JmsSenderCallback action) {
        TopicConnection topicConnection = null;
        try {
            topicConnection =
                ((TopicConnectionFactory) getConnectionFactory())
                    .createTopicConnection();
            TopicSession topicSession =
                topicConnection.createTopicSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());
            TopicPublisher publisher = topicSession.createPublisher(null);

            action.doInJms(topicSession, publisher);

        } catch (JMSException e) {
            throw convertJMSException("Call JmsSenderCallback", e);
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

    private void executeTopicSessionCallback(SessionCallback action) {
        TopicConnection topicConnection = null;
        try {
            topicConnection =
                ((TopicConnectionFactory) getConnectionFactory())
                    .createTopicConnection();
            TopicSession topicSession =
                topicConnection.createTopicSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());

            action.doInJms(topicSession);

        } catch (JMSException e) {
            throw convertJMSException("Call SessionCallback", e);
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

    private void executeQueueJmsSenderCallback(JmsSenderCallback action) {
        QueueConnection queueConnection = null;
        try {
            queueConnection =
                ((QueueConnectionFactory) getConnectionFactory())
                    .createQueueConnection();

            QueueSession queueSession =
                queueConnection.createQueueSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());

            QueueSender queueSender = queueSession.createSender(null);

            action.doInJms(queueSession, queueSender);

        } catch (JMSException e) {
            throw convertJMSException("Call SessionCallback", e);
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

    private void executeQueueSessionCallback(SessionCallback action) {
        QueueConnection queueConnection = null;
        try {
            queueConnection =
                ((QueueConnectionFactory) getConnectionFactory())
                    .createQueueConnection();

            QueueSession queueSession =
                queueConnection.createQueueSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());

            action.doInJms(queueSession);

        } catch (JMSException e) {
            throw convertJMSException("Call SessionCallback", e);
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

}
