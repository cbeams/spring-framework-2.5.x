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
import javax.naming.NamingException;

/**
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class JmsSender11 extends AbstractJmsSender
{

    public void send(String destinationName, MessageCreator messageCreator)
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
            Destination dest =
                (Destination) super.lookupJndiResource(destinationName);
            if (logger.isInfoEnabled())
            {
                logger.info(
                    "Looked up destination with name ["
                        + destinationName
                        + "]");
            }
            MessageProducer producer = session.createProducer(dest);

            Message message = messageCreator.createMessage(session);
            if (logger.isInfoEnabled())
            {
                logger.info("Message created was [" + message + "]");
            }
            producer.send(dest, message);

        } catch (NamingException e)
        {
            throw new JmsException(
                "Couldn't get destination name ["
                    + destinationName
                    + "] from JNDI",
                e);

        } catch (JMSException e)
        {
            throw new JmsException(
                "Couln't create JMS Connection or Session",
                e);
        } finally
        {
            if (connection != null)
            {
                try
                {
                    //session.close();
                    connection.close();
                } catch (JMSException e)
                {
                    logger.warn("Failed to close the connection", e);
                }
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
