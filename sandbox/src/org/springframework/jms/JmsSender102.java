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
import javax.naming.NamingException;


/**
 * An implementation of the JmsSender interface that uses the 
 * JMS 1.0.2 specification.  You must specify the domain or
 * style of messaging to be either Point-to-Point (Queues) 
 * or Publish/Subscribe(Topics) The default is to use the Point-to-Point domain.   
 * The property 'setPubSubDomain(boolean)' to true will configure 
 * the JmsSender to use the Pub/Sub domain (Topics).
 * This boolean property is an implementation detail due to the use of similar
 * but seperate class heirarchies in the JMS 1.0.2 API.  JMS 1.1
 * provides a new domain independent API that allows for easy 
 * mix-and-match use of Point-to-Point and Publish/Subscribe domain.
 */
public class JmsSender102 extends AbstractJmsSender
{

    /**
     * By default the JmsSender uses the Point-to-Point domain.
     */
    private boolean _isPubSub = false;

    /**
     * Construct a new JmsSender for bean usage.
     * Note: The ConnectionFactory has to be set before using the instance.
     * This constructor can be used to prepare a JmsSender via a BeanFactory,
     * typically setting the ConnectionFactory via setConnectionFactory.
     * @see #setConnectionFactory
     */
    public JmsSender102()
    {
    }

    /**
     * Set the JmsSender to be for the Publish/Subscribe domain only. This
     * implies the use of Topics.
     * @param isTopic 
     */
    public void setPubSubDomain(boolean isPubSub)
    {
        _isPubSub = isPubSub;
    }

    /**
     * Returns true if the destination(s) of the JmsSender is to a topic.
     * False indicates sending to a Queue which is the default destination
     * type.
     * @return true if the destination(s) is a Topic.
     */
    public boolean isPubSubDomain()
    {
        return _isPubSub;
    }

    public void send(String destinationName, MessageCreator messageCreator)
        throws JmsException
    {
        if (isPubSubDomain())
        {
            sendPubSub(destinationName, messageCreator);
        } else
        {
            sendP2P(destinationName, messageCreator);
        }
    }

    private void sendPubSub(String topicName, MessageCreator messageCreator)
    {
        TopicConnection topicConnection = null;
        try
        {
            topicConnection =
                ((TopicConnectionFactory) getConnectionFactory())
                    .createTopicConnection();
            TopicSession topicSession =
                topicConnection.createTopicSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());
            Topic topic = (Topic) super.lookupJndiResource(topicName);
            if (logger.isInfoEnabled())
            {
                logger.info("Looked up topic with name [" + topicName + "]");
            }
            TopicPublisher publisher = topicSession.createPublisher(topic);

            Message message = messageCreator.createMessage(topicSession);
            if (logger.isInfoEnabled())
            {
                logger.info("Message created was [" + message + "]");
            }
            publisher.publish(topic, message);

        } catch (NamingException e)
        {
            throw new JmsException(
                "Couldn't get topic name [" + topicName + "] from JNDI",
                e);

        } catch (JMSException e)
        {
            throw new JmsException(
                "Couln't create JMS TopicConnection or TopicSession",
                e);
        } finally
        {
            if (topicConnection != null)
            {
                try
                {
                    topicConnection.close();
                } catch (JMSException e)
                {
                    logger.warn("Failed to close the topic connection", e);
                }
            }
        }
    }

    /**
     * @param jndiDestinationName
     * @param messageCreator
     */
    private void sendP2P(String queueName, MessageCreator messageCreator)
    {
        //TODO code to deal with JNDI prefix.

        QueueConnection queueConnection = null;
        try
        {
            //TODO provide our own stand alone connectionfactory impl to
            //set the username, password, and client id can be set on the 
            //connection.  This seems to be how it is done in administration
            //of j2ee apps...otherwise move username,password into this class.
            queueConnection =
                ((QueueConnectionFactory) getConnectionFactory())
                    .createQueueConnection();

            //TODO think if properties in the sender class is the
            //place to hold this config information.
            QueueSession queueSession =
                queueConnection.createQueueSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());

            final Queue queue = (Queue) super.lookupJndiResource(queueName);
            if (logger.isInfoEnabled())
            {
                logger.info("Looked up queue with name [" + queueName + "]");
            }
            QueueSender queueSender = queueSession.createSender(queue);
            Message message = messageCreator.createMessage(queueSession);
            //TODO put in pretty printer.
            if (logger.isInfoEnabled())
            {
                logger.info("Message created was [" + message + "]");
            }

            queueSender.send(queue, message);
            logger.info("Message sent OK");

        } catch (NamingException e)
        {
            throw new JmsException(
                "Couldn't get queue name [" + queueName + "] from JNDI",
                e);

        } catch (JMSException e)
        {
            throw new JmsException(
                "Couln't create JMS QueueConnection or QueueSession",
                e);
        } finally
        {
            if (queueConnection != null)
            {
                try
                {
                    queueConnection.close();
                } catch (JMSException e)
                {
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
    public void afterPropertiesSet()
    {
        super.afterPropertiesSet();
        //Make sure that the ConnectionFactory passed is consistent
        //with sending on the specifies destination type.
        //Some providers implementations of the ConnectionFactory implement both
        //domain interfaces under the cover, so just check if the selected
        //domain is consistent with the type of connection factory.
        if (isPubSubDomain())
        {
            if (getConnectionFactory() instanceof TopicConnectionFactory)
            {
                //Do nothing, all ok.
            } else
            {
                throw new IllegalArgumentException("Specified a Spring JMS 1.0.2 Sender for topics but did not supply an instance of a TopicConnectionFactory");
            }
        } else
        {
            if (getConnectionFactory() instanceof QueueConnectionFactory)
            {
                //Do nothing, all ok.
            } else
            {
                throw new IllegalArgumentException("Specified a Spring JMS 1.0.2 Sender for queues but did not supply an instance of a QueueConnectionFactory");
            }
        }

    }

    /* (non-Javadoc)
     * @see org.springframework.jms.JmsSender#send(java.lang.String, org.springframework.jms.JmsSenderCallback)
     */
    public void send(String destinationName, JmsSenderCallback callback) throws JmsException
    {
        // TODO Auto-generated method stub
        
    }

}
