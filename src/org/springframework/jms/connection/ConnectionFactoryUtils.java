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

import org.springframework.jms.support.JmsUtils;
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

	/**
	 * Obtain a JMS Session that is synchronized with the current transaction, if any.
	 * @param cf the ConnectionFactory to obtain a Session for
	 * @return the transactional Session, or <code>null</code> if none found
	 * @throws JMSException in case of JMS failure
	 */
	public static Session getTransactionalSession(final ConnectionFactory cf) throws JMSException {
		return doGetTransactionalSession(cf, new ResourceFactory() {
			public Connection getConnection(JmsResourceHolder holder) {
				return holder.getConnection();
			}
			public Session getSession(JmsResourceHolder holder) {
				return holder.getSession();
			}
			public Connection createConnection() throws JMSException {
				return cf.createConnection();
			}
			public Session createSession(Connection con) throws JMSException {
				return con.createSession(false, Session.AUTO_ACKNOWLEDGE);
			}
		});
	}

	/**
	 * Obtain a JMS QueueSession that is synchronized with the current transaction, if any.
	 * <p>Mainly intended for use with the JMS 1.0.2 API.
	 * @param cf the ConnectionFactory to obtain a Session for
	 * @return the transactional Session, or <code>null</code> if none found
	 * @throws JMSException in case of JMS failure
	 */
	public static QueueSession getTransactionalQueueSession(final QueueConnectionFactory cf) throws JMSException {
		return (QueueSession) doGetTransactionalSession(cf, new ResourceFactory() {
			public Connection getConnection(JmsResourceHolder holder) {
				return holder.getConnection(QueueConnection.class);
			}
			public Session getSession(JmsResourceHolder holder) {
				return holder.getSession(QueueSession.class);
			}
			public Connection createConnection() throws JMSException {
				return cf.createQueueConnection();
			}
			public Session createSession(Connection con) throws JMSException {
				return ((QueueConnection) con).createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			}
		});
	}

	/**
	 * Obtain a JMS TopicSession that is synchronized with the current transaction, if any.
	 * <p>Mainly intended for use with the JMS 1.0.2 API.
	 * @param cf the ConnectionFactory to obtain a Session for
	 * @return the transactional Session, or <code>null</code> if none found
	 * @throws JMSException in case of JMS failure
	 */
	public static TopicSession getTransactionalTopicSession(final TopicConnectionFactory cf) throws JMSException {
		return (TopicSession) doGetTransactionalSession(cf, new ResourceFactory() {
			public Connection getConnection(JmsResourceHolder holder) {
				return holder.getConnection(TopicConnection.class);
			}
			public Session getSession(JmsResourceHolder holder) {
				return holder.getSession(TopicSession.class);
			}
			public Connection createConnection() throws JMSException {
				return cf.createTopicConnection();
			}
			public Session createSession(Connection con) throws JMSException {
				return ((TopicConnection) con).createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
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

		JmsResourceHolder conHolder = (JmsResourceHolder) TransactionSynchronizationManager.getResource(resourceKey);
		if (conHolder != null) {
			Session session = resourceFactory.getSession(conHolder);
			if (session != null || conHolder.isFrozen()) {
				return session;
			}
		}
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			return null;
		}
		JmsResourceHolder conHolderToUse = conHolder;
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
			conHolderToUse.addSession(session);
			if (!isExistingCon) {
				con.start();
			}
		}
		catch (JMSException ex) {
			JmsUtils.closeSession(session);
			JmsUtils.closeConnection(con);
			throw ex;
		}
		if (conHolderToUse != conHolder) {
			TransactionSynchronizationManager.registerSynchronization(
					new JmsResourceSynchronization(resourceKey, conHolderToUse));
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
		 * Fetch an appropriate Connection from the given JmsResourceHolder.
		 * @param holder the JmsResourceHolder
		 * @return an appropriate Connection fetched from the holder,
		 * or <code>null</code> if none found
		 */
		Connection getConnection(JmsResourceHolder holder);

		/**
		 * Fetch an appropriate Session from the given JmsResourceHolder.
		 * @param holder the JmsResourceHolder
		 * @return an appropriate Session fetched from the holder,
		 * or <code>null</code> if none found
		 */
		Session getSession(JmsResourceHolder holder);

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
	}


	/**
	 * Callback for resource cleanup at the end of a non-native JMS transaction
	 * (e.g. when participating in a JtaTransactionManager transaction).
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class JmsResourceSynchronization extends TransactionSynchronizationAdapter {

		private final Object resourceKey;

		private final JmsResourceHolder resourceHolder;

		private boolean holderActive = true;

		public JmsResourceSynchronization(Object resourceKey, JmsResourceHolder resourceHolder) {
			this.resourceKey = resourceKey;
			this.resourceHolder = resourceHolder;
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
			this.resourceHolder.closeAll();
		}
	}

}
