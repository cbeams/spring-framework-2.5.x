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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;
import oracle.toplink.threetier.ClientSession;
import oracle.toplink.threetier.ConnectionPolicy;
import oracle.toplink.threetier.ServerSession;

/**
 * Full-fledged default implementation of the SessionFactory interface:
 * creates ClientSessions for a given ServerSession.
 *
 * <p>Can create a special ClientSession subclass for managed Sessions, carrying
 * an active UnitOfWork that expects to be committed at transaction completion
 * (just like a plain TopLink Session does within a JTA transaction).
 *
 * <p>Can also create a transaction-aware Session reference that returns the
 * active transactional Session on <code>getActiveSession</code>.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see SingleSessionFactory
 * @see oracle.toplink.sessions.Session#getActiveUnitOfWork()
 * @see oracle.toplink.sessions.Session#getActiveSession()
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
	public Session createManagedClientSession() {
		return new ManagedClientSession(this.serverSession, this.serverSession.getDefaultConnectionPolicy());
	}

	/**
	 * Create a transaction-aware Session refeence for this factory's ServerSession,
	 * expecting transactions to be registered for this SessionFactory.
	 * @see oracle.toplink.sessions.Session#getActiveSession()
	 * @see oracle.toplink.sessions.Session#getActiveUnitOfWork()
	 */
	public Session createTransactionAwareSession() throws TopLinkException {
		return createTransactionAwareSession(this);
	}

	/**
	 * Create a transaction-aware Session refeence for this factory's ServerSession,
	 * expecting transactions to be registered for the given SessionFactory.
	 * <p>This method is public to allow custom SessionFactory facades to access
	 * it directly, if necessary.
	 * @param sessionFactory the SessionFactory that transactions
	 * are expected to be registered for
	 * @see oracle.toplink.sessions.Session#getActiveSession()
	 * @see oracle.toplink.sessions.Session#getActiveUnitOfWork()
	 */
	public Session createTransactionAwareSession(SessionFactory sessionFactory) throws TopLinkException {
		return (Session) Proxy.newProxyInstance(
				Session.class.getClassLoader(),
				new Class[] {Session.class},
				new TransactionAwareInvocationHandler(sessionFactory, this.serverSession));
	}

	/**
	 * Shut the pre-configured TopLink ServerSession down.
	 * @see oracle.toplink.sessions.DatabaseSession#logout()
	 * @see oracle.toplink.sessions.Session#release()
	 */
	public void close() {
		this.serverSession.logout();
		this.serverSession.release();
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


	/**
	 * Invocation handler that delegates <code>getActiveSession</code> calls
	 * to SessionFactoryUtils, for being aware of thread-bound transactions.
	 */
	private static class TransactionAwareInvocationHandler implements InvocationHandler {

		private final SessionFactory sessionFactory;

		private final Session target;

		public TransactionAwareInvocationHandler(SessionFactory sessionFactory, Session target) {
			this.sessionFactory = sessionFactory;
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on Session interface coming in...

			if (method.getName().equals("getActiveSession")) {
				// Handle getActiveSession method: return transactional Session, if any.
				try {
					return SessionFactoryUtils.doGetSession(this.sessionFactory, false);
				}
				catch (IllegalStateException ex) {
					// getActiveSession is supposed to return the Session itself if no active one found.
					return this.target;
				}
			}
			else if (method.getName().equals("getActiveUnitOfWork")) {
				// Handle getActiveUnitOfWork method: return transactional UnitOfWork, if any.
				try {
					return SessionFactoryUtils.doGetSession(this.sessionFactory, false).getActiveUnitOfWork();
				}
				catch (IllegalStateException ex) {
					// getActiveUnitOfWork is supposed to return null if no active one found.
					return null;
				}
			}
			else if (method.getName().equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of SessionFactory proxy.
				return new Integer(hashCode());
			}

			// Invoke method on target SessionFactory.
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
