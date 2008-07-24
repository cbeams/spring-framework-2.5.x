/*
 * Copyright 2002-2008 the original author or authors.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicSession;

import org.springframework.util.Assert;

/**
 * {@link SingleConnectionFactory} subclass that adds {@link javax.jms.Session}
 * caching as well {@link javax.jms.MessageProducer} caching. This ConnectionFactory
 * also switches the {@link #setReconnectOnException "reconnectOnException" property}
 * to "true" by default, allowing for automatic recovery of the underlying Connection.
 *
 * <p>By default, only one single Session will be cached, with further requested
 * Sessions being created and disposed on demand. Consider raising the
 * {@link #setSessionCacheSize "sessionCacheSize" value} in case of a
 * high-concurrency environment.
 *
 * <p><b>NOTE: For full flexibility, including mixed use of queues and topics,
 * this ConnectionFactory decorator requires JMS 1.1 or higher.</b>
 *
 * <p>When using the JMS 1.0.2 API, this ConnectionFactory will switch
 * into queue/topic mode according to the JMS API methods used at runtime:
 * <code>createQueueConnection</code> and <code>createTopicConnection</code> will
 * lead to queue/topic mode, respectively; generic <code>createConnection</code>
 * calls will lead to a JMS 1.1 connection which is able to serve both modes.
 *
 * @author Juergen Hoeller
 * @since 2.5.3
 */
public class CachingConnectionFactory extends SingleConnectionFactory {

	private int sessionCacheSize = 1;

	private boolean cacheProducers = true;

	private volatile boolean active = true;

	private final Map cachedSessions = new HashMap();


	/**
	 * Create a new CachingConnectionFactory for bean-style usage.
	 * @see #setTargetConnectionFactory
	 */
	public CachingConnectionFactory() {
		super();
		setReconnectOnException(true);
	}

	/**
	 * Create a new CachingConnectionFactory for the given target
	 * ConnectionFactory.
	 * @param targetConnectionFactory the target ConnectionFactory
	 */
	public CachingConnectionFactory(ConnectionFactory targetConnectionFactory) {
		super(targetConnectionFactory);
		setReconnectOnException(true);
	}


	/**
	 * Specify the desired size for the JMS Session cache (per JMS Session type).
	 * <p>This cache size is the maximum limit for the number of cached Sessions
	 * per session acknowledgement type (auto, client, dups_ok, transacted).
	 * As a consequence, the actual number of cached Sessions may be up to
	 * four times as high as the specified value - in the unlikely case
	 * of mixing and matching different acknowledgement types.
	 * <p>Default is 1: caching a single Session, (re-)creating further ones on
	 * demand. Specify a number like 10 if you'd like to raise the number of cached
	 * Sessions; that said, 1 may be sufficient for low-concurrency scenarios.
	 * @see #setCacheProducers
	 */
	public void setSessionCacheSize(int sessionCacheSize) {
		Assert.isTrue(sessionCacheSize >= 1, "Session cache size must be 1 or higher");
		this.sessionCacheSize = sessionCacheSize;
	}

	/**
	 * Return the desired size for the JMS Session cache (per JMS Session type).
	 */
	public int getSessionCacheSize() {
		return this.sessionCacheSize;
	}

	/**
	 * Specify whether to cache JMS MessageProducers per JMS Session instance
	 * (more specifically: one MessageProducer per Destination and Session).
	 * <p>Default is "true". Switch this to "false" in order to recreate
	 * MessageProducers on demand.
	 */
	public void setCacheProducers(boolean cacheProducers) {
		this.cacheProducers = cacheProducers;
	}

	/**
	 * Return whether to cache JMS MessageProducers per JMS Session instance.
	 */
	public boolean isCacheProducers() {
		return this.cacheProducers;
	}


	/**
	 * Resets the Session cache as well.
	 */
	public void resetConnection() {
		this.active = false;
		synchronized (this.cachedSessions) {
			for (Iterator it = this.cachedSessions.values().iterator(); it.hasNext();) {
				LinkedList sessionList = (LinkedList) it.next();
				synchronized (sessionList) {
					for (Iterator it2 = sessionList.iterator(); it2.hasNext();) {
						Session session = (Session) it2.next();
						try {
							session.close();
						}
						catch (Throwable ex) {
							logger.debug("Could not close cached JMS Session", ex);
						}
					}
				}
			}
			this.cachedSessions.clear();
		}
		this.active = true;

		// Now proceed with actual closing of the shared Connection...
		super.resetConnection();
	}

	/**
	 * Checks for a cached Session for the given mode.
	 */
	protected Session getSession(Connection con, Integer mode) throws JMSException {
		LinkedList sessionList = null;
		synchronized (this.cachedSessions) {
			sessionList = (LinkedList) this.cachedSessions.get(mode);
			if (sessionList == null) {
				sessionList = new LinkedList();
				this.cachedSessions.put(mode, sessionList);
			}
		}
		Session session = null;
		synchronized (sessionList) {
			if (!sessionList.isEmpty()) {
				session = (Session) sessionList.removeFirst();
			}
		}
		if (session != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found cached Session for mode " + mode + ": " + session);
			}
		}
		else {
			Session targetSession = createSession(con, mode);
			session = getCachedSessionProxy(targetSession, sessionList);
			if (logger.isDebugEnabled()) {
				logger.debug("Created cached Session for mode " + mode + ": " + session);
			}
		}
		return session;
	}

	/**
	 * Wrap the given Session with a proxy that delegates every method call to it
	 * but adapts close calls. This is useful for allowing application code to
	 * handle a special framework Session just like an ordinary Session.
	 * @param target the original Session to wrap
	 * @param sessionList the List of cached Sessions that the given Session belongs to
	 * @return the wrapped Session
	 */
	protected Session getCachedSessionProxy(Session target, LinkedList sessionList) {
		List classes = new ArrayList(3);
		classes.add(Session.class);
		if (target instanceof QueueSession) {
			classes.add(QueueSession.class);
		}
		if (target instanceof TopicSession) {
			classes.add(TopicSession.class);
		}
		return (Session) Proxy.newProxyInstance(
				Session.class.getClassLoader(),
				(Class[]) classes.toArray(new Class[classes.size()]),
				new CachedSessionInvocationHandler(target, sessionList));
	}


	/**
	 * Invocation handler for a cached JMS Session proxy.
	 */
	private class CachedSessionInvocationHandler implements InvocationHandler {

		private final Session target;

		private final LinkedList sessionList;

		private final Map cachedProducers = new HashMap();

		private boolean transactionOpen = false;

		public CachedSessionInvocationHandler(Session target, LinkedList sessionList) {
			this.target = target;
			this.sessionList = sessionList;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of Connection proxy.
				return new Integer(System.identityHashCode(proxy));
			}
			else if (method.getName().equals("toString")) {
				return "Cached JMS Session: " + this.target;
			}
			else if (method.getName().equals("close")) {
				// Handle close method: don't pass the call on.
				if (active) {
					synchronized (this.sessionList) {
						if (this.sessionList.size() < getSessionCacheSize()) {
							// Preserve rollback-on-close semantics.
							if (this.transactionOpen && this.target.getTransacted()) {
								this.transactionOpen = false;
								this.target.rollback();
							}
							// Allow for multiple close calls...
							if (!this.sessionList.contains(proxy)) {
								this.sessionList.addLast(proxy);
								if (logger.isDebugEnabled()) {
									logger.debug("Returned cached Session: " + this.target);
								}
							}
							// Remain open in the session list.
							return null;
						}
					}
				}
				// If we get here, we're supposed to shut down.
				physicalClose();
				return null;
			}
			else if ((method.getName().equals("createProducer") || method.getName().equals("createSender") ||
					method.getName().equals("createPublisher")) && isCacheProducers()) {
				Destination dest = (Destination) args[0];
				MessageProducer producer = (MessageProducer) this.cachedProducers.get(dest);
				if (producer != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Found cached MessageProducer for destination [" + dest + "]: " + producer);
					}
				}
				else {
					producer = this.target.createProducer(dest);
					this.cachedProducers.put(dest, producer);
					if (logger.isDebugEnabled()) {
						logger.debug("Created cached MessageProducer for destination [" + dest + "]: " + producer);
					}
				}
				this.transactionOpen = true;
				return new CachedMessageProducer(producer);
			}
			else if (method.getName().equals("commit") || method.getName().equals("rollback")) {
				this.transactionOpen = false;
			}
			else {
				this.transactionOpen = true;
			}
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}

		private void physicalClose() throws JMSException {
			try {
				for (Iterator it = this.cachedProducers.values().iterator(); it.hasNext();) {
					((MessageProducer) it.next()).close();
				}
			}
			finally {
				this.target.close();
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Closed cached Session: " + this.target);
			}
		}
	}

}
