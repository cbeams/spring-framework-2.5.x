package org.springframework.jms;

import org.springframework.jms.support.JmsTemplateSupport;

import javax.jms.*;

import java.util.Properties;

/**
 * This is the point-to-point JMS template.
 *
 * @version 0.1
 * @author Andre Biryukov
 */
public final class JmsP2PTemplate extends JmsTemplateSupport {
    //---------------------------------------------------------------------
    // Instance data
    //---------------------------------------------------------------------

    private QueueConnectionFactory queueConnectionFactory;

    //---------------------------------------------------------------------
    // End of Instance data
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // Constructors
    //---------------------------------------------------------------------

    /**
     * Create a new JmsP2PTemplate using the default initial jndi context.
     *
     * @param queueConnectionFactory queue connection factory object.
     * @throws JmsException spring jms exception
     */
    public JmsP2PTemplate(final QueueConnectionFactory queueConnectionFactory) throws JmsException {
        super();
        if (queueConnectionFactory == null) {
            throw new JmsException("Attempting to create a jms template using a null connection factory.");
        }
        this.queueConnectionFactory = queueConnectionFactory;
    }

    /**
     * Creates a new jms template using a customized environment.
     *
     * @param queueConnectionFactory queue connection facory object
     * @param env jndi environment
     * @throws JmsException spring jms exception
     */
    public JmsP2PTemplate(final QueueConnectionFactory queueConnectionFactory, final Properties env) throws JmsException {
        super(env);
        if (queueConnectionFactory == null) {
            throw new JmsException("Attempting to create a jms template using a null connection factory.");
        }
        this.queueConnectionFactory = queueConnectionFactory;
    }

    /**
     * Create a new JmsP2PTemplate using the default initial jndi context.
     *
     * @param queueConnectionFactoryJndiName jndi name for the queue connection factory.
     * @throws JmsException spring jms exception
     */
    public JmsP2PTemplate(final String queueConnectionFactoryJndiName) throws JmsException {
        super();
        this.queueConnectionFactory = (QueueConnectionFactory) super.lookupJndiResource(queueConnectionFactoryJndiName);
    }

    /**
     * Creates a new jms template using a customized environment.
     *
     * @param queueConnectionFactoryJndiName jndi name for a queue connection factory
     * @param env jndi environment
     * @throws JmsException spring jms exception
     */
    public JmsP2PTemplate(final String queueConnectionFactoryJndiName, final Properties env) throws JmsException {
        super(env);
        this.queueConnectionFactory = (QueueConnectionFactory) super.lookupJndiResource(queueConnectionFactoryJndiName);
    }
    //---------------------------------------------------------------------
    // End of Constructors
    //---------------------------------------------------------------------

    //---------------------------------------------------------------------
    // Private methods
    //---------------------------------------------------------------------

    /**
     * Creates a queue connection using connection factory.
     *
     * @param uName user name, can be null if no authentication required
     * @param password ignored if uName is null
     * @return queue connection object
     * @throws JmsException spring jms exception
     */
    private QueueConnection fetchQueueConnection(final String uName,
                                                 final String password) throws JmsException {
        final QueueConnection qConnection;
        try {
            if (uName == null) {
                qConnection = this.queueConnectionFactory.createQueueConnection();
            } else {
                qConnection = this.queueConnectionFactory.createQueueConnection(uName, password);
            }
        } catch (JMSException e) {
            throw new JmsException("Exception thrown while getting a queue connection from the connection factory.", e);
        }

        if (qConnection == null) {
            throw new JmsException("The queue connection factory returned a null connection.");
        } else {
            return qConnection;
        }
    }

    /**
     * Looks up a queue by its jndi name.
     *
     * @param queueName queue name including any jndi prefixes
     * @return queue object
     * @throws JmsException spring jms exception
     */
    private Queue lookupQueue(final String queueName) throws JmsException {
        // Lookup Destination (queue) via JNDI
        final Queue aQueue = (Queue) super.lookupJndiResource(queueName);
        logger.info("Looked up queue with name '" + queueName + "'");
        return aQueue;
    }

    //---------------------------------------------------------------------
    // End of Private methods
    //---------------------------------------------------------------------

    /**
     * Most straight forward way to send a message to a queue. The caller is supposed to implement MessageCreator
     * inline interface to define the actual message to be sent.
     *
     * @param queueName name of the queue
     * @param messageCreator implementation of MessageCreator interface
     * @throws JmsException spring jms exception
     */
    public void send(final String queueName,
                     final MessageCreator messageCreator) throws JmsException {
        this.send(queueName, messageCreator, null, null);
    }

    /**
     * Sends a message to the queue with the specified name. The caller is supposed to implement MessageCreator
     * inline interface to define the actual message to be sent. User name and password may be used to
     * satisfy app server authentication requirements.
     *
     * @param queueName Name of the queue as bound in JNDI.
     * @param messageCreator implementation of MessageCreator interface
     * @param uName User name, if the container requires authentication, null if no authentication
     * @param password Ignored if uName is null.
     * @throws JmsException spring jms exception
     */
    public void send(final String queueName,
                     final MessageCreator messageCreator,
                     final String uName,
                     final String password) throws JmsException {
        final Queue queue = this.lookupQueue(queueName);
        this.send(queue, messageCreator, uName, password);
    }

    /**
     * If the queue has been already looked up through JNDI, you can use this method.
     *
     * @param aQueue queue object
     * @param messageCreator implementation of MessageCreator interface
     * @param uName user name, or null if no authentication is required
     * @param password password or null
     * @throws JmsException spring jms exception
     */
    public void send(final Queue aQueue,
                     final MessageCreator messageCreator,
                     final String uName,
                     final String password
                     ) throws JmsException {
        QueueConnection qConnection = null;
        try {
            qConnection = this.fetchQueueConnection(uName, password);

            final QueueSession qSession = qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            //create a sender
            final QueueSender qSender = qSession.createSender(aQueue);
            //produce a message from the client provided method
            final Message msg = messageCreator.createMessage(qSession);
            //send the message
            qSender.send(msg,
                    messageCreator.getDeliveryMode(),
                    messageCreator.getPriority(),
                    messageCreator.getTimeToLive());
        } catch (JMSException e) {
            throw new JmsException("Failed to send a message to a queue.", e);
        } finally {
            if (qConnection != null) {
                try {
                    qConnection.close();
                } catch (JMSException e) {
                    logger.warn("Failed to close the queue connection", e);
                }
            }
        }
    }

    /**
     * The most straight forward to register as a listener to a queue and get back a queue connection.
     *
     * @param queueName name of a queue as bound in jndi
     * @param listener implementation of javax.jms.MessageListener interface
     * @return a queue connection
     * @throws JmsException any jms exception wrapped
     */
    public QueueConnection receive(final String queueName,
                                   final MessageListener listener) throws JmsException {
        return this.receive(queueName, listener, null, null, null);
    }

    /**
     * Call this method to subscribe to a queue with a given name using a user name and a password.
     *
     * @param queueName Name of the target queue as it is bound in JNDI.
     * @param listener Every p2p receiver is supposed to implement MessageListener interface and the onMessage method.
     * @param messageSelector Determines what messages will be received by the listener, null implies all messages.
     * @param uName User name, if the container requires authentication, null if no authentication
     * @param password Ignored if uName is null.
     * @return a queue connection
     * @throws JmsException any jms exception wrapped
     */
    public QueueConnection receive(final String queueName,
                                   final MessageListener listener,
                                   final String messageSelector,
                                   final String uName,
                                   final String password) throws JmsException {
        final Queue aQueue = this.lookupQueue(queueName);
        return this.receive(aQueue, listener, messageSelector, uName, password);
    }

    /**
     * If the lookup of a queue is already done, use this method.
     *
     * @param aQueue a queue object
     * @param listener implementation of javax.jms.MessageListener
     * @param messageSelector message filter or null for all messages
     * @param uName user name
     * @param password password
     * @return a queue connection
     * @throws JmsException any jms exception wrapped
     */
    public QueueConnection receive(final Queue aQueue,
                                   final MessageListener listener,
                                   final String messageSelector,
                                   final String uName,
                                   final String password) throws JmsException {
        final QueueConnection qConnection;
        try {
            qConnection = this.fetchQueueConnection(uName, password);

            final QueueSession qSession = qConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            final QueueReceiver qReceiver = qSession.createReceiver(aQueue, messageSelector);
            qReceiver.setMessageListener(listener);
            qConnection.start();

            logger.info("Message listener [" + listener + "] is a receiver for queue [" + aQueue.getQueueName() + "].");
            return qConnection;
        } catch (JMSException e) {
            throw new JmsException("JMS Exception occured.", e);
        }
    }

    /**
     * This method allows a consumer to synchronously receive a message within the specified amount of time.
     *
     * @param queueName queue name as it is bound in jndi
     * @param messageSelector message filter or null for all messages
     * @param timeout time in milliseconds, 0 means return immedeatly or the equivalent of receiveNoWait
     * @param uName user name or null
     * @param password password or null
     * @return message object or null if after timeout. cast the result to one of the specific message types interfaces.
     * @throws JmsException any jms exception
     */
    public Message consumeMessageSynch(final String queueName,
                                       final String messageSelector,
                                       final long timeout,
                                       final String uName,
                                       final String password) throws JmsException {
        QueueConnection queueConnection = null;
        try {
            final Queue aQueue = this.lookupQueue(queueName);
            queueConnection = this.fetchQueueConnection(uName, password);

            // Use Connection to create session
            final QueueSession session = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            //subscribers will not get messages originated on the same connection
            final QueueReceiver queueReceiver = session.createReceiver(aQueue, messageSelector);
            //start the connection before going into the "receive" mode
            queueConnection.start();
            final Message msg = queueReceiver.receive(timeout);

            return msg;
        } catch (JMSException ex) {
            throw new JmsException("Failed to retrieve a message from a queue.", ex);
        } finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException ex) {
                    logger.warn("Failed to close a queue connection.", ex);
                }
            }
        }
    }
}
