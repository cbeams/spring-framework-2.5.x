package org.springframework.jms;

import org.springframework.jms.support.JmsTemplateSupport;

import javax.jms.*;

import java.util.Properties;

/**
 * This template abstracts JMS APIs away from the user. It is supposed to make JMS API easy to use.
 * The attempt is to hide all the complexity of JMS in this template. Some of the details the user
 * is hopefully won't have to deal with is Sessions, JNDI lookups, topic subscribers/publishers etc.
 * Users are given flexibility, however, in whether looking up jndi resources themselves or let template
 * do that. For instance, there're two versions of this template's constructor, one that takes a string name of the
 * topic connection factory, the other takes the TopicConnectionFactory object.
 *
 * @version 0.1
 * @author Andre Biryukov
 */
public final class JmsPubSubTemplate extends JmsTemplateSupport {

    //---------------------------------------------------------------------
    // Instance data
    //---------------------------------------------------------------------

    private TopicConnectionFactory topicConnectionFactory;

    //---------------------------------------------------------------------
    // Constructors
    //---------------------------------------------------------------------
    /**
     * Create a new JmsPubSubTemplate using the initial JNDI context.
     *
     * @param topicConnectionFactory topic connection object if the jndi lookup has been done prior to using this template.
     * @throws JmsException
     */
    public JmsPubSubTemplate(final TopicConnectionFactory topicConnectionFactory) throws JmsException {
        super();
        if (topicConnectionFactory == null) {
            throw new JmsException("Attempting to create a jms template using a null connection factory.");
        }
        this.topicConnectionFactory = topicConnectionFactory;
    }

    /**
     * Creates a new JmsPubSubTemplate if the JNDI service requires an environment passed to it.
     *
     * @param topicConnectionFactory topic connection object if the jndi lookup has been done prior to using this template.
     * @param env set of properties to create a JNDI context
     * @throws JmsException
     */
    public JmsPubSubTemplate(final TopicConnectionFactory topicConnectionFactory,
                             final Properties env) throws JmsException {
        super(env);
        if (topicConnectionFactory == null) {
            throw new JmsException("Attempting to create a jms template using a null connection factory.");
        }
        this.topicConnectionFactory = topicConnectionFactory;
    }

    /**
     * Creates a new JmsPubSubTemplate using the Initial Jndi context.
     *
     * @param topicConnectionFactoryJndiName connection factory name including any Jndi specific prefixes.
     * @throws JmsException if jndi lookup fails
     */
    public JmsPubSubTemplate(final String topicConnectionFactoryJndiName) throws JmsException {
        super();
        this.topicConnectionFactory = (TopicConnectionFactory) super.lookupJndiResource(topicConnectionFactoryJndiName);
    }

    /**
     * Creates a new JmsPubSubTemplate using the environement provided.
     *
     * @param topicConnectionFactoryJndiName connection factory name including any Jndi specific prefixes.
     * @param env Environment required to create a jndi context.
     */
    public JmsPubSubTemplate(final String topicConnectionFactoryJndiName, final Properties env) {
        super(env);
        this.topicConnectionFactory = (TopicConnectionFactory) super.lookupJndiResource(topicConnectionFactoryJndiName);
    }
    //---------------------------------------------------------------------
    // End of Constructors
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // Private methods
    //---------------------------------------------------------------------

    /**
     * This private method is called to get a topic connection using user's identity.
     * If the user name is a null, the default identity is used.
     *
     * @param uName User name.
     * @param password Clear text password.
     * @return Topic connection that will be used to create a session.
     * @throws JmsException
     */
    private TopicConnection fetchTopicConnection(final String uName,
                                                 final String password) throws JmsException {
        final TopicConnection topicConnection;

        try {
            // Use ConnectionFactory to create a JMS connection
            if (uName == null) {
                topicConnection = this.topicConnectionFactory.createTopicConnection();
            } else {
                topicConnection = this.topicConnectionFactory.createTopicConnection(uName, password);
            }
        } catch (JMSException e) {
            throw new JmsException("Error while getting a topic connection.", e);
        }

        if (topicConnection == null) {
            throw new JmsException("The topic connection factory returned a null connection.");
        } else {
            return topicConnection;
        }
    }

    /**
     * Lookup a topic using superclass' search method.
     *
     * @param topicName Jndi name the topic is bound to.
     * @return Topic a topic object
     * @throws JmsException every exception is wrapped into JmsException
     */
    private Topic lookupTopic(final String topicName) throws JmsException {
        // Lookup Destination (topic) via JNDI
        final Topic aTopic = (Topic) super.lookupJndiResource(topicName);
        logger.info("Looked up topic with name '" + topicName + "'");
        return aTopic;
    }

    //---------------------------------------------------------------------
    // End of Private mathods
    //---------------------------------------------------------------------

    /**
     * The simplest way to publish a message to a topic with a given name.
     * Use it if your JMS provider does not require authentication.
     *
     * @param topicName Topic name including any prefixes that might be app server specific.
     * @param pubSubMessageCreator implementation of MessageCreator interface.
     * @throws JmsException
     */
    public void publish(final String topicName, final MessageCreator pubSubMessageCreator) throws JmsException {
        this.publish(topicName, pubSubMessageCreator, null, null);
    }

    /**
     * This method is called to publish a message to a topic that requires authentication.
     * You may still use this method even if no authetication required, just pass a null for both
     * user name and password.
     *
     * @param topicName The name of the topic, as it is bound in JNDI.
     * @param pubSubMessageCreator
     * @param uName User name if the container requires authentication to create a topic connection.
     *              Pass null if no authentication required.
     * @param password Ignored if uName value is null.
     * @throws JmsException
     */
    public void publish(final String topicName,
                              final MessageCreator pubSubMessageCreator,
                              final String uName,
                              final String password) throws JmsException {
        final Topic aTopic = this.lookupTopic(topicName);
        this.publish(aTopic, pubSubMessageCreator, uName, password);
    }

    /**
     * This method can be used if a topic object is already available to avoid additional
     * jndi lookups. User name and password can be null if no authentication is required.
     *
     * @param aTopic Topic to publish a nessage to.
     * @param pubSubMessageCreator Interface that defines the message.
     * @param uName User name. Can be null if no authentication is required.
     * @param password Password. Can be null.
     * @throws JmsException
     */
    public void publish(final Topic aTopic,
                              final MessageCreator pubSubMessageCreator,
                              final String uName,
                              final String password) throws JmsException {
        TopicConnection topicConnection = null;
        try {
            topicConnection = this.fetchTopicConnection(uName, password);

            // Use Connection to create session
            final TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            final TopicPublisher publisher = session.createPublisher(aTopic);

            // Create a message using passed MessageCreator interface and publish it
            final Message message = pubSubMessageCreator.createMessage(session);
            logger.info("Message created was [" + message + "]");
            publisher.publish(message,
                    pubSubMessageCreator.getDeliveryMode(),
                    pubSubMessageCreator.getPriority(),
                    pubSubMessageCreator.getTimeToLive());
            logger.info("Message published OK");
        } catch (JMSException ex) {
            throw new JmsException("Could not publish a message.", ex);
        } finally {
            if (topicConnection != null) {
                try {
                    topicConnection.close();
                } catch (JMSException ex) {
                    logger.warn("Failed to close topic", ex);
                }
            }
        }
    }

    /**
     * The simplest way to subscribe to a topic with the given name. Message selector can be null,
     * which implies "all messages". This method creates a non-durable default subscription. The session
     * created is not transacted and the acknowledgement mode is set to AUTO_ACKNOWLEDGE. The subscriber
     * will not get messages originated on the same jms connection.
     *
     * @param topicName name of the topic, as it is bound in JNDI.
     * @param listener implementation of javax.jms.MessageListener interface
     * @param messageSelector  allows a message consumer to specify the messages it is interested in.
     * @return TopicConnection object that should be closed by the consumer of this method.
     * @throws JmsException
     */
    public TopicConnection subscribe(final String topicName,
                                           final MessageListener listener,
                                           final String messageSelector) throws JmsException {
        return this.subscribe(topicName, listener, messageSelector, null, null);
    }

    /**
     * Creates a non durable subscription with most options available to users.
     * The subscribers still don't receive the messages that were generated on the same connection.
     *
     * @param topicName The name of the topic, as it is bound in JNDI.
     * @param listener implementation of javax.jms.MessageListener interface
     * @param messageSelector allows a message consumer to specify the messages it is interested in.
     * @param uName User name if the container requires authentication to create a topic connection.
     *              Pass null if no authentication required.
     * @param password Ignored if uName value is null.
     * @return a topic connection object
     * @throws JmsException
     */
    public TopicConnection subscribe(final String topicName,
                                           final MessageListener listener,
                                           final String messageSelector,
                                           final String uName,
                                           final String password) throws JmsException {
        final Topic aTopic = this.lookupTopic(topicName);
        return this.subscribe(aTopic, listener, messageSelector, uName, password);
    }

    /**
     * Creates a non durable subscription with most options available to users.
     * The subscribers still don't receive the messages that were generated on the same connection.
     * Use this method if the lookup of the topic has already been done.
     *
     * @param aTopic topic object
     * @param listener implementation of javax.jms.MessageListener interface
     * @param messageSelector allows a message consumer to specify the messages it is interested in.
     * @param uName User name if the container requires authentication to create a topic connection.
     *              Pass null if no authentication required.
     * @param password Ignored if uName value is null.
     * @return a topic connection object
     * @throws JmsException any jms exception wrapped
     */
    public TopicConnection subscribe(final Topic aTopic,
                                           final MessageListener listener,
                                           final String messageSelector,
                                           final String uName,
                                           final String password) throws JmsException {
        final TopicConnection topicConnection;
        try {
            topicConnection = this.fetchTopicConnection(uName, password);

            // Use Connection to create session
            final TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            //subscribers will not get messages originated on the same connection
            final TopicSubscriber topicSubscriber = session.createSubscriber(aTopic, messageSelector, false);
            topicSubscriber.setMessageListener(listener);

            topicConnection.start();

            logger.info("MessageListener [" + listener + "] subscribed OK to topic with name '" + aTopic.getTopicName() + "'");

            return topicConnection;
        } catch (JMSException ex) {
            throw new JmsException("Failed to subscribe to a topic.", ex);
        }
    }

    /**
     * Receives the next message that arrives within the specified timeout interval.
     * This call blocks until a message arrives or the timeout expires.
     * A timeout of zero never expires, and the call blocks indefinitely.
     *
     * @param topicName topic name as it is bound in jndi.
     * @param messageSelector filters unwanted messages, can be null (all messages)
     * @param timeout timeout in milliseconds, 0 for "never time out".
     * @param uName ignored if null
     * @param password ignored if null
     * @return message object or null if after timeout. cast the result to one of the specific message types interfaces.
     * @throws JmsException jms exception
     */
    public Message consumeMessageSynch(final String topicName,
                                             final String messageSelector,
                                             final long timeout,
                                             final String uName,
                                             final String password) throws JmsException {
        TopicConnection topicConnection = null;
        try {
            final Topic aTopic = this.lookupTopic(topicName);
            topicConnection = this.fetchTopicConnection(uName, password);

            // Use Connection to create session
            final TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            //subscribers will not get messages originated on the same connection
            final TopicSubscriber topicSubscriber = session.createSubscriber(aTopic, messageSelector, false);
            //start the connection before calling the receive method
            topicConnection.start();
            final Message msg = topicSubscriber.receive(timeout);

            return msg;
        } catch (JMSException ex) {
            throw new JmsException("Failed to read a message from a topic.", ex);
        } finally {
            if (topicConnection != null) {
                try {
                    topicConnection.close();
                } catch (JMSException ex) {
                    logger.warn("Failed to close a topic connection.", ex);
                }
            }
        }
    }

    /**
     * Creates a durable subscription that allows to receive all messages on a specific topic.
     * This method only works if no authentication is required by the JMS provider.
     *
     * @param topicName topic name as bound it is bound in jndi.
     * @param name unique identifier of the durable subscriber.
     * @param listener implementation of javax.jms.MessageListener interface.
     * @return a topic connection
     * @throws JmsException any jms exception wrapped
     */
    public TopicConnection subscribeDurable(final String topicName,
                                            final String name,
                                            final MessageListener listener) throws JmsException {
        return this.subscribeDurable(topicName, name, listener, null, null, null);
    }

    /**
     * Creates a durable subscription using a user identity.
     *
     * @param topicName topic name
     * @param name unique durable subscription name
     * @param listener implementation of javax.jms.MessageListener interface
     * @param messageSelector message filter, null for all messages
     * @param uName user name or null for no authentication
     * @param password password or null
     * @return a topic connection
     * @throws JmsException any jms exception wrapped
     */
    public TopicConnection subscribeDurable(final String topicName,
                                            final String name,
                                            final MessageListener listener,
                                            final String messageSelector,
                                            final String uName,
                                            final String password) throws JmsException {
        final Topic aTopic = this.lookupTopic(topicName);
        return this.subscribeDurable(aTopic, name, listener, messageSelector, uName, password);
    }

    /**
     * Creates a durable subscription on a given topic.
     * Each durable subscriber has to have a unique name identifier associated with a durable subscription.
     * A client can change an existing durable subscription by creating a durable TopicSubscriber with the same
     * name and a new topic and/or message selector. Changing a durable subscriber is equivalent to unsubscribing
     * (deleting) the old one and creating a new one.
     *
     * @param aTopic topic object
     * @param name unique name for this subscriber
     * @param listener implementation of javax.jms.MessageListener interface
     * @param messageSelector can be used to filter out unwanted messages, or null for all messages.
     * @param uName user name, or null for no authentication
     * @param password ignored if uName is null
     * @return topic connection object
     * @throws JmsException
     */
    public TopicConnection subscribeDurable(final Topic aTopic,
                                            final String name,
                                            final MessageListener listener,
                                            final String messageSelector,
                                            final String uName,
                                            final String password) throws JmsException {
        final TopicConnection topicConnection;
        try {
            topicConnection = this.fetchTopicConnection(uName, password);

            // Use Connection to create session
            final TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            //subscribers will not get messages originated on the same connection
            final TopicSubscriber topicSubscriber = session.createDurableSubscriber(aTopic, name, messageSelector, false);
            topicSubscriber.setMessageListener(listener);

            topicConnection.start();

            logger.info("MessageListener [" + listener +
                    "] aquired a durable subscription '" + name + "' to topic with name '" + aTopic.getTopicName() + "'");

            return topicConnection;
        } catch (JMSException ex) {
            throw new JmsException("Failed to subscribe to a topic.", ex);
        }
    }
}
