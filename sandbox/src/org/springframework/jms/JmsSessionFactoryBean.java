/*
 * Created on May 27, 2004
 *
 * Copyright (C) 2004 CodeStreet.  All Rights Reserved.
 */
package org.springframework.jms;

import javax.jms.Connection;
import javax.jms.Session;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Create a JMS Session from a Connection.
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class JmsSessionFactoryBean
    implements FactoryBean, InitializingBean, DisposableBean
{
    private Connection _connection;
    
    private Session _session;
    
    private boolean _transacted;
    
    private int _acknowledgeMode;
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    public Object getObject() throws Exception
    {
        return _session;
    }

    public Class getObjectType()
    {
        return Session.class;
    }

    public boolean isSingleton()
    {
        return true;
    }


    public void afterPropertiesSet() throws Exception
    {    
        if (_connection == null){
            throw new IllegalArgumentException("Did not set required JMS connection property");        
        }
        logger.info("Creating JMS Session");
        _session = _connection.createSession(_transacted, _acknowledgeMode);
    }


    public void destroy() throws Exception
    {
        logger.info("Closing JMS Session");
        _session.close();
    }

    /**
     * @param i
     */
    public void setAcknowledgeMode(int i)
    {
        _acknowledgeMode = i;
    }

    /**
     * @param b
     */
    public void setTransacted(boolean b)
    {
        _transacted = b;
    }

    /**
     * @param connection
     */
    public void setConnection(Connection connection)
    {
        _connection = connection;
    }

}
