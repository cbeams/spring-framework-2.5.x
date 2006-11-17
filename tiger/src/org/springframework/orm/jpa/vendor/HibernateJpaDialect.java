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

import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;

import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.SimpleConnectionHandle;
import org.springframework.orm.jpa.DefaultJpaDialect;

/**
 * {@link org.springframework.orm.jpa.JpaDialect} implementation for
 * Hibernate EntityManager. Developed and tested against Hibernate 3.2.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 2.0
 */
public class HibernateJpaDialect extends DefaultJpaDialect {

	@Override
	public ConnectionHandle getJdbcConnection(EntityManager entityManager, boolean readOnly)
			throws PersistenceException, SQLException {

		Session session = getSession(entityManager);
		Connection con = session.connection();
		return (con != null ? new SimpleConnectionHandle(con) : null);
	}

	protected Session getSession(EntityManager em) {
		return ((HibernateEntityManager) em).getSession();
	}

}
