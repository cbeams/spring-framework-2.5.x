/*
 * Copyright 2002-2004 the original author or authors.
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
import javax.jms.Session;
import javax.jms.TransactionRolledBackException;

import org.springframework.jms.support.JmsUtils;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation for a single JMS ConnectionFactory.
 * Binds a JMS Connection/Session pair from the specified ConnectionFactory to
 * the thread, potentially allowing for one thread session per connection factory.
 *
 * <p>This local strategy is an alternative to executing JMS operations within
 * JTA transactions. Its advantage is that it is able to work in any environment,
 * for example a standalone application or a test suite. It is <i>not</i> able to
 * provide XA transactions, for example to share transactions with data access.
 *
 * <p>JmsTemplate will auto-detect such thread-bound connection/session pairs
 * and automatically participate in them. There is currently no support for
 * letting plain JMS code participate in such transactions.
 *
 * <p>This transaction strategy will typically be used in combination with
 * SingleConnectionFactory, which uses a single JMS connection for all JMS
 * access to save resources, typically in a standalone application. Each
 * transaction will then use the same JMS Connection but its own JMS Session.
 *
 * <p>Turns off transaction synchronization by default, as this manager might
 * be used alongside a datastore-based Spring transaction manager like
 * DataSourceTransactionManager, which has stronger needs for synchronization.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.jms.core.JmsTemplate
 * @see SingleConnectionFactory
 */
public class JmsTransactionManager extends AbstractPlatformTransactionManager {

	private ConnectionFactory connectionFactory;


	/**
	 * Create a new JmsTransactionManager for bean-style usage.
	 * <p>Note: The ConnectionFactory has to be set before using the instance.
	 * This constructor can be used to prepare a JmsTemplate via a BeanFactory,
	 * typically setting the ConnectionFactory via setConnectionFactory.
	 * <p>Turns off transaction synchronization by default, as this manager might
	 * be used alongside a datastore-based Spring transaction manager like
	 * DataSourceTransactionManager, which has stronger needs for synchronization.
	 * Only one manager is allowed to drive synchronization at any point of time.
	 * @see #setConnectionFactory
	 * @see #setTransactionSynchronization
	 */
	public JmsTransactionManager() {
		setTransactionSynchronization(SYNCHRONIZATION_NEVER);
	}

	/**
	 * Create a new JmsTransactionManager, given a ConnectionFactory.
	 * @param connectionFactory the ConnectionFactory to obtain connections from
	 */
	public JmsTransactionManager(ConnectionFactory connectionFactory) {
		this();
		this.connectionFactory = connectionFactory;
		afterPropertiesSet();
	}

	/**
	 * Set the JMS ConnectionFactory that this instance should manage transactions for.
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Return the JMS ConnectionFactory that this instance should manage transactions for.
	 */
	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	/**
	 * Make sure the connection factory has been set.
	 */
	public void afterPropertiesSet() {
		if (this.connectionFactory == null) {
			throw new IllegalArgumentException("connectionFactory is required");
		}
	}


	/**
	 * Create a JMS Connection via this template's ConnectionFactory.
	 * <p>This implementation uses JMS 1.1 API.
	 * @return the new JMS Connection
	 * @throws javax.jms.JMSException if thrown by JMS API methods
	 */
	protected Connection createConnection() throws JMSException {
		return getConnectionFactory().createConnection();
	}

	/**
	 * Create a JMS Session for the given Connection.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param con the JMS Connection to create a Session for
	 * @return the new JMS Session
	 * @throws javax.jms.JMSException if thrown by JMS API methods
	 */
	protected Session createSession(Connection con) throws JMSException {
		return con.createSession(true, Session.AUTO_ACKNOWLEDGE);
	}


	protected Object doGetTransaction() {
		JmsTransactionObject txObject = new JmsTransactionObject();
		txObject.setConnectionHolder(
				(ConnectionHolder) TransactionSynchronizationManager.getResource(getConnectionFactory()));
		return txObject;
	}

	protected boolean isExistingTransaction(Object transaction) {
		JmsTransactionObject txObject = (JmsTransactionObject) transaction;
		return (txObject.getConnectionHolder() != null);
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException("JMS does not support an isolation level concept");
		}
		JmsTransactionObject txObject = (JmsTransactionObject) transaction;
		Connection con = null;
		Session session = null;
		try {
			con = createConnection();
			session = createSession(con);
			txObject.setConnectionHolder(new ConnectionHolder(con, session));
			txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getConnectionHolder().setTimeoutInSeconds(definition.getTimeout());
			}
			TransactionSynchronizationManager.bindResource(
					getConnectionFactory(), txObject.getConnectionHolder());
		}
		catch (JMSException ex) {
			JmsUtils.closeSession(session);
			JmsUtils.closeConnection(con);
			throw new CannotCreateTransactionException("Could not create JMS transaction", ex);
		}
	}

	protected Object doSuspend(Object transaction) {
		JmsTransactionObject txObject = (JmsTransactionObject) transaction;
		txObject.setConnectionHolder(null);
		return TransactionSynchronizationManager.unbindResource(getConnectionFactory());
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		ConnectionHolder conHolder = (ConnectionHolder) suspendedResources;
		TransactionSynchronizationManager.bindResource(getConnectionFactory(), conHolder);
	}

	protected void doCommit(DefaultTransactionStatus status) {
		JmsTransactionObject txObject = (JmsTransactionObject) status.getTransaction();
		try {
			txObject.getConnectionHolder().getSession().commit();
		}
		catch (TransactionRolledBackException ex) {
			throw new UnexpectedRollbackException("JMS transaction rolled back", ex);
		}
		catch (JMSException ex) {
			throw new TransactionSystemException("JMS failure on commit", ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		JmsTransactionObject txObject = (JmsTransactionObject) status.getTransaction();
		try {
			txObject.getConnectionHolder().getSession().rollback();
		}
		catch (JMSException ex) {
			throw new TransactionSystemException("JMS failure on rollback", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		JmsTransactionObject txObject = (JmsTransactionObject) status.getTransaction();
		txObject.getConnectionHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		JmsTransactionObject txObject = (JmsTransactionObject) transaction;
		TransactionSynchronizationManager.unbindResource(getConnectionFactory());
		txObject.getConnectionHolder().clear();
		JmsUtils.closeSession(txObject.getConnectionHolder().getSession());
		JmsUtils.closeConnection(txObject.getConnectionHolder().getConnection());
	}

}
