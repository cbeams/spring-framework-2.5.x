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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A connection factory that return the same connection and can optionally 
 * ignores calls to close.
 * 
 * @author Mark Pollack
 */
public class SingleConnectionFactory11 implements ConnectionFactory {


    protected final Log logger = LogFactory.getLog(getClass());
    
    //The wrapped connection.
    private Connection connection;
    
    //The providers connection factory.
    private ConnectionFactory connectionFactory;
    
    private boolean suppressClose;

    /**
     * Constructor for bean style usage.
     *
     */
    public SingleConnectionFactory11() {
        
    }

    public Connection createConnection() throws JMSException {
        synchronized (this) {
            if (this.connection == null) {
                // no underlying connection -> lazy init via DriverManager
                init(connectionFactory.createConnection());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Returning single connection: " + this.connection);
        }
        return this.connection;
    }


    public Connection createConnection(String username, String password)
        throws JMSException {
            synchronized (this) {
                if (this.connection == null) {
                    // no underlying connection -> lazy init via DriverManager
                    init(connectionFactory.createConnection(username, password));
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Returning single connection: " + this.connection);
            }
            return this.connection;
    }
    
    /**
     * Initialize the underlying connection.
     * Wraps the connection with a close-suppressing proxy if necessary.
     * @param source the JDBC Connection to use
     */
    protected void init(Connection source) {
        this.connection = this.suppressClose ? getCloseSuppressingConnectionProxy(source) : source;
    }
    
    /**
     * Return if the returned connection will be a close-suppressing proxy
     * or the physical connection.
     */
    public boolean isSuppressClose() {
        return suppressClose;
    }
    
    /**
     * Wrap the given Connection with a proxy that delegates every method call to it
     * but suppresses close calls. This is useful for allowing application code to
     * handle a special framework Connection just like an ordinary Connection from a
     * JMS ConnectionFactory
     * @param source original Connection
     * @return the wrapped Connection
     */
    static Connection getCloseSuppressingConnectionProxy(Connection source) {
        return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                                   new Class[] {Connection.class},
                                                   new CloseSuppressingInvocationHandler(source));
    }


    /**
     * Invocation handler that suppresses close calls on JDBC Connections.
     * @see #getCloseSuppressingConnectionProxy
     */
    private static class CloseSuppressingInvocationHandler implements InvocationHandler {

        private final Connection source;

        private CloseSuppressingInvocationHandler(Connection source) {
            this.source = source;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("close")) {
                // Don't pass the call on
                return null;
            }
            try {
                return method.invoke(this.source, args);
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }


    /**
     * @param factory
     */
    public void setConnectionFactory(ConnectionFactory factory) {
        connectionFactory = factory;
    }

}
