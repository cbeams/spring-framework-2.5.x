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
package org.springframework.jms.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;

/**
 * A JMS ConnectionFactory that returns the same Connection on all
 * <code>createConnection</code> calls, and ignores calls to <code>close</code>.
 *
 * <p>Useful for testing, to keep using the same Connection for multiple
 * JmsTemplate calls, without having a pooling ConnectionFactory.
 *
 * <p>This implementation just works with JMS 1.1, as it implements the
 * domain-independent javax.jms.ConnectionFactory interface only.
 *
 * @author Mark Pollack
 * @author Juergen Hoeller
 * @see org.springframework.jms.core.JmsTemplate
 */
public class SingleConnectionFactory implements ConnectionFactory, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	/* The wrapped connection */
	private final Connection connection;


	public SingleConnectionFactory(Connection connection) {
		this.connection = getCloseSuppressingConnectionProxy(connection);
	}

	/**
	 * Close the underlying connection.
	 * The provider of this ConnectionFactory needs to care for proper shutdown.
	 * <p>As this bean implements DisposableBean, a bean factory will
	 * automatically invoke this on destruction of its cached singletons.
	 */
	public void destroy() throws JMSException {
		this.connection.close();
	}


	public Connection createConnection() throws JMSException {
		if (logger.isDebugEnabled()) {
			logger.debug("Returning single connection: " + this.connection);
		}
		return this.connection;
	}

	public Connection createConnection(String username, String password) throws JMSException {
		throw new JMSException("SingleConnectionFactory does not support custom username and password");
	}


	/**
	 * Wrap the given Connection with a proxy that delegates every method call to it
	 * but suppresses close calls. This is useful for allowing application code to
	 * handle a special framework Connection just like an ordinary Connection from a
	 * JMS ConnectionFactory.
	 * @param target the original Connection to wrap
	 * @return the wrapped Connection
	 */
	protected Connection getCloseSuppressingConnectionProxy(Connection target) {
		return (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
		                                           new Class[] {Connection.class},
		                                           new CloseSuppressingInvocationHandler(target));
	}


	/**
	 * Invocation handler that suppresses close calls on JMS Connections.
	 */
	private static class CloseSuppressingInvocationHandler implements InvocationHandler {

		private final Connection target;

		private CloseSuppressingInvocationHandler(Connection source) {
			this.target = source;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals("close")) {
				// Don't pass the call on
				return null;
			}
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
