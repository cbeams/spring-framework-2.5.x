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

package org.springframework.jca.cci.connection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;

/**
 * A CCI ConnectionFactory adapter that returns the same Connection on all
 * <code>getConnection</code> calls, and ignores calls to
 * <code>Connection.close()</code>.
 *
 * <p>Useful for testing and standalone environemtns, to keep using the same
 * Connection for multiple CciTemplate calls, without having a pooling
 * ConnectionFactory, also spanning any number of transactions.
 *
 * <p>You can either pass in a CCI Connection directly, or let this
 * factory lazily create a Connection via a given target ConnectionFactory.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see #getConnection()
 * @see javax.resource.cci.Connection#close()
 * @see org.springframework.jca.cci.core.CciTemplate
 */
public class SingleConnectionFactory extends DelegatingConnectionFactory implements DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

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
	 * given Connection.
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
		setTargetConnectionFactory(targetConnectionFactory);
		afterPropertiesSet();
	}

	/**
	 * Make sure a connection or connection factory has been set.
	 */
	public void afterPropertiesSet() {
		if (this.connection == null && getTargetConnectionFactory() == null) {
			throw new IllegalArgumentException("connection or targetConnectionFactory is required");
		}
	}


	/**
	 * Initialize the single Connection.
	 * @throws javax.resource.ResourceException if thrown by CCI API methods
	 */
	protected void init() throws ResourceException {
		if (getTargetConnectionFactory() == null) {
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
	 * Create a CCI Connection via this template's ConnectionFactory.
	 * @return the new CCI Connection
	 * @throws javax.resource.ResourceException if thrown by CCI API methods
	 */
	protected Connection doCreateConnection() throws ResourceException {
		return getTargetConnectionFactory().getConnection();
	}

	/**
	 * Close the underlying connection.
	 * The provider of this ConnectionFactory needs to care for proper shutdown.
	 * <p>As this bean implements DisposableBean, a bean factory will
	 * automatically invoke this on destruction of its cached singletons.
	 */
	public void destroy() throws ResourceException {
		if (this.target != null) {
			this.target.close();
		}
	}


	public Connection getConnection() throws ResourceException {
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

	public Connection getConnection(ConnectionSpec connectionSpec) throws ResourceException {
		throw new NotSupportedException(
				"SingleConnectionFactory does not support custom ConnectionSpec");
	}

	public RecordFactory getRecordFactory() throws ResourceException {
		return getTargetConnectionFactory().getRecordFactory();
	}

	public ResourceAdapterMetaData getMetaData() throws ResourceException {
		return getTargetConnectionFactory().getMetaData();
	}

	public void setReference(Reference reference) {
		getTargetConnectionFactory().setReference(reference);
	}

	public Reference getReference() throws NamingException {
		return getTargetConnectionFactory().getReference();
	}


	/**
	 * Wrap the given Connection with a proxy that delegates every method call to it
	 * but suppresses close calls. This is useful for allowing application code to
	 * handle a special framework Connection just like an ordinary Connection from a
	 * CCI ConnectionFactory.
	 * @param target the original Connection to wrap
	 * @return the wrapped Connection
	 */
	protected Connection getCloseSuppressingConnectionProxy(Connection target) {
		return (Connection) Proxy.newProxyInstance(
				Connection.class.getClassLoader(),
				new Class[] {Connection.class},
				new CloseSuppressingInvocationHandler(target));
	}


	/**
	 * Invocation handler that suppresses close calls on CCI Connections.
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
