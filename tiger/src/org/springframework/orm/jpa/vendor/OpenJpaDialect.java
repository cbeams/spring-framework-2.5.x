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

package org.springframework.orm.jpa.vendor;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.SimpleConnectionHandle;
import org.springframework.orm.jpa.DefaultJpaDialect;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * {@link org.springframework.orm.jpa.JpaDialect} implementation for
 * Apache OpenJPA. Developed and tested against OpenJPA 0.9.6.
 *
 * @author Costin Leau
 * @since 2.0
 */
public class OpenJpaDialect extends DefaultJpaDialect {

	@Override
	public ConnectionHandle getJdbcConnection(EntityManager entityManager, boolean readOnly)
			throws PersistenceException, SQLException {

		Connection connection = (Connection) getOpenJPAEntityManager(entityManager).getConnection();
		return new SimpleConnectionHandle(connection);
	}

	@Override
	public void releaseJdbcConnection(ConnectionHandle conHandle, EntityManager em)
			throws PersistenceException, SQLException {

		if (conHandle != null && conHandle.getConnection() != null) {
			conHandle.getConnection().close();
		}
	}

	@Override
	public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition)
			throws PersistenceException, SQLException, TransactionException {

		super.beginTransaction(entityManager, definition);
		if (!definition.isReadOnly()) {
			// Like with TopLink, make sure to start the logic transaction early so that other
			// participants using the connection (such as JdbcTemplate) run in a transaction.
			getOpenJPAEntityManager(entityManager).beginStore();
		}
		return null;
	}

	/**
	 * Return the OpenJPA-specific interface of <code>EntityManager</code>.
	 * @param em the generic <code>EntityManager</code> instance
	 * @return the OpenJPA-specific interface of <code>EntityManager</code>
	 */
	protected OpenJPAEntityManager getOpenJPAEntityManager(EntityManager em) {
		return (OpenJPAEntityManager) em;
	}

}
