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

import org.springframework.jms.support.JmsUtils;

/**
 * @author Juergen Hoeller
 * @since 26.05.2005
 */
public class SingleServerSessionFactory implements ServerSessionFactory {

	private final Map singleServerSessionCache = Collections.synchronizedMap(new HashMap());

	public ServerSession getServerSession(ListenerSessionManager sessionManager) throws JMSException {
		SingleServerSession serverSession =
				(SingleServerSession) this.singleServerSessionCache.get(sessionManager);
		if (serverSession == null) {
			serverSession = new SingleServerSession(sessionManager);
			this.singleServerSessionCache.put(sessionManager, serverSession);
		}
		return serverSession;
	}

	public void close(ListenerSessionManager sessionManager) {
		SingleServerSession serverSession =
				(SingleServerSession) this.singleServerSessionCache.get(sessionManager);
		if (serverSession != null) {
			serverSession.close();
		}
	}


	private class SingleServerSession implements ServerSession {

		private final ListenerSessionManager sessionManager;

		private final Session session;

		public SingleServerSession(ListenerSessionManager sessionManager) throws JMSException {
			this.sessionManager = sessionManager;
			this.session = sessionManager.createListenerSession();
		}

		public Session getSession() {
			return session;
		}

		public void start() {
			this.sessionManager.executeListenerSession(this.session);
		}

		public void close() {
			JmsUtils.closeSession(this.session);
		}
	}

}
