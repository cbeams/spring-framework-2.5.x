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
package org.springframework.orm.jpa.spi.hibernate;

import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.SimpleConnectionHandle;
import org.springframework.orm.jpa.DefaultJpaDialect;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.PortableEntityManagerFactoryPlusOperations;
import org.springframework.orm.jpa.PortableEntityManagerPlusOperations;

/**
 * Hibernate specific JPA dialect.
 * 
 * @author Costin Leau
 * @since 2.0
 * 
 */
public class HibernateJpaDialect extends DefaultJpaDialect {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.orm.jpa.DefaultJpaDialect#getJdbcConnection(javax.persistence.EntityManager,
	 *      boolean)
	 */
	@Override
	public ConnectionHandle getJdbcConnection(EntityManager em, boolean readOnly) throws PersistenceException,
			SQLException {
		Session session = getHibernateSession(em);
		Connection connection = session.connection();
		return (connection == null) ? 
				null : 
				new SimpleConnectionHandle(connection);
	}

	/**
	 * Retrieve the Hibernate connection from the entity manager.
	 * 
	 * @param em
	 * @return
	 */
	protected Session getHibernateSession(EntityManager em) {
		return ((HibernateEntityManager) em).getSession();
	}
	
	
	@Override
	public HibernatePortableEntityManagerPlusOperations getPortableEntityManagerPlusOperations(EntityManager nativeEm) {
		if (!(nativeEm instanceof HibernateEntityManager)) {
			throw new IllegalStateException("Not a HibernateEntityManager: " + nativeEm);
		}
		return new HibernatePortableEntityManagerPlusOperations((HibernateEntityManager) nativeEm);
	}
	
	private static class HibernatePortableEntityManagerPlusOperations implements PortableEntityManagerPlusOperations {
		
		private final HibernateEntityManager hibernateEntityManager;
		
		public HibernatePortableEntityManagerPlusOperations(HibernateEntityManager hibernateEntityManager) {
			this.hibernateEntityManager = hibernateEntityManager;
		}

		public void evict(Object o) {
			hibernateEntityManager.getSession().evict(o);
		}

		public EntityManager getNativeEntityManager() {
			return hibernateEntityManager;
		}
	}
	
	
	@Override
	public HibernatePortableEntityManagerFactoryPlusOperations getPortableEntityManagerFactoryPlusOperations(
			EntityManagerFactory nativeEmf,
			EntityManagerFactoryInfo emfi) {
		if (!(nativeEmf instanceof HibernateEntityManagerFactory)) {
			throw new IllegalStateException("Not a HibernateEntityManagerFactory: " + nativeEmf);
		}
		return new HibernatePortableEntityManagerFactoryPlusOperations(
				(HibernateEntityManagerFactory) nativeEmf, emfi);
	}
	
	private static class HibernatePortableEntityManagerFactoryPlusOperations 
					implements PortableEntityManagerFactoryPlusOperations {
		
		private final HibernateEntityManagerFactory hibernateEntityManagerFactory;
		
		private final EntityManagerFactoryInfo emfi;
		
		public HibernatePortableEntityManagerFactoryPlusOperations(
								HibernateEntityManagerFactory hibernateEntityManagerFactory,
								EntityManagerFactoryInfo emfi) {
			this.hibernateEntityManagerFactory = hibernateEntityManagerFactory;
			this.emfi = emfi;
		}
		
		public EntityManagerFactoryInfo getEntityManagerFactoryInfo() {
			return emfi;
		}

		public void evict(Class clazz) {
			hibernateEntityManagerFactory.getSessionFactory().evict(clazz);
		}
	}
}
