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

package org.springframework.orm.toplink;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;
import oracle.toplink.threetier.ClientSession;
import oracle.toplink.threetier.ConnectionPolicy;
import oracle.toplink.threetier.ServerSession;

/**
 * Full-fledged implementation of the SessionFactory interface:
 * creates ClientSessions for a given ServerSession.
 *
 * <p>Creates a special ClientSession subclass for managed Sessions, carrying
 * an active UnitOfWork that expects to be committed at transaction completion
 * (just like a plain TopLink Session does within a JTA transaction).
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see SingleSessionFactory
 */
public class ServerSessionFactory implements SessionFactory {

	private final ServerSession serverSession;

	/**
	 * Create a new ServerSessionFactory for the given ServerSession.
	 * @param serverSession the TopLink ServerSession to create ClientSessions for
	 */
	public ServerSessionFactory(ServerSession serverSession) {
		this.serverSession = serverSession;
	}

	/**
	 * Create a plain ClientSession for this factory's ServerSession.
	 * @see oracle.toplink.threetier.ServerSession#acquireClientSession()
	 */
	public Session createSession() {
		return this.serverSession.acquireClientSession();
	}

	/**
	 * Create a managed ClientSession for this factory's ServerSession.
	 * @see oracle.toplink.threetier.ClientSession
	 */
	public Session createManagedSession() {
		return new ManagedClientSession(this.serverSession, this.serverSession.getDefaultConnectionPolicy());
	}


	/**
	 * Special ClientSession subclass that carries an active UnitOfWork,
	 * which expects to be committed at transaction completion
	 * (just like a plain TopLink Session does within a JTA transaction).
	 */
	private static class ManagedClientSession extends ClientSession {

		private final UnitOfWork activeUnitOfWork;

		public ManagedClientSession(ServerSession server, ConnectionPolicy connectionPolicy) {
			super(server, connectionPolicy);
			this.activeUnitOfWork = acquireUnitOfWork();
		}

		/**
		 * Return this Session as active Session.
		 */
		public Session getActiveSession() {
			return this;
		}

		/**
		 * Return the UnitOfWork held by this managed Session
		 * as active UnitOfWork.
		 */
		public UnitOfWork getActiveUnitOfWork() {
			return this.activeUnitOfWork;
		}

		/**
		 * Release both the UnitOfWork held by this managed Session
		 * and this Session itself.
		 */
		public void release() throws DatabaseException {
			this.activeUnitOfWork.release();
			super.release();
		}
	}

}
