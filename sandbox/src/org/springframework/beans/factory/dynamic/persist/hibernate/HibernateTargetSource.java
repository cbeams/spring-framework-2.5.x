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

package org.springframework.beans.factory.dynamic.persist.hibernate;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;

import org.springframework.beans.factory.dynamic.persist.AbstractPersistenceStoreRefreshableTargetSource;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.HibernateTemplate;

/**
 * TargetSource that wraps an object persisted by Hibernate.
 * Note that the Hibernate session will be closed once the object is loaded.
 * Thus the object should be set up for eager loading, or its
 * dependencies should be materialized in the protected
 * beforeAutowiring() method.
 * @author Rod Johnson
 */
public class HibernateTargetSource extends AbstractPersistenceStoreRefreshableTargetSource {
	
	private HibernateTemplate hibernateTemplate;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.persist.AbstractPersistenceStoreRefreshableTargetSource#loadFromPersistentStore()
	 */
	protected Object loadFromPersistentStore() {
		return hibernateTemplate.execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Object o = session.load(getPersistentClass(), new Long(getPrimaryKey()));
				beforeAutowiring(o, session);
				return o;
			}
		});
	}
	
	/**
	 * Subclasses can override this to materialize the object's relationships or perform
	 * any other processing necessary. Autowiring will not yet have occurred.
	 * @param o
	 * @param session
	 */
	protected void beforeAutowiring(Object o, Session session ) throws HibernateException, SQLException {
		// Do nothing
	}
	
	protected String storeDetails() {
		return "HIBERNATE";
	}

}
