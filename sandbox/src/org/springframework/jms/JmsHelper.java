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
     * The JMS message producer for use in the Helper send methods.
     * @return A message producer
     */
    public MessageProducer getMessageProducer()
    {
        return _messageProducer;
    }

    /**
     * The JMS session 
     * @return The JMS sesion
     */
    public Session getSession()
    {
        return _session;
    }

    /**
     * Set the JMS MessageProducer
     * @param producer the message producer
     */
    public void setMessageProducer(MessageProducer producer)
    {
        _messageProducer = producer;
    }

    /**
     * Set the JMS session
     * @param session the session
     */
    public void setSession(Session session)
    {
        _session = session;
    }

}
