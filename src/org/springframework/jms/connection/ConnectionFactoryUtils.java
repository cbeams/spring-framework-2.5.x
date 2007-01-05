/*
 * Copyright 2002-2006 the original author or authors.
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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Helper class for obtaining transactional JMS resources
 * for a given ConnectionFactory.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class ConnectionFactoryUtils {

	private static final Log logger = LogFactory.getLog(ConnectionFactoryUtils.class);


	/**
	 * Release the given Connection, stopping it (if necessary) and eventually closing it.
	 * <p>Checks {@link SmartConnectionFactory#shouldStop}, if available.
	 * This is essentially a more sophisticated version of
	 * {@link org.springframework.jms.support.JmsUtils#closeConnection}.
	 * @param con the Connection to release
	 * (if this is <code>null</code>, the call will be ignored)
	 * @param cf the ConnectionFactory that the Connection was obtained from
	 * (may be <code>null</code>)
	 * @param started whether the Connection might have been started by the application
	 * @see SmartConnectionFactory#shouldStop
	 * @see org.springframework.jms.support.JmsUtils#closeConnection
	 */
	public static void releaseConnection(Connection con, ConnectionFactory cf, boolean started) {
		if (con == null) {
			return;
		}
		if (started && cf instanceof SmartConnectionFactory && ((SmartConnectionFactory) cf).shouldStop(con)) {
			try {
				con.stop();
			}
			catch (Throwable ex) {
				logger.debug("Could not stop JMS Connection before closing it", ex);
			}
		}
		try {
			con.close();
		}
		catch (Throwable ex) {
			logger.debug("Could not close JMS Connection", ex);
		}
	}


	/**
	 * Obtain a JMS Session that is synchronized with the current transaction, if any.
	 * @param cf the ConnectionFactory to obtain a Session for
	 * @param existingCon the existing JMS Connection to obtain a Session for
	 * (may be <code>null</code>)
	 * @param synchedLocalTransactionAllowed whether to allow for a local JMS transaction
	 * that is synchronized with a Spring-managed transaction (where the main transaction
	 * might be a JDBC-based one for a specific DataSource, for example), with the JMS
	 * transaction committing right after the main transaction. If not allowed, the given
	 * ConnectionFactory needs to handle transaction enlistment underneath the covers.
	 * @return the transactional Session, or <code>null</code> if none found
	 * @throws JMSException in case of JMS failure
	 */
	public static Session getTransactionalSession(
			final ConnectionFactory cf, final Connection existingCon, final boolean synchedLocalTransactionAllowed)
			throws JMSException {

		return doGetTransactionalSession(cf, new ResourceFactory() {
			public Session getSession(JmsResourceHolder holder) {
				return holder.getSession(Session.class, existingCon);
			}
			public Connection getConnection(JmsResourceHolder holder) {
				return (existingCon != null ? existingCon : holder.getConnection());
			}
			public Connection createConnection() throws JMSException {
				return cf.createConnection();
			}
			public Session createSession(Connection con) throws JMSException {
				return con.createSession(synchedLocalTransactionAllowed, Session.AUTO_ACKNOWLEDGE);
			}
			public boolean isSynchedLocalTransactionAllowed() {
				return synchedLocalTransactionAllowed;
			}
		});
	}

	/**
	 * Obtain a JMS QueueSession that is synchronized with the current transaction, if any.
	 * <p>Mainly intended for use with the JMS 1.0.2 API.
	 * @param cf the ConnectionFactory to obtain a Session for
	 * @param existingCon the existing JMS Connection to obtain a Session for
	 * (may be <code>null</code>)
	 * @param synchedLocalTransactionAllowed whether to allow for a local JMS transaction
	 * that is synchronized with a Spring-managed transaction (where the main transaction
	 * might be a JDBC-based one for a specific DataSource, for example), with the JMS
	 * transaction committing right after the main transaction. If not allowed, the given
	 * ConnectionFactory needs to handle transaction enlistment underneath the covers.
	 * @return the transactional Session, or <code>null</code> if none found
	 * @throws JMSException in case of JMS failure
	 */
	public static QueueSession getTransactionalQueueSession(
			final QueueConnectionFactory cf, final QueueConnection existingCon, final boolean synchedLocalTransactionAllowed)
			throws JMSException {

		return (QueueSession) doGetTransactionalSession(cf, new ResourceFactory() {
			public Session getSession(JmsResourceHolder holder) {
				return holder.getSession(QueueSession.class, existingCon);
			}
			public Connection getConnection(JmsResourceHolder holder) {
				return (existingCon != null ? existingCon : holder.getConnection(QueueConnection.class));
			}
			public Connection createConnection() throws JMSException {
				return cf.createQueueConnection();
			}
			public Session createSession(Connection con) throws JMSException {
				return ((QueueConnection) con).createQueueSession(synchedLocalTransactionAllowed, Session.AUTO_ACKNOWLEDGE);
			}
			public boolean isSynchedLocalTransactionAllowed() {
				return synchedLocalTransactionAllowed;
			}
		});
	}

	/**
	 * Obtain a JMS TopicSession that is synchronized with the current transaction, if any.
	 * <p>Mainly intended for use with the JMS 1.0.2 API.
	 * @param cf the ConnectionFactory to obtain a Session for
	 * @param existingCon the existing JMS Connection to obtain a Session for
	 * (may be <code>null</code>)
	 * @param synchedLocalTransactionAllowed whether to allow for a local JMS transaction
	 * that is synchronized with a Spring-managed transaction (where the main transaction
	 * might be a JDBC-based one for a specific DataSource, for example), with the JMS
	 * transaction committing right after the main transaction. If not allowed, the given
	 * ConnectionFactory needs to handle transaction enlistment underneath the covers.
	 * @return the transactional Session, or <code>null</code> if none found
	 * @throws JMSException in case of JMS failure
	 */
	public static TopicSession getTransactionalTopicSession(
			final TopicConnectionFactory cf, final TopicConnection existingCon, final boolean synchedLocalTransactionAllowed)
			throws JMSException {

		return (TopicSession) doGetTransactionalSession(cf, new ResourceFactory() {
			public Session getSession(JmsResourceHolder holder) {
				return holder.getSession(TopicSession.class, existingCon);
			}
			public Connection getConnection(JmsResourceHolder holder) {
				return (existingCon != null ? existingCon : holder.getConnection(TopicConnection.class));
			}
			public Connection createConnection() throws JMSException {
				return cf.createTopicConnection();
			}
			public Session createSession(Connection con) throws JMSException {
				return ((TopicConnection) con).createTopicSession(synchedLocalTransactionAllowed, Session.AUTO_ACKNOWLEDGE);
			}
			public boolean isSynchedLocalTransactionAllowed() {
				return synchedLocalTransactionAllowed;
			}
		});
	}

	/**
	 * Obtain a JMS Session that is synchronized with the current transaction, if any.
	 * @param resourceKey the TransactionSynchronizationManager key to bind to
	 * (usually the ConnectionFactory)
	 * @param resourceFactory the ResourceFactory to use for extracting or creating
	 * JMS resources
	 * @return the transactional Session, or <code>null</code> if none found
	 * @throws JMSException in case of JMS failure
	 */
	public static Session doGetTransactionalSession(Object resourceKey, ResourceFactory resourceFactory)
			throws JMSException {

		Assert.notNull(resourceKey, "Resource key must not be null");
		Assert.notNull(resourceKey, "ResourceFactory must not be null");

		JmsResourceHolder resourceHolder =
				(JmsResourceHolder) TransactionSynchronizationManager.getResource(resourceKey);
		if (resourceHolder != null) {
			Session session = resourceFactory.getSession(resourceHolder);
			if (session != null || resourceHolder.isFrozen()) {
				return session;
			}
		}
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			return null;
		}
		JmsResourceHolder conHolderToUse = resourceHolder;
		if (conHolderToUse == null) {
			conHolderToUse = new JmsResourceHolder();
		}
		Connection con = resourceFactory.getConnection(conHolderToUse);
		Session session = null;
		try {
			boolean isExistingCon = (con != null);
			if (!isExistingCon) {
				con = resourceFactory.createConnection();
				conHolderToUse.addConnection(con);
			}
			session = resourceFactory.createSession(con);
			conHolderToUse.addSession(session, con);
			if (!isExistingCon) {
				con.start();
			}
		}
		catch (JMSException ex) {
			if (session != null) {
				try {
					session.close();
				}
				catch (Throwable ex2) {
					// ignore
				}
			}
			if (con != null) {
				try {
					con.close();
				}
				catch (Throwable ex2) {
					// ignore
				}
			}
			throw ex;
		}
		if (conHolderToUse != resourceHolder) {
			TransactionSynchronizationManager.registerSynchronization(
					new JmsResourceSynchronization(
							resourceKey, conHolderToUse, resourceFactory.isSynchedLocalTransactionAllowed()));
			conHolderToUse.setSynchronizedWithTransaction(true);
			TransactionSynchronizationManager.bindResource(resourceKey, conHolderToUse);
		}
		return session;
	}


	/**
	 * Callback interface for resource creation.
	 * Serving as argument for the <code>doGetTransactionalSession</code> method.
	 */
	public interface ResourceFactory {

		/**
		 * Fetch an appropriate Session from the given JmsResourceHolder.
		 * @param holder the JmsResourceHolder
		 * @return an appropriate Session fetched from the holder,
		 * or <code>null</code> if none found
		 */
		Session getSession(JmsResourceHolder holder);

		/**
		 * Fetch an appropriate Connection from the given JmsResourceHolder.
		 * @param holder the JmsResourceHolder
		 * @return an appropriate Connection fetched from the holder,
		 * or <code>null</code> if none found
		 */
		Connection getConnection(JmsResourceHolder holder);

		/**
		 * Create a new JMS Connection for registration with a JmsResourceHolder.
		 * @return the new JMS Connection
		 * @throws JMSException if thrown by JMS API methods
		 */
		Connection createConnection() throws JMSException;

		/**
		 * Create a new JMS Session for registration with a JmsResourceHolder.
		 * @param con the JMS Connection to create a Session for
		 * @return the new JMS Session
		 * @throws JMSException if thrown by JMS API methods
		 */
		Session createSession(Connection con) throws JMSException;

		/**
		 * Return whether to allow for a local JMS transaction that is synchronized with
		 * a Spring-managed transaction (where the main transaction might be a JDBC-based
		 * one for a specific DataSource, for example), with the JMS transaction
		 * committing right after the main transaction.
		 */
		boolean isSynchedLocalTransactionAllowed();
	}


	/**
	 * Callback for resource cleanup at the end of a non-native JMS transaction
	 * (e.g. when participating in a JtaTransactionManager transaction).
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class JmsResourceSynchronization extends TransactionSynchronizationAdapter {

		private final Object resourceKey;

		private final JmsResourceHolder resourceHolder;

		private final boolean transacted;

		private boolean holderActive = true;

		public JmsResourceSynchronization(Object resourceKey, JmsResourceHolder resourceHolder, boolean transacted) {
			this.resourceKey = resourceKey;
			this.resourceHolder = resourceHolder;
			this.transacted = transacted;
		}

		public void suspend() {
			if (this.holderActive) {
				TransactionSynchronizationManager.unbindResource(this.resourceKey);
			}
		}

		public void resume() {
			if (this.holderActive) {
				TransactionSynchronizationManager.bindResource(this.resourceKey, this.resourceHolder);
			}
		}

		public void beforeCompletion() {
			TransactionSynchronizationManager.unbindResource(this.resourceKey);
			this.holderActive = false;
			if (!this.transacted) {
				this.resourceHolder.closeAll();
			}
		}

		public void afterCommit() {
			if (this.transacted) {
				try {
					this.resourceHolder.commitAll();
				}
				catch (JMSException ex) {
					throw new SynchedLocalTransactionFailedException("Local JMS transaction failed to commit", ex);
				}
			}
		}

		public void afterCompletion(int status) {
			if (this.transacted) {
				this.resourceHolder.closeAll();
			}
		}
	}

}
