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

package org.springframework.orm.hibernate;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

/**
 * Callback interface for Hibernate code. To be used with HibernateTemplate's execute
 * method, assumably often as anonymous classes within a method implementation.
 * The typical implementation will call Session.load/find/save/update to perform
 * some operations on persistent objects. It can also perform direct JDBC operations
 * via Hibernate's Session.connection() method, returning the active JDBC connection.
 *
 * <p>Note that Hibernate works on unmodified plain Java objects, performing dirty
 * detection via copies made at load time. Returned objects can thus be used outside
 * of an active Hibernate Session without any hassle, e.g. for display in a web GUI.
 * Reassociating such instances with a new Session, e.g. for updates when coming
 * back from the GUI, is straightforward, as the instance has kept its identity.
 * You should care to reassociate them as early as possible though, to avoid having
 * already loaded a version from the database in the same Session.
 *
 * @author Juergen Hoeller
 * @since 02.05.2003
 * @see HibernateTemplate
 */
public interface HibernateCallback {

	/**
	 * Gets called by HibernateTemplate.execute with an active Hibernate Session.
	 * Does not need to care about activating or closing the Session,
	 * or handling transactions.
	 *
	 * <p>If called without a thread-bound Hibernate transaction (initiated
	 * by HibernateTransactionManager), the code will simply get executed on the
	 * underlying JDBC connection with its transactional semantics. If Hibernate
	 * is configured to use a JTA-aware DataSource, the JDBC connection and thus
	 * the callback code will be transactional if a JTA transaction is active.
	 *
	 * <p>Allows for returning a result object created within the callback, i.e.
	 * a domain object or a collection of domain objects. Note that there's
	 * special support for single step actions: see HibernateTemplate.find etc.
	 * A thrown RuntimeException is treated as application exception, it gets
	 * propagated to the caller of the template.
	 *
	 * @param session active Hibernate session
	 * @return a result object, or null if none
	 * @throws HibernateException in case of Hibernate errors
	 * @throws SQLException in case of errors on direct JDBC access
	 * @see HibernateTemplate#execute
	 * @see HibernateTransactionManager
	 */
	Object doInHibernate(Session session) throws HibernateException, SQLException;

}
