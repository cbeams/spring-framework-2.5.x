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
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * An implementation of the JmsTemplate interface that uses the 
 * JMS 1.1 specification.
 * 
 * <b>The automatic creation of dynamic destinations is turned off by
 * default.</b>  Use
 * {@link AbstractJmsTemplate#setEnabledDynamicDestinations(boolean) setEnabledDynamicDestinations}
 * to enable this functionality.
 * 
 * If dynamic destination creation is enabled, you must specify the type of
 * JMS destination to create using the method
 * {@link AbstractJmsTemplate#setPubSubDomain(boolean) setPubSubDomain}.  
 *  
 * 
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class JmsTemplate11 extends AbstractJmsTemplate
{

    public void send(MessageCreator messageCreator)
    {
        if (this.getDefaultDestination() == null)
        {
            logger.warn(
                "No Default Destination specified. Check configuration of JmsSender");
            return;
        } else
        {
            send(null, getDefaultDestination(), true, messageCreator);
        }

    }

    public void send(String destinationName, MessageCreator messageCreator)
        throws JmsException
    {
        send(destinationName, null, false, messageCreator);
    }

    public void send(Destination d, MessageCreator messageCreator)
    {

        send(null, d, true, messageCreator);

    }

    public void execute(JmsSenderCallback action)
        throws JmsException
    {
        Connection connection = null;
         try
         {
             connection = getConnectionFactory().createConnection();
             Session session =
                 connection.createSession(
                     isSessionTransacted(),
                     getSessionAcknowledgeMode());

             MessageProducer producer = session.createProducer(null);

             action.doInJms(session, producer);

         } catch (JMSException e)
         {
             throw convertJMSException("Call JmsSenderCallback", e);
         } finally
         {
             if (connection != null)
             {
                 try
                 {
                     connection.close();
                 } catch (JMSException e)
                 {
                     logger.warn("Failed to close the connection", e);
                 }
             }
         }

    }

    public void execute(SessionCallback action) throws JmsException
    {
        Connection connection = null;
        try
        {
            connection = getConnectionFactory().createConnection();
            Session session =
                connection.createSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());
            action.doInJms(session);

        } catch (JMSException e)
        {
            throw convertJMSException("Call SessionCallback", e);
        } finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                } catch (JMSException e)
                {
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
        MessageCreator messageCreator)
    {

        Connection connection = null;
        try
        {
            connection = getConnectionFactory().createConnection();
            Session session =
                connection.createSession(
                    isSessionTransacted(),
                    getSessionAcknowledgeMode());

            if (!explicitDestination)
            {
                destination =
                    (Destination) getJmsAdmin().lookup(
                        destinationName,
                        isDynamicDestinationEnabled(),
                        isPubSubDomain());
            }
            if (logger.isInfoEnabled())
            {
                logger.info(
                    "Looked up destination with name ["
                        + destinationName
                        + "]");
            }
            MessageProducer producer = session.createProducer(destination);

            Message message = messageCreator.createMessage(session);
            if (logger.isInfoEnabled())
            {
                logger.info("Message created was [" + message + "]");
            }
            if (isExplicitQosEnabled())
            {
                producer.send(
                    destination,
                    message,
                    getDeliveryMode(),
                    getPriority(),
                    getTimeToLive());
            } else
            {
                producer.send(destination, message);
            }

        } catch (JMSException e)
        {
            throw convertJMSException(
                "Send message on destinaton name = " + destinationName,
                e);
        } finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                } catch (JMSException e)
                {
                    logger.warn("Failed to close the connection", e);
                }
            }
        }
    }
    


}
