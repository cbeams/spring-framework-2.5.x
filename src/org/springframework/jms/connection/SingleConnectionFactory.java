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
package org.springframework.jms.connection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A JMS ConnectionFactory adapter that returns the same Connection on all
 * <code>createConnection</code> calls, and ignores calls to <code>close</code>.
 *
 * <p>Useful for testing and standalone environemtns, to keep using the same
 * Connection for multiple JmsTemplate calls, without having a pooling
 * ConnectionFactory, also spanning any number of transactions.
 *
 * <p>You can either pass in a JMS Connection directly, or let this
 * factory lazily create a Connection via a given target ConnectionFactory.
 * In the latter case, this factory just works with JMS 1.1; use
 * SingleConnectionFactory102 for JMS 1.0.2.
 *
 * @author Mark Pollack
 * @author Juergen Hoeller
 * @since 1.1
 * @see SingleConnectionFactory102
 * @see org.springframework.jms.core.JmsTemplate
 */
public class SingleConnectionFactory
		implements ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private ConnectionFactory targetConnectionFactory;

	/** Wrapped connection */
	private Connection target;

	/** Proxy connection */
	private Connection connection;


	/**
	 * Create a new SingleConnectionFactory for bean-style usage.
	 * @see #setTargetConnectionFactory
	 */
	public SingleConnectionFactory() {
	}

	/**
	 * Create a new SingleConnectionFactory that always returns the
	 * given Connection. Works with both JMS 1.1 and 1.0.2.
	 * @param target the single Connection
	 */
	public SingleConnectionFactory(Connection target) {
		this.target = target;
		this.connection = getCloseSuppressingConnectionProxy(target);
		afterPropertiesSet();
	}

	/**
	 * Create a new SingleConnectionFactory that always returns a single
	 * Connection that it will lazily create via the given target
	 * ConnectionFactory.
	 * @param targetConnectionFactory the target ConnectionFactory
	 */
	public SingleConnectionFactory(ConnectionFactory targetConnectionFactory) {
		this.targetConnectionFactory = targetConnectionFactory;
		afterPropertiesSet();
	}

	/**
	 * Set the target ConnectionFactory which will be used to lazily
	 * create a single Connection.
	 */
	public void setTargetConnectionFactory(ConnectionFactory targetConnectionFactory) {
		this.targetConnectionFactory = targetConnectionFactory;
	}

	/**
	 * Return the target ConnectionFactory which will be used to lazily
	 * create a single Connection, if any.
	 */
	public ConnectionFactory getTargetConnectionFactory() {
		return targetConnectionFactory;
	}

	/**
	 * Make sure a connection or connection factory has been set.
	 */
	public void afterPropertiesSet() {
		if (this.connection == null && this.targetConnectionFactory == null) {
			throw new IllegalArgumentException("connection or targetConnectionFactory is required");
		}
	}


	/**
	 * Initialize the single Connection.
	 * @throws javax.jms.JMSException if thrown by JMS API methods
	 */
	protected void init() throws JMSException {
		if (this.targetConnectionFactory == null) {
			throw new IllegalStateException("targetConnectionFactory is required for lazily initializing a connection");
		}
		Connection target = doCreateConnection();
		if (logger.isDebugEnabled()) {
			logger.debug("Created single connection: " + target);
		}
		this.target = target;
		this.connection = getCloseSuppressingConnectionProxy(target);
	}

	/**
	 * Create a JMS Connection via this template's ConnectionFactory.
	 * <p>This implementation uses JMS 1.1 API.
	 * @return the new JMS Connection
	 * @throws javax.jms.JMSException if thrown by JMS API methods
	 */
	protected Connection doCreateConnection() throws JMSException {
		return this.targetConnectionFactory.createConnection();
	}

	/**
	 * Close the underlying connection.
	 * The provider of this ConnectionFactory needs to care for proper shutdown.
	 * <p>As this bean implements DisposableBean, a bean factory will
	 * automatically invoke this on destruction of its cached singletons.
	 */
	public void destroy() throws JMSException {
		if (this.target != null) {
			this.target.close();
		}
	}


	public Connection createConnection() throws JMSException {
		synchronized (this) {
			if (this.connection == null) {
				init();
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning single connection: " + this.connection);
		}
		return this.connection;
	}

	public Connection createConnection(String username, String password) throws JMSException {
		throw new javax.jms.IllegalStateException(
				"SingleConnectionFactory does not support custom username and password");
	}

	public QueueConnection createQueueConnection() throws JMSException {
		Connection con = createConnection();
		if (!(con instanceof QueueConnection)) {
			throw new javax.jms.IllegalStateException(
					"This SingleConnectionFactory does not hold a QueueConnection but rather: " + con);
		}
		return ((QueueConnection) con);
	}

	public QueueConnection createQueueConnection(String username, String password) throws JMSException {
		throw new javax.jms.IllegalStateException(
				"SingleConnectionFactory does not support custom username and password");
	}

	public TopicConnection createTopicConnection() throws JMSException {
		Connection con = createConnection();
		if (!(con instanceof TopicConnection)) {
			throw new javax.jms.IllegalStateException(
					"This SingleConnectionFactory does not hold a TopicConnection but rather: " + con);
		}
		return ((TopicConnection) con);
	}

	public TopicConnection createTopicConnection(String username, String password) throws JMSException {
		throw new javax.jms.IllegalStateException(
				"SingleConnectionFactory does not support custom username and password");
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
		List classes = new ArrayList(3);
		classes.add(Connection.class);
		if (target instanceof QueueConnection) {
			classes.add(QueueConnection.class);
		}
		if (target instanceof TopicConnection) {
			classes.add(TopicConnection.class);
		}
		return (Connection) Proxy.newProxyInstance(
				Connection.class.getClassLoader(),
				(Class[]) classes.toArray(new Class[classes.size()]),
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
				// don't pass the call on
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
