/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Experimenting with the approach suggested by James Strachan.  Basically his
 * code copied into here ;)  Lets see how we can use spring to DI the 
 * 'raw' JMS objects...
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class JmsHelper
{

    private Session _session;
    
    private MessageProducer _messageProducer;
    
    public JmsHelper()
    {
        
    }
    
    public void send(Message m) throws JMSException
    {
        _messageProducer.send(m);
    }
    
    public void send(Destination d, Message m) throws JMSException
    {
        _messageProducer.send(d,m);
    }
    
    
    /**
     * @return
     */
    public MessageProducer getMessageProducer()
    {
        return _messageProducer;
    }

    /**
     * @return
     */
    public Session getSession()
    {
        return _session;
    }

    /**
     * @param producer
     */
    public void setMessageProducer(MessageProducer producer)
    {
        _messageProducer = producer;
    }

    /**
     * @param session
     */
    public void setSession(Session session)
    {
        _session = session;
    }

}
