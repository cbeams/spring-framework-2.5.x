/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Sets up a JMS Connection and exposes it for bean references.
 * @author <a href="mailto:mark.pollack@codestreet.com">Mark Pollack</a>
 */
public class JmsConnectionFactoryBean
    implements FactoryBean, InitializingBean, DisposableBean
{
    protected final Log logger = LogFactory.getLog(getClass());
    
    private Connection _connection;
    
    private String _userName;
    
    private String _password; 

    private ConnectionFactory _connectionFactory;
    
    private String _clientID;
    
    private ExceptionListener _exceptionListener;
    
    

    public Object getObject() throws Exception
    {
        return _connection;
    }
    
    
    public Class getObjectType()
    {
        return Connection.class;
    }


    public boolean isSingleton()
    {
        return true;
    }


    public void afterPropertiesSet() throws Exception
    {
        if (_connectionFactory == null){
            throw new IllegalArgumentException("Did not set required JMS connection factory property");        
        }
        logger.info("Creating JMS Connection");
        _connection = _connectionFactory.createConnection(_userName, _password);
        if (_clientID != null) _connection.setClientID(_clientID);
        if (_exceptionListener != null) _connection.setExceptionListener(_exceptionListener);

    }


    public void destroy() throws Exception
    {
        logger.info("Closing JMS Connection");
        _connection.close();
    }


    /**
     * Set the connection factory that will be used to create the connection.
     * @param factory
     */
    public void setConnectionFactory(ConnectionFactory factory)
    {
        _connectionFactory = factory;
    }

    /**
     * @param string
     */
    public void setPassword(String string)
    {
        _password = string;
    }

    /**
     * @param string
     */
    public void setUserName(String string)
    {
        _userName = string;
    }

    /**
     * @param string
     */
    public void setClientID(String string)
    {
        _clientID = string;
    }

    /**
     * @param listener
     */
    public void setExceptionListener(ExceptionListener listener)
    {
        _exceptionListener = listener;
    }

}
