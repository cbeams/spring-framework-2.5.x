/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Create a JMS Producer from a Session
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class JmsProducerFactoryBean
    implements FactoryBean, InitializingBean, DisposableBean
{

    private MessageProducer _messageProducer;
    
    private Session _session;
    
    private Destination _jndiDestination;
    
    private String _topicDestination;
    
    private String _queueDestination;
    
    private int _deliveryMode = Message.DEFAULT_DELIVERY_MODE;
    
    private int _priority = Message.DEFAULT_PRIORITY;
    
    private long _timeToLive = Message.DEFAULT_TIME_TO_LIVE;
    
    private boolean _disableMessageTimestamp = false;

    private boolean _disableMessageID = false;
    
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    public Object getObject() throws Exception
    {
        return _messageProducer;
    }

    public Class getObjectType()
    {
        return MessageProducer.class;
    }

    public boolean isSingleton()
    {
        return true;
    }


    public void afterPropertiesSet() throws Exception
    {    
        if (_session == null){
            throw new IllegalArgumentException("Did not set required JMS session property");        
        }
        logger.info("Creating JMS Producer");
        int destCount = 0;
        if (_jndiDestination != null)
        {
            destCount++;
        }
        if (_topicDestination != null)
        {
            destCount++;
        }       
        if (_queueDestination != null)
        {
            destCount ++;
        }
        if (destCount > 1)
        {
            throw new IllegalArgumentException("Must only specify one of jndi, queue, or topic destination");
        }
        if (_jndiDestination != null)
        {
            _messageProducer = _session.createProducer(_jndiDestination);
        } else if (_topicDestination != null)
        {
            _messageProducer = _session.createProducer(_session.createTopic(_topicDestination));
        } else if (_queueDestination != null)
        {
            _messageProducer = _session.createProducer(_session.createQueue(_queueDestination));
        } else
        {
            logger.info("Creating a MessageProducer without a destination.  Just specify destination on each " +                "send operation.");
            _messageProducer = _session.createProducer(null);
        }
  
        _messageProducer.setDeliveryMode(_deliveryMode);
        _messageProducer.setPriority(_priority);
        _messageProducer.setTimeToLive(_timeToLive);
        _messageProducer.setDisableMessageID(_disableMessageID);
        _messageProducer.setDisableMessageTimestamp(_disableMessageTimestamp);
        
        
     
    }


    public void destroy() throws Exception
    {
        logger.info("Closing JMS Session");
        _session.close();
    }

    /**
     * @param i
     */
    public void setDeliveryMode(int i)
    {
        _deliveryMode = i;
    }

    /**
     * @param i
     */
    public void setPriority(int i)
    {
        _priority = i;
    }

    /**
     * @param l
     */
    public void setTimeToLive(long l)
    {
        _timeToLive = l;
    }

    /**
     * @param session
     */
    public void setSession(Session session)
    {
        _session = session;
    }

    /**
     * @param b
     */
    public void setDisableMessageID(boolean b)
    {
        _disableMessageID = b;
    }

    /**
     * @param b
     */
    public void setDisableMessageTimestamp(boolean b)
    {
        _disableMessageTimestamp = b;
    }

    /**
     * @param destination
     */
    public void setJndiDestination(Destination destination)
    {
        _jndiDestination = destination;
    }

    /**
     * @param producer
     */
    public void setMessageProducer(MessageProducer producer)
    {
        _messageProducer = producer;
    }

    /**
     * @param string
     */
    public void setQueueDestination(String string)
    {
        _queueDestination = string;
    }

    /**
     * @param string
     */
    public void setTopicDestination(String string)
    {
        _topicDestination = string;
    }

}
