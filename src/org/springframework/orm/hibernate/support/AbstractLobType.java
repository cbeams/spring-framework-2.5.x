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

package org.springframework.orm.hibernate.support;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.UserType;
import net.sf.hibernate.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.orm.hibernate.LocalSessionFactoryBean;
import org.springframework.orm.hibernate.SessionFactoryUtils;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Abstract base class for Hibernate UserType implementations that map to LOBs.
 * Retrieves the LobHandler to use from LocalSessionFactoryBean at config time.
 *
 * <p>For writing LOBs, either an active Spring transaction synchronization
 * or an active JTA transaction (with "jtaTransactionManager" specified on
 * LocalSessionFactoryBean) is required.
 *
 * <p>Offers template methods for setting parameters and getting result values,
 * passing in the LobHandler or LobCreator to use.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.jdbc.support.lob.LobHandler
 * @see org.springframework.jdbc.support.lob.LobCreator
 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#setLobHandler
 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#setJtaTransactionManager
 */
public abstract class AbstractLobType implements UserType {

	/**
	 * Order value for TransactionSynchronization objects that clean up LobCreators.
	 * Return SessionFactoryUtils.SESSION_SYNCHRONIZATION_ORDER - 100 to execute
	 * LobCreator cleanup before Hibernate Session and JDBC Connection cleanup, if any.
	 * @see org.springframework.orm.hibernate.SessionFactoryUtils#SESSION_SYNCHRONIZATION_ORDER
	 */
	public static final int LOB_CREATOR_SYNCHRONIZATION_ORDER =
			SessionFactoryUtils.SESSION_SYNCHRONIZATION_ORDER - 100;

	protected final Log logger = LogFactory.getLog(getClass());

	private final LobHandler lobHandler;

	private final TransactionManager jtaTransactionManager;


	/**
	 * Constructor used by Hibernate: fetches config-time LobHandler and
	 * config-time JTA TransactionManager from LocalSessionFactoryBean.
	 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#getConfigTimeLobHandler
	 * @see org.springframework.orm.hibernate.LocalSessionFactoryBean#getConfigTimeTransactionManager
	 */
	protected AbstractLobType() {
		this(LocalSessionFactoryBean.getConfigTimeLobHandler(),
		    LocalSessionFactoryBean.getConfigTimeTransactionManager());
	}

	/**
	 * Constructor used for testing: takes an explicit LobHandler
	 * and an explicit JTA TransactionManager (can be <code>null</code>).
	 */
	protected AbstractLobType(LobHandler lobHandler, TransactionManager jtaTransactionManager) {
		this.lobHandler = lobHandler;
		this.jtaTransactionManager = jtaTransactionManager;
	}


	/**
	 * This implementation returns false.
	 */
	public boolean isMutable() {
		return false;
	}

	/**
	 * This implementation delegates to the Hibernate EqualsHelper.
	 * @see net.sf.hibernate.util.EqualsHelper#equals
	 */
	public boolean equals(Object x, Object y) throws HibernateException {
		return EqualsHelper.equals(x, y);
	}

	/**
	 * This implementation returns the passed-in value as-is.
	 */
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}


	/**
	 * This implementation delegates to nullSafeGetInternal,
	 * passing in the LobHandler of this type.
	 * @see #nullSafeGetInternal
	 */
	public final Object nullSafeGet(ResultSet rs, String[] names, Object owner)
			throws HibernateException, SQLException {

		if (this.lobHandler == null) {
			throw new IllegalStateException("No LobHandler found for configuration - " +
			    "lobHandler property must be set on LocalSessionFactoryBean");
		}

		try {
			return nullSafeGetInternal(rs, rs.findColumn(names[0]), this.lobHandler);
		}
		catch (IOException ex) {
			throw new HibernateException("I/O errors during LOB access", ex);
		}
	}

	/**
	 * This implementation delegates to nullSafeSetInternal,
	 * passing in a transaction-synchronized LobCreator for the
	 * LobHandler of this type.
	 * @see #nullSafeSetInternal
	 */
	public final void nullSafeSet(PreparedStatement st, Object value, int index)
			throws HibernateException, SQLException {

		if (this.lobHandler == null) {
			throw new IllegalStateException("No LobHandler found for configuration - " +
			    "lobHandler property must be set on LocalSessionFactoryBean");
		}

		LobCreator lobCreator = this.lobHandler.getLobCreator();
		try {
			nullSafeSetInternal(st, index, value, lobCreator);
		}
		catch (IOException ex) {
			throw new HibernateException("I/O errors during LOB access", ex);
		}
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering Spring transaction synchronization for Hibernate LOB type");
			TransactionSynchronizationManager.registerSynchronization(
			    new SpringLobCreatorSynchronization(lobCreator));
		}
		else {
			if (this.jtaTransactionManager != null) {
				try {
					int jtaStatus = this.jtaTransactionManager.getStatus();
					if (jtaStatus == Status.STATUS_ACTIVE || jtaStatus == Status.STATUS_MARKED_ROLLBACK) {
						logger.debug("Registering JTA transaction synchronization for Hibernate LOB type");
						this.jtaTransactionManager.getTransaction().registerSynchronization(
								new JtaLobCreatorSynchronization(lobCreator));
						return;
					}
				}
				catch (Exception ex) {
					throw new DataAccessResourceFailureException(
							"Could not register synchronization with JTA TransactionManager", ex);
				}
			}
			throw new IllegalStateException("Active Spring transaction synchronization or active " +
			    "JTA transaction with 'jtaTransactionManager' on LocalSessionFactoryBean required");
		}
	}

	/**
	 * Template method to extract a value from the given result set.
	 * @param rs the ResultSet to extract from
	 * @param index the index in the ResultSet
	 * @param lobHandler the LobHandler to use
	 * @return the extracted value
	 * @throws SQLException if thrown by JDBC methods
	 * @throws IOException if thrown by streaming methods
	 * @throws HibernateException in case of any other exceptions
	 */
	protected abstract Object nullSafeGetInternal(ResultSet rs, int index, LobHandler lobHandler)
			throws SQLException, IOException, HibernateException;

	/**
	 * Template method to set the given parameter value on the given statement.
	 * @param ps the PreparedStatement to set on
	 * @param index the statement parameter index
	 * @param value the value to set
	 * @param lobCreator the LobCreator to use
	 * @throws SQLException if thrown by JDBC methods
	 * @throws IOException if thrown by streaming methods
	 * @throws HibernateException in case of any other exceptions
	 */
	protected abstract void nullSafeSetInternal(
	    PreparedStatement ps, int index, Object value, LobCreator lobCreator)
			throws SQLException, IOException, HibernateException;


	/**
	 * Callback for resource cleanup at the end of a Spring transaction.
	 * Invokes LobCreator.close to clean up temporary LOBs that might have been created.
	 * @see org.springframework.jdbc.support.lob.LobCreator#close
	 */
	private static class SpringLobCreatorSynchronization extends TransactionSynchronizationAdapter {

		private final LobCreator lobCreator;

		private boolean beforeCompletionCalled = false;

		public SpringLobCreatorSynchronization(LobCreator lobCreator) {
			this.lobCreator = lobCreator;
		}

		public int getOrder() {
			return LOB_CREATOR_SYNCHRONIZATION_ORDER;
		}

		public void beforeCompletion() {
			// Close the LobCreator early if possible, to avoid issues with strict JTA
			// implementations that issue warnings when doing JDBC operations after
			// transaction completion.
			this.beforeCompletionCalled = true;
			this.lobCreator.close();
		}

		public void afterCompletion(int status) {
			if (!this.beforeCompletionCalled) {
				// beforeCompletion not called before (probably because of flushing on commit
				// in HibernateTransactionManager, after the chain of beforeCompletion calls).
				// Close the LobCreator here.
				this.lobCreator.close();
			}
		}
	}


	/**
	 * Callback for resource cleanup at the end of a JTA transaction.
	 * Invokes LobCreator.close to clean up temporary LOBs that might have been created.
	 * @see org.springframework.jdbc.support.lob.LobCreator#close
	 */
	private static class JtaLobCreatorSynchronization implements Synchronization {

		private final LobCreator lobCreator;

		private boolean beforeCompletionCalled = false;

		public JtaLobCreatorSynchronization(LobCreator lobCreator) {
			this.lobCreator = lobCreator;
		}

		public void beforeCompletion() {
			// Close the LobCreator early if possible, to avoid issues with strict JTA
			// implementations that issue warnings when doing JDBC operations after
			// transaction completion.
			this.beforeCompletionCalled = true;
			this.lobCreator.close();
		}

		public void afterCompletion(int status) {
			if (!this.beforeCompletionCalled) {
				// beforeCompletion not called before (probably because of JTA rollback).
				// Close the LobCreator here.
				this.lobCreator.close();
			}
		}
	}

}
