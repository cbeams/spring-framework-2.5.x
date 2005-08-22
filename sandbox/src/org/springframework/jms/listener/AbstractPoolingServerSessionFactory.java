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

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jms.support.JmsUtils;

/**
 * @author Juergen Hoeller
 * @since 1.3
 */
public abstract class AbstractPoolingServerSessionFactory implements ServerSessionFactory {

	protected final Log logger = LogFactory.getLog(getClass());

	private int maxSize;


	/**
	 * Set the maximum size of the pool.
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * Return the maximum size of the pool.
	 */
	public int getMaxSize() {
		return this.maxSize;
	}


	protected ServerSession createServerSession(ListenerSessionManager sessionManager) throws JMSException {
		return new PooledServerSession(sessionManager);
	}

	protected void destroyServerSession(ServerSession serverSession) {
		((PooledServerSession) serverSession).close();
	}

	protected abstract void serverSessionFinished(ServerSession serverSession, ListenerSessionManager sessionManager);


	private class PooledServerSession implements ServerSession {

		private final Session session;

		private final Object monitor = new Object();

		private boolean active = false;

		public PooledServerSession(final ListenerSessionManager sessionManager) throws JMSException {
			this.session = sessionManager.createListenerSession();

			new Thread() {
				public void run() {
					active = true;
					synchronized (monitor) {
						while (active) {
							try {
								logger.debug("Waiting for PooledServerSession monitor");
								monitor.wait();
								logger.debug("Notified by PooledServerSession monitor");
							}
							catch (InterruptedException ex) {
							}
							if (active) {
								try {
									sessionManager.executeListenerSession(session);
								}
								finally {
									serverSessionFinished(PooledServerSession.this, sessionManager);
								}
							}
						}
					}
				}
			}.start();

			while (!this.active) {
			}
		}

		public Session getSession() {
			return session;
		}

		public void start() {
			synchronized (this.monitor) {
				logger.debug("Notifying PooledServerSession monitor");
				this.monitor.notify();
				logger.debug("Notified PooledServerSession monitor");
			}
		}

		public void close() {
			this.active = false;
			synchronized (this.monitor) {
				this.monitor.notify();
			}
			JmsUtils.closeSession(this.session);
		}
	}

}
