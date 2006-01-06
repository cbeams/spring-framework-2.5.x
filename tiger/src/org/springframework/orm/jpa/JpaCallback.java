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

package org.springframework.orm.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

/**
 * Callback interface for JPA code. To be used with JpaTemplate's execute
 * method, assumably often as anonymous classes within a method implementation.
 * The typical implementation will call EntityManager CRUD to perform
 * some operations on persistent objects.
 *
 * <p>Note that JPA works on bytecode-modified Java objects, to be able to
 * perform dirty detection on each modification of a persistent instance field.
 * In contrast to Hibernate, using returned objects outside of an active
 * EntityManager poses a problem: To be able to read and modify fields
 * e.g. in a web GUI, one has to explicitly make the instances "transient".
 * Reassociation with a new EntityManager, e.g. for updates when coming
 * back from the GUI, isn't possible, as the JPA instances have lost their
 * identity when turned transient. This means that either value objects have
 * to be used as parameters, or the contents of the outside-modified instance
 * have to be copied to a freshly loaded active instance on reassociation.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.orm.jpa.JpaTemplate
 * @see org.springframework.orm.hibernate.HibernateCallback
 */
public interface JpaCallback {

	/**
	 * Gets called by <code>JpaTemplate.execute</code> with an active EntityManager.
	 * Does not need to care about activating or closing the EntityManager,
	 * or handling transactions.
	 *
	 * <p>Note that JPA callback code will not flush any modifications to the
	 * database if not executed within a transaction. Thus, you need to make
	 * sure that JpaTransactionManager has initiated a JPA transaction when
	 * the callback gets called, at least if you want to write to the database.
	 *
	 * <p>Allows for returning a result object created within the callback,
	 * i.e. a domain object or a collection of domain objects.
	 * A thrown RuntimeException is treated as application exception,
	 * it gets propagated to the caller of the template.
	 *
	 * @param em active EntityManager
	 * @return a result object, or <code>null</code> if none
	 * @throws javax.persistence.PersistenceException in case of JPA errors
	 * @see org.springframework.orm.jpa.JpaTemplate#execute
	 * @see org.springframework.orm.jpa.JpaTransactionManager
	 */
	Object doInJpa(EntityManager em) throws PersistenceException;

}
