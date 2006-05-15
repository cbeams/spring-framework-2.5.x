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

package org.springframework.orm.jpa.spi.toplink;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerImpl;
import oracle.toplink.essentials.internal.ejb.cmp3.base.EntityManagerFactoryImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.sessions.UnitOfWork;

import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.SimpleConnectionHandle;
import org.springframework.orm.jpa.DefaultJpaDialect;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.PortableEntityManagerFactoryPlusOperations;
import org.springframework.orm.jpa.PortableEntityManagerPlusOperations;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * TopLink implementation of JpaDialect interface. Main value add
 * is the ability to obtain a JDBC Connection from the current TopLink session, which
 * enables the mixing of JDBC and TopLink JPA operations in the same transaction,
 * as per Spring's normal data access contract. Spring's JPA support will work
 * correctly, except for mixing JPA with JDBC operations, with the
 * default JpaDialect implementation.
 * <p>
 * By default this class acquires a TopLink transaction early to get the JDBC connection.
 * This allows mixing JDBC and JPA/TopLink operations in the same transaction.
 * In some cases this eager acquisition of a transaction/connection may impact
 * scalability. In that case, set the lazyDatabaseTransaction flag to true if
 * you do not require mixing JDBC and JPA operations in the same transaction.
 * Otherwise, use a LazyConnectionDataSourceProxy to ensure that the cost of
 * connection acquisition is near zero until code really needs a connection.
 * 
 * @author Rod Johnson
 * @since 2.0
 * @see LazyConnectionDataSourceProxy
 */
public class TopLinkJpaDialect extends DefaultJpaDialect {
	
	private boolean lazyDatabaseTransaction;
	
	/**
	 * Set whether to lazily start a database transaction within a TopLink
	 * transaction.
	 * <p>By default, database transactions are started early. This allows
	 * for reusing the same JDBC Connection throughout an entire transaction,
	 * including read operations, and also for exposing TopLink transactions
	 * to JDBC access code (working on the same DataSource).
	 * <p>It is only recommended to switch this flag to "true" when no JDBC access
	 * code is involved in any of the transactions, and when it is acceptable to
	 * perform read operations outside of the transactional JDBC Connection.
	 * @see #setDataSource(javax.sql.DataSource)
	 * @see oracle.toplink.sessions.UnitOfWork#beginEarlyTransaction()
	 */
	public void setLazyDatabaseTransaction(boolean lazyDatabaseTransaction) {
		this.lazyDatabaseTransaction = lazyDatabaseTransaction;
	}
	
	/**
	 * Return whether to lazily start a database transaction within a TopLink
	 * transaction.
	 */
	public boolean isLazyDatabaseTransaction() {
		return lazyDatabaseTransaction;
	}
	

	@Override
	public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition)
			throws PersistenceException, SQLException, TransactionException {
		super.beginTransaction(entityManager, definition);
		if (!definition.isReadOnly() && !isLazyDatabaseTransaction()) {
			// This is the magic bit. As with the existing Spring TopLink integration,
			// begin an early transaction to force TopLink to get a JDBC
			// connection so that Spring can manage transactions withy
			// JDBC as well as toplink
			AbstractSession sess = getTopLinkSession(entityManager);
			((UnitOfWork) sess).beginEarlyTransaction();
		}

		// Could return the UOW, if there were any advantage in having it later
		return null;
	}

	/**
	 * Get a traditional TopLink Session from the given EntityManager
	 * Actually seems to be a unit of work
	 * @param em
	 * @return
	 */
	protected AbstractSession getTopLinkSession(EntityManager em) {
		oracle.toplink.essentials.ejb.cmp3.EntityManager emi = (oracle.toplink.essentials.ejb.cmp3.EntityManager) em;
		AbstractSession sess = (AbstractSession) emi.getActiveSession();
		return sess;
	}

	@Override
	public ConnectionHandle getJdbcConnection(EntityManager em, boolean readOnly) throws PersistenceException,
			SQLException {
		AbstractSession sess = getTopLinkSession(em);
		
		// The connection was already acquired eagerly in beginTransaction,
		// unless lazyDatabaseTransaction was set to true
		Connection con = sess.getAccessor().getConnection();
		return con != null ? 
				new SimpleConnectionHandle(sess.getAccessor().getConnection()) : 
				null;
	}
	
	
	@Override
	public TopLinkPortableEntityManagerPlusOperations getPortableEntityManagerPlusOperations(EntityManager nativeEm) {
		// TopLink requires an additional level of unwrapping
		nativeEm = (EntityManager) nativeEm.getDelegate();
		
		if (!(nativeEm instanceof EntityManagerImpl)) {
			throw new IllegalStateException("Not a TopLink EntityManagerImpl: " + nativeEm);
		}
		return new TopLinkPortableEntityManagerPlusOperations((EntityManagerImpl) nativeEm);
	}
	
	
	private static class TopLinkPortableEntityManagerPlusOperations implements PortableEntityManagerPlusOperations {
		
		private final EntityManagerImpl entityManagerImpl;
		
		public TopLinkPortableEntityManagerPlusOperations(EntityManagerImpl entityManagerImpl) {
			this.entityManagerImpl = entityManagerImpl;
		}

		public void evict(Object o) {
			throw new UnsupportedOperationException("How the hell do we do this?");
		}

		public EntityManager getNativeEntityManager() {
			return entityManagerImpl;
		}
		
	}
	
	@Override
	public TopLinkPortableEntityManagerFactoryPlusOperations getPortableEntityManagerFactoryPlusOperations(
			EntityManagerFactory nativeEmf, EntityManagerFactoryInfo emfi) {
		if (!(nativeEmf instanceof EntityManagerFactoryImpl)) {
			throw new IllegalStateException("Not a TopLink EntityManagerFactoryImpl: " + nativeEmf);
		}
		return new TopLinkPortableEntityManagerFactoryPlusOperations((EntityManagerFactoryImpl) nativeEmf, emfi);
	}
	
	
	private static class TopLinkPortableEntityManagerFactoryPlusOperations 
				implements PortableEntityManagerFactoryPlusOperations {
		
		private final EntityManagerFactoryImpl entityManagerFactoryImpl;
		
		private final EntityManagerFactoryInfo emfi;
		
		public TopLinkPortableEntityManagerFactoryPlusOperations(EntityManagerFactoryImpl entityManagerFactoryImpl,
				EntityManagerFactoryInfo emfi) {
			this.entityManagerFactoryImpl = entityManagerFactoryImpl;
			this.emfi = emfi;
		}

		public void evict(Class clazz) {
			// TODO implement this
			System.err.println("What do I do??");
		}
		
		public EntityManagerFactoryInfo getEntityManagerFactoryInfo() {
			return this.emfi;
		}
		
	}

}
