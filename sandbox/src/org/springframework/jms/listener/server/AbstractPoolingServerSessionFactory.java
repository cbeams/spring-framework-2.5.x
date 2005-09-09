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

package org.springframework.jms.listener.server;

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.support.JmsUtils;
import org.springframework.scheduling.timer.TimerTaskExecutor;

/**
 * @author Juergen Hoeller
 * @since 1.3
 */
public abstract class AbstractPoolingServerSessionFactory implements ServerSessionFactory {

	protected final Log logger = LogFactory.getLog(getClass());

	private TaskExecutor taskExecutor;

	private int maxSize;


	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	protected TaskExecutor getTaskExecutor() {
		return this.taskExecutor;
	}

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


	protected final ServerSession createServerSession(ListenerSessionManager sessionManager) throws JMSException {
		return new PooledServerSession(sessionManager);
	}

	protected final void destroyServerSession(ServerSession serverSession) {
		if (serverSession != null) {
			((PooledServerSession) serverSession).close();
		}
	}


	protected abstract void serverSessionFinished(ServerSession serverSession, ListenerSessionManager sessionManager);


	private class PooledServerSession implements ServerSession {

		private final ListenerSessionManager sessionManager;

		private final Session session;

		private TaskExecutor taskExecutor;

		private TimerTaskExecutor internalExecutor;

		public PooledServerSession(final ListenerSessionManager sessionManager) throws JMSException {
			this.sessionManager = sessionManager;
			this.session = sessionManager.createListenerSession();
			this.taskExecutor = getTaskExecutor();
			if (this.taskExecutor == null) {
				this.internalExecutor = new TimerTaskExecutor();
				this.internalExecutor.afterPropertiesSet();
				this.taskExecutor = this.internalExecutor;
			}
		}

		public Session getSession() {
			return session;
		}

		public void start() {
			this.taskExecutor.execute(new Runnable() {
				public void run() {
					try {
						sessionManager.executeListenerSession(session);
					}
					finally {
						serverSessionFinished(PooledServerSession.this, sessionManager);
					}
				}
			});
		}

		public void close() {
			if (this.internalExecutor != null) {
				this.internalExecutor.destroy();
			}
			JmsUtils.closeSession(this.session);
		}
	}

}
