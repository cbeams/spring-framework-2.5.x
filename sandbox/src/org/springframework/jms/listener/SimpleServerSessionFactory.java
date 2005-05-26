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

package org.springframework.jms.listener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jms.support.JmsUtils;

/**
 * @author Juergen Hoeller
 * @since 26.05.2005
 */
public class SimpleServerSessionFactory implements ServerSessionFactory {

	protected final Log logger = LogFactory.getLog(getClass());

	private transient final Object monitor = new Object();

	private int concurrencyLimit = 10;

	private int concurrencyCount = 0;

	private Map concurrencyCountMap = Collections.synchronizedMap(new HashMap());


	/**
	 * Set the maximum number of parallel invocations that this interceptor
	 * allows. Default is 1 (having the same effect as a synchronized block).
	 */
	public void setConcurrencyLimit(int concurrencyLimit) {
		this.concurrencyLimit = concurrencyLimit;
	}


	public ServerSession getServerSession(ListenerSessionManager sessionManager) throws JMSException {
		boolean debug = logger.isDebugEnabled();
		synchronized (this.monitor) {
			while (this.concurrencyCount >= this.concurrencyLimit) {
				if (debug) {
					logger.debug("JMS ServerSession concurrency count " + this.concurrencyCount +
							" has reached limit " + this.concurrencyLimit + " - blocking");
				}
				try {
					this.monitor.wait();
				}
				catch (InterruptedException ex) {
				}
			}
			if (debug) {
				logger.debug("Creating JMS ServerSession at concurrency count " + this.concurrencyCount);
			}
			this.concurrencyCount++;
			synchronized (this.concurrencyCountMap) {
				Integer specificCount = (Integer) this.concurrencyCountMap.get(sessionManager);
				if (specificCount == null) {
					specificCount = new Integer(0);
				}
				this.concurrencyCountMap.put(sessionManager, new Integer(specificCount.intValue() + 1));
			}
		}
		return new SimpleServerSession(sessionManager);
	}

	protected void serverSessionFinished(ListenerSessionManager sessionManager) {
		synchronized (this.monitor) {
			this.concurrencyCount--;
			synchronized (this.concurrencyCountMap) {
				Integer specificCount = (Integer) this.concurrencyCountMap.get(sessionManager);
				if (specificCount.intValue() > 1) {
					this.concurrencyCountMap.put(sessionManager, new Integer(specificCount.intValue() - 1));
				}
				else {
					this.concurrencyCountMap.remove(sessionManager);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("JMS ServerSession finished at concurrency count " + this.concurrencyCount);
			}
			this.monitor.notify();
		}
	}

	public void close(ListenerSessionManager sessionManager) {
		boolean debug = logger.isDebugEnabled();
		synchronized (this.monitor) {
			Integer specificCount = null;
			while ((specificCount = (Integer) this.concurrencyCountMap.get(sessionManager)) != null) {
				if (debug) {
					logger.debug("Still " + specificCount +
							" JMS ServerSessions open for ListenerSessionFactory - delaying shutdown");
				}
				try {
					this.monitor.wait();
				}
				catch (InterruptedException ex) {
				}
			}
		}
	}


	private class SimpleServerSession implements ServerSession {

		private final ListenerSessionManager sessionManager;

		private final Session session;

		public SimpleServerSession(ListenerSessionManager sessionManager) throws JMSException {
			this.sessionManager = sessionManager;
			this.session = sessionManager.createListenerSession();
		}

		public Session getSession() {
			return session;
		}

		public void start() {
			new Thread() {
				public void run() {
					try {
						sessionManager.executeListenerSession(session);
					}
					finally {
						JmsUtils.closeSession(session);
						serverSessionFinished(sessionManager);
					}
				}
			}.start();
		}
	}

}
