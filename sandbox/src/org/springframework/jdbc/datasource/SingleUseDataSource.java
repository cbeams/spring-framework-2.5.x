/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.jdbc.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Thomas Risberg
 */
public class SingleUseDataSource extends SingleConnectionDataSource {
    private static final int DEFAULT_CACHE_ENTRIES = 10;
    
    private int maxStatements = DEFAULT_CACHE_ENTRIES;
    
    /** Creates a new instance of SingleUseDataSource */
    public SingleUseDataSource() {
    }

    public int getMaxStatements() {
        return maxStatements;
    }

    public void setMaxStatements(int maxStatements) {
        this.maxStatements = maxStatements;
    }


    
    /**
     * Override getting a connection using the static from DriverManager providing
     * a connection proxy that allows caching of prepared statements.
     * @see java.sql.DriverManager#getConnection(String, java.util.Properties)
     */
    protected Connection getConnectionFromDriverManager(String url, Properties props)
        throws SQLException {

        if(this.maxStatements > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating new JDBC Connection to [" + url + "] with a statement cache of " + maxStatements);
            }
            return getStatementCachingConnectionProxy(DriverManager.getConnection(url, props));
        }
        else {
            return super.getConnectionFromDriverManager(url, props);
        }
                
    }

    /**
     * Wrap the given Connection with a proxy that delegates every method call to it
     * and caches any prepared statements.
     * @param target the original Connection to wrap
     * @return the wrapped Connection
     */
    protected Connection getStatementCachingConnectionProxy(Connection target) {
        return (Connection) Proxy.newProxyInstance(
                ConnectionProxy.class.getClassLoader(),
                new Class[] {ConnectionProxy.class},
                new StatementCachingInvocationHandler(target, maxStatements));
    }


    /**
     * Invocation handler that suppresses close calls on JDBC Connections.
     */
    private static class StatementCachingInvocationHandler implements InvocationHandler {
        protected final Log logger = LogFactory.getLog(getClass());

        private static final String GET_TARGET_CONNECTION_METHOD_NAME = "getTargetConnection";

        private static final String PREPARE_STATEMENT_METHOD_NAME = "prepareStatement";

        private static final String CONNECTION_CLOSE_METHOD_NAME = "close";

        private final Connection target;

        private Map statementCache;
    
        private int cacheSize;

        public StatementCachingInvocationHandler(Connection target, int maxStatements) {
            this.target = target;
            this.cacheSize = maxStatements;
            statementCache = new LinkedHashMap(cacheSize, .75F, true) {
                public boolean removeEldestEntry(Map.Entry eldest) {
                    if (size() > cacheSize) {
                        close(eldest.getKey());
                        return true;
                    }
                    else {
                        return false;
                    }
                }

                public void close(Object key) {
                    try {
                        ((PreparedStatementProxy)get(key)).getTargetPreparedStatement().close();
                    }
                    catch (SQLException se) {
                        logger.warn("Exception during statement cache cleanup: [" + se.getClass().getName() + "] " + se.getMessage());
                    }
                }

                public void clear() {
                    Object[] keys = statementCache.keySet().toArray();
                    for (int i = 0; i < keys.length; i++) {
                        close(keys[i]);
                    }
                    super.clear();
                }

                public Object remove(Object key) {
                    close(key);
                    return super.remove(key);
                }
                
            }; 
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            // Handle getTargetConnection method: return underlying connection.
            if (method.getName().equals(GET_TARGET_CONNECTION_METHOD_NAME)) {
                return this.target;
            }

            // Handle close method: close cache and then pass the call on.
            if (method.getName().equals(CONNECTION_CLOSE_METHOD_NAME)) {
                statementCache.clear();
                // Invoke close method on target connection.
                try {
                    return method.invoke(this.target, args);
                }
                catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
            }

            // Handle prepareStatement method
            if (method.getName().equals(PREPARE_STATEMENT_METHOD_NAME)) {
                // Invoke method on target connection.
                PreparedStatement ps = (PreparedStatement)statementCache.get(args[0]);
                if (ps == null) {
                    try {
                        PreparedStatement newPs = (PreparedStatement)method.invoke(this.target, args);
                        ps = getCloseSuppressingPreparedStatementProxy(newPs);
                        statementCache.put(args[0], ps);
                    }
                    catch (InvocationTargetException ex) {
                        throw ex.getTargetException();
                    }
                }
                else {
                    try {
                        ps.clearParameters();
                    }
                    catch (SQLException ignore) {}
                    try {
                        ps.clearBatch();
                    }
                    catch (SQLException ignore) {}
                    try {
                        ps.clearWarnings();
                    }
                    catch (SQLException ignore) {}
                }
                return ps;
            }
            
            // Invoke method on target connection.
            try {
                return method.invoke(this.target, args);
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }

        /**
         * Wrap the given PreparedStatement with a proxy that delegates every method call to it
         * but suppresses close calls.
         * @param target the original PreparedStatement to wrap
         * @return the wrapped PreparedStatement
         */
        protected PreparedStatement getCloseSuppressingPreparedStatementProxy(PreparedStatement target) {
            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatementProxy.class.getClassLoader(),
                    new Class[] {PreparedStatementProxy.class},
                    new CloseSuppressingInvocationHandler(target));
        }

        /**
         * Invocation handler that suppresses close calls on JDBC Statements.
         */
        private static class CloseSuppressingInvocationHandler implements InvocationHandler {

            private static final String GET_TARGET_PREPARED_STATEMENT_METHOD_NAME = "getTargetPreparedStatement";

            private static final String PREPARED_STATEMENT_CLOSE_METHOD_NAME = "close";

            private final PreparedStatement target;

            public CloseSuppressingInvocationHandler(PreparedStatement target) {
                this.target = target;
            }

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // Invocation on ConnectionProxy interface coming in...

                // Handle getTargetConnection method: return underlying connection.
                if (method.getName().equals(GET_TARGET_PREPARED_STATEMENT_METHOD_NAME)) {
                    return this.target;
                }

                // Handle close method: don't pass the call on.
                if (method.getName().equals(PREPARED_STATEMENT_CLOSE_METHOD_NAME)) {
                    return null;
                }

                // Invoke method on target connection.
                try {
                    return method.invoke(this.target, args);
                }
                catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
            }
        }
        
    }
    
}
